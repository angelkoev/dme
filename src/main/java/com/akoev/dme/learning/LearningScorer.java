package com.akoev.dme.learning;

import com.akoev.dme.decisionengine.ScoringStrategy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class LearningScorer implements ScoringStrategy<LearningContext, Course> {

    private static final double BASE_SCORE = 50;
    private static final double EXACT_LEVEL_MATCH_BONUS = 20;
    private static final double VARIETY_JITTER_MAX = 5;

    @Override
    public double score(LearningContext context, Course candidate) {
        double score = BASE_SCORE;
        if (candidate.level() == context.currentLevel()) {
            score += EXACT_LEVEL_MATCH_BONUS;
        }
        score += ThreadLocalRandom.current().nextDouble(0, VARIETY_JITTER_MAX);
        return score;
    }
}
