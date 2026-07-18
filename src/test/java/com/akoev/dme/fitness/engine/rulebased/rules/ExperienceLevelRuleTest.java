package com.akoev.dme.fitness.engine.rulebased.rules;

import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.ExperienceLevel;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExperienceLevelRuleTest {

    private final ExperienceLevelRule rule = new ExperienceLevelRule();

    @Test
    void beginnerUserCannotGetAdvancedExercise() {
        UserProfile profile = UserProfile.builder().experienceLevel(ExperienceLevel.BEGINNER).build();
        FitnessDecisionContext context = FitnessDecisionContext.builder().profile(profile).build();

        assertThat(rule.isSatisfiedBy(context, exercise(DifficultyLevel.ADVANCED))).isFalse();
        assertThat(rule.isSatisfiedBy(context, exercise(DifficultyLevel.BEGINNER))).isTrue();
    }

    @Test
    void advancedUserCanGetAnyDifficulty() {
        UserProfile profile = UserProfile.builder().experienceLevel(ExperienceLevel.ADVANCED).build();
        FitnessDecisionContext context = FitnessDecisionContext.builder().profile(profile).build();

        assertThat(rule.isSatisfiedBy(context, exercise(DifficultyLevel.BEGINNER))).isTrue();
        assertThat(rule.isSatisfiedBy(context, exercise(DifficultyLevel.INTERMEDIATE))).isTrue();
        assertThat(rule.isSatisfiedBy(context, exercise(DifficultyLevel.ADVANCED))).isTrue();
    }

    private Exercise exercise(DifficultyLevel difficultyLevel) {
        return Exercise.builder()
                .id(1L)
                .name("Exercise")
                .primaryMuscleGroup(MuscleGroup.CHEST)
                .movementPattern(MovementPattern.PUSH)
                .difficultyLevel(difficultyLevel)
                .exerciseType(ExerciseType.COMPOUND)
                .build();
    }
}
