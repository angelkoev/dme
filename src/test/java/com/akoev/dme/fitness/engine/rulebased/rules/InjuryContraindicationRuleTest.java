package com.akoev.dme.fitness.engine.rulebased.rules;

import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.UserLimitation;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InjuryContraindicationRuleTest {

    private final InjuryContraindicationRule rule = new InjuryContraindicationRule();

    @Test
    void excludesExerciseTargetingInjuredMuscleGroup() {
        UserProfile profile = UserProfile.builder()
                .limitations(List.of(UserLimitation.builder()
                        .muscleGroup(MuscleGroup.SHOULDERS)
                        .note("Rotator cuff strain")
                        .build()))
                .build();
        FitnessDecisionContext context = FitnessDecisionContext.builder().profile(profile).build();

        assertThat(rule.isSatisfiedBy(context, exercise(MuscleGroup.SHOULDERS))).isFalse();
        assertThat(rule.isSatisfiedBy(context, exercise(MuscleGroup.QUADRICEPS))).isTrue();
    }

    @Test
    void allowsEverythingWhenNoLimitations() {
        UserProfile profile = UserProfile.builder().limitations(List.of()).build();
        FitnessDecisionContext context = FitnessDecisionContext.builder().profile(profile).build();

        assertThat(rule.isSatisfiedBy(context, exercise(MuscleGroup.SHOULDERS))).isTrue();
    }

    private Exercise exercise(MuscleGroup muscleGroup) {
        return Exercise.builder()
                .id(1L)
                .name("Exercise")
                .primaryMuscleGroup(muscleGroup)
                .movementPattern(MovementPattern.PUSH)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .exerciseType(ExerciseType.COMPOUND)
                .build();
    }
}
