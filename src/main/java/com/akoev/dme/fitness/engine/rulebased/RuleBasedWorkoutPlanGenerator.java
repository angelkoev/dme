package com.akoev.dme.fitness.engine.rulebased;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.decisionengine.RuleBasedDecisionEngine;
import com.akoev.dme.decisionengine.ScoredCandidate;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.GenerationSource;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.SessionExercise;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.domain.model.WorkoutPlan;
import com.akoev.dme.domain.model.WorkoutSession;
import com.akoev.dme.domain.repository.ExerciseRepository;
import com.akoev.dme.domain.repository.UserRepository;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import com.akoev.dme.fitness.engine.GenerationRequest;
import com.akoev.dme.fitness.engine.RecentActivitySummary;
import com.akoev.dme.fitness.engine.RecentActivitySummaryBuilder;
import com.akoev.dme.fitness.engine.WorkoutPlanGenerator;
import com.akoev.dme.fitness.engine.assist.AmbiguityResolver;
import com.akoev.dme.fitness.engine.rulebased.scoring.FitnessExerciseScorer;
import com.akoev.dme.fitness.engine.rulebased.strategy.GoalStrategyResolver;
import com.akoev.dme.fitness.engine.rulebased.strategy.GoalWorkoutStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The rule-based implementation of {@link WorkoutPlanGenerator} (Template
 * Method): resolve the goal strategy, build a {@link RecentActivitySummary}
 * from workout history, then for every session slot rank the exercise
 * catalog through the generic {@link RuleBasedDecisionEngine} and assemble
 * the result into a {@link WorkoutPlan}.
 */
@Component
@RequiredArgsConstructor
public class RuleBasedWorkoutPlanGenerator implements WorkoutPlanGenerator {

    private static final double TIE_SCORE_TOLERANCE = 1.0;

    // Duration-based slot trimming (see trimToFitDuration): rough assumptions,
    // not measurements — mirrored (not shared) in WorkoutSession's own
    // duration estimate, which serves a different purpose (display) and
    // shouldn't couple to this class's algorithm.
    private static final int ASSUMED_WORK_SECONDS_PER_SET = 40;
    private static final int WARMUP_COOLDOWN_SECONDS = 600;
    private static final int MIN_EXERCISE_SLOTS = 3;

    // Adaptive volume (see adjustForRecentFeedback): perceivedIntensity (how
    // hard a session FELT) drives this, not the satisfaction-oriented
    // "rating" field — a workout can be rated highly and still have felt
    // too easy or too hard.
    private static final int LOW_COMPLETION_THRESHOLD = 60;
    private static final int HIGH_COMPLETION_THRESHOLD = 90;
    private static final double HIGH_INTENSITY_THRESHOLD = 4.5;
    private static final double LOW_INTENSITY_THRESHOLD = 2.0;
    private static final int REST_ADJUSTMENT_SECONDS = 15;
    private static final int MIN_SETS = 2;
    private static final int MIN_REST_SECONDS = 20;

    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final RecentActivitySummaryBuilder recentActivitySummaryBuilder;
    private final GoalStrategyResolver goalStrategyResolver;
    private final List<Rule<FitnessDecisionContext, Exercise>> rules;
    private final FitnessExerciseScorer scorer;
    private final AmbiguityResolver ambiguityResolver;

    @Override
    public WorkoutPlan generate(GenerationRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown user id: " + request.userId()));
        UserProfile profile = user.getProfile();
        if (profile == null) {
            // Same expected, user-actionable condition (and status) as
            // UserProfileService.getProfile() — not a server misconfiguration,
            // so it must not fall into the generic 500 catch-all.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not created yet");
        }

        TrainingGoal goal = request.goalOverride() != null ? request.goalOverride() : profile.getPrimaryGoal();
        GoalWorkoutStrategy strategy = goalStrategyResolver.resolve(goal);
        RecentActivitySummary recentActivity = recentActivitySummaryBuilder.build(request.userId());
        List<Exercise> catalog = exerciseRepository.findAll();
        RuleBasedDecisionEngine<FitnessDecisionContext, Exercise> engine = new RuleBasedDecisionEngine<>(rules, scorer);

        List<GoalWorkoutStrategy.SessionBlueprint> blueprints = strategy.buildSplit(profile.getDaysPerWeek());
        GoalWorkoutStrategy.SetRepScheme setRepScheme = adjustForRecentFeedback(strategy.setRepScheme(), recentActivity);
        List<DayOfWeek> availableDays = nonRestDaysInWeekOrder(profile.getRestDays());

        List<WorkoutSession> sessions = new ArrayList<>();
        Set<Long> usedInThisPlan = new HashSet<>();

        for (int sessionIndex = 0; sessionIndex < blueprints.size(); sessionIndex++) {
            GoalWorkoutStrategy.SessionBlueprint blueprint = trimToFitDuration(
                    blueprints.get(sessionIndex), setRepScheme, profile.getSessionDurationMinutes());
            DayOfWeek dayOfWeek = availableDays.get(sessionIndex % availableDays.size());
            sessions.add(buildSession(sessionIndex + 1, blueprint, setRepScheme, dayOfWeek, profile, recentActivity,
                    catalog, engine, usedInThisPlan));
        }

        return WorkoutPlan.builder()
                .userId(request.userId())
                .goal(goal)
                .generatedAt(Instant.now())
                .active(true)
                .generationSource(GenerationSource.RULE_BASED)
                .sessions(sessions)
                .build();
    }

