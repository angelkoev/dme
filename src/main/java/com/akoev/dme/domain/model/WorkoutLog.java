package com.akoev.dme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutLog {
    private Long id;
    private Long workoutSessionId;
    private Long userId;
    private Instant performedAt;
    private int completionPercentage;
    private Integer rating;
    private Integer perceivedIntensity;
    private String notes;
}
