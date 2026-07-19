package com.akoev.dme.productivity;

import com.akoev.dme.decisionengine.ScoringStrategy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A simple Eisenhower-matrix-style weighted sum (urgency + importance),
 * plus one energy-aware adjustment: a low-energy context favors quick tasks
 * instead of the biggest/hardest ones, since "rank by urgency+importance
 * alone" would otherwise recommend the same heavy task regardless of
 * whether the user actually has the energy for it right now.
 */
@Component
public class TaskScorer implements ScoringStrategy<ProductivityContext, Task> {

    private static final double BASE_SCORE = 50;
    private static final double LOW_ENERGY_QUICK_TASK_BONUS = 15;
    private static final int QUICK_TASK_MINUTES_THRESHOLD = 15;
    private static final double VARIETY_JITTER_MAX = 5;

    @Override
    public double score(ProductivityContext context, Task candidate) {
        double score = BASE_SCORE;
        score += weight(candidate.urgency());
        score += weight(candidate.importance());
        if (context.energyLevel() == EnergyLevel.LOW && candidate.estimatedMinutes() <= QUICK_TASK_MINUTES_THRESHOLD) {
            score += LOW_ENERGY_QUICK_TASK_BONUS;
        }
        score += ThreadLocalRandom.current().nextDouble(0, VARIETY_JITTER_MAX);
        return score;
    }

    private double weight(Urgency urgency) {
        return switch (urgency) {
            case LOW -> 0;
            case MEDIUM -> 10;
            case HIGH -> 20;
        };
    }

    private double weight(Importance importance) {
        return switch (importance) {
            case LOW -> 0;
            case MEDIUM -> 10;
            case HIGH -> 20;
        };
    }
}
