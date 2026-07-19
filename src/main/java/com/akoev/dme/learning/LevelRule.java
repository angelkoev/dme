package com.akoev.dme.learning;

import com.akoev.dme.decisionengine.Rule;
import org.springframework.stereotype.Component;

/**
 * Allows the user's current level or one step above (a reasonable stretch
 * course) but not further — a beginner shouldn't be offered an advanced
 * course. This is the one domain (of the four catalog-based ones) whose
 * natural next step would be a prerequisite chain rather than a flat level
 * cap — see STUDY_GUIDE.md's note on why DecisionTreeStrategy wasn't built
 * without a real use case; this rule is deliberately the flat, simple
 * version instead.
 */
@Component
public class LevelRule implements Rule<LearningContext, Course> {

    @Override
    public boolean isSatisfiedBy(LearningContext context, Course candidate) {
        return rank(candidate.level()) <= rank(context.currentLevel()) + 1;
    }

    @Override
    public String description() {
        return "Course level must not be more than one step above the user's current level";
    }

    private static int rank(SkillLevel level) {
        return switch (level) {
            case BEGINNER -> 0;
            case INTERMEDIATE -> 1;
            case ADVANCED -> 2;
        };
    }
}
