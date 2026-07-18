package com.akoev.dme.decisionengine;

import java.util.Comparator;
import java.util.List;

/**
 * The only implementation of {@link DecisionEngine} in the rule-based v1.
 * Composes its rules with AND semantics (Chain of Responsibility /
 * Specification style): a candidate must satisfy every rule to be scored
 * at all.
 */
public class RuleBasedDecisionEngine<C, T> implements DecisionEngine<C, T> {

    private final List<Rule<C, T>> rules;
    private final ScoringStrategy<C, T> scoringStrategy;

    public RuleBasedDecisionEngine(List<Rule<C, T>> rules, ScoringStrategy<C, T> scoringStrategy) {
        this.rules = List.copyOf(rules);
        this.scoringStrategy = scoringStrategy;
    }

    @Override
    public List<ScoredCandidate<T>> rank(C context, List<T> candidates) {
        return candidates.stream()
                .filter(candidate -> isAdmissible(context, candidate))
                .map(candidate -> new ScoredCandidate<>(candidate, scoringStrategy.score(context, candidate)))
                .sorted(Comparator.comparingDouble(ScoredCandidate<T>::score).reversed())
                .toList();
    }

    private boolean isAdmissible(C context, T candidate) {
        return rules.stream().allMatch(rule -> rule.isSatisfiedBy(context, candidate));
    }
}
