package com.akoev.dme.meals;

import com.akoev.dme.decisionengine.ScoringStrategy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class MealScorer implements ScoringStrategy<MealContext, Meal> {

    private static final double BASE_SCORE = 50;
    private static final double GOAL_FIT_WEIGHT = 20;
    private static final double VARIETY_JITTER_MAX = 5;

    private static final int LOW_CALORIE_THRESHOLD = 400;
    private static final int HIGH_PROTEIN_THRESHOLD = 25;

    @Override
    public double score(MealContext context, Meal candidate) {
        double score = BASE_SCORE;
        score += goalFitScore(context, candidate);
        score += ThreadLocalRandom.current().nextDouble(0, VARIETY_JITTER_MAX);
        return score;
    }

    private double goalFitScore(MealContext context, Meal candidate) {
        return switch (context.dietGoal()) {
            case WEIGHT_LOSS -> candidate.calories() <= LOW_CALORIE_THRESHOLD ? GOAL_FIT_WEIGHT : 0;
            case MUSCLE_GAIN -> candidate.proteinGrams() >= HIGH_PROTEIN_THRESHOLD ? GOAL_FIT_WEIGHT : 0;
            case MAINTENANCE -> GOAL_FIT_WEIGHT / 2;
        };
    }
}
