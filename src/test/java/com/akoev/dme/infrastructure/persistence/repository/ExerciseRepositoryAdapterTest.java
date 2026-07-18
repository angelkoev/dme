package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.AbstractIntegrationTest;
import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.repository.EquipmentRepository;
import com.akoev.dme.domain.repository.ExerciseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ExerciseRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Test
    void savesAndReloadsExerciseWithRequiredEquipment() {
        List<Equipment> equipment = equipmentRepository.findAll();
        Equipment barbell = equipment.stream().filter(e -> e.getName().equals("Barbell")).findFirst().orElseThrow();
        Equipment bench = equipment.stream().filter(e -> e.getName().equals("Bench")).findFirst().orElseThrow();

        Exercise newExercise = Exercise.builder()
                .name("Barbell Bench Press")
                .description("Flat barbell press for chest")
                .primaryMuscleGroup(MuscleGroup.CHEST)
                .movementPattern(MovementPattern.PUSH)
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .exerciseType(ExerciseType.COMPOUND)
                .instructions("Lie on the bench, lower the bar to the chest, press up.")
                .requiredEquipment(Set.of(barbell, bench))
                .build();

        Exercise saved = exerciseRepository.save(newExercise);
        assertThat(saved.getId()).isNotNull();

        Exercise reloaded = exerciseRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getName()).isEqualTo("Barbell Bench Press");
        assertThat(reloaded.getPrimaryMuscleGroup()).isEqualTo(MuscleGroup.CHEST);
        assertThat(reloaded.getMovementPattern()).isEqualTo(MovementPattern.PUSH);
        assertThat(reloaded.getRequiredEquipment())
                .extracting(Equipment::getName)
                .containsExactlyInAnyOrder("Barbell", "Bench");
    }
}
