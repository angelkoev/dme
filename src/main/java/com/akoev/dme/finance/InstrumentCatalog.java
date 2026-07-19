package com.akoev.dme.finance;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * In-memory seed catalog — no migration, no entity, no repository. Fitness's
 * catalog is DB-backed because it's admin-editable and referenced by
 * historical logs; this domain has neither concern, so a fixed in-memory
 * list is the honest, proportionate choice for a thin demo module.
 */
@Component
public class InstrumentCatalog {

    private static final List<Instrument> INSTRUMENTS = List.of(
            new Instrument(1L, "Global Total Market ETF", AssetClass.ETF, RiskLevel.LOW, Sector.DIVERSIFIED, 6.0),
            new Instrument(2L, "Government Bond Fund", AssetClass.BOND, RiskLevel.LOW, Sector.DIVERSIFIED, 3.0),
            new Instrument(3L, "High-Yield Savings / Cash", AssetClass.CASH, RiskLevel.LOW, Sector.DIVERSIFIED, 2.0),
            new Instrument(4L, "Healthcare Sector ETF", AssetClass.ETF, RiskLevel.MEDIUM, Sector.HEALTHCARE, 8.0),
            new Instrument(5L, "Consumer Staples ETF", AssetClass.ETF, RiskLevel.MEDIUM, Sector.CONSUMER, 7.0),
            new Instrument(6L, "Blue-Chip Tech Stock", AssetClass.STOCK, RiskLevel.MEDIUM, Sector.TECHNOLOGY, 10.0),
            new Instrument(7L, "Regional Bank Stock", AssetClass.STOCK, RiskLevel.MEDIUM, Sector.FINANCIAL, 9.0),
            new Instrument(8L, "Renewable Energy Stock", AssetClass.STOCK, RiskLevel.HIGH, Sector.ENERGY, 14.0),
            new Instrument(9L, "Growth Tech Stock", AssetClass.STOCK, RiskLevel.HIGH, Sector.TECHNOLOGY, 16.0),
            new Instrument(10L, "Emerging Market Fund", AssetClass.ETF, RiskLevel.HIGH, Sector.DIVERSIFIED, 13.0),
            new Instrument(11L, "Bitcoin", AssetClass.CRYPTO, RiskLevel.HIGH, Sector.DIVERSIFIED, 20.0),
            new Instrument(12L, "Ethereum", AssetClass.CRYPTO, RiskLevel.HIGH, Sector.DIVERSIFIED, 18.0)
    );

    public List<Instrument> findAll() {
        return INSTRUMENTS;
    }
}
