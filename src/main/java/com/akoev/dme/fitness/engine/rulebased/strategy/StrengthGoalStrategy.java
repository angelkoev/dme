package com.akoev.dme.fitness.engine.rulebased.strategy;

import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.TrainingGoal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
public class StrengthGoalStrategy implements GoalWorkoutStrategy {

    private static final List<MovementPattern> FULL_BODY_SLOTS =
            List.of(MovementPattern.LEGS, MovementPattern.PUSH, MovementPattern.PULL, MovementPattern.CORE);

    @Override
    public TrainingGoal supportedGoal() {
        return TrainingGoal.STRENGTH;
    }

    @Override
    public List<SessionBlueprint> buildSplit(int daysPerWeek) {
        int days = Math.clamp(daysPerWeek, 1, 6);
        return IntStream.rangeClosed(1, days)
                .mapToObj(day -> new SessionBlueprint("Strength Day " + day, FULL_BODY_SLOTS))
                .toList();
    }

    @Override
    public SetRepScheme setRepScheme() {
        return new SetRepScheme(4, 3, 6, 150);
    }
}
