package com.akoev.dme.decisionengine;

/**
 * Ranks admissible candidates against each other. Unlike {@link Rule}, this
 * is soft — it never excludes a candidate, only orders them.
 */
public interface ScoringStrategy<C, T> {

    double score(C context, T candidate);
}
