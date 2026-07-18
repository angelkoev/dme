package com.akoev.dme.fitness.engine.assist;

import com.akoev.dme.domain.model.WorkoutPlan;
import com.akoev.dme.fitness.engine.FitnessDecisionContext;
import org.springframework.stereotype.Component;

@Component
public class TemplateWorkoutExplanationService implements WorkoutExplanationService {

    @Override
    public String explain(WorkoutPlan plan, FitnessDecisionContext context) {
        return "Generated a %d-session %s plan matched to your %s experience level and available equipment."
                .formatted(plan.getSessions().size(), plan.getGoal(), context.getProfile().getExperienceLevel());
    }
}
