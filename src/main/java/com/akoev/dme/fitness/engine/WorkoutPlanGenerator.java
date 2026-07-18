package com.akoev.dme.fitness.engine;

import com.akoev.dme.domain.model.WorkoutPlan;

/**
 * The swap point for AI. {@link com.akoev.dme.fitness.engine.rulebased.RuleBasedWorkoutPlanGenerator}
 * is the only implementation today; a future {@code AiWorkoutPlanGenerator}
 * would implement this same port and be wired in via a Spring profile or
 * {@code @Primary}, with zero change to callers such as
 * {@code WorkoutPlanService}.
 */
public interface WorkoutPlanGenerator {

    WorkoutPlan generate(GenerationRequest request);
}
