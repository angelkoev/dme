package com.akoev.dme.application.service;

import java.math.BigDecimal;
import java.util.List;

public record CompleteSessionCommand(
        int completionPercentage,
        Integer rating,
        Integer perceivedIntensity,
        String notes,
        List<ExercisePerformanceCommand> exercisePerformances
) {

    public record ExercisePerformanceCommand(Long exerciseId, BigDecimal weightKg, Integer reps) {
    }
}
