package com.akoev.dme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutStreak {
    private Long userId;
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastWorkoutDate;
}
