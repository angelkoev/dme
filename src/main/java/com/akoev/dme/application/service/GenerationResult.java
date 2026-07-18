package com.akoev.dme.application.service;

import com.akoev.dme.domain.model.WorkoutPlan;

public record GenerationResult(WorkoutPlan plan, String explanation, String motivationalMessage) {
}
