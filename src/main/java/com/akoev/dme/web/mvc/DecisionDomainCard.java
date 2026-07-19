package com.akoev.dme.web.mvc;

/**
 * A placeholder entry for a decision domain not built yet (see the "Coming
 * soon" grid on the home page). The one domain that IS built —
 * fitness/workout planning — is intentionally not modeled as one of these;
 * its home-page card renders its actual nav links directly (home.html),
 * since unlike a placeholder it has real routes to link to.
 * <p>
 * {@code decisionengine} (Rule/ScoringStrategy/RuleBasedDecisionEngine) has
 * zero fitness-specific code, so any of these could plug into it the same
 * way {@code fitness.engine} does, with its own context/candidate types and
 * rules/scoring — see the "fitness" package for the shape a new one would
 * follow.
 */
public record DecisionDomainCard(String name, String description) {
}
