package com.akoev.dme.fitness.engine.rulebased.rules;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class EquipmentAvailabilityRule implements Rule<FitnessDecisionContext, Exercise> {

    @Override
    public boolean isSatisfiedBy(FitnessDecisionContext context, Exercise candidate) {
        return context.getProfile().getAvailableEquipment().containsAll(candidate.getRequiredEquipment());
    }

    @Override
    public String description() {
        return "Exercise must only require equipment the user has available";
    }
}
