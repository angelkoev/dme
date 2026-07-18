package com.akoev.dme.fitness.engine.rulebased.rules;

import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DislikedExerciseRuleTest {

    private final DislikedExerciseRule rule = new DislikedExerciseRule();

    @Test
    void excludesDislikedExercise() {
        Exercise burpee = exercise(42L);
        UserProfile profile = UserProfile.builder().dislikedExercises(Set.of(burpee)).build();
        FitnessDecisionContext context = FitnessDecisionContext.builder().profile(profile).build();

        assertThat(rule.isSatisfiedBy(context, burpee)).isFalse();
        assertThat(rule.isSatisfiedBy(context, exercise(99L))).isTrue();
    }

    private Exercise exercise(Long id) {
        return Exercise.builder()
                .id(id)
                .name("Exercise " + id)
                .primaryMuscleGroup(MuscleGroup.FULL_BODY)
                .movementPattern(MovementPattern.FULL_BODY)
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .exerciseType(ExerciseType.COMPOUND)
                .build();
    }
}
