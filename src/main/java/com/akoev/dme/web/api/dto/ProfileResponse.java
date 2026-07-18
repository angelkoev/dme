package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.ExperienceLevel;
import com.akoev.dme.domain.model.Location;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.Sex;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.UserProfile;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ProfileResponse(
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
        Set<EquipmentResponse> availableEquipment,
        Set<ExerciseResponse> favoriteExercises,
        Set<ExerciseResponse> dislikedExercises,
        Set<MuscleGroup> preferredCategories,
        Set<MuscleGroup> unwantedCategories,
        List<LimitationResponse> limitations,
        Set<DayOfWeek> restDays
) {

    public static ProfileResponse from(UserProfile profile) {
        return new ProfileResponse(
                profile.getBirthDate(),
                profile.getSex(),
                profile.getHeightCm(),
                profile.getWeightKg(),
                profile.getExperienceLevel(),
                profile.getPrimaryGoal(),
                profile.getDaysPerWeek(),
                profile.getSessionDurationMinutes(),
                profile.getNotes(),
                profile.getLocation(),
                profile.getAvailableEquipment().stream().map(EquipmentResponse::from).collect(Collectors.toSet()),
                profile.getFavoriteExercises().stream().map(ExerciseResponse::from).collect(Collectors.toSet()),
                profile.getDislikedExercises().stream().map(ExerciseResponse::from).collect(Collectors.toSet()),
                profile.getPreferredCategories(),
                profile.getUnwantedCategories(),
                profile.getLimitations().stream().map(LimitationResponse::from).toList(),
                profile.getRestDays()
        );
    }
}
