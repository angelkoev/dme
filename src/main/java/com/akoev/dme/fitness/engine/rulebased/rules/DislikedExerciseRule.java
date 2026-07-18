package com.akoev.dme.fitness.engine.rulebased.rules;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class DislikedExerciseRule implements Rule<FitnessDecisionContext, Exercise> {

    @Override
    public boolean isSatisfiedBy(FitnessDecisionContext context, Exercise candidate) {
        return context.getProfile().getDislikedExercises().stream()
                .noneMatch(exercise -> exercise.getId().equals(candidate.getId()));
    }

    @Override
    public String description() {
        return "Exercise must not be one the user marked as disliked";
    }
}
