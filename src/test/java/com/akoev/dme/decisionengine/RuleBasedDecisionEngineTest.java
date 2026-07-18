package com.akoev.dme.decisionengine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the generic engine with a made-up context/candidate pair
 * (a minimum "budget" and candidate "prices") that has nothing to do with
 * fitness, proving the core carries no domain knowledge.
 */
class RuleBasedDecisionEngineTest {

    private record Budget(int max) {
    }

    private record Item(String name, int price) {
    }

    @Test
    void excludesCandidatesFailingAnyRule() {
        Rule<Budget, Item> withinBudget = new Rule<>() {
            @Override
            public boolean isSatisfiedBy(Budget context, Item candidate) {
                return candidate.price() <= context.max();
            }

            @Override
            public String description() {
                return "price must not exceed budget";
            }
        };

        ScoringStrategy<Budget, Item> cheaperIsBetter = (context, candidate) -> -candidate.price();

        RuleBasedDecisionEngine<Budget, Item> engine = new RuleBasedDecisionEngine<>(List.of(withinBudget), cheaperIsBetter);

        List<ScoredCandidate<Item>> ranked = engine.rank(new Budget(50),
                List.of(new Item("cheap", 10), new Item("mid", 40), new Item("expensive", 100)));

        assertThat(ranked).extracting(ScoredCandidate::candidate)
                .extracting(Item::name)
                .containsExactly("cheap", "mid");
    }

    @Test
    void ordersAdmissibleCandidatesByScoreDescending() {
        ScoringStrategy<Void, Integer> identityScore = (context, candidate) -> candidate;
        RuleBasedDecisionEngine<Void, Integer> engine = new RuleBasedDecisionEngine<>(List.of(), identityScore);

        List<ScoredCandidate<Integer>> ranked = engine.rank(null, List.of(3, 1, 4, 1, 5));

        assertThat(ranked).extracting(ScoredCandidate::candidate).containsExactly(5, 4, 3, 1, 1);
    }

    @Test
    void returnsEmptyListWhenNoCandidateIsAdmissible() {
        Rule<Void, Integer> alwaysFails = new Rule<>() {
            @Override
            public boolean isSatisfiedBy(Void context, Integer candidate) {
                return false;
            }

            @Override
            public String description() {
                return "never satisfied";
            }
        };

        RuleBasedDecisionEngine<Void, Integer> engine = new RuleBasedDecisionEngine<>(List.of(alwaysFails), (c, x) -> x);

        assertThat(engine.rank(null, List.of(1, 2, 3))).isEmpty();
    }
}
