package com.akoev.dme.infrastructure.persistence.mapper;

import com.akoev.dme.domain.model.SessionExercise;
import com.akoev.dme.infrastructure.persistence.entity.SessionExerciseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionExerciseMapper {

    private final ExerciseMapper exerciseMapper;

    public SessionExercise toDomain(SessionExerciseEntity entity) {
        return SessionExercise.builder()
                .id(entity.getId())
                .exercise(exerciseMapper.toDomain(entity.getExercise()))
                .orderIndex(entity.getOrderIndex())
                .sets(entity.getSets())
                .repRangeMin(entity.getRepRangeMin())
                .repRangeMax(entity.getRepRangeMax())
                .restSeconds(entity.getRestSeconds())
                .notes(entity.getNotes())
                .build();
    }
}
