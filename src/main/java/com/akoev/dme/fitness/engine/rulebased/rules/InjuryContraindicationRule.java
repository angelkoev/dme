package com.akoev.dme.fitness.engine.rulebased.rules;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.UserLimitation;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class InjuryContraindicationRule implements Rule<FitnessDecisionContext, Exercise> {

    @Override
    public boolean isSatisfiedBy(FitnessDecisionContext context, Exercise candidate) {
        return context.getProfile().getLimitations().stream()
                .map(UserLimitation::getMuscleGroup)
                .filter(Objects::nonNull)
                .noneMatch(muscleGroup -> muscleGroup == candidate.getPrimaryMuscleGroup());
    }

    @Override
    public String description() {
        return "Exercise must not target a muscle group the user has flagged as injured";
    }
}
