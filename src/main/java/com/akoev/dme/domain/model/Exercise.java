package com.akoev.dme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Exercise {
    private Long id;
    private String name;
    private String description;
    private MuscleGroup primaryMuscleGroup;
    private MovementPattern movementPattern;
    private DifficultyLevel difficultyLevel;
    private ExerciseType exerciseType;
    private String instructions;
    private String videoUrl;
    @Builder.Default
    private Set<Equipment> requiredEquipment = new HashSet<>();
}
