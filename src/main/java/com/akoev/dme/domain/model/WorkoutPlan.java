package com.akoev.dme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlan {
    private Long id;
    private Long userId;
    private TrainingGoal goal;
    private Instant generatedAt;
    private boolean active;
    private GenerationSource generationSource;
    @Builder.Default
    private List<WorkoutSession> sessions = new ArrayList<>();
}
