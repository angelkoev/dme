package com.akoev.dme.web.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CompleteSessionRequest(
        @NotNull @Min(0) @Max(100) Integer completionPercentage,
        @Min(1) @Max(5) Integer rating,
        // 1-5, matching `rating` above and dashboard.html's "1=too easy,
        // 5=too hard" label: RuleBasedWorkoutPlanGenerator.adjustForRecentFeedback
        // treats >=4.5 as "too hard" and <=2.0 as "too easy" on that scale — a
        // client-legal 1-10 value used to silently defeat that calibration.
        @Min(1) @Max(5) Integer perceivedIntensity,
        String notes,
        @Valid List<ExercisePerformanceRequest> exercisePerformances
) {

    public record ExercisePerformanceRequest(
            @NotNull Long exerciseId,
            @DecimalMin(value = "0.0", inclusive = true) BigDecimal weightKg,
            @Min(0) Integer reps
    ) {
    }
}
