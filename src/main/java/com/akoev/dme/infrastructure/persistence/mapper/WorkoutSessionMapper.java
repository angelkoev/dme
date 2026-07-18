package com.akoev.dme.infrastructure.persistence.mapper;

import com.akoev.dme.domain.model.WorkoutSession;
import com.akoev.dme.infrastructure.persistence.entity.WorkoutSessionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkoutSessionMapper {

    private final SessionExerciseMapper sessionExerciseMapper;

    public WorkoutSession toDomain(WorkoutSessionEntity entity) {
        return WorkoutSession.builder()
                .id(entity.getId())
                .sessionIndex(entity.getSessionIndex())
                .name(entity.getName())
                .exercises(entity.getSessionExercises().stream().map(sessionExerciseMapper::toDomain).toList())
                .build();
    }
}
