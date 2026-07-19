# DME Study Guide — How Everything Works

This is a deep-dive companion to `README.md`, written to be read start-to-finish
so you can rebuild your mental model of this project from scratch later
(e.g. after a break from Claude Code / this subscription). Where useful it
points at exact files and line numbers so you can jump straight to the code.

If you only remember one thing: **this project is a hexagonal/clean-architecture
Spring Boot app whose "product" is a rule-based Decision Engine that picks
exercises for a workout plan.** Everything else (auth, persistence, MVC pages)
exists to support exercising that engine end-to-end.

---

## 1. What this project is (and isn't)

- **Purpose**: a learning project to practice Spring Boot, Spring Security,
  Spring Data JPA, REST + server-rendered MVC, database design (MySQL +
  Flyway), hexagonal/clean layering, and classic GoF design patterns — using
  a real, non-trivial domain (personalized workout generation) rather than a
  toy CRUD app.
- **Not** a static list-of-exercises app. The interesting part is
  `decisionengine` + `fitness.engine`: given a user's profile and recent
  training history, rank a catalog of exercises and assemble a multi-session
  plan.
- **No AI is called today.** The "intelligence" is hand-written rules +
  weighted scoring. Three interfaces (`fitness.engine.assist`) are seams
  where an LLM-backed implementation could be swapped in later without
  touching the engine, controllers, or services (see §7).
- **Tech stack**: Java 25, Spring Boot 4.1.0 (spring-boot-starter-parent),
  Spring Data JPA / Hibernate, Spring Security 6 (JWT + form login), Spring
  MVC + Thymeleaf, Spring Boot Actuator (health endpoint only), MySQL 8.4,
  Flyway (versioned migrations V1–V14, plus a dev-only V13 seed migration in
  its own location — see §3), Lombok, jjwt 0.12.6 for JWT, JUnit 5 + Mockito
  + Testcontainers for tests. See `pom.xml`.
- **Every capability exists both as a REST endpoint and as a browser page**
  backed by the same application-service method — see §13.

---

## 2. Layered architecture

```
web.api (REST @RestController)   web.mvc (Thymeleaf @Controller)
                    \                  /
                 application.service (use-case orchestration)
                              |
   fitness.engine (fitness-specific) --uses--> decisionengine (generic core)
                              |
        domain.model (plain objects)  +  domain.repository (interfaces/ports)
                              |
   infrastructure.persistence (JPA entities, mappers, repository adapters)
   infrastructure.security (JWT, UserDetailsService, SecurityConfig)
                              |
                     MySQL (Flyway-versioned)
```

The dependency rule: arrows only point **downward**. `domain.model` has zero
framework annotations (no `@Entity`, no JPA) — it's plain Java + Lombok.
`infrastructure.persistence.entity` holds the actual `@Entity` classes, and
a parallel set of `infrastructure.persistence.mapper` classes converts
between `XxxEntity` and the domain `Xxx`. This is more boilerplate than just
using `@Entity` classes everywhere, but it buys two things:

1. `application.service` and `fitness.engine` never import Hibernate.
2. The entire rule/scoring engine can be unit-tested with **plain
   constructed objects and no Spring context at all** — see §8.

`domain.repository` holds interface-only **ports** (e.g. `ExerciseRepository`,
`UserRepository`). `infrastructure.persistence.repository` has the
implementations: a thin Spring Data JPA repository (`ExerciseJpaRepository`)
plus an `Adapter` class (`ExerciseRepositoryAdapter`) that implements the
domain port by delegating to the JPA repo and mapping entities <-> domain
objects. This Adapter pattern is what lets `application.service` code depend
only on `domain.repository` interfaces (Dependency Inversion).

### Package tour (where to find things)

