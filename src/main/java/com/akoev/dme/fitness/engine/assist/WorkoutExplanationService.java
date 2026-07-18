package com.akoev.dme.fitness.engine.assist;

import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.domain.model.WorkoutPlan;

/**
 * Produces a human-readable "why this workout" explanation. A future
 * AI-backed implementation could generate a richer, personalized
 * explanation from the same inputs without touching the generator.
 */
public interface WorkoutExplanationService {

    String explain(WorkoutPlan plan, UserProfile profile);
}
