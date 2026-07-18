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
        @Min(1) @Max(10) Integer perceivedIntensity,
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
