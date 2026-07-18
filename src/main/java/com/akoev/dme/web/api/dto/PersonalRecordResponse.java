package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.MetricType;
import com.akoev.dme.domain.model.PersonalRecord;

import java.math.BigDecimal;
import java.time.Instant;

public record PersonalRecordResponse(Long exerciseId, MetricType metricType, BigDecimal value, Instant achievedAt) {

    public static PersonalRecordResponse from(PersonalRecord record) {
        return new PersonalRecordResponse(record.getExerciseId(), record.getMetricType(), record.getValue(), record.getAchievedAt());
    }
}
