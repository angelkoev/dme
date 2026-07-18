package com.akoev.dme.application.service;

import com.akoev.dme.domain.model.ExperienceLevel;
import com.akoev.dme.domain.model.Location;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.Sex;
import com.akoev.dme.domain.model.TrainingGoal;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record ProfileUpdateCommand(
        LocalDate birthDate,
        Sex sex,
        Integer heightCm,
        BigDecimal weightKg,
        ExperienceLevel experienceLevel,
        TrainingGoal primaryGoal,
        int daysPerWeek,
        int sessionDurationMinutes,
        String notes,
        Location location,
        Set<Long> equipmentIds,
        Set<Long> favoriteExerciseIds,
        Set<Long> dislikedExerciseIds,
        Set<MuscleGroup> preferredCategories,
        Set<MuscleGroup> unwantedCategories,
        List<LimitationCommand> limitations,
        Set<DayOfWeek> restDays
) {

    public record LimitationCommand(MuscleGroup muscleGroup, String note) {
    }
}
