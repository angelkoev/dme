package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.WorkoutStreak;

import java.time.LocalDate;

public record WorkoutStreakResponse(int currentStreak, int longestStreak, LocalDate lastWorkoutDate) {

    public static WorkoutStreakResponse from(WorkoutStreak streak) {
        return new WorkoutStreakResponse(streak.getCurrentStreak(), streak.getLongestStreak(), streak.getLastWorkoutDate());
    }

    public static WorkoutStreakResponse empty() {
        return new WorkoutStreakResponse(0, 0, null);
    }
}
