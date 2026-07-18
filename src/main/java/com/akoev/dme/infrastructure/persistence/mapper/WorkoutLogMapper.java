package com.akoev.dme.infrastructure.persistence.mapper;

import com.akoev.dme.domain.model.WorkoutLog;
import com.akoev.dme.infrastructure.persistence.entity.WorkoutLogEntity;
import org.springframework.stereotype.Component;

@Component
public class WorkoutLogMapper {

    public WorkoutLog toDomain(WorkoutLogEntity entity) {
        return WorkoutLog.builder()
                .id(entity.getId())
                .workoutSessionId(entity.getWorkoutSession().getId())
                .userId(entity.getUser().getId())
                .performedAt(entity.getPerformedAt())
                .completionPercentage(entity.getCompletionPercentage())
                .rating(entity.getRating())
                .perceivedIntensity(entity.getPerceivedIntensity())
                .notes(entity.getNotes())
                .build();
    }
}