| Package | Contents |
|---|---|
| `decisionengine` | Generic core: `Rule<C,T>`, `ScoringStrategy<C,T>`, `DecisionEngine<C,T>`/`RuleBasedDecisionEngine<C,T>`, `ScoredCandidate<T>`. Zero fitness knowledge — reusable for any ranking problem. |
| `fitness.engine` | Fitness plugs into the generic core: `FitnessDecisionContext` (the `C`), `Exercise` (the `T`, from `domain.model`), `RecentActivitySummary`(`Builder`), the `WorkoutPlanGenerator` port, `GenerationRequest`. |
| `fitness.engine.rulebased` | v1 (only) implementation: `RuleBasedWorkoutPlanGenerator`, `rules/` (6 hard filters), `scoring/FitnessExerciseScorer`, `strategy/` (5 `GoalWorkoutStrategy` + `GoalStrategyResolver`). |
| `fitness.engine.assist` | The AI-ready seam: `AmbiguityResolver`, `WorkoutExplanationService`, `MotivationalMessageService` interfaces + template (non-AI) default impls. |
| `domain.model` | Plain domain objects: `User`, `UserProfile`, `Exercise`, `WorkoutPlan`, `WorkoutSession`, `SessionExercise`, `WorkoutLog`, `PersonalRecord`, `WorkoutStreak`, `Equipment`, `Role`, plus enums. |
| `domain.repository` | Port interfaces only (`UserRepository`, `ExerciseRepository`, `WorkoutPlanRepository`, etc.). |
| `application.service` | Use-case orchestration: `AuthService`, `UserProfileService`, `ExerciseService`, `WorkoutPlanService`. Also holds `*Command`/`*Result` records — the internal request/response shapes between web and service layers. |
| `infrastructure.persistence.entity` | `@Entity` classes mirroring the schema. |
| `infrastructure.persistence.mapper` | Entity <-> domain object conversion. |
| `infrastructure.persistence.repository` | Spring Data JPA repos (`XxxJpaRepository`) + `XxxRepositoryAdapter` implementing the domain port. |
| `infrastructure.security` | `SecurityConfig`, `CustomUserDetails`(`Service`), `RestAuthenticationEntryPoint`/`RestAccessDeniedHandler`, and `jwt/` (`JwtAuthFilter`, `JwtService`, `JwtProperties`). |
| `web.api` | REST controllers (`AuthController`, `AccountController`, `ProfileController`, `ExerciseController`, `WorkoutPlanController`, `ProgressController`, `CurrentUserController`) + `dto/` (request/response records). |
| `web.mvc` | Thymeleaf controllers (`HomeController`, `HelpController`, `LoginController`, `RegisterController`, `DashboardController`, `ProfileViewController`, `AccountViewController`, `AdminExerciseController`, `ProgressViewController`, `ExerciseViewController`) + `DecisionDomainCard` (the home page's "coming soon" placeholder registry) + `form/` (`ProfileForm`, `ExerciseForm` — mutable form-backing beans, see §13). |
| `web.exception` | `ApiExceptionHandler` (REST-only `@RestControllerAdvice`), `ApiError`. |

### Design patterns actually in the code (not just claimed)

| Pattern | Where | Why it's real, not decorative |
|---|---|---|
| Strategy | `GoalWorkoutStrategy` (one class per `TrainingGoal`), `ScoringStrategy` | `GoalStrategyResolver` picks one at runtime; the generator never branches on goal itself. |
| Chain of Responsibility / Specification | `Rule<C,T>` list in `RuleBasedDecisionEngine.isAdmissible` | AND-combines an injected `List<Rule<...>>` via `allMatch` — adding a 7th rule is a new `@Component`, zero changes to the engine. |
| Factory | `GoalStrategyResolver` | Builds a `Map<TrainingGoal, GoalWorkoutStrategy>` from all `GoalWorkoutStrategy` beans Spring hands it. |
| Template Method | `RuleBasedWorkoutPlanGenerator.generate()` | Fixed skeleton (resolve strategy → build activity summary → loop sessions → loop slots → rank → pick) with pluggable steps (rules, scorer, tie-breaker). |
| Builder | Nearly every domain model, via Lombok `@Builder` | e.g. `FitnessDecisionContext.builder()...build()` in `RuleBasedWorkoutPlanGenerator.buildSession`. |
| Adapter | `infrastructure.persistence.repository.*Adapter` | e.g. `ExerciseRepositoryAdapter` implements `domain.repository.ExerciseRepository` by wrapping `ExerciseJpaRepository` + `ExerciseMapper`. |
| Port/Adapter (Hexagonal) | `WorkoutPlanGenerator` interface vs. `RuleBasedWorkoutPlanGenerator` | The whole point of §7 — an `AiWorkoutPlanGenerator` could implement the same port. |

---

## 3. Database schema

MySQL 8.4, fully versioned by Flyway migrations `V1`–`V14` in
`src/main/resources/db/migration/` (plus a dev-only `V13` seed migration in
a *separate* folder, `src/main/resources/db/dev-migration/` — see below).
Nothing is created by Hibernate (`ddl-auto: validate` in `application.yml` —
Hibernate checks the schema matches the entities but never generates DDL).

| Migration | What it adds |
|---|---|
| V1 | `users`, `roles`, `user_roles` (auth identity + RBAC) |
| V2 | `equipment` lookup, `user_profiles` (1:1 with `users`, PK = `user_id`), `user_equipment`, `user_limitations` |
| V3 | `exercises`, `exercise_equipment` |
| V4 | `workout_plans` → `workout_sessions` → `session_exercises` (the generated-plan tree) |
| V5 | Seeds `ROLE_USER`, `ROLE_ADMIN` |
| V6 | Seeds 9 equipment rows (Bodyweight, Dumbbell, Barbell, Kettlebell, Resistance Band, Pull-up Bar, Bench, Cable Machine, Machine) |
| V7 | Adds `user_profiles.location`, `user_rest_days` |
| V8 | `user_favorite_exercises`, `user_disliked_exercises` |
| V9 | `user_preferred_categories`, `user_unwanted_categories` (by `muscle_group`) |
| V10 | `workout_logs` (a completed session) + index on `(user_id, performed_at)` |
| V11 | `personal_records`, `workout_streaks`, `user_favorite_workout_plans` |
| V12 | Seeds 28 exercises spanning every muscle group / movement pattern / difficulty / equipment combination, plus their `exercise_equipment` rows |
| V13 *(dev-only, `db/dev-migration/`)* | Seeds `testuser`/`testadmin` test accounts — see §14 |
| V14 | Adds `workout_sessions.day_of_week` (nullable — plans generated before this migration have no value here) |

Key modeling decisions worth remembering:

- **`equipment` is reused for both directions**: `user_equipment` ("what the
  user owns") and `exercise_equipment` ("what the exercise needs") point at
  the same lookup table. This is exactly why `EquipmentAvailabilityRule` can
  be a one-line `Set.containsAll` check (§5).
- **`@ElementCollection`s instead of tables-with-entities** for things that
  are just enum sets: `user_rest_days`, `user_preferred_categories`,
  `user_unwanted_categories`. No Java entity class backs these — Hibernate
  maps them straight from a `Set<Enum>` field.
- **`workout_plans.generation_source`** (`RULE_BASED` / `AI`) exists from V4
  — a future AI generator needs no migration, just a new enum value being
  written.
- **`workout_logs`** is the table that makes generation *adaptive*. Nothing
  else in the schema feeds back into exercise selection — see
  `RecentActivitySummaryBuilder` in §6. `perceived_intensity` in particular
  sat unused for a while (captured, never read back) until it became the
  signal for the "too easy"/"too hard" adaptive volume rule (§5.7) —
  distinct from `rating`, which stays a satisfaction signal, not a
  difficulty one.
- **`session_exercises.exercise_id` has `ON DELETE RESTRICT`**, unlike almost
  everything else which cascades — you cannot delete an `Exercise` that is
  referenced by a historical plan.

---

## 4. Security model

Two independent `SecurityFilterChain` beans in `infrastructure/security/SecurityConfig.java`:

### `/api/**` chain (`@Order(1)`) — stateless JWT
- `securityMatcher("/api/**")`, CSRF disabled, `SessionCreationPolicy.STATELESS`.
- `/api/v1/auth/**` and `GET /api/v1/exercises/**` / `GET /api/v1/equipment/**`
  are `permitAll()`; everything else requires authentication.
- `JwtAuthFilter` (a `OncePerRequestFilter`) runs before
  `UsernamePasswordAuthenticationFilter`: reads the `Authorization: Bearer <token>`
  header, verifies+parses it via `JwtService` (HS256, `jjwt`), loads a
  `UserDetails` via `CustomUserDetailsService`, and manually populates
  `SecurityContextHolder`. A bad/expired token is swallowed silently
  (`catch (JwtException | IllegalArgumentException ignored)`) — the request
  just proceeds unauthenticated, and `RestAuthenticationEntryPoint` turns
  that into a JSON 401 later if the endpoint required auth.
- **Token issuance**: `AuthService.login()` calls Spring's
  `AuthenticationManager.authenticate(...)` (which internally re-validates
  the password via the same `BCryptPasswordEncoder` + `CustomUserDetailsService`
  used everywhere else), then `JwtService.issueToken(username, roles)` signs
  a token with the `subject` = username, a `roles` claim, and a 60-minute
  expiry (`app.jwt.expiration-minutes`, `application.yml`).
- `app.jwt.secret` defaults to a dev placeholder
  (`dev-only-secret-change-me-please-use-a-long-random-value`) unless
  `JWT_SECRET` env var is set — **must** be overridden for any real deployment.

### Everything else chain (`@Order(2)`) — session + form login
- `/`, `/login`, `/register`, `/exercises`, `/exercises/**`, `/css/**`,
  `/js/**` are public; everything else needs an authenticated session.
- `/admin/**` additionally requires `hasRole("ADMIN")` — this is what gates
  `AdminExerciseController` (§13). `@PreAuthorize("hasRole('ADMIN')")` at the
  class level on that controller is defense in depth on top of this
  filter-chain rule, same as `ExerciseController`'s REST admin endpoints.
- `/actuator/health` and `/actuator/health/**` are `permitAll()` —
  deliberately, since a cloud host's health check runs before any
  credentials exist. Spring Boot's default `management.endpoints.web.exposure.include`
  is just `health`, so every other actuator endpoint (`/actuator/env`,
  `/actuator/beans`, ...) is unregistered (404) regardless of auth — verified
  by hand: authenticated requests to those paths still 404, only
  `/actuator` (the HAL discovery page) and `/actuator/health` resolve.
- Standard `formLogin()` targeting `/login`, redirecting to `/` on success;
  standard `logout()`.
- CSRF is **enabled** here (unlike the API chain) — Thymeleaf's Spring
  integration auto-injects the CSRF token into `th:action` forms, so none of
  the templates need to handle it manually.

### Shared plumbing
- Both chains share `CustomUserDetailsService` (backed by the domain
  `UserRepository` port — not JPA directly) and one `BCryptPasswordEncoder`
  bean.
- `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")` guards the
  exercise-catalog write endpoints (`POST`/`PUT /api/v1/exercises`) — see
  `ExerciseController`.
- `CustomUserDetails` wraps the domain `User` and exposes its id via
  `getId()`/`getDomainUser()` — every controller that needs "the current
  user" takes `@AuthenticationPrincipal CustomUserDetails principal` and
  calls `principal.getId()`.

### Error shape consistency
`ApiExceptionHandler` (`web/exception/ApiExceptionHandler.java`) is scoped to
`web.api` only (`@RestControllerAdvice(basePackages = "com.akoev.dme.web.api")`)
so Thymeleaf pages get normal HTML error pages instead. Every REST error body
is `{"message": "..."}` (`ApiError`), regardless of which layer raised it:

| Exception | Status | Note |
|---|---|---|
| `ResponseStatusException` | whatever status it carries | This is how the service layer signals expected, user-facing failures (404 no profile, 403 not your plan, etc.) |
| `IllegalArgumentException` | 409 | Used for things like "username already taken" |
| `BadCredentialsException` | 401 | |
| `AccessDeniedException` | 403 | Covers both `@PreAuthorize` denials (caught here) and filter-chain-level denials (caught earlier by `RestAccessDeniedHandler`, same shape) |
| `MethodArgumentNotValidException` | 400 | Bean validation failures, message lists `field: reason` |
| `HttpMessageNotReadableException` | 400 | Malformed JSON body |
| `MethodArgumentTypeMismatchException` | 400 | e.g. non-numeric path variable |
| anything else | 500 | Generic message, no leakage of internal details |

Note there is **deliberately no handler for `IllegalStateException`** — it's
reserved for genuine server misconfiguration (e.g. `GoalStrategyResolver`
finding no strategy bean for a goal, or the `ROLE_USER` seed missing) and
intentionally surfaces as an opaque 500.

---

## 5. The Decision Engine — deep dive

This is the part of the codebase worth understanding cold, since it's the
whole point of the project.

### 5.1 The generic core (`decisionengine`, zero fitness knowledge)

```java
public interface Rule<C, T> {
    boolean isSatisfiedBy(C context, T candidate);
    String description();
}

public interface ScoringStrategy<C, T> {
    double score(C context, T candidate);
}

public interface DecisionEngine<C, T> {
    List<ScoredCandidate<T>> rank(C context, List<T> candidates);
}

public record ScoredCandidate<T>(T candidate, double score) {}
```

`RuleBasedDecisionEngine<C, T>` is the only implementation:

```java
candidates.stream()
    .filter(candidate -> rules.stream().allMatch(r -> r.isSatisfiedBy(context, candidate)))  // hard filter, AND semantics
    .map(candidate -> new ScoredCandidate<>(candidate, scoringStrategy.score(context, candidate)))
    .sorted(Comparator.comparingDouble(ScoredCandidate<T>::score).reversed())                 // best first
    .toList();
```

That's the entire generic algorithm. There's a unit test
(`RuleBasedDecisionEngineTest`) that exercises this with made-up types
unrelated to fitness, proving the module boundary is real.

**The domain-agnostic flow, named explicitly.** Any domain plugging into
`decisionengine` follows the same shape, whether it's workout planning,
meal planning, or something not built yet:

```
Input (a GenerationRequest-shaped call)
    │
Collect user context      →  a domain's own "C" (FitnessDecisionContext for fitness)
    │
Apply rules (hard filter) →  Rule<C,T> list, AND-combined
    │
Calculate scores          →  ScoringStrategy<C,T>
    │
Rank results               →  RuleBasedDecisionEngine.rank() — filter, score, sort
    │
Generate recommendation    →  the domain assembles ranked picks into its own
                               result shape (WorkoutPlan for fitness)
    │
Collect feedback           →  a domain's own history log (workout_logs for fitness)
    │
Update future decisions    →  a domain's own "recent activity" summary
                               (RecentActivitySummary for fitness) feeds back
                               into the context on the next generation
```

Fitness is the only domain that exists today, but nothing about
`decisionengine`, `Rule`, or `ScoringStrategy` mentions fitness — a second
domain supplies its own context/candidate types and its own rules/scoring,
without touching this package at all. The home page's "coming soon" cards
(`web.mvc.DecisionDomainCard`, §13) are where a second one would surface
once built; none of them have any code behind them yet.

**Why `Rule` and `ScoringStrategy` are two interfaces, not one.** A tempting
alternative is a single `DecisionStrategy.generate(context)` interface with
swappable whole-algorithm implementations (`RuleBasedStrategy`,
`WeightedScoringStrategy`, `ConstraintBasedStrategy`, ...). That was
considered and declined: filtering (constraints) and ranking (scoring) are
different concerns that always both apply together, not alternatives you'd
pick one of. The current split lets you change the scorer without touching
a single rule, or add a rule without touching the scorer — a unified
interface would force every combination to be its own class instead
(`RuleBasedWeightedScoringStrategy`, `RuleBasedDecisionTreeStrategy`, ...),
which is worse, not more flexible. `RuleBasedDecisionEngine` is exactly
"rules + a scoring strategy, composed" — keeping them separate is the
design, not a gap to close.

### 5.2 The fitness instantiation: `C = FitnessDecisionContext`, `T = Exercise`

`FitnessDecisionContext` (`fitness/engine/FitnessDecisionContext.java`) bundles:
- `UserProfile profile` — goal, experience level, equipment, favorites/dislikes,
  preferred/unwanted categories, injuries (`limitations`), location, rest days.
- `RecentActivitySummary recentActivity` — see §5.5.
- `MovementPattern targetMovementPattern` — which slot in the session blueprint
  is currently being filled (see §5.4).

### 5.3 The six rules (all hard filters, `fitness.engine.rulebased.rules`)

A candidate `Exercise` must pass **every** rule to be scored at all:

| Rule | Check |
|---|---|
| `EquipmentAvailabilityRule` | `profile.availableEquipment.containsAll(exercise.requiredEquipment)` |
| `ExperienceLevelRule` | `rank(exercise.difficultyLevel) <= rank(profile.experienceLevel)` — uses an explicit `switch`-based rank rather than `.ordinal()`, specifically so that `DifficultyLevel` and `ExperienceLevel` (two independently-declared enums) can't silently desync if one gets reordered |
| `InjuryContraindicationRule` | none of `profile.limitations[].muscleGroup` equals `exercise.primaryMuscleGroup` |
| `DislikedExerciseRule` | exercise id not in `profile.dislikedExercises` |
| `UnwantedCategoryRule` | `exercise.primaryMuscleGroup` not in `profile.unwantedCategories` |
| `MovementPatternMatchRule` | `exercise.movementPattern == context.targetMovementPattern` (this is what actually ties an exercise to "which slot" it can fill) |

If **no** exercise survives all six rules for a slot, that slot is simply
skipped (`RuleBasedWorkoutPlanGenerator.buildSession`, `if (chosen == null) continue;`)
— generation degrades gracefully instead of failing the whole plan.

### 5.4 Scoring (`FitnessExerciseScorer`, base score 50)

| Signal | Effect | Code |
|---|---|---|
| Compound vs. isolation matches goal (Strength/Hypertrophy favor compound; Fat Loss/Endurance/General favor isolation) | +20 | `goalFitScore` |
| In `profile.favoriteExercises` | +15 | `favoriteScore` |
| Targets a `profile.preferredCategories` muscle group | +10 | `preferredCategoryScore` |
| Exercise id in `recentActivity.recentlyUsedExerciseIds` (used in last 14 days) | −30 | `recencyPenalty` |
| Muscle group trained recently | −12 **per recent session** that hit it | `overtrainingPenalty` |
| Muscle group with **zero** recent sessions (only once `recentLoadByMuscleGroup` is non-empty at all — an empty map means no data, not "everything is weak") | +10 | `weakMuscleGroupBonus` |
| `lastCompletionPercentage < 60` | +10 if `BEGINNER` difficulty, else −10 (deload) | `difficultyAdjustment` |
| `lastCompletionPercentage > 90` | +10 if `ADVANCED` difficulty, else 0 (progressive overload) | `difficultyAdjustment` |
| — | + random jitter in `[0, 5)` (breaks ties among otherwise-equal exercises for variety) | `ThreadLocalRandom` |

Every fixed weight is ≥ 2× the max jitter (5), so signal ordering can never
be accidentally flipped by the random component — this is asserted directly
in `FitnessExerciseScorerTest`. The comment in the source calls out the
12-vs-10(2×5) margin on the overtraining penalty specifically, because that
penalty is the smallest per-unit weight and must still dominate jitter even
after just **one** recent session.

`weakMuscleGroupBonus` is the deliberate complement to `overtrainingPenalty`:
the two signals always move in the same direction for any two muscle groups
being compared (one has recent load and gets the penalty; the other has
none and gets the bonus), so there's no test that isolates one from the
other via a relative A-vs-B comparison the way the rest of this table's
tests do — `FitnessExerciseScorerTest.untrainedMuscleGroupIsRewardedOnceThereIsHistoryShowingOthersWereTrained`
instead compares the *same* exercise/muscle-group scored against two
different `RecentActivitySummary`s (empty vs. "something else was trained")
to isolate just this signal.

### 5.5 `RecentActivitySummary` — what makes generation adaptive

Built per-user by `RecentActivitySummaryBuilder.build(userId)` from
`workout_logs` within a **14-day window**:
- `recentlyUsedExerciseIds` — every exercise id from every session logged
  in the window (via `workout_sessions.exercises` → `SessionExercise.exercise`).
- `recentLoadByMuscleGroup` — count of sessions per `MuscleGroup` in the window.
- `lastCompletionPercentage` — from the single most recent `WorkoutLog`.
- `daysSinceLastWorkout` — days between now and that log's `performedAt`.
- `averageRating` — mean of `WorkoutLog.rating` across the window (nulls filtered).
- `averagePerceivedIntensity` — mean of `WorkoutLog.perceivedIntensity` across
  the window (nulls filtered). Feeds the "too easy"/"too hard" adaptive
  volume rule (§5.7), not the scorer — `rating` and `perceivedIntensity` are
  deliberately different signals (satisfaction vs. felt difficulty).

Both averages are boxed `Double`, not primitive `double` — `average()`
defaults to `0.0` when nobody logged a value, and `0.0` would read as "very
low intensity" to the adaptive rule below if it weren't distinguished from
"no data." `RecentActivitySummaryBuilder.average()` returns `null` in that
case instead (backed by `OptionalDouble.isPresent()`), so "nobody said
anything" and "everyone said it was trivially easy" can never be confused.

If there are no logs at all, `RecentActivitySummary.empty()` is used (all
fields empty/null) — a brand-new user's first plan is unaffected by any of
the recency/overtraining/difficulty/weak-muscle-group/adaptive-volume terms.

### 5.6 The five `GoalWorkoutStrategy` implementations

Each decides (a) the weekly split — a list of `SessionBlueprint(name, movementPatternSlots)`
— and (b) the `SetRepScheme(sets, repMin, repMax, restSeconds)`. `daysPerWeek`
is clamped to `[1, 6]` via `Math.clamp` in every implementation.

| Goal | Split logic | Sets × Reps | Rest |
|---|---|---|---|
| `STRENGTH` | Every day is full-body: `LEGS, PUSH, PULL, CORE` | 4 × 3–6 | 150s |
| `HYPERTROPHY` | Cycles Push/Pull/Legs (`PUSH_PULL_LEGS_CYCLE`), 4 slots of the day's focus + 1 `CORE` slot | 4 × 8–12 | 75s |
| `FAT_LOSS` | Every day: `FULL_BODY, LEGS, PUSH, PULL, CORE` circuit | 3 × 12–20 | 30s |
| `ENDURANCE` | Every day: `FULL_BODY, LEGS, CORE, PUSH, PULL` | 3 × 15–25 | 30s |
| `GENERAL_FITNESS` | Every day: `PUSH, PULL, LEGS, CORE` | 3 × 10–15 | 60s |

`GoalStrategyResolver` is a one-line Factory: it collects every
`GoalWorkoutStrategy` Spring bean into a `Map<TrainingGoal, GoalWorkoutStrategy>`
at construction time (via `supportedGoal()`), then `.resolve(goal)` is a map
lookup that throws `IllegalStateException` if a goal has no registered
strategy (a misconfiguration bug, not a user error → falls to 500, §4).

### 5.7 Putting it together: `RuleBasedWorkoutPlanGenerator.generate()`

1. Load the `User` (→ 400/404 flow: `IllegalArgumentException` if the user id
   itself doesn't exist — this is an internal invariant violation since the
   id comes from the authenticated principal, not user input; `ResponseStatusException(404)`
   if the user exists but has no `UserProfile` yet).
2. Resolve `goal` = request override or `profile.primaryGoal`; resolve the
   matching `GoalWorkoutStrategy`.
3. Build `RecentActivitySummary` for the user (§5.5).
4. Load the **entire** exercise catalog once (`exerciseRepository.findAll()`)
   — filtering happens in memory per slot, not via SQL `WHERE` clauses. Fine
   at 28 seed rows; would need revisiting at catalog scale (see §10).
5. Construct one `RuleBasedDecisionEngine<FitnessDecisionContext, Exercise>`
   for the whole generation (rules + scorer are stateless singletons, safe
   to reuse across slots).
6. `adjustForRecentFeedback(strategy.setRepScheme(), recentActivity)` —
   compute the plan-wide sets/rest scheme once (see below), before touching
   any session.
7. `nonRestDaysInWeekOrder(profile.getRestDays())` — the ordered list of
   weekdays sessions will be assigned to (see below), also computed once.
8. For each `SessionBlueprint` in the goal's split (index `i`):
   - `trimToFitDuration(blueprint, setRepScheme, profile.getSessionDurationMinutes())`
     (see below) — a *possibly* shortened blueprint for this session.
   - `dayOfWeek = availableDays.get(i % availableDays.size())` — cycles if
     there are more sessions than non-rest days in a week.
   - For each movement-pattern slot in the (possibly trimmed) blueprint:
     - Build a fresh `FitnessDecisionContext` targeting that slot's pattern.
     - Exclude any exercise already used **elsewhere in this same plan**
       (`usedInThisPlan`, a running `Set<Long>`) — guarantees no repeats
       within one generated plan.
     - Rank the remaining catalog through the engine.
     - `pickBest`: if the top two scores are within `TIE_SCORE_TOLERANCE = 1.0`
       of each other, delegate to `AmbiguityResolver.resolveTie(...)` (today:
       `NoOpAmbiguityResolver` just takes candidate #1, since the list is
       already sorted best-first — this is the seam an AI tie-breaker would
       plug into). Otherwise take candidate #1 directly.
     - If ranking is empty, skip the slot.
9. Assemble `WorkoutSession`s (with `dayOfWeek` and `SessionExercise`s
   carrying the adjusted scheme's values) into a `WorkoutPlan` with
   `generationSource = RULE_BASED`, `active = true`, `generatedAt = now`.

Three private methods do the new work — all three exist specifically
because a piece of `UserProfile`/`WorkoutLog` data was being captured and
then silently ignored by generation:

- **`nonRestDaysInWeekOrder(Set<DayOfWeek> restDays)`** — `profile.restDays`
  used to have zero effect on anything. Walks `DayOfWeek.values()` (already
  Monday-first in the JDK enum) filtering out rest days; if the profile
  contradicts itself by marking every day a rest day, falls back to all
  seven rather than returning an empty list sessions could never index
  into. This is why `/dashboard` can show "Monday — Push Day 1" instead of
  "Day 1" — see the template in §13.
- **`trimToFitDuration(blueprint, setRepScheme, sessionDurationMinutes)`** —
  `profile.sessionDurationMinutes` used to have zero effect on anything.
  Estimates seconds-per-exercise as `sets * (restSeconds + 40)` (a flat
  assumption, not measured), computes how many slots fit in
  `sessionDurationMinutes * 60 - 600` (600s reserved for warm-up/cool-down),
  floors at `MIN_EXERCISE_SLOTS = 3`, and **only ever trims**, never pads —
  a goal strategy's blueprint is an authored movement-pattern balance (e.g.
  Hypertrophy's 4-focus-slots-plus-1-core), and a duration preference alone
  doesn't justify unbalancing it by adding a slot. `WorkoutSession` has its
  own, separately-computed `getEstimatedDurationMinutes()` for display
  (§13) — same two constants, deliberately duplicated rather than shared,
  since a generation-time sizing decision and a post-hoc display estimate
  are different concerns that happen to reuse the same rough math.
- **`adjustForRecentFeedback(base, recentActivity)`** — before this, a low
  `lastCompletionPercentage` or an extreme `averagePerceivedIntensity` only
  ever shifted *which difficulty tier of exercise* got picked
  (`FitnessExerciseScorer.difficultyAdjustment`, §5.4) — the actual
  prescribed sets/rest never moved. "Too hard" (completion < 60 OR average
  intensity ≥ 4.5) drops sets by 1 (floor `MIN_SETS = 2`) and adds 15s rest;
  "too easy" (completion > 90 AND average intensity ≤ 2.0) adds a set and
  removes 15s rest (floor `MIN_REST_SECONDS = 20`). Applied once per plan,
  to every session in it — deliberately simple over per-session tuning.

`WorkoutPlanService.generate()` (application layer) then, **before** saving
the newly generated plan, calls `workoutPlanRepository.deactivateAllForUser(userId)`
— a bulk `UPDATE workout_plans SET active = false WHERE user_id = ? AND active = true`
(`WorkoutPlanJpaRepository.deactivateAllForUser`). This was a real bug fixed
after the fact: every `generate()` call inserts a plan with `active = true`,
and without this step, repeated generation left *multiple* plans marked
active — `findActiveByUserId` (`findFirstByUser_IdAndActiveTrueOrderByGeneratedAtDesc`,
now ordered defensively) had no way to know which one was "current." The fix
is a bulk flag flip rather than loading + re-saving the old plan entity,
specifically because `WorkoutPlanEntity.sessions` is `CascadeType.ALL` +
`orphanRemoval = true` — re-saving an entity just to flip one boolean risks
Hibernate rewriting its child `workout_sessions`/`session_exercises`, which
would cascade-delete any `workout_logs` pointing at the old session ids.
After deactivating, it persists the new plan, loads the (now-saved) user
again, and calls `WorkoutExplanationService.explain()` +
`MotivationalMessageService.motivate()` to build the human-readable response
(`GenerationResult`).

---

## 6. User journey / request flows

### Register → log in (API)
```
POST /api/v1/auth/register {username, email, password}  → 201, AuthController → AuthService.register()
POST /api/v1/auth/login    {username, password}          → 200 {token, "Bearer"}, AuthController → AuthService.login()
```
`AuthService.register` checks username/email uniqueness (→ 409 via
`IllegalArgumentException` if taken), looks up the seeded `ROLE_USER`,
hashes the password with `BCryptPasswordEncoder`, saves via `UserRepository`.
`login` re-uses Spring's `AuthenticationManager` (so the exact same
credential-checking path as any other Spring Security integration is
exercised) then mints a JWT via `JwtService`.

### Register → log in (browser)
`/register` (`RegisterController`) and `/login` (`LoginController`,
`formLogin()`) render Thymeleaf pages; the browser then holds a session
cookie instead of a bearer token.

### Fill in profile
```
PUT /api/v1/profile/me  (ProfileController → UserProfileService.updateProfile)
GET/POST /profile       (ProfileViewController → same UserProfileService.updateProfile)
```
Either way, one call/submit replaces the whole `UserProfile` (goal,
experience, days/week, session length, location, equipment ids,
favorite/disliked exercise ids, preferred/unwanted categories, injuries,
rest days). `UpdateProfileRequest` (web DTO) → `ProfileUpdateCommand`
(application-layer record) mapping happens **in the controller**, not via a
static factory on the Command — deliberately, so `application.service` never
depends on `web.api.dto` (see the comment in `ProfileController.toCommand`).
`ProfileViewController` does the same mapping from its own `ProfileForm`
(§13) — same rationale, same pattern, different source type.

### Generate a plan
```
POST /api/v1/workout-plans/generate  (optional {goalOverride})
POST /dashboard/generate              (optional goalOverride form field)
```
→ `WorkoutPlanService.generate()` → `RuleBasedWorkoutPlanGenerator.generate()`
(§5.7, which now also deactivates the previous plan first) → saved via
`WorkoutPlanRepository` → response includes the plan + `explanation` +
`motivation` text (`GenerateWorkoutPlanResponse`). The dashboard's
`goalOverride` is bound as a raw `String`, not `TrainingGoal`, in
`DashboardController.generate()` — `@RequestParam`'s `ConversionService` has
no "empty string means null" special case (unlike bean-property binding via
`WebDataBinder`, which does), so an unset `<select>` submitting `""` would
otherwise fail enum conversion instead of meaning "use my profile's goal."
The controller parses it manually: blank/null → `null`, otherwise
`TrainingGoal.valueOf(...)`.

### Change password
```
PUT /api/v1/account/password  {currentPassword, newPassword}   (AccountController)
GET/POST /account/password                                       (AccountViewController)
```
Both call `AuthService.changePassword(userId, currentPassword, newPassword)`:
verifies `currentPassword` against the stored hash via
`PasswordEncoder.matches` (→ 400 `ResponseStatusException` if wrong, same
error-shape story as everywhere else, §4), then re-encodes and saves. Reuses
the same full-`User`-object `save()` path as `UserProfileService`/`AuthService.register`
— safe because `UserRepositoryAdapter.save()` only touches what a loaded
`User` actually carries (roles, profile) and here that's the same object
`findById` returned, so nothing else is disturbed.

### Manage the exercise catalog (web)
```
GET  /admin/exercises              list
GET  /admin/exercises/new          blank create form
POST /admin/exercises/new          create
GET  /admin/exercises/{id}/edit    prefilled edit form
POST /admin/exercises/{id}/edit    update
```
`AdminExerciseController` (ROLE_ADMIN only, §4) calls the exact same
`ExerciseService.create()`/`update()`/`getById()`/`listAll()` the REST
`ExerciseController` calls — the catalog-write rules live in exactly one
place regardless of which surface reaches them. `ExerciseForm` (§13) handles
the equipment checkbox list.

### View the plan
`GET /api/v1/workout-plans/active` (the one currently `active=true`),
`GET /api/v1/workout-plans` (history), `GET /api/v1/workout-plans/{id}`
(ownership-checked: 403 if `plan.userId != principal.getId()`), or the
`/dashboard` Thymeleaf page.

### Perform + log a session
```
POST /api/v1/workout-plans/{planId}/sessions/{sessionId}/complete
  {completionPercentage, rating, perceivedIntensity, notes, exercisePerformances[]}
POST /dashboard/sessions/{sessionId}/complete
  completionPercentage, rating, perceivedIntensity (planId as a hidden field)
```
The web form didn't have a `perceivedIntensity` field at all until the
adaptive-volume rule (§5.7) started actually consuming it — before that it
was silently always `null` for anyone using the dashboard instead of the
API, which would have made "too easy"/"too hard" a REST-API-only feature by
accident. `WorkoutPlanService.completeSession`:
1. Verifies the plan belongs to the user and the session belongs to the plan.
2. Saves a `WorkoutLog` row.
3. `updateStreak`: if `lastWorkoutDate == yesterday` → increment; if
   `== today` → no-op (already logged today); otherwise reset to 1.
   `longestStreak = max(longestStreak, newCurrentStreak)`.
4. `detectPersonalRecords`: for each `exercisePerformances[]` entry
   (validated to belong to this session, else 400), checks
   `weightKg`/`reps` against the user's current best
   (`PersonalRecordRepository.findBestByUserIdAndExerciseIdAndMetricType`)
   for `MAX_WEIGHT`/`MAX_REPS` respectively, and inserts a new
   `PersonalRecord` only if it's a strict improvement. This method is
   `synchronized` specifically to close a check-then-act race on concurrent
   completions for the same (user, exercise, metric) — see the comment in
   `WorkoutPlanService.maybeSavePersonalRecord`; it's a single-JVM guard,
   which the code explicitly notes matches this project's actual deployment
   model (no horizontal scaling).

### Adaptive feedback loop
The next `generate()` call rebuilds `RecentActivitySummary` from the logs
just written, so recently-used exercises/muscle groups score lower (and a
neglected one scores a little higher), and a low completion % or high
perceived intensity both nudges which difficulty tier of exercise gets
picked *and* actually reduces prescribed sets while adding rest — the
inverse for a high completion % with low perceived intensity — see
§5.4/§5.5/§5.7. Rest days and session length also reshape every plan (§5.7),
though those aren't "adaptive" in the history-driven sense — they come
straight from the profile, not from what happened last time. This is the
entire "learning" mechanism; there's no ML model.

### Progress
`GET /api/v1/me/streak`, `/personal-records`, `/workout-history`
(`ProgressController`, thin — reads straight from the repositories, no
service class) or the `/progress` Thymeleaf page.

---

## 7. The AI-ready seam (`fitness.engine.assist`)

Three narrow interfaces, each with a template (non-AI) default `@Component`
already wired in via normal Spring bean injection:

| Interface | Called when | Default impl | What it returns |
|---|---|---|---|
| `AmbiguityResolver` | Top-2 candidates for a slot score within 1.0 of each other | `NoOpAmbiguityResolver` | Just candidate #1 (already-sorted) |
| `WorkoutExplanationService` | Every successful `generate()` | `TemplateWorkoutExplanationService` | A one-line templated string, e.g. "Generated a 4-session HYPERTROPHY plan matched to your INTERMEDIATE experience level and available equipment." |
| `MotivationalMessageService` | Every successful `generate()` | `TemplateMotivationalMessageService` | A canned message keyed off `daysSinceLastWorkout` (welcome / same-day praise / "it's been a while" / "keep it up") |

**How you'd actually add AI later**: write `AiAmbiguityResolver` /
`AiWorkoutExplanationService` / `AiMotivationalMessageService` implementing
these same interfaces (backed by whatever LLM call), then either mark it
`@Primary` or gate the template vs. AI beans behind a Spring `@Profile`.
Nothing in `RuleBasedWorkoutPlanGenerator`, `WorkoutPlanService`, or any
controller needs to change — that's the entire point of depending on the
interface rather than the concrete class.

The bigger swap — an AI-backed generator instead of just AI-backed
explanations — is the same pattern one level up: `WorkoutPlanGenerator` is
itself a port (`fitness.engine.WorkoutPlanGenerator`); `RuleBasedWorkoutPlanGenerator`
is its only implementation today. An `AiWorkoutPlanGenerator` (or a hybrid
that falls back to rule-based) implementing the same interface, wired via
`@Primary`/profile, is a drop-in replacement. `workout_plans.generation_source`
already distinguishes `RULE_BASED` from `AI` (added in V4, years before any
AI code exists) specifically so this swap needs no migration.

---

## 8. Testing strategy

- **No-Spring-context tests** for the core logic: `Rule`/`ScoringStrategy`
  implementations and `RuleBasedDecisionEngine`/`RuleBasedWorkoutPlanGenerator`
  are constructed directly with `new` in test code (see
  `RuleBasedDecisionEngineTest`, `FitnessExerciseScorerTest`, the individual
  rule tests, `RuleBasedWorkoutPlanGeneratorTest` with Mockito-mocked repo
  ports). Fast feedback loop, no database needed.
- **`AbstractIntegrationTest`** (`src/test/java/.../AbstractIntegrationTest.java`):
  starts **one** MySQL Testcontainer as a **JVM-wide singleton**, started in
  a static initializer rather than per-test-class `@Testcontainers`
  lifecycle. The code comment explains why: per-class lifecycle recreated a
  container per test class and was unreliable on this Windows/Docker Desktop
  setup. It also waits for the MySQL "ready for connections" log line
  **twice**, because the official MySQL image restarts its server process
  once during first-time initialization, and waiting for just one such line
  can report ready before that restart, causing flaky connection-refused
  errors. Cleanup is via the Testcontainers Ryuk reaper at JVM shutdown, not
  explicit teardown code.
- All classes touching the DB or HTTP layer extend that base class and reuse
  the same container + Spring context cache across test classes.
- Controller/security tests use `MockMvc`
  (`@AutoConfigureMockMvc`). Some (`WebLoginFlowTest`,
  `DashboardControllerTest`, `ProgressViewControllerTest`) actually render
  the real Thymeleaf templates with real data — this is called out in the
  README as having caught a genuine bug (`dashboard.html` used `session` as
  a `th:each` loop variable name, which Thymeleaf reserves internally) that a
  REST-only test suite would never have found.
- The Maven Surefire config forces IPv4
  (`-Djava.net.preferIPv4Stack=true`) specifically to avoid an IPv6-first
  `localhost` resolution race against Testcontainers on Windows + Docker
  Desktop (`pom.xml` comment).

Run everything: `./mvnw test`.

---

## 9. Running locally

```bash
docker compose up -d          # MySQL 8.4 on localhost:3306, db/user/pass = dme/dme/dme
./mvnw spring-boot:run         # runs with "dev" profile → application-dev.yml
```

Flyway seeds roles/equipment/28-exercise catalog on first startup
automatically. Register at `POST /api/v1/auth/register` or
`http://localhost:8080/register`; then either call the API with the JWT from
`/api/v1/auth/login`, or log in at `http://localhost:8080/login` for the
browser/session flow. Or skip registration and use one of the seeded
[test accounts](#14-test-accounts-dev-only) (§14).

`application.yml` (base defaults) + `application-dev.yml` (local overrides:
datasource/flyway pointed at the docker-compose MySQL, plus the extra
`db/dev-migration` Flyway location) + `application-prod.yml` (cloud
overrides, §15) is the whole Spring profile setup. `app.jwt.secret` **must**
be overridden via the `JWT_SECRET` env var outside of local dev.

---

## 10. Known limitations / places to think before "upgrading"

These aren't bugs so much as places where the v1 scope was deliberately kept
small — worth knowing before you extend anything:

- **Catalog loaded entirely into memory per generation** (`exerciseRepository.findAll()`
  in `RuleBasedWorkoutPlanGenerator.generate()`). Fine at 28 rows; if you
  grow the catalog a lot, filtering (movement pattern, equipment) should
  move into the query instead of happening after loading everything.
- **`personalRecord` race guard is a single-JVM `synchronized`** — explicitly
  noted as matching current deployment (one instance), but it will silently
  stop being correct if the app is ever horizontally scaled; would need a
  DB-level unique constraint + upsert, or a pessimistic lock, instead.
- **No pagination anywhere** (`GET /api/v1/exercises`, `/workout-plans`,
  `/workout-history` all return full lists) — fine at seed-catalog scale,
  not at real scale.
- **`RestAccessDeniedHandler`/`RestAuthenticationEntryPoint` vs.
  `ApiExceptionHandler`** overlap in shape (both produce `{"message": ...}`
  for 401/403) but live in different layers (filter chain vs. controller
  advice) — worth remembering if you ever touch one, so you update the
  other to match.
- **v0.8+ roadmap (per README), not built**: full gamification
  (achievements/badges catalog, levels, weekly/monthly goal dashboards,
  charts), a real AI-backed `AmbiguityResolver`/`WorkoutExplanationService`/
  `MotivationalMessageService` (§7), and — as proof `decisionengine` is
  genuinely domain-agnostic — a second decision domain (e.g. nutrition)
  built on the same generic core.
- **Only 5 `TrainingGoal`s / 4 `MovementPattern`s / 10 `MuscleGroup`s** are
  modeled (see enum table below) — adding a goal means writing a new
  `GoalWorkoutStrategy` bean (no other change needed, per the Strategy
  pattern), but adding a movement pattern or muscle group touches the seed
  data (V12) and possibly the rule set.
- **`trimToFitDuration`'s and `WorkoutSession.getEstimatedDurationMinutes()`'s
  timing assumptions are flat guesses** (40s of work per set, 600s of
  warm-up/cool-down), not measured or configurable — good enough to roughly
  size a session, not a scheduling-grade estimate. Also only ever trims a
  blueprint, never pads it past what the goal strategy authored (§5.7) — a
  long session preference doesn't currently add a slot back.
- **Injuries are modeled per-`MuscleGroup`, not per-joint**
  (`UserLimitation.muscleGroup`) — "knee problems" or "shoulder problems"
  don't map cleanly onto a muscle group (a joint, not a muscle), so
  `InjuryContraindicationRule` can't actually exclude an exercise based on a
  joint issue unless the user also names a muscle group affected by it.
  Fixing this properly means a new `BodyArea`/joint concept plus re-tagging
  which joints each of the 28 seed exercises stresses — flagged, not done,
  since it touches every seed row.
- **As of this round, `restDays`, `sessionDurationMinutes`, and
  `perceivedIntensity` are no longer dead data** (§5.7) — they used to be
  captured on the profile/log and never read back by generation at all.
  Worth remembering if you're auditing for *other* fields with the same
  problem: `WorkoutLog.notes` and `SessionExercise.notes` are exposed
  through the REST DTOs (`WorkoutLogResponse`, `SessionExerciseResponse`)
  but still never consumed by the generation engine, nor shown anywhere in
  the Thymeleaf templates — a real gap, just a smaller one (free-text notes
  aren't the kind of thing a rule engine can act on directly anyway).

---

## 11. Domain enum reference

| Enum | Values |
|---|---|
| `TrainingGoal` | `STRENGTH`, `HYPERTROPHY`, `FAT_LOSS`, `ENDURANCE`, `GENERAL_FITNESS` |
| `ExperienceLevel` / `DifficultyLevel` | `BEGINNER`, `INTERMEDIATE`, `ADVANCED` (two separate enums, deliberately not compared by ordinal — see §5.3) |
| `MovementPattern` | `PUSH`, `PULL`, `LEGS`, `CORE`, `FULL_BODY` |
| `MuscleGroup` | `CHEST`, `BACK`, `SHOULDERS`, `BICEPS`, `TRICEPS`, `QUADRICEPS`, `HAMSTRINGS`, `GLUTES`, `CALVES`, `CORE`, `FULL_BODY` |
| `ExerciseType` | `COMPOUND`, `ISOLATION` |
| `Location` | `HOME`, `GYM`, `OUTDOOR`, `ANYWHERE` |
| `Sex` | `MALE`, `FEMALE` |
| `GenerationSource` | `RULE_BASED`, `AI` (the latter unused today, see §7) |
| `MetricType` | `MAX_WEIGHT`, `MAX_REPS` (personal records) |
| `RoleName` | `ROLE_USER`, `ROLE_ADMIN` |

---

## 12. Quick reference: REST API surface

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/v1/auth/register` | public | Create account |
| POST | `/api/v1/auth/login` | public | Get JWT |
| GET | `/api/v1/users/me` | JWT | Current user id/username/email/roles |
| GET/PUT | `/api/v1/profile/me` | JWT | Read/replace profile |
| GET | `/api/v1/exercises`, `/api/v1/exercises/{id}` | public | Catalog browse |
| POST/PUT | `/api/v1/exercises`, `/api/v1/exercises/{id}` | JWT + ROLE_ADMIN | Catalog write |
| GET | `/api/v1/equipment` | public | Equipment lookup list |
| POST | `/api/v1/workout-plans/generate` | JWT | Generate + persist a plan |
| GET | `/api/v1/workout-plans/active` | JWT | Current active plan |
| GET | `/api/v1/workout-plans` | JWT | All plans for the user |
| GET | `/api/v1/workout-plans/{id}` | JWT (owner only) | One plan |
| POST | `/api/v1/workout-plans/{planId}/sessions/{sessionId}/complete` | JWT | Log a completed session |
| GET | `/api/v1/me/streak` | JWT | Current/longest streak |
| GET | `/api/v1/me/personal-records` | JWT | All PRs |
| GET | `/api/v1/me/workout-history` | JWT | All workout logs |
| PUT | `/api/v1/account/password` | JWT | Change password |

Browser/Thymeleaf routes: `/`, `/login`, `/register`, `/dashboard`,
`/profile`, `/progress`, `/exercises`, `/account/password`, `/admin/exercises`
(+ `/new`, `/{id}/edit`) — full reference in §13.

---

## 13. Web UI page reference

Every page below is backed by the *same* application-service call the
equivalent REST endpoint uses (§12) — there is no separate "web" business
logic anywhere to drift out of sync with the API.

| Route(s) | Controller | Template | Backing form | Auth |
|---|---|---|---|---|
| `/` | `HomeController` | `home.html` | — | public — card grid: the live "Workout Planner" domain (its usual nav embedded in the card) plus `comingSoonDomains` placeholder cards (`DecisionDomainCard`, no routes yet) |
| `/register` | `RegisterController` | `register.html` | `RegisterRequest` (shared with the API DTO) | public |
| `/login` | `LoginController` (mostly Spring's `formLogin()`) | `login.html` | — | public |
| `/help` | `HelpController` | `help.html` | — | public |
| `/exercises` | `ExerciseViewController` | `exercises.html` | — | public |
| `/profile` (GET+POST) | `ProfileViewController` | `profile.html` | `ProfileForm` | session |
| `/dashboard` (GET), `/dashboard/generate` (POST), `/dashboard/sessions/{id}/complete` (POST) | `DashboardController` | `dashboard.html` | plain `@RequestParam`s | session — shows each session's day-of-week and `getEstimatedDurationMinutes()` (§5.7); the complete-session form includes `perceivedIntensity`, not just completion %/rating |
| `/progress` | `ProgressViewController` | `progress.html` | — | session |
| `/account/password` (GET+POST) | `AccountViewController` | `account-password.html` | `ChangePasswordRequest` (shared with the API DTO) | session |
| `/admin/exercises`, `/admin/exercises/new`, `/admin/exercises/{id}/edit` | `AdminExerciseController` | `admin-exercises.html`, `admin-exercise-form.html` | `ExerciseForm` | session + `ROLE_ADMIN` |

### Why some pages need a dedicated form-backing bean and some don't

`RegisterRequest`/`ChangePasswordRequest` are plain immutable records with
only flat `String`/primitive fields — Spring 6.1+'s constructor-based data
class binding handles them directly in an `@ModelAttribute`, the same way
`RegisterController` has always used `RegisterRequest` as-is (proving this
was already an established pattern before this round of work, not something
new invented for it).

`ProfileForm` and `ExerciseForm` (`web.mvc.form`) exist because `/profile`
and the admin exercise form need two things records can't give a Thymeleaf
template for free:
1. **Checkbox-list binding** (`List<Long> equipmentIds`, `List<MuscleGroup>
   preferredCategories`, etc.) — Thymeleaf's `th:field` manages the
   "unchecked box submits nothing" problem automatically (injecting a hidden
   `_fieldName` marker) but only against a plain JavaBean property with a
   getter *and* setter; a record's accessor-only shape doesn't fit that.
2. **Indexed nested rows** (`ProfileForm.limitations`, a fixed-size padded
   list of `LimitationRow`) — bound via
   `th:field="*{limitations[__${iter.index}__].note}"`, which needs the same
   mutable-bean shape one level down.

Both controllers convert their form back to the application-layer Command
type themselves (`toCommand(...)`, private method) rather than the Command
type knowing how to build itself from a form — same layering rule as
`ProfileController.toCommand` in `web.api`: Command types live in
`application.service` and must never depend on anything in `web.*`.

### A bug the manual verification of this UI actually caught

While building `/profile`, a `th:each="eq : ${allEquipment}"` loop threw
`IllegalArgumentException: Iteration variable cannot be null` — not because
`allEquipment` was null, but because `eq` collides with a reserved
comparison keyword in Thymeleaf's expression language (`eq`/`ne`/`gt`/`lt`/...
are textual operators, same family as the `session`-as-a-`th:each`-variable
gotcha already called out in the README's Testing section). Renamed the loop
variable to `equipment` and it rendered fine. Worth remembering before
naming any future `th:each` variable in this codebase.

---

## 14. Test accounts (dev-only)

`src/main/resources/db/dev-migration/V13__seed_test_users.sql` seeds two
accounts, both with password **`Test1234!`**:

| Username | Roles | Notes |
|---|---|---|
| `testuser` | `ROLE_USER` | Profile pre-filled (HYPERTROPHY, INTERMEDIATE, 4 days/week, GYM, some equipment, one preferred category) plus a `workout_streaks` row (3-day current streak) — can generate a plan immediately with zero setup |
| `testadmin` | `ROLE_USER`, `ROLE_ADMIN` | Deliberately left without a profile, so the "no profile yet" state (`/profile`'s `hasProfile == false` branch, §13) is still reachable to click through — use this account to reach `/admin/exercises` |

**Why this migration lives outside `db/migration`.** Flyway's migration
`locations` is itself just a Spring property
(`spring.flyway.locations`), and only `application-dev.yml` sets it to
`classpath:db/migration,classpath:db/dev-migration`; the base
`application.yml` (and therefore `application-prod.yml`, which doesn't
override it) implicitly keeps Spring Boot's default of just
`classpath:db/migration`. So this migration only ever runs when the `dev`
profile is active — never against a deployment that doesn't explicitly opt
in the same way. Versioned as `V13` (continuing the main sequence, not
restarting at `V1`) specifically because Flyway merges every configured
location into **one** chronological version history — two files both
claiming `V1` from different locations would be a hard error
("Found more than one migration with version 1").

If you ever reuse this project as a base for something with real users,
**delete `db/dev-migration` entirely** rather than trusting the profile
flag alone to keep known credentials off a real database.

---

## 15. Cloud deployment

Three additions, no application code changes:

- **`Dockerfile`** (repo root) — multi-stage: `eclipse-temurin:25-jdk` runs
  `./mvnw package` (a `chmod +x mvnw` first, since the executable bit
  doesn't reliably survive a Windows checkout → Docker COPY), then
  `eclipse-temurin:25-jre` runs the resulting jar. `.dockerignore` keeps
  `target/`, `.git/`, `.idea/` out of the build context.
- **`application-prod.yml`** — activated via `SPRING_PROFILES_ACTIVE=prod`.
  Sets `server.port: ${PORT:8080}` (most PaaS hosts inject `PORT` to dictate
  the bind port) and `spring.datasource.*`/`spring.flyway.*` from
  `SPRING_DATASOURCE_URL`/`SPRING_DATASOURCE_USERNAME`/`SPRING_DATASOURCE_PASSWORD`
  — **with no defaults**, unlike `app.jwt.secret`'s dev fallback in the base
  `application.yml`. That's deliberate: a missing datasource env var should
  fail the app at startup with a clear Spring Boot error, not silently do
  nothing or connect to `localhost`.
- **`spring-boot-starter-actuator`** (new `pom.xml` dependency) exposing
  `GET /actuator/health`, made `permitAll()` in `SecurityConfig` (§4) so a
  cloud health check doesn't need credentials. Point your host's health
  check/readiness probe at this path.

What this does **not** do: provision a database, pick a specific cloud
provider, or seed any data beyond the normal `V1`–`V12` migrations (the
`db/dev-migration` test accounts, §14, are dev-profile-only and won't exist
in a `prod`-profile deployment).
