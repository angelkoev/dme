package com.akoev.dme.fitness.engine;

import com.akoev.dme.domain.model.TrainingGoal;

public record GenerationRequest(Long userId, TrainingGoal goalOverride) {

    public GenerationRequest(Long userId) {
        this(userId, null);
    }
}
