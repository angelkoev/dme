package com.akoev.dme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalRecord {
    private Long id;
    private Long userId;
    private Long exerciseId;
    private MetricType metricType;
    private BigDecimal value;
    private Instant achievedAt;
}
