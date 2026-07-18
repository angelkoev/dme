package com.akoev.dme.fitness.engine.assist;

import com.akoev.dme.decisionengine.ScoredCandidate;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Default, non-AI tie-break: deterministically takes the top-ranked
 * candidate (candidates are already sorted best-first by the engine).
 */
@Component
public class NoOpAmbiguityResolver implements AmbiguityResolver {

    @Override
    public Exercise resolveTie(FitnessDecisionContext context, List<ScoredCandidate<Exercise>> tiedCandidates) {
        return tiedCandidates.get(0).candidate();
    }
}
