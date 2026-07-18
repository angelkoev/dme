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

class UnwantedCategoryRuleTest {

    private final UnwantedCategoryRule rule = new UnwantedCategoryRule();

    @Test
    void excludesExerciseInUnwantedCategory() {
        UserProfile profile = UserProfile.builder().unwantedCategories(Set.of(MuscleGroup.CALVES)).build();
        FitnessDecisionContext context = FitnessDecisionContext.builder().profile(profile).build();

        assertThat(rule.isSatisfiedBy(context, exercise(MuscleGroup.CALVES))).isFalse();
        assertThat(rule.isSatisfiedBy(context, exercise(MuscleGroup.BACK))).isTrue();
    }

    private Exercise exercise(MuscleGroup muscleGroup) {
        return Exercise.builder()
                .id(1L)
                .name("Exercise")
                .primaryMuscleGroup(muscleGroup)
                .movementPattern(MovementPattern.PUSH)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .exerciseType(ExerciseType.ISOLATION)
                .build();
    }
}
