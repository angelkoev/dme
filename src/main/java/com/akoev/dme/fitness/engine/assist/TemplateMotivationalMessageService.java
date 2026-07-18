package com.akoev.dme.fitness.engine.assist;

import com.akoev.dme.fitness.engine.RecentActivitySummary;
import org.springframework.stereotype.Component;

@Component
public class TemplateMotivationalMessageService implements MotivationalMessageService {

    @Override
    public String motivate(RecentActivitySummary recentActivity) {
        Integer daysSince = recentActivity.getDaysSinceLastWorkout();
        if (daysSince == null) {
            return "Welcome! Let's get your first workout done.";
        }
        if (daysSince == 0) {
            return "Great job training today — keep the momentum going!";
        }
        if (daysSince > 7) {
            return "It's been a while — let's ease back in and rebuild the habit.";
        }
        return "Keep it up, you're on track!";
    }
}
