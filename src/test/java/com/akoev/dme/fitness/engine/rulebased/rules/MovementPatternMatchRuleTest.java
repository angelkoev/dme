package com.akoev.dme.fitness.engine.rulebased.rules;

import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MovementPatternMatchRuleTest {

    private final MovementPatternMatchRule rule = new MovementPatternMatchRule();

    @Test
    void onlyMatchesExercisesWithTheTargetedPattern() {
        FitnessDecisionContext context = FitnessDecisionContext.builder()
                .profile(UserProfile.builder().build())
                .targetMovementPattern(MovementPattern.PULL)
                .build();

        assertThat(rule.isSatisfiedBy(context, exercise(MovementPattern.PULL))).isTrue();
        assertThat(rule.isSatisfiedBy(context, exercise(MovementPattern.PUSH))).isFalse();
        assertThat(rule.isSatisfiedBy(context, exercise(MovementPattern.LEGS))).isFalse();
    }

    private Exercise exercise(MovementPattern movementPattern) {
        return Exercise.builder()
                .id(1L)
                .name("Exercise")
                .primaryMuscleGroup(MuscleGroup.BACK)
                .movementPattern(movementPattern)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .exerciseType(ExerciseType.COMPOUND)
                .build();
    }
}
