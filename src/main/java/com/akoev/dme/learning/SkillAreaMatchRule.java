package com.akoev.dme.learning;

import com.akoev.dme.decisionengine.Rule;
import org.springframework.stereotype.Component;

@Component
public class SkillAreaMatchRule implements Rule<LearningContext, Course> {

    @Override
    public boolean isSatisfiedBy(LearningContext context, Course candidate) {
        return context.targetSkillArea() == candidate.skillArea();
    }

    @Override
    public String description() {
        return "Course must be in the skill area the user is targeting";
    }
}
