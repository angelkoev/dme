package com.akoev.dme.fitness.engine.rulebased;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.decisionengine.RuleBasedDecisionEngine;
import com.akoev.dme.decisionengine.ScoredCandidate;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.GenerationSource;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.SessionExercise;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.domain.model.WorkoutLog;
import com.akoev.dme.domain.model.WorkoutPlan;
import com.akoev.dme.domain.model.WorkoutSession;
import com.akoev.dme.domain.repository.ExerciseRepository;
import com.akoev.dme.domain.repository.UserRepository;
import com.akoev.dme.domain.repository.WorkoutLogRepository;
import com.akoev.dme.domain.repository.WorkoutSessionRepository;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import com.akoev.dme.fitness.engine.GenerationRequest;
import com.akoev.dme.fitness.engine.RecentActivitySummary;
import com.akoev.dme.fitness.engine.WorkoutPlanGenerator;
import com.akoev.dme.fitness.engine.assist.AmbiguityResolver;
import com.akoev.dme.fitness.engine.rulebased.scoring.FitnessExerciseScorer;
import com.akoev.dme.fitness.engine.rulebased.strategy.GoalStrategyResolver;
import com.akoev.dme.fitness.engine.rulebased.strategy.GoalWorkoutStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    private static final int RECENT_ACTIVITY_WINDOW_DAYS = 14;
    private static final double TIE_SCORE_TOLERANCE = 1.0;

    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
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
            throw new IllegalStateException("User has no profile yet: " + request.userId());
        }

        TrainingGoal goal = request.goalOverride() != null ? request.goalOverride() : profile.getPrimaryGoal();
        GoalWorkoutStrategy strategy = goalStrategyResolver.resolve(goal);
        RecentActivitySummary recentActivity = buildRecentActivitySummary(request.userId());
        List<Exercise> catalog = exerciseRepository.findAll();
        RuleBasedDecisionEngine<FitnessDecisionContext, Exercise> engine = new RuleBasedDecisionEngine<>(rules, scorer);

        List<GoalWorkoutStrategy.SessionBlueprint> blueprints = strategy.buildSplit(profile.getDaysPerWeek());
        GoalWorkoutStrategy.SetRepScheme setRepScheme = strategy.setRepScheme();

        List<WorkoutSession> sessions = new ArrayList<>();
        Set<Long> usedInThisPlan = new HashSet<>();

        for (int sessionIndex = 0; sessionIndex < blueprints.size(); sessionIndex++) {
            GoalWorkoutStrategy.SessionBlueprint blueprint = blueprints.get(sessionIndex);
            sessions.add(buildSession(sessionIndex + 1, blueprint, setRepScheme, profile, recentActivity,
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
                                         GoalWorkoutStrategy.SetRepScheme setRepScheme, UserProfile profile,
                                         RecentActivitySummary recentActivity, List<Exercise> catalog,
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
                .exercises(sessionExercises)
                .build();
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

    private RecentActivitySummary buildRecentActivitySummary(Long userId) {
        Instant since = Instant.now().minus(RECENT_ACTIVITY_WINDOW_DAYS, ChronoUnit.DAYS);
        List<WorkoutLog> recentLogs = workoutLogRepository.findRecentByUserId(userId, since);
        if (recentLogs.isEmpty()) {
            return RecentActivitySummary.empty();
        }

        Set<Long> recentlyUsedExerciseIds = new HashSet<>();
        Map<MuscleGroup, Integer> loadByMuscleGroup = new EnumMap<>(MuscleGroup.class);

        for (WorkoutLog log : recentLogs) {
            workoutSessionRepository.findById(log.getWorkoutSessionId()).ifPresent(session ->
                    session.getExercises().forEach(sessionExercise -> {
                        Exercise exercise = sessionExercise.getExercise();
                        recentlyUsedExerciseIds.add(exercise.getId());
                        loadByMuscleGroup.merge(exercise.getPrimaryMuscleGroup(), 1, Integer::sum);
                    }));
        }

        WorkoutLog mostRecent = recentLogs.get(0);
        long daysSince = ChronoUnit.DAYS.between(mostRecent.getPerformedAt(), Instant.now());
        double averageRating = recentLogs.stream()
                .map(WorkoutLog::getRating)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        return RecentActivitySummary.builder()
                .recentlyUsedExerciseIds(recentlyUsedExerciseIds)
                .recentLoadByMuscleGroup(loadByMuscleGroup)
                .lastCompletionPercentage(mostRecent.getCompletionPercentage())
                .daysSinceLastWorkout((int) daysSince)
                .averageRating(averageRating)
                .build();
    }
}
