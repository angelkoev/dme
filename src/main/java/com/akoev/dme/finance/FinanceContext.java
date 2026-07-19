package com.akoev.dme.finance;

import java.util.Set;

/**
 * The finance domain's {@code C} (context) type — supplied directly on each
 * request rather than loaded from a persisted profile, unlike fitness's
 * {@code UserProfile}. Nothing here is saved.
 */
public record FinanceContext(RiskTolerance riskTolerance, Set<Sector> excludedSectors) {

    public FinanceContext {
        excludedSectors = excludedSectors == null ? Set.of() : excludedSectors;
    }
}
