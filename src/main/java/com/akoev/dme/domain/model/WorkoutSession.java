package com.akoev.dme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutSession {
    private Long id;
    private int sessionIndex;
    private String name;
    @Builder.Default
    private List<SessionExercise> exercises = new ArrayList<>();
}
