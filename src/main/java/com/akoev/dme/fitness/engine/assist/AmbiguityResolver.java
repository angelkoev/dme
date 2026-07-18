package com.akoev.dme.fitness.engine.assist;

import com.akoev.dme.decisionengine.ScoredCandidate;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;

import java.util.List;

/**
 * Invoked only when the top-ranked candidates for a slot are within a
 * negligible score of each other — a genuine tie the rule-based scorer
 * cannot break confidently. This is one of the three places (see also
 * {@link WorkoutExplanationService}, {@link MotivationalMessageService})
 * where an AI implementation could later add judgement without replacing
 * the engine.
 */
public interface AmbiguityResolver {

    Exercise resolveTie(FitnessDecisionContext context, List<ScoredCandidate<Exercise>> tiedCandidates);
}
