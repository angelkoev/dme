package com.akoev.dme.fitness.engine.rulebased.rules;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExperienceLevel;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class ExperienceLevelRule implements Rule<FitnessDecisionContext, Exercise> {

    @Override
    public boolean isSatisfiedBy(FitnessDecisionContext context, Exercise candidate) {
        return rank(candidate.getDifficultyLevel()) <= rank(context.getProfile().getExperienceLevel());
    }

    @Override
    public String description() {
        return "Exercise difficulty must not exceed the user's experience level";
    }

    // Explicit ranking instead of comparing .ordinal() across two independently
    // declared enums: DifficultyLevel and ExperienceLevel happen to list
    // BEGINNER/INTERMEDIATE/ADVANCED in the same order today, but nothing
    // enforces that — a future reorder or insertion in just one of them would
    // silently break an ordinal comparison. The switch below is exhaustive, so
    // adding a new constant to either enum without updating its ranking here
    // fails to compile instead of failing silently.
    private static int rank(DifficultyLevel level) {
        return switch (level) {
            case BEGINNER -> 0;
            case INTERMEDIATE -> 1;
            case ADVANCED -> 2;
        };
    }

    private static int rank(ExperienceLevel level) {
        return switch (level) {
            case BEGINNER -> 0;
            case INTERMEDIATE -> 1;
            case ADVANCED -> 2;
        };
    }
}
