package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;

import java.util.Set;
import java.util.stream.Collectors;

public record ExerciseResponse(
        Long id,
        String name,
        String description,
        MuscleGroup primaryMuscleGroup,
        MovementPattern movementPattern,
        DifficultyLevel difficultyLevel,
        ExerciseType exerciseType,
        String instructions,
        String videoUrl,
        Set<EquipmentResponse> requiredEquipment
) {

    public static ExerciseResponse from(Exercise exercise) {
        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getPrimaryMuscleGroup(),
                exercise.getMovementPattern(),
                exercise.getDifficultyLevel(),
                exercise.getExerciseType(),
                exercise.getInstructions(),
                exercise.getVideoUrl(),
                exercise.getRequiredEquipment().stream().map(EquipmentResponse::from).collect(Collectors.toSet())
        );
    }
}
