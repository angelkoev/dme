package com.akoev.dme.learning;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.decisionengine.RuleBasedDecisionEngine;
import com.akoev.dme.decisionengine.ScoredCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningPathService {

    private static final int DEFAULT_RECOMMENDATION_COUNT = 3;

    private final CourseCatalog catalog;
    private final List<Rule<LearningContext, Course>> rules;
    private final LearningScorer scorer;

    public List<Course> listCourses() {
        return catalog.findAll();
    }

    public List<Course> recommend(LearningContext context) {
        RuleBasedDecisionEngine<LearningContext, Course> engine = new RuleBasedDecisionEngine<>(rules, scorer);
        return engine.rank(context, catalog.findAll()).stream()
                .limit(DEFAULT_RECOMMENDATION_COUNT)
                .map(ScoredCandidate::candidate)
                .toList();
    }
}
