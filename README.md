# DME — Decision Making Engine

A Spring Boot learning project built around a generic, domain-agnostic
**Decision Engine** (`decisionengine` — rule filtering + weighted scoring +
ranking, with zero knowledge of any specific domain) — in other words, a
small **Decision Engine Framework**, not a single-purpose app. Six domains
plug into it today: **Workout Planner** is the deep one (persisted profile,
history-driven adaptation); **Portfolio Advisor**, **Meal Planner**,
**Movie & Show Picks**, **Learning Path**, and **Daily Task Prioritizer**
are deliberately thin proofs that the same core generalizes — no account,
no persistence, just rules + scoring computed on the spot (see [Other
domains](#other-domains)). The generic flow is always the same regardless
of domain — context in, hard rules filter, a scoring strategy ranks,
(optionally) feedback from history adapts the next round — see [The
Decision Engine](#the-decision-engine) for how workout planning instantiates
it most fully.

This is not a list-of-exercises app, and fitness is not the point — it's the
first domain proving the engine is real, not aspirational (there's a unit
test in `decisionengine` that exercises the core with made-up types
unrelated to fitness, and five more domains besides fitness now prove it at
the whole-application level). The point of the project is to practice
Spring Boot, Spring Security, Spring Data JPA, REST + MVC, database design,
clean/hexagonal architecture and classic design patterns using a real,
non-trivial domain problem — one designed from the start to generalize.

**Development note**: this project was built mainly with AI (Claude Code)
— the user directed the work through prompts rather than writing the
implementation by hand. Visible in `git log` via `Co-Authored-By: Claude
Sonnet 5` trailers on many commits.

## Table of contents

- [Architecture](#architecture)
- [Database schema](#database-schema)
- [Security model](#security-model)
- [The Decision Engine](#the-decision-engine)
- [Other domains](#other-domains)
- [User journey](#user-journey)
- [Web UI](#web-ui)
- [Running locally](#running-locally)
- [Test accounts](#test-accounts)
- [Testing](#testing)
- [Deploying to the cloud](#deploying-to-the-cloud)
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
├── finance/ meals/ movies/ learning/ productivity/
│   │                        # Five thin sibling domains (Portfolio Advisor,
│   │                        # Meal Planner, Movie & Show Picks, Learning Path,
│   │                        # Daily Task Prioritizer) — see Other domains below.
│   │                        # No rulebased/assist/strategy subpackages: each is
│   │                        # small enough to keep its Context/Candidate/Rule(s)/
│   │                        # Scorer/Service flat in one package.
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
    ├── mvc/                  # Thymeleaf controllers + form/ (mutable form-backing
    │                         # beans for pages that need checkbox-list/indexed-row
    │                         # binding — ProfileForm, ExerciseForm)
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
Five more domains (`finance`, `meals`, `movies`, `learning`, `productivity` —
see [Other domains](#other-domains)) reuse `decisionengine` unchanged today,
each supplying only its own context/candidate types and rule/scoring
implementations — the module boundary is real, not aspirational (there's
also a unit test in `decisionengine` itself that exercises the engine with
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
`V1`–`V14`, plus a dev-only `V13` seed migration in a separate location —
see [Test accounts](#test-accounts)). Highlights:

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
  migration. `workout_sessions.day_of_week` (`V14`) is nullable — plans
  generated before that migration have no value there.
- `workout_logs` — a completed session (completion %, rating, perceived
  intensity). This is the table that makes generation *adaptive*: it feeds
  `RecentActivitySummaryBuilder`.
- `workout_streaks`, `personal_records`, `user_favorite_workout_plans` —
  basic gamification primitives (see [Roadmap](#roadmap)).

## Security model

Two independent `SecurityFilterChain` beans (`infrastructure.security.SecurityConfig`):

- **`/api/**`** (`@Order(1)`) — stateless, JWT bearer auth (`jwt/JwtAuthFilter`
  + `jwt/JwtService`, HS256 via `jjwt`), CSRF disabled, JSON 401/403 via
  `RestAuthenticationEntryPoint` / `RestAccessDeniedHandler`. The five basic
  domains' endpoints (`/api/v1/finance/**`, `/meals/**`, `/movies/**`,
  `/learning/**`, `/productivity/**`) are `permitAll()` — nothing they do is
  persisted per-user, so there's no account to protect.
- **Everything else** (`@Order(2)`) — session-based form login for the
  Thymeleaf pages (`/login`, `/dashboard`, `/profile`, `/progress`, `/account/**`),
  CSRF enabled (Spring's Thymeleaf integration injects the token into
  `th:action` forms automatically). `/admin/**` additionally requires
  `hasRole("ADMIN")`, `/actuator/health` is `permitAll()` (cloud platforms
  probe it before any credentials exist; Spring Boot's default web exposure
  keeps every other actuator endpoint unregistered regardless), and the five
  basic domains' pages (`/finance`, `/meals`, `/movies`, `/learning`,
  `/tasks`, all `/**`) are `permitAll()` for the same no-account-needed
  reason as their API endpoints.

Both chains share `CustomUserDetailsService` (backed by the domain
`UserRepository` port, not JPA directly) and a `BCryptPasswordEncoder`.
`@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")` protects the
exercise-catalog write endpoints (both the REST ones and `web.mvc.AdminExerciseController`).

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
disliked exercises, preferred/unwanted categories, injuries, rest days,
session length, the movement pattern the current session slot targets, plus
— built from `workout_logs` by `RecentActivitySummaryBuilder` —
recently-used exercise ids, per-muscle-group load over the last 14 days,
last completion %, days since last workout, average rating, and average
perceived intensity.

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
| Muscle group *not* trained at all recently (only once there's history to compare against) | +10 (complements the overtraining penalty — rewards a neglected group instead of only punishing an overworked one) |
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
one at runtime from all `GoalWorkoutStrategy` beans. The strategy's base
sets/reps/rest scheme is then adjusted once per plan based on recent
feedback (see below) before being applied to every session.

### Putting it together (`RuleBasedWorkoutPlanGenerator`)

For each session in the goal's blueprint, for each exercise slot: build a
`FitnessDecisionContext` for that slot's target movement pattern, filter +
score the catalog through a `RuleBasedDecisionEngine<FitnessDecisionContext,
Exercise>`, and pick the top-ranked exercise (ties broken by
`AmbiguityResolver`). Exercises already used elsewhere in the same plan are
excluded so a plan never repeats an exercise. If no candidate is admissible
for a slot, that slot is skipped rather than failing the whole generation.

Two profile fields that used to be captured and never used now actually
shape the plan:

- **Rest days** — sessions are assigned to real weekdays (Monday-first),
  skipping any day the profile marks as a rest day, so `/dashboard` shows
  "Monday — Push Day 1," not "Day 1."
- **Session length** — each session's exercise-slot count is trimmed (never
  padded) to roughly fit the profile's preferred session duration, based on
  the sets/rest scheme; a 20-minute preference yields a shorter session than
  a 90-minute one for the same goal.

The scheme itself adapts to how recent sessions actually went: low
completion % or a high average perceived intensity ("too hard") reduces
sets and adds rest; high completion % with a low average intensity ("too
easy") adds a set and trims rest. This is separate from — and complements —
the difficulty-tier nudge in the scoring table above, which only affects
*which* exercises get picked, not how much work is prescribed.

## Other domains

Five more domains plug into the exact same `decisionengine` (`Rule`,
`ScoringStrategy`, `RuleBasedDecisionEngine`) with their own context/candidate
types — proof the core generalizes beyond fitness, kept deliberately thin
rather than built out to fitness's depth:

| Domain | Package | Candidate catalog | Rules | What's different |
|---|---|---|---|---|
| Portfolio Advisor | `finance` | 12 instruments, in-memory | Risk tolerance cap, excluded sector | Numeric scoring (expected return, weighted by risk appetite) |
| Meal Planner | `meals` | 12 meals, in-memory | Allergen exclusion, meal-type slot match | Builds a 4-slot daily plan (breakfast/lunch/dinner/snack), excluding meals already used — the same "one engine call per slot, no repeats" shape as a workout session |
| Movie & Show Picks | `movies` | 15 titles, in-memory | Excluded genre, fits available time | The simplest of the five — one rule for taste, one for a hard time budget |
| Learning Path | `learning` | 15 courses, in-memory | Skill area match, level cap (current + 1) | The one domain shaped for a future `DecisionTreeStrategy` (prerequisite chains) — deliberately not built that way yet, see below |
| Daily Task Prioritizer | `productivity` | **none — no catalog** | Fits available time today | The odd one out: ranks tasks the caller submits in the request itself, not a fixed pool — proves the engine works for "rank the user's own items," not just "pick from a catalog" |

**What's deliberately not here, compared to fitness**: no persisted
profile (context comes from the request/form directly), no history table,
no adaptive feedback loop, no admin catalog management, no `assist`/AI seam,
no multi-session weekly planning. Catalogs are plain in-memory `List`s in a
`@Component` (e.g. `InstrumentCatalog`), not Flyway-migrated tables — there's
no admin-editing or historical-reference need to justify a database table
the way fitness's `exercises` table has. Each domain's REST controller binds
its own `Context`/candidate records directly as the request/response body
(no separate web DTO layer) since, unlike `UserProfile`, none of them is a
persisted entity the web layer needs decoupling from.

All five are public (`permitAll`) on both `/api/v1/<domain>/**` and their
`/<domain>` web page — nothing is saved per-user, so there's no account to
protect. Try one: `POST /api/v1/finance/recommend`
`{"riskTolerance":"AGGRESSIVE","excludedSectors":["TECHNOLOGY"]}`, or just
open `/finance`, `/meals`, `/movies`, `/learning`, or `/tasks`.

**On `DecisionTreeStrategy`**: Learning Path's "skill area + level cap" rule
is the flat, simple version of what would ideally be a prerequisite chain
(e.g. "Statistical Modeling requires Intro to Data Analysis"). That's the
one place among all six domains where a decision-tree-shaped strategy might
eventually earn its keep — but it wasn't built speculatively; the flat rule
ships today, and a real prerequisite graph is a concrete next step if this
domain gets built out further, not before.

## User journey

1. **Register** (`POST /api/v1/auth/register` for API clients, or the
   `/register` form in the browser) → **log in** (`POST /api/v1/auth/login`
   for API clients issuing a JWT, or the `/login` form for the browser
   session).
2. **Fill in the profile** (`PUT /api/v1/profile/me`, or the `/profile` form):
   goal, experience level, days/week, session length, location, available
   equipment, favorite/disliked exercises, preferred/unwanted categories,
   injuries, rest days — all in one call/submit.
3. **Generate a plan** (`POST /api/v1/workout-plans/generate`, or the
   "Generate" button on `/dashboard`, which also lets you pick a one-off goal
   override for just that plan). The response includes the plan, a
   plain-language explanation, and a short motivational message. Generating
   a new plan retires whatever was previously active, so `/dashboard` and
   `GET /api/v1/workout-plans/active` always agree on which one is current.
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

## Web UI

Every capability above is reachable from the browser, not just the REST API:

| Page | Who | What it does |
|---|---|---|
| `/` | anyone | Domain card grid — "Workout Planner" (live, with the nav below embedded in its card) plus one card per other domain, linking straight in now that all six are live (`HomeController`'s `DecisionDomainCard` list) |
| `/register`, `/login` | anyone | Session-based signup/login (separate from the JWT flow) |
| `/profile` | any logged-in user | Full create/edit form for the profile: scalars, enums, and checkbox groups for equipment/categories/rest-days/exercises, plus a small set of indexed rows for injury limitations (`web.mvc.form.ProfileForm`) |
| `/dashboard` | any logged-in user | Generate a plan (with an optional goal-override dropdown), view the active plan, mark a session complete |
| `/progress` | any logged-in user | Streak, personal records, workout history |
| `/help` | anyone | Plain-language explanation of how the app works, for end users |
| `/exercises` | anyone | Read-only catalog browse |
| `/account/password` | any logged-in user | Change password (verifies the current one first) |
| `/admin/exercises`, `/admin/exercises/new`, `/admin/exercises/{id}/edit` | `ROLE_ADMIN` | Create/edit the exercise catalog (`web.mvc.form.ExerciseForm`) |
| `/finance`, `/meals`, `/movies`, `/learning`, `/tasks` | anyone | The five basic domains — see [Other domains](#other-domains) |

Every one of these pages is backed by the exact same application-service
methods the REST controllers call — there's no separate "web" business logic
to keep in sync.

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
`http://localhost:8080/login` for the browser/session flow. Or skip
registration entirely and use one of the [test accounts](#test-accounts).

`docker-compose.yml` only starts MySQL — the app itself runs directly via
`./mvnw spring-boot:run` for local dev. See
[Deploying to the cloud](#deploying-to-the-cloud) for running the app itself
in a container.

## Test accounts

The `dev` profile seeds two ready-to-use accounts (Flyway migration
`src/main/resources/db/dev-migration/V13__seed_test_users.sql`), both with
password **`Test1234!`**:

| Username | Role(s) | State |
|---|---|---|
| `testuser` | `ROLE_USER` | Profile already filled in (HYPERTROPHY, INTERMEDIATE, 4 days/week, some equipment) plus a 3-day streak — generate a plan immediately with no setup |
| `testadmin` | `ROLE_USER`, `ROLE_ADMIN` | No profile yet, so you can also see that "no profile" state — use it to reach `/admin/exercises` |

This migration lives **outside** `src/main/resources/db/migration` on
purpose: only `application-dev.yml` adds `classpath:db/dev-migration` to
`spring.flyway.locations`, so it never runs against any deployment that
doesn't explicitly opt into the dev profile the same way. **If you fork this
project for anything with real users, delete `db/dev-migration` entirely** —
don't rely on a profile flag alone to keep known credentials out of a real
database.

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

## Deploying to the cloud

The app has no cloud-specific code — only configuration + a container image:

- **`Dockerfile`** (repo root) — multi-stage build: `eclipse-temurin:25-jdk`
  compiles the jar, `eclipse-temurin:25-jre` runs it. `docker build -t dme .`
  then `docker run -p 8080:8080 --env-file .env dme` (with the env vars
  below in `.env`).
- **`application-prod.yml`** — activate with `SPRING_PROFILES_ACTIVE=prod`.
  Requires these env vars, with **no defaults**, so a missing one fails
  fast at startup instead of silently misconfiguring:
  - `SPRING_DATASOURCE_URL` (a real `jdbc:mysql://...` URL — you still need a
    reachable MySQL instance; this project doesn't provision one)
  - `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
  - `JWT_SECRET` — a long random value (this one *does* have a dev-only
    fallback in `application.yml`, but override it for anything real)
  - `PORT` (optional) — most PaaS hosts inject this to dictate the bind port
- **`GET /actuator/health`** — unauthenticated (see [Security
  model](#security-model)), returns `{"status":"UP"}` once the datasource is
  reachable. Point your host's health check here.

None of this seeds the database — Flyway still runs `V1`–`V14` against
whatever schema `SPRING_DATASOURCE_URL` points at on first boot, same as
local dev, just without the `db/dev-migration` test accounts (see [Test
accounts](#test-accounts)).

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
| **v0.8+** | *Not built yet, described only:* full gamification (achievements/badges catalog, levels, weekly/monthly goals dashboard, charts) and a real AI-backed implementation of the `AmbiguityResolver`/`WorkoutExplanationService`/`MotivationalMessageService` ports. (The other planned v0.8+ item — a second decision domain proving `decisionengine` is genuinely reusable — shipped early and then some: see v1.4.) |
| v1.0 | Polish: this README, expanded exception handling and validation, final test pass (M7) |
| v1.1 | Web parity with the API: `/profile` create/edit form, dashboard goal-override, `/admin/exercises` catalog editor, `/account/password`. Fixed a bug where regenerating a plan never deactivated the previous one. Added cloud-hosting basics: `Dockerfile`, `application-prod.yml`, `/actuator/health`, and dev-only seeded test accounts. |
| v1.2 | Multi-domain home page (`/help` plus a "coming soon" card grid — fitness is domain #1 of what's meant to be several, see [Architecture](#architecture)). Fixed the `/dashboard/generate` and session-complete actions leaking an unhandled error page on expected failures. Closed several rule-engine gaps where profile data was captured but never used: rest days now drive real weekday scheduling, session length now trims (not pads) exercise slots, and completion %/perceived intensity now adjust the actual prescribed sets/rest ("too easy"/"too hard"), not just which difficulty tier gets picked. Added a "weak muscle group" scoring bonus. |
| v1.3 | Clarified in the docs (name unchanged — still DME, Decision Making Engine) that this is architected as a general decision engine framework rather than a fitness app with a generic core bolted on — the distinction was already true in the code, just not said plainly. Added a 4th "coming soon" domain (Learning Path), then a 5th (Daily Task Prioritizer). No architecture change: evaluated and declined collapsing `Rule`/`ScoringStrategy` into one monolithic `DecisionStrategy` interface, since the current split is independently composable and a unified interface would be a regression, not a simplification. |
| v1.4 | Built all five "coming soon" domains as real, deliberately thin applications of `decisionengine` (Portfolio Advisor, Meal Planner, Movie & Show Picks, Learning Path, Daily Task Prioritizer) — see [Other domains](#other-domains). Each is public, has no persisted profile/history, and uses an in-memory catalog (or, for the task prioritizer, no catalog at all — it ranks caller-submitted items). The home page's domain cards all link live now instead of showing "coming soon." |

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
