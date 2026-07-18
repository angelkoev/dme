package com.akoev.dme.fitness.engine.rulebased.strategy;

import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.TrainingGoal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
public class FatLossGoalStrategy implements GoalWorkoutStrategy {

    private static final List<MovementPattern> CIRCUIT_SLOTS = List.of(
            MovementPattern.FULL_BODY, MovementPattern.LEGS, MovementPattern.PUSH,
            MovementPattern.PULL, MovementPattern.CORE);

    @Override
    public TrainingGoal supportedGoal() {
        return TrainingGoal.FAT_LOSS;
    }

    @Override
    public List<SessionBlueprint> buildSplit(int daysPerWeek) {
        int days = Math.clamp(daysPerWeek, 1, 6);
        return IntStream.rangeClosed(1, days)
                .mapToObj(day -> new SessionBlueprint("Fat Loss Circuit " + day, CIRCUIT_SLOTS))
                .toList();
    }

    @Override
    public SetRepScheme setRepScheme() {
        return new SetRepScheme(3, 12, 20, 30);
    }
}
