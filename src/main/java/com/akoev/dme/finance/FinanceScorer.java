package com.akoev.dme.finance;

import com.akoev.dme.decisionengine.ScoringStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Deliberately simple compared to fitness's {@code FitnessExerciseScorer}:
 * two signals instead of six, no adaptive/history-based term (this domain
 * has no persisted history to adapt from). Still the same shape — a base
 * score plus weighted signals plus jitter.
 */
@Component
public class FinanceScorer implements ScoringStrategy<FinanceContext, Instrument> {

    private static final double BASE_SCORE = 50;
    private static final double RISK_MATCH_BONUS = 20;
    private static final double VARIETY_JITTER_MAX = 5;

    private static final Map<RiskTolerance, RiskLevel> IDEAL_RISK_LEVEL = Map.of(
            RiskTolerance.CONSERVATIVE, RiskLevel.LOW,
            RiskTolerance.BALANCED, RiskLevel.MEDIUM,
            RiskTolerance.AGGRESSIVE, RiskLevel.HIGH);

    private static final Map<RiskTolerance, Double> RETURN_WEIGHT = Map.of(
            RiskTolerance.CONSERVATIVE, 0.5,
            RiskTolerance.BALANCED, 1.5,
            RiskTolerance.AGGRESSIVE, 3.0);

    @Override
    public double score(FinanceContext context, Instrument candidate) {
        double score = BASE_SCORE;
        if (IDEAL_RISK_LEVEL.get(context.riskTolerance()) == candidate.riskLevel()) {
            score += RISK_MATCH_BONUS;
        }
        score += candidate.expectedReturnPercent() * RETURN_WEIGHT.get(context.riskTolerance());
        score += ThreadLocalRandom.current().nextDouble(0, VARIETY_JITTER_MAX);
        return score;
    }
}
