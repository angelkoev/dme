package com.akoev.dme.fitness.engine.rulebased.strategy;

import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.TrainingGoal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
public class EnduranceGoalStrategy implements GoalWorkoutStrategy {

    private static final List<MovementPattern> SLOTS = List.of(
            MovementPattern.FULL_BODY, MovementPattern.LEGS, MovementPattern.CORE,
            MovementPattern.PUSH, MovementPattern.PULL);

    @Override
    public TrainingGoal supportedGoal() {
        return TrainingGoal.ENDURANCE;
    }

    @Override
    public List<SessionBlueprint> buildSplit(int daysPerWeek) {
        int days = Math.clamp(daysPerWeek, 1, 6);
        return IntStream.rangeClosed(1, days)
                .mapToObj(day -> new SessionBlueprint("Endurance Session " + day, SLOTS))
                .toList();
    }

    @Override
    public SetRepScheme setRepScheme() {
        return new SetRepScheme(3, 15, 25, 30);
    }
}