    private WorkoutSession buildSession(int sessionIndex, GoalWorkoutStrategy.SessionBlueprint blueprint,
                                         GoalWorkoutStrategy.SetRepScheme setRepScheme, DayOfWeek dayOfWeek,
                                         UserProfile profile, RecentActivitySummary recentActivity, List<Exercise> catalog,
                                         RuleBasedDecisionEngine<FitnessDecisionContext, Exercise> engine,
                                         Set<Long> usedInThisPlan) {
        List<SessionExercise> sessionExercises = new ArrayList<>();
        List<MovementPattern> slots = blueprint.exerciseSlots();

        for (int slot = 0; slot < slots.size(); slot++) {
            MovementPattern targetPattern = slots.get(slot);
            FitnessDecisionContext context = FitnessDecisionContext.builder()
                    .profile(profile)
                    .recentActivity(recentActivity)
                    .targetMovementPattern(targetPattern)
                    .build();

            List<Exercise> availableCandidates = catalog.stream()
                    .filter(exercise -> !usedInThisPlan.contains(exercise.getId()))
                    .toList();

            Exercise chosen = pickBest(context, engine.rank(context, availableCandidates));
            if (chosen == null) {
                continue;
            }

            usedInThisPlan.add(chosen.getId());
            sessionExercises.add(SessionExercise.builder()
                    .exercise(chosen)
                    .orderIndex(slot + 1)
                    .sets(setRepScheme.sets())
                    .repRangeMin(setRepScheme.repMin())
                    .repRangeMax(setRepScheme.repMax())
                    .restSeconds(setRepScheme.restSeconds())
                    .build());
        }

        return WorkoutSession.builder()
                .sessionIndex(sessionIndex)
                .name(blueprint.name())
                .dayOfWeek(dayOfWeek)
                .exercises(sessionExercises)
                .build();
    }

    // Rest days are captured on the profile but otherwise never consulted —
    // without this, a user's chosen rest days had zero effect on generation.
    // Walks the week Monday-first, skipping rest days; if every day is
    // marked rest (a contradictory profile, but not our job to reject it
    // here), falls back to using all seven rather than looping forever or
    // returning an empty list sessions could never index into.
    private List<DayOfWeek> nonRestDaysInWeekOrder(Set<DayOfWeek> restDays) {
        List<DayOfWeek> available = Arrays.stream(DayOfWeek.values())
                .filter(day -> !restDays.contains(day))
                .collect(Collectors.toCollection(ArrayList::new));
        return available.isEmpty() ? List.of(DayOfWeek.values()) : available;
    }

    // sessionDurationMinutes is captured on the profile but otherwise never
    // consulted — without this, "how long do you want to train" had zero
    // effect on the generated plan. Only ever trims, never pads: adding a
    // slot beyond what the goal strategy authored risks unbalancing its
    // intended movement-pattern split (e.g. a 5th PUSH slot on a Hypertrophy
    // push day), which a duration preference alone doesn't justify.
    private GoalWorkoutStrategy.SessionBlueprint trimToFitDuration(
            GoalWorkoutStrategy.SessionBlueprint blueprint, GoalWorkoutStrategy.SetRepScheme setRepScheme,
            int sessionDurationMinutes) {
        int secondsPerExercise = setRepScheme.sets() * (setRepScheme.restSeconds() + ASSUMED_WORK_SECONDS_PER_SET);
        int availableSeconds = sessionDurationMinutes * 60 - WARMUP_COOLDOWN_SECONDS;
        int targetSlots = Math.max(MIN_EXERCISE_SLOTS, availableSeconds / secondsPerExercise);

        List<MovementPattern> slots = blueprint.exerciseSlots();
        if (slots.size() <= targetSlots) {
            return blueprint;
        }
        return new GoalWorkoutStrategy.SessionBlueprint(blueprint.name(), slots.subList(0, targetSlots));
    }

    // completionPercentage/perceivedIntensity are captured on every logged
    // session but otherwise only nudge which DIFFICULTY of exercise gets
    // picked (FitnessExerciseScorer) — the actual prescribed sets/rest never
    // changed in response. This is the "too easy"/"too hard" adaptive rule:
    // it adjusts the scheme once per plan, applied to every session in it.
    private GoalWorkoutStrategy.SetRepScheme adjustForRecentFeedback(
            GoalWorkoutStrategy.SetRepScheme base, RecentActivitySummary recentActivity) {
        Integer completion = recentActivity.getLastCompletionPercentage();
        Double intensity = recentActivity.getAveragePerceivedIntensity();

        boolean tooHard = (completion != null && completion < LOW_COMPLETION_THRESHOLD)
                || (intensity != null && intensity >= HIGH_INTENSITY_THRESHOLD);
        if (tooHard) {
            return new GoalWorkoutStrategy.SetRepScheme(
                    Math.max(base.sets() - 1, MIN_SETS), base.repMin(), base.repMax(),
                    base.restSeconds() + REST_ADJUSTMENT_SECONDS);
        }

        boolean tooEasy = completion != null && completion > HIGH_COMPLETION_THRESHOLD
                && intensity != null && intensity <= LOW_INTENSITY_THRESHOLD;
        if (tooEasy) {
            return new GoalWorkoutStrategy.SetRepScheme(
                    base.sets() + 1, base.repMin(), base.repMax(),
                    Math.max(base.restSeconds() - REST_ADJUSTMENT_SECONDS, MIN_REST_SECONDS));
        }

        return base;
    }

    private Exercise pickBest(FitnessDecisionContext context, List<ScoredCandidate<Exercise>> ranked) {
        if (ranked.isEmpty()) {
            return null;
        }
        if (ranked.size() > 1 && isTie(ranked.get(0), ranked.get(1))) {
            return ambiguityResolver.resolveTie(context, ranked);
        }
        return ranked.get(0).candidate();
    }

    private boolean isTie(ScoredCandidate<Exercise> first, ScoredCandidate<Exercise> second) {
        return Math.abs(first.score() - second.score()) < TIE_SCORE_TOLERANCE;
    }
}
