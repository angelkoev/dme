package com.akoev.dme.infrastructure.persistence.mapper;

import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.infrastructure.persistence.entity.ExerciseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExerciseMapper {

    private final EquipmentMapper equipmentMapper;

    public Exercise toDomain(ExerciseEntity entity) {
        if (entity == null) {
            return null;
        }
        return Exercise.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .primaryMuscleGroup(entity.getPrimaryMuscleGroup())
                .movementPattern(entity.getMovementPattern())
                .difficultyLevel(entity.getDifficultyLevel())
                .exerciseType(entity.getExerciseType())
                .instructions(entity.getInstructions())
                .videoUrl(entity.getVideoUrl())
                .requiredEquipment(equipmentMapper.toDomainSet(entity.getRequiredEquipment()))
                .build();
    }
}
