package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record ExerciseRequest(
        @NotBlank String name,
        String description,
        @NotNull MuscleGroup primaryMuscleGroup,
        @NotNull MovementPattern movementPattern,
        @NotNull DifficultyLevel difficultyLevel,
        @NotNull ExerciseType exerciseType,
        String instructions,
        String videoUrl,
        Set<Long> equipmentIds
) {
}
