package com.akoev.dme.fitness.engine.assist;

import com.akoev.dme.fitness.engine.RecentActivitySummary;

/**
 * Produces a short motivational message from the user's recent activity.
 * A future AI-backed implementation could personalize this further.
 */
public interface MotivationalMessageService {

    String motivate(RecentActivitySummary recentActivity);
}
