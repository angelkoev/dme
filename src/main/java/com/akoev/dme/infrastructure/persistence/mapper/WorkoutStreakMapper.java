package com.akoev.dme.infrastructure.persistence.mapper;

import com.akoev.dme.domain.model.WorkoutStreak;
import com.akoev.dme.infrastructure.persistence.entity.WorkoutStreakEntity;
import org.springframework.stereotype.Component;

@Component
public class WorkoutStreakMapper {

    public WorkoutStreak toDomain(WorkoutStreakEntity entity) {
        return WorkoutStreak.builder()
                .userId(entity.getId())
                .currentStreak(entity.getCurrentStreak())
                .longestStreak(entity.getLongestStreak())
                .lastWorkoutDate(entity.getLastWorkoutDate())
                .build();
    }
}
