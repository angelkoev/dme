package com.akoev.dme.fitness.engine.rulebased.strategy;

import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.TrainingGoal;

import java.util.List;

/**
 * Decides the weekly split (which movement pattern each exercise slot in
 * each session should target) and the sets/reps/rest scheme, for one
 * training goal. One implementation per {@link TrainingGoal}, selected at
 * runtime by {@link GoalStrategyResolver}.
 */
public interface GoalWorkoutStrategy {

    TrainingGoal supportedGoal();

    List<SessionBlueprint> buildSplit(int daysPerWeek);

    SetRepScheme setRepScheme();

    record SessionBlueprint(String name, List<MovementPattern> exerciseSlots) {
    }

    record SetRepScheme(int sets, int repMin, int repMax, int restSeconds) {
    }
}
