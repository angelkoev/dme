package com.akoev.dme.fitness.engine.rulebased.strategy;

import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.TrainingGoal;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class HypertrophyGoalStrategy implements GoalWorkoutStrategy {

    private static final List<MovementPattern> PUSH_PULL_LEGS_CYCLE =
            List.of(MovementPattern.PUSH, MovementPattern.PULL, MovementPattern.LEGS);

    @Override
    public TrainingGoal supportedGoal() {
        return TrainingGoal.HYPERTROPHY;
    }

    @Override
    public List<SessionBlueprint> buildSplit(int daysPerWeek) {
        int days = Math.clamp(daysPerWeek, 1, 6);
        List<SessionBlueprint> sessions = new ArrayList<>();
        for (int day = 1; day <= days; day++) {
            MovementPattern focus = PUSH_PULL_LEGS_CYCLE.get((day - 1) % PUSH_PULL_LEGS_CYCLE.size());
            List<MovementPattern> slots = new ArrayList<>(Collections.nCopies(4, focus));
            slots.add(MovementPattern.CORE);
            sessions.add(new SessionBlueprint(capitalize(focus) + " Day " + day, slots));
        }
        return sessions;
    }

    @Override
    public SetRepScheme setRepScheme() {
        return new SetRepScheme(4, 8, 12, 75);
    }

    private String capitalize(MovementPattern pattern) {
        String name = pattern.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
