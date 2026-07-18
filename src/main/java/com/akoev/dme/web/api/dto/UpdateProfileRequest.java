package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.ExperienceLevel;
import com.akoev.dme.domain.model.Location;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.Sex;
import com.akoev.dme.domain.model.TrainingGoal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record UpdateProfileRequest(
        LocalDate birthDate,
        Sex sex,
        Integer heightCm,
        BigDecimal weightKg,
        @NotNull ExperienceLevel experienceLevel,
        @NotNull TrainingGoal primaryGoal,
        @Min(1) @Max(7) int daysPerWeek,
        @Min(10) int sessionDurationMinutes,
        String notes,
        @NotNull Location location,
        Set<Long> equipmentIds,
        Set<Long> favoriteExerciseIds,
        Set<Long> dislikedExerciseIds,
        Set<MuscleGroup> preferredCategories,
        Set<MuscleGroup> unwantedCategories,
        List<LimitationRequest> limitations,
        Set<DayOfWeek> restDays
) {

    public record LimitationRequest(MuscleGroup muscleGroup, @NotBlank String note) {
    }
}
