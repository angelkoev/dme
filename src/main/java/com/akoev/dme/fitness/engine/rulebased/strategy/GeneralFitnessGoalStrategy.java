package com.akoev.dme.fitness.engine.rulebased.strategy;

import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.TrainingGoal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
public class GeneralFitnessGoalStrategy implements GoalWorkoutStrategy {

    private static final List<MovementPattern> SLOTS =
            List.of(MovementPattern.PUSH, MovementPattern.PULL, MovementPattern.LEGS, MovementPattern.CORE);

    @Override
    public TrainingGoal supportedGoal() {
        return TrainingGoal.GENERAL_FITNESS;
    }

    @Override
    public List<SessionBlueprint> buildSplit(int daysPerWeek) {
        int days = Math.clamp(daysPerWeek, 1, 6);
        return IntStream.rangeClosed(1, days)
                .mapToObj(day -> new SessionBlueprint("Full Body " + day, SLOTS))
                .toList();
    }

    @Override
    public SetRepScheme setRepScheme() {
        return new SetRepScheme(3, 10, 15, 60);
    }
}
