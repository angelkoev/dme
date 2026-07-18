package com.akoev.dme.fitness.engine.rulebased.rules;

import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EquipmentAvailabilityRuleTest {

    private final EquipmentAvailabilityRule rule = new EquipmentAvailabilityRule();

    private static final Equipment BARBELL = Equipment.builder().id(1L).name("Barbell").build();
    private static final Equipment BENCH = Equipment.builder().id(2L).name("Bench").build();
    private static final Equipment DUMBBELL = Equipment.builder().id(3L).name("Dumbbell").build();

    @Test
    void satisfiedWhenUserHasAllRequiredEquipment() {
        UserProfile profile = UserProfile.builder().availableEquipment(Set.of(BARBELL, BENCH)).build();
        Exercise benchPress = exercise(Set.of(BARBELL, BENCH));

        FitnessDecisionContext context = FitnessDecisionContext.builder().profile(profile).build();

        assertThat(rule.isSatisfiedBy(context, benchPress)).isTrue();
    }

    @Test
    void notSatisfiedWhenEquipmentIsMissing() {
        UserProfile profile = UserProfile.builder().availableEquipment(Set.of(DUMBBELL)).build();
        Exercise benchPress = exercise(Set.of(BARBELL, BENCH));

        FitnessDecisionContext context = FitnessDecisionContext.builder().profile(profile).build();

        assertThat(rule.isSatisfiedBy(context, benchPress)).isFalse();
    }

    @Test
    void satisfiedWhenExerciseNeedsNoEquipment() {
        UserProfile profile = UserProfile.builder().availableEquipment(Set.of()).build();
        Exercise pushUp = exercise(Set.of());

        FitnessDecisionContext context = FitnessDecisionContext.builder().profile(profile).build();

        assertThat(rule.isSatisfiedBy(context, pushUp)).isTrue();
    }

    private Exercise exercise(Set<Equipment> requiredEquipment) {
        return Exercise.builder()
                .id(100L)
                .name("Test Exercise")
                .primaryMuscleGroup(MuscleGroup.CHEST)
                .movementPattern(MovementPattern.PUSH)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .exerciseType(ExerciseType.COMPOUND)
                .requiredEquipment(requiredEquipment)
                .build();
    }
}
