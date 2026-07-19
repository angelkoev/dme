package com.akoev.dme.movies;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.decisionengine.RuleBasedDecisionEngine;
import com.akoev.dme.decisionengine.ScoredCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieRecommendationService {

    private static final int DEFAULT_RECOMMENDATION_COUNT = 5;

    private final TitleCatalog catalog;
    private final List<Rule<MovieContext, Title>> rules;
    private final MovieScorer scorer;

    public List<Title> listTitles() {
        return catalog.findAll();
    }

    public List<Title> recommend(MovieContext context) {
        RuleBasedDecisionEngine<MovieContext, Title> engine = new RuleBasedDecisionEngine<>(rules, scorer);
        return engine.rank(context, catalog.findAll()).stream()
                .limit(DEFAULT_RECOMMENDATION_COUNT)
                .map(ScoredCandidate::candidate)
                .toList();
    }
}
