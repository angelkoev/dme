package com.akoev.dme.fitness.engine;

import com.akoev.dme.domain.model.MuscleGroup;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

/**
 * A digest of a user's recent training history, built from {@code workout_logs}.
 * This is what makes the rule-based engine adaptive: without it, generation
 * would be stateless and would repeat the same exercises every time.
 */
@Getter
@Builder
public class RecentActivitySummary {

    @Builder.Default
    private Set<Long> recentlyUsedExerciseIds = Set.of();
    @Builder.Default
    private Map<MuscleGroup, Integer> recentLoadByMuscleGroup = Map.of();
    private Integer lastCompletionPercentage;
    private Integer daysSinceLastWorkout;
    private Double averageRating;

    public static RecentActivitySummary empty() {
        return RecentActivitySummary.builder().build();
    }
}
