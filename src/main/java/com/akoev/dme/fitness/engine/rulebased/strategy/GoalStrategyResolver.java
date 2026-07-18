package com.akoev.dme.fitness.engine.rulebased.strategy;

import com.akoev.dme.domain.model.TrainingGoal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GoalStrategyResolver {

    private final Map<TrainingGoal, GoalWorkoutStrategy> strategiesByGoal;

    public GoalStrategyResolver(List<GoalWorkoutStrategy> strategies) {
        this.strategiesByGoal = strategies.stream()
                .collect(Collectors.toMap(GoalWorkoutStrategy::supportedGoal, Function.identity()));
    }

    public GoalWorkoutStrategy resolve(TrainingGoal goal) {
        GoalWorkoutStrategy strategy = strategiesByGoal.get(goal);
        if (strategy == null) {
            throw new IllegalStateException("No GoalWorkoutStrategy registered for goal: " + goal);
        }
        return strategy;
    }
}
