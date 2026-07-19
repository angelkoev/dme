package com.akoev.dme.finance;

/**
 * A candidate in the finance domain's instantiation of the generic
 * {@link com.akoev.dme.decisionengine.DecisionEngine} (the {@code T}).
 * Seeded in-memory ({@link InstrumentCatalog}) rather than persisted —
 * this domain is intentionally a thin proof that the engine generalizes,
 * not a full second application like fitness.
 */
public record Instrument(Long id, String name, AssetClass assetClass, RiskLevel riskLevel,
                          Sector sector, double expectedReturnPercent) {
}
