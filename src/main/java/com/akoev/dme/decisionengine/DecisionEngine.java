package com.akoev.dme.decisionengine;

import java.util.List;

/**
 * Ranks a pool of candidates for a given context: filters out inadmissible
 * ones via {@link Rule}s, scores the rest via a {@link ScoringStrategy}, and
 * returns them best-first. This is the whole "Decision Engine" contract —
 * it has no notion of workouts, exercises, or any other fitness concept, so
 * any domain (nutrition, rehab, ...) can reuse it by supplying its own
 * context/candidate types and rules/scoring.
 */
public interface DecisionEngine<C, T> {

    List<ScoredCandidate<T>> rank(C context, List<T> candidates);
}
