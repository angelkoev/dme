package com.akoev.dme.finance;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.decisionengine.RuleBasedDecisionEngine;
import com.akoev.dme.decisionengine.ScoredCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Thin wrapper around the generic {@link RuleBasedDecisionEngine} — the
 * whole point of this domain existing is to prove that engine plugs in with
 * nothing more than its own context/candidate types and rules/scoring, the
 * same way {@code fitness.engine.rulebased.RuleBasedWorkoutPlanGenerator} does.
 */
@Service
@RequiredArgsConstructor
public class FinanceAdvisorService {

    private static final int DEFAULT_RECOMMENDATION_COUNT = 5;

    private final InstrumentCatalog catalog;
    private final List<Rule<FinanceContext, Instrument>> rules;
    private final FinanceScorer scorer;

    public List<Instrument> listInstruments() {
        return catalog.findAll();
    }

    public List<Instrument> recommend(FinanceContext context) {
        RuleBasedDecisionEngine<FinanceContext, Instrument> engine = new RuleBasedDecisionEngine<>(rules, scorer);
        return engine.rank(context, catalog.findAll()).stream()
                .limit(DEFAULT_RECOMMENDATION_COUNT)
                .map(ScoredCandidate::candidate)
                .toList();
    }
}
