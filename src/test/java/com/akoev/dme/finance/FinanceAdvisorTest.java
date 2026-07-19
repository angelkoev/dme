package com.akoev.dme.finance;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Deliberately thin, matching the domain itself: one test per rule/scoring
 * behavior, no Spring context — same no-context-needed style as fitness's
 * rule-based tests.
 */
class FinanceAdvisorTest {

    private final FinanceScorer scorer = new FinanceScorer();

    @Test
    void aggressiveToleranceFavorsHigherReturnHighRiskInstrument() {
        FinanceContext context = new FinanceContext(RiskTolerance.AGGRESSIVE, Set.of());
        Instrument highRiskHighReturn = new Instrument(1L, "A", AssetClass.STOCK, RiskLevel.HIGH, Sector.TECHNOLOGY, 16.0);
        Instrument lowRiskLowReturn = new Instrument(2L, "B", AssetClass.BOND, RiskLevel.LOW, Sector.DIVERSIFIED, 3.0);

        assertThat(scorer.score(context, highRiskHighReturn)).isGreaterThan(scorer.score(context, lowRiskLowReturn));
    }

    @Test
    void conservativeToleranceFavorsLowerRiskInstrument() {
        FinanceContext context = new FinanceContext(RiskTolerance.CONSERVATIVE, Set.of());
        Instrument highRisk = new Instrument(1L, "A", AssetClass.STOCK, RiskLevel.HIGH, Sector.TECHNOLOGY, 16.0);
        Instrument lowRisk = new Instrument(2L, "B", AssetClass.BOND, RiskLevel.LOW, Sector.DIVERSIFIED, 3.0);

        assertThat(scorer.score(context, lowRisk)).isGreaterThan(scorer.score(context, highRisk));
    }

    @Test
    void riskToleranceRuleExcludesTooRiskyInstrument() {
        RiskToleranceRule rule = new RiskToleranceRule();
        FinanceContext conservative = new FinanceContext(RiskTolerance.CONSERVATIVE, Set.of());
        Instrument highRisk = new Instrument(1L, "A", AssetClass.STOCK, RiskLevel.HIGH, Sector.TECHNOLOGY, 16.0);

        assertThat(rule.isSatisfiedBy(conservative, highRisk)).isFalse();
    }

    @Test
    void excludedSectorRuleExcludesThatSector() {
        ExcludedSectorRule rule = new ExcludedSectorRule();
        FinanceContext context = new FinanceContext(RiskTolerance.AGGRESSIVE, Set.of(Sector.TECHNOLOGY));
        Instrument techStock = new Instrument(1L, "A", AssetClass.STOCK, RiskLevel.HIGH, Sector.TECHNOLOGY, 16.0);

        assertThat(rule.isSatisfiedBy(context, techStock)).isFalse();
    }

    @Test
    void recommendReturnsOnlyInstrumentsMatchingConstraints() {
        InstrumentCatalog catalog = new InstrumentCatalog();
        FinanceAdvisorService service = new FinanceAdvisorService(
                catalog, List.of(new RiskToleranceRule(), new ExcludedSectorRule()), new FinanceScorer());

        FinanceContext conservative = new FinanceContext(RiskTolerance.CONSERVATIVE, Set.of());
        List<Instrument> recommendations = service.recommend(conservative);

        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations).allMatch(instrument -> instrument.riskLevel() == RiskLevel.LOW);
    }
}
