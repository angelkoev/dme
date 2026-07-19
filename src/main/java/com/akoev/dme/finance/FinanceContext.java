package com.akoev.dme.finance;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * The finance domain's {@code C} (context) type — supplied directly on each
 * request rather than loaded from a persisted profile, unlike fitness's
 * {@code UserProfile}. Nothing here is saved.
 * <p>
 * {@code riskTolerance} is {@code @NotNull} (enforced via {@code @Valid} on
 * {@code FinanceController.recommend}) specifically because
 * {@code RiskToleranceRule}/{@code FinanceScorer} both {@code switch} on it —
 * a plain {@code switch} throws on a null enum, which without this
 * validation surfaced as an opaque 500 instead of a 400 for a missing field.
 */
public record FinanceContext(@NotNull RiskTolerance riskTolerance, Set<Sector> excludedSectors) {

    public FinanceContext {
        excludedSectors = excludedSectors == null ? Set.of() : excludedSectors;
    }
}
