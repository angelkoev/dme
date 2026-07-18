package com.akoev.dme.fitness.engine;

import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.UserProfile;
import lombok.Builder;
import lombok.Getter;

/**
 * The {@code C} (context) type the fitness domain plugs into the generic
 * {@link com.akoev.dme.decisionengine.DecisionEngine}. Bundles everything a
 * {@link com.akoev.dme.decisionengine.Rule} or
 * {@link com.akoev.dme.decisionengine.ScoringStrategy} needs to judge one
 * exercise slot.
 */
@Getter
@Builder
public class FitnessDecisionContext {

    private final UserProfile profile;
    private final RecentActivitySummary recentActivity;
    private final MovementPattern targetMovementPattern;
}
