package com.akoev.dme.infrastructure.persistence.mapper;

import com.akoev.dme.domain.model.WorkoutPlan;
import com.akoev.dme.infrastructure.persistence.entity.WorkoutPlanEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkoutPlanMapper {

    private final WorkoutSessionMapper workoutSessionMapper;

    public WorkoutPlan toDomain(WorkoutPlanEntity entity) {
        return WorkoutPlan.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .goal(entity.getGoal())
                .generatedAt(entity.getGeneratedAt())
                .active(entity.isActive())
                .generationSource(entity.getGenerationSource())
                .sessions(entity.getSessions().stream().map(workoutSessionMapper::toDomain).toList())
                .build();
    }
}
