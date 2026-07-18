# DME — Decision Engine for Personalized Workouts

A Spring Boot learning project: a rule-based **Decision Engine** that generates
personalized workout plans, built so the "brain" of the system (rule
filtering + scoring + strategy selection) can be reused for other decision
domains later, and so a rule-based generator can be swapped or augmented
with an AI-backed one without touching the rest of the application.

This is not a list-of-exercises app. The point of the project is to practice
Spring Boot, Spring Security, Spring Data JPA, REST + MVC, database design,
clean/hexagonal architecture and classic design patterns using a real,
non-trivial domain problem.

## Table of contents

- [Architecture](#architecture)
- [Database schema](#database-schema)
- [Security model](#security-model)
- [The Decision Engine](#the-decision-engine)
- [User journey](#user-journey)
- [Running locally](#running-locally)
- [Testing](#testing)
- [Roadmap](#roadmap)
- [Future AI integration](#future-ai-integration)

## Architecture

```
Presentation (web.api REST / web.mvc Thymeleaf)
        │
Application services (application.service)
        │
Fitness Decision Engine (fitness.engine) ──uses──▶ Generic Decision Engine (decisionengine)
        │
Domain (domain.model / domain.repository ports)
        │
Infrastructure (infrastructure.persistence JPA adapters, infrastructure.security)
        │
Database (MySQL, Flyway-versioned)
```

```
com.akoev.dme
├── decisionengine/         # Generic, domain-agnostic core — zero fitness knowledge
│   ├── Rule<C,T>            # hard admissibility check
│   ├── ScoringStrategy<C,T> # soft ranking
│   ├── DecisionEngine<C,T> / RuleBasedDecisionEngine<C,T>
│   └── ScoredCandidate<T>
│
├── fitness/engine/          # The fitness domain plugged into that core
│   ├── FitnessDecisionContext, RecentActivitySummary(Builder)
│   ├── WorkoutPlanGenerator                # port — the AI swap point
│   ├── rulebased/                          # v1 implementation (Template Method)
│   │   ├── strategy/  (5x GoalWorkoutStrategy + GoalStrategyResolver factory)
│   │   ├── rules/     (6x Rule<FitnessDecisionContext,Exercise>)
│   │   └── scoring/   (FitnessExerciseScorer)
│   └── assist/                              # AI-ready seam (see below)
│
├── domain/
│   ├── model/               # Framework-agnostic domain model (User, UserProfile,
│   │                        # Exercise, WorkoutPlan, WorkoutLog, PersonalRecord, ...)
│   └── repository/          # Repository ports (interfaces only)
│
├── application/service/     # Use-case orchestration (AuthService, ExerciseService,
│                             # UserProfileService, WorkoutPlanService, ...)
│
├── infrastructure/
│   ├── persistence/         # JPA entities + entity<->domain mappers + repository adapters
│   └── security/            # JWT (jwt/) + CustomUserDetails(Service) + SecurityConfig
│
└── web/
    ├── api/                  # REST controllers + dto/ (records)
    ├── mvc/                  # Thymeleaf controllers
    └── exception/            # ApiExceptionHandler (REST-only, consistent ApiError shape)
```

**Why the domain model is separate from the JPA entities.** `domain.model`
classes are plain objects with no JPA annotations; `infrastructure.persistence.entity`
holds the `@Entity` classes; dedicated mapper classes convert between them.
This is more boilerplate than using entities directly, but it means the
domain and application layers never depend on Hibernate, which is the point
of a hexagonal/clean layering — and it is genuinely useful in this project:
`FitnessDecisionContext` carries plain `UserProfile`/`Exercise` objects
through rule and scoring logic that has unit tests with **no Spring context
at all** (see [Testing](#testing)).

**Why the Decision Engine is generic.** `decisionengine` never imports
anything from `fitness` or `domain`. `RuleBasedDecisionEngine<C, T>` filters
a candidate list through `Rule<C, T>`s (AND semantics — Chain of
Responsibility / Specification), scores the survivors with a
`ScoringStrategy<C, T>`, and returns them ranked. The fitness module
"instantiates" this core with `C = FitnessDecisionContext`, `T = Exercise`.
A second decision domain (nutrition, rehab programming, ...) could reuse
`decisionengine` unchanged by supplying its own context/candidate types and
rule/scoring implementations — the module boundary is real, not aspirational
(there's a unit test in `decisionengine` that exercises the engine with
made-up types that have nothing to do with fitness).

### Design patterns in play

| Pattern | Where |
|---|---|
| Strategy | `GoalWorkoutStrategy` (one per `TrainingGoal`), `ScoringStrategy` |
| Chain of Responsibility / Specification | `Rule<C,T>` composition in `RuleBasedDecisionEngine` |
| Factory | `GoalStrategyResolver` |
| Template Method | `RuleBasedWorkoutPlanGenerator.generate()` |
| Builder | Domain model construction (Lombok `@Builder`) |
| Adapter | `infrastructure.persistence.repository.*Adapter` classes implementing domain repository ports |

### SOLID

- **SRP** — each `Rule`/`GoalWorkoutStrategy` has exactly one reason to change.
- **OCP** — new rules or goal strategies are added as new classes; `RuleBasedDecisionEngine` and `GoalStrategyResolver` never change.
- **LSP** — any `GoalWorkoutStrategy` is substitutable for another; the generator doesn't know which one it got.
- **ISP** — `Rule`/`ScoringStrategy` are single-method interfaces.
- **DIP** — `WorkoutPlanService` depends on the `WorkoutPlanGenerator` port, not on `RuleBasedWorkoutPlanGenerator` directly.

## Database schema

MySQL, schema fully versioned via Flyway (`src/main/resources/db/migration`,
`V1`–`V12`). Highlights:

- `users` / `roles` / `user_roles` — auth identity + RBAC.
- `user_profiles` (1:1 with `users`, shared PK) — goal, experience level,
  days/week, session length, location, notes.
- `equipment` (lookup) + `user_equipment` / `exercise_equipment` (join
  tables) — the same lookup table is reused for "what the user has" and
  "what an exercise needs", which is what makes the equipment rule a simple
  `containsAll` check.
- `user_rest_days`, `user_preferred_categories`, `user_unwanted_categories`
  — `@ElementCollection`s (no need for full entities for plain enum sets).
- `user_favorite_exercises` / `user_disliked_exercises` — join tables.
- `exercises` — seeded catalog (28 rows, `V12`) spanning every muscle
  group / movement pattern / difficulty / equipment combination the engine
  needs.
- `workout_plans` → `workout_sessions` → `session_exercises` — the
  generated plan tree. `workout_plans.generation_source` is
  `RULE_BASED`/`AI`, anticipating the future generator swap without a
  migration.
- `workout_logs` — a completed session (completion %, rating, perceived
  intensity). This is the table that makes generation *adaptive*: it feeds
  `RecentActivitySummaryBuilder`.
- `workout_streaks`, `personal_records`, `user_favorite_workout_plans` —
  basic gamification primitives (see [Roadmap](#roadmap)).

## Security model

Two independent `SecurityFilterChain` beans (`infrastructure.security.SecurityConfig`):

- **`/api/**`** (`@Order(1)`) — stateless, JWT bearer auth (`jwt/JwtAuthFilter`
  + `jwt/JwtService`, HS256 via `jjwt`), CSRF disabled, JSON 401/403 via
  `RestAuthenticationEntryPoint` / `RestAccessDeniedHandler`.
- **Everything else** (`@Order(2)`) — session-based form login for the
  Thymeleaf pages (`/login`, `/dashboard`, `/profile`, `/progress`), CSRF
  enabled (Spring's Thymeleaf integration injects the token into `th:action`
  forms automatically).

Both chains share `CustomUserDetailsService` (backed by the domain
`UserRepository` port, not JPA directly) and a `BCryptPasswordEncoder`.
`@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")` protects the
exercise-catalog write endpoints.

`ApiExceptionHandler` (`web.exception`, scoped to `web.api` only — the
Thymeleaf controllers get normal HTML error handling) maps every REST error
to one consistent `{"message": "..."}` shape: `ResponseStatusException`s
raised from the application/service and engine layers keep their status code,
bean validation failures and malformed/type-mismatched request bodies map to
400, auth/access failures to 401/403, and anything unexpected falls through
to a generic 500. `RestAuthenticationEntryPoint` / `RestAccessDeniedHandler`
(filter-chain-level 401/403, before a controller is even reached) emit the
same `{"message": "..."}` shape so API clients see one consistent error body
regardless of which layer produced the failure. There is deliberately no
handler for `IllegalStateException`: every place that used to throw it for an
expected, user-actionable condition (like "profile not created yet") now
throws `ResponseStatusException` instead, reserving `IllegalStateException`
for genuine server misconfiguration bugs that should surface as a plain 500.

## The Decision Engine

### Inputs (`FitnessDecisionContext` + `RecentActivitySummary`)

Goal, age/sex, experience level, available equipment, location, favorite/
disliked exercises, preferred/unwanted categories, injuries, the movement
pattern the current session slot targets, plus — built from `workout_logs`
by `RecentActivitySummaryBuilder` — recently-used exercise ids, per-muscle-
group load over the last 14 days, last completion %, days since last
workout, average rating.

### Rules (hard filters, `fitness.engine.rulebased.rules`)

`EquipmentAvailabilityRule`, `ExperienceLevelRule`,
`InjuryContraindicationRule`, `DislikedExerciseRule`,
`UnwantedCategoryRule`, `MovementPatternMatchRule`. A candidate exercise
must satisfy **all** of them to be scored at all.

### Scoring (`FitnessExerciseScorer`)

A weighted sum, starting from a base score of 50:

| Signal | Effect |
|---|---|
| Compound vs. isolation matches the goal (Strength/Hypertrophy favor compound; others favor isolation) | +20 |
| In the user's favorite exercises | +15 |
| Targets a preferred category | +10 |
| Was used in the last 14 days | −30 (avoids repeating the same workout) |
| Muscle group trained recently, per recent session count | −12 each (avoids overtraining) |
| Last completion % < 60 | +10 for BEGINNER difficulty, −10 otherwise (deload) |
| Last completion % > 90 | +10 for ADVANCED difficulty (progressive overload) |
| — | + random jitter in [0, 5) for variety among near-ties |

Every weight above is at least twice the maximum jitter, so the effect of
each signal is deterministic and independently unit-tested
(`FitnessExerciseScorerTest`). The overtraining penalty in particular must
clear that bar even for a single recent session (12 > 2×5), otherwise jitter
could cancel or invert the "avoid overtraining" ordering right when it
matters most.

### Strategy (weekly split + sets/reps/rest, `rulebased/strategy`)

One `GoalWorkoutStrategy` per `TrainingGoal` decides the session blueprint
(e.g. Hypertrophy cycles Push/Pull/Legs days with 8–12 reps; Strength does
full-body compound days with 3–6 reps and long rest; Fat Loss/Endurance run
higher-rep circuits with short rest). `GoalStrategyResolver` picks the right
one at runtime from all `GoalWorkoutStrategy` beans.

### Putting it together (`RuleBasedWorkoutPlanGenerator`)

For each session in the goal's blueprint, for each exercise slot: build a
`FitnessDecisionContext` for that slot's target movement pattern, filter +
score the catalog through a `RuleBasedDecisionEngine<FitnessDecisionContext,
Exercise>`, and pick the top-ranked exercise (ties broken by
`AmbiguityResolver`). Exercises already used elsewhere in the same plan are
excluded so a plan never repeats an exercise. If no candidate is admissible
for a slot, that slot is skipped rather than failing the whole generation.

## User journey

1. **Register** (`POST /api/v1/auth/register` for API clients, or the
   `/register` form in the browser) → **log in** (`POST /api/v1/auth/login`
   for API clients issuing a JWT, or the `/login` form for the browser
   session).
2. **Fill in the profile** (`PUT /api/v1/profile/me`, or eventually a form):
   goal, experience level, days/week, session length, location, available
   equipment, favorite/disliked exercises, preferred/unwanted categories,
   injuries, rest days — all in one call.
3. **Generate a plan** (`POST /api/v1/workout-plans/generate`, or the
   "Generate" button on `/dashboard`). The response includes the plan, a
   plain-language explanation, and a short motivational message.
4. **View the plan** — sessions with exercises, sets, rep ranges, rest
   (`GET /api/v1/workout-plans/active` or `/dashboard`).
5. **Perform a session, then log it** (`POST
   /api/v1/workout-plans/{planId}/sessions/{sessionId}/complete` with
   completion %, rating, perceived intensity, and optionally per-exercise
   weight/reps). This writes a `workout_log`, updates the streak, and
   auto-detects new personal records.
6. **The next generated plan adapts**: recently-used exercises and
   recently-loaded muscle groups score lower; a low completion % nudges
   difficulty down, a high one nudges it up.
7. **Check progress** (`/progress`, or `GET /api/v1/me/streak` /
   `/personal-records` / `/workout-history`).

## Running locally

Requires Docker (for MySQL) and Java 25.

```bash
docker compose up -d          # starts MySQL on localhost:3306
./mvnw spring-boot:run         # runs with the "dev" profile (application-dev.yml)
```

The app seeds roles, equipment and a 28-exercise catalog via Flyway on
first startup. Register either via `POST http://localhost:8080/api/v1/auth/register`
or at `http://localhost:8080/register` in the browser, then either call the
REST API with the JWT from `/api/v1/auth/login`, or log in at
`http://localhost:8080/login` for the browser/session flow.

## Testing

- **Rule-based engine tests run without a Spring context** — `Rule` and
  `ScoringStrategy` implementations, and the generic
  `RuleBasedDecisionEngine`, are plain objects constructed directly in unit
  tests (`decisionengine`, `fitness/engine/rulebased/**`). This keeps the
  core logic's feedback loop fast and independent of the database.
- `RuleBasedWorkoutPlanGeneratorTest` exercises the whole generator
  end-to-end with Mockito-mocked repository ports — no Spring, no database.
- Everything touching the database or HTTP layer extends
  `AbstractIntegrationTest`, which starts **one** MySQL Testcontainer as a
  JVM-wide singleton (started in a static initializer, not per-test-class
  `@Testcontainers` lifecycle — the latter recreated a container per test
  class and was unreliable on this Windows/Docker Desktop setup) and reuses
  it across every test class via Spring's context cache.
- Controller/security tests use `MockMvc` (`@AutoConfigureMockMvc`).
  `WebLoginFlowTest`/`DashboardControllerTest`/`ProgressViewControllerTest`
  actually render the Thymeleaf pages with real data — this caught a real
  bug (`dashboard.html` used `session` as a `th:each` variable name, which
  Thymeleaf reserves) that a REST-only test suite would have missed.

```bash
./mvnw test
```

## Roadmap

| Version | Scope |
|---|---|
| v0.1 | Project setup, core domain, base schema (M0–M1) |
| v0.2 | Extended schema: location, rest days, favorites/dislikes, categories, workout_logs, streaks, personal_records (M1x) |
| v0.3 | Security: JWT + form login (M2) |
| v0.4 | Exercise catalog + profile management endpoints (M3) |
| v0.5 | Generic Decision Engine core + fitness rule-based engine + AI-ready seam (M4) |
| v0.6 | Workout plan generate/view API + dashboard, closing the feedback loop (M5) — **MVP complete here** |
| v0.7 | Basic gamification surfacing: streaks, personal records, progress view (M6) |
| **v0.8+** | *Not built yet, described only:* full gamification (achievements/badges catalog, levels, weekly/monthly goals dashboard, charts), a real AI-backed implementation of the `AmbiguityResolver`/`WorkoutExplanationService`/`MotivationalMessageService` ports, and — as a demonstration that `decisionengine` is genuinely reusable — a second decision domain (e.g. nutrition) built on the same core. |
| v1.0 | Polish: this README, expanded exception handling and validation, final test pass (M7) |

## Future AI integration

The project deliberately does **not** call an AI model in v1 — the rule-based
engine is the entire "intelligence" today. Three narrow ports in
`fitness.engine.assist` mark where AI can be added later, each with a
template (non-AI) default implementation already wired in:

- `AmbiguityResolver` — invoked only when the top two candidates for a slot
  score within 1 point of each other (a genuine tie the rule-based scorer
  can't break confidently).
- `WorkoutExplanationService` — produces the "why this workout" text
  returned alongside a generated plan.
- `MotivationalMessageService` — produces the short encouragement message
  based on recent activity.

An `AiAmbiguityResolver` / `AiWorkoutExplanationService` /
`AiMotivationalMessageService` implementing these same interfaces (backed by
an LLM call) could be wired in via a Spring profile or `@Primary`, with zero
change to `RuleBasedWorkoutPlanGenerator`, `WorkoutPlanService`, or any
controller. The same applies to the generator itself:
`WorkoutPlanGenerator` is a port; an `AiWorkoutPlanGenerator` (or a hybrid
that falls back to the rule-based one) can implement it later —
`workout_plans.generation_source` already distinguishes `RULE_BASED` from
`AI` rows, so no migration is needed when that day comes.
