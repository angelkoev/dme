package com.akoev.dme.fitness.engine.assist;

import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.domain.model.WorkoutPlan;
import org.springframework.stereotype.Component;

@Component
public class TemplateWorkoutExplanationService implements WorkoutExplanationService {

    @Override
    public String explain(WorkoutPlan plan, UserProfile profile) {
        return "Generated a %d-session %s plan matched to your %s experience level and available equipment."
                .formatted(plan.getSessions().size(), plan.getGoal(), profile.getExperienceLevel());
    }
}
