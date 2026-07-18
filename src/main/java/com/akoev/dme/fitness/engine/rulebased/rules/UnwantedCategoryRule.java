package com.akoev.dme.fitness.engine.rulebased.rules;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class UnwantedCategoryRule implements Rule<FitnessDecisionContext, Exercise> {

    @Override
    public boolean isSatisfiedBy(FitnessDecisionContext context, Exercise candidate) {
        return !context.getProfile().getUnwantedCategories().contains(candidate.getPrimaryMuscleGroup());
    }

    @Override
    public String description() {
        return "Exercise must not target a muscle group category the user marked as unwanted";
    }
}
