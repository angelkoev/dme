package com.akoev.dme.fitness.engine.rulebased.rules;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class ExperienceLevelRule implements Rule<FitnessDecisionContext, Exercise> {

    @Override
    public boolean isSatisfiedBy(FitnessDecisionContext context, Exercise candidate) {
        // ExperienceLevel and DifficultyLevel both declare BEGINNER/INTERMEDIATE/ADVANCED
        // in that order, so comparing ordinals ranks them consistently.
        return candidate.getDifficultyLevel().ordinal() <= context.getProfile().getExperienceLevel().ordinal();
    }

    @Override
    public String description() {
        return "Exercise difficulty must not exceed the user's experience level";
    }
}
