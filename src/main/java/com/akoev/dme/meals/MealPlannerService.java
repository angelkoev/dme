package com.akoev.dme.meals;

import com.akoev.dme.decisionengine.Rule;
import com.akoev.dme.decisionengine.RuleBasedDecisionEngine;
import com.akoev.dme.decisionengine.ScoredCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@code recommendDailyPlan} is the meal-domain analog of
 * {@code RuleBasedWorkoutPlanGenerator.generate()}: one slot per
 * {@link MealType}, ranked and picked in turn, excluding meals already used
 * elsewhere in the same day so a plan never repeats a meal.
 */
@Service
@RequiredArgsConstructor
public class MealPlannerService {

    private final MealCatalog catalog;
    private final List<Rule<MealContext, Meal>> rules;
    private final MealScorer scorer;

    public List<Meal> listMeals() {
        return catalog.findAll();
    }

    public List<Meal> recommendDailyPlan(DietGoal dietGoal, Set<Allergen> allergies) {
        RuleBasedDecisionEngine<MealContext, Meal> engine = new RuleBasedDecisionEngine<>(rules, scorer);
        Set<Long> usedInThisPlan = new HashSet<>();
        List<Meal> plan = new ArrayList<>();

        for (MealType mealType : MealType.values()) {
            MealContext context = new MealContext(dietGoal, mealType, allergies);
            List<Meal> available = catalog.findAll().stream()
                    .filter(meal -> !usedInThisPlan.contains(meal.id()))
                    .toList();
            List<ScoredCandidate<Meal>> ranked = engine.rank(context, available);
            if (!ranked.isEmpty()) {
                Meal chosen = ranked.get(0).candidate();
                usedInThisPlan.add(chosen.id());
                plan.add(chosen);
            }
        }
        return plan;
    }
}
