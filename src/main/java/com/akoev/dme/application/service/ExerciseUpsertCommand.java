package com.akoev.dme.application.service;

import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;

import java.util.Set;

public record ExerciseUpsertCommand(
        String name,
        String description,
        MuscleGroup primaryMuscleGroup,
        MovementPattern movementPattern,
        DifficultyLevel difficultyLevel,
        ExerciseType exerciseType,
        String instructions,
        String videoUrl,
        Set<Long> equipmentIds
) {
}
