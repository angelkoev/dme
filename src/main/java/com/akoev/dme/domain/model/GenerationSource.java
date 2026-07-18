package com.akoev.dme.domain.model;

/**
 * Marks how a {@link WorkoutPlan} was produced. The rule-based engine is the only
 * source today; an AI-backed generator can add {@code AI} later without touching
 * this enum's existing consumers.
 */
public enum GenerationSource {
    RULE_BASED,
    AI
}
