package com.akoev.dme.fitness.engine;

import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.WorkoutLog;
import com.akoev.dme.domain.repository.WorkoutLogRepository;
import com.akoev.dme.domain.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Builds a {@link RecentActivitySummary} from {@code workout_logs}. Shared
 * by {@link com.akoev.dme.fitness.engine.rulebased.RuleBasedWorkoutPlanGenerator}
 * (to adapt exercise selection) and the application service layer (e.g. to
 * feed {@link com.akoev.dme.fitness.engine.assist.MotivationalMessageService}).
 */
@Component
@RequiredArgsConstructor
public class RecentActivitySummaryBuilder {

    private static final int RECENT_ACTIVITY_WINDOW_DAYS = 14;

    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutSessionRepository workoutSessionRepository;

    public RecentActivitySummary build(Long userId) {
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
