package com.akoev.dme.infrastructure.persistence.mapper;

import com.akoev.dme.domain.model.PersonalRecord;
import com.akoev.dme.infrastructure.persistence.entity.PersonalRecordEntity;
import org.springframework.stereotype.Component;

@Component
public class PersonalRecordMapper {

    public PersonalRecord toDomain(PersonalRecordEntity entity) {
        return PersonalRecord.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .exerciseId(entity.getExercise().getId())
                .metricType(entity.getMetricType())
                .value(entity.getValue())
                .achievedAt(entity.getAchievedAt())
                .build();
    }
}
