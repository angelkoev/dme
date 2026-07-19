package com.akoev.dme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutSession {

    // Rough estimate only (fixed assumptions, not measured) — good enough to
    // show the user "does this roughly match my available time," not a
    // scheduling-grade figure. Mirrored (not shared) in
    // RuleBasedWorkoutPlanGenerator, which uses the same assumptions to decide
    // how many exercise slots fit a session — see the comment there for why
    // duplicating two constants was preferred over coupling the generator to
    // this display-only method.
    private static final int ASSUMED_WORK_SECONDS_PER_SET = 40;
    private static final int WARMUP_COOLDOWN_SECONDS = 600;

    private Long id;
    private int sessionIndex;
    private String name;
    private DayOfWeek dayOfWeek;
    @Builder.Default
    private List<SessionExercise> exercises = new ArrayList<>();

    public int getEstimatedDurationMinutes() {
        int workSeconds = exercises.stream()
                .mapToInt(exercise -> exercise.getSets() * (exercise.getRestSeconds() + ASSUMED_WORK_SECONDS_PER_SET))
                .sum();
        return Math.round((workSeconds + WARMUP_COOLDOWN_SECONDS) / 60f);
    }
}
