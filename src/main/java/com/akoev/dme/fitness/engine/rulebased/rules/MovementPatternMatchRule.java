package com.akoev.dme.fitness.engine.rulebased.rules;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class MovementPatternMatchRule implements Rule<FitnessDecisionContext, Exercise> {

    @Override
    public boolean isSatisfiedBy(FitnessDecisionContext context, Exercise candidate) {
        return context.getTargetMovementPattern() == candidate.getMovementPattern();
    }

    @Override
    public String description() {
        return "Exercise must match the movement pattern the current session slot targets";
    }
}
