package com.akoev.dme.meals;

import com.akoev.dme.decisionengine.Rule;
import org.springframework.stereotype.Component;

@Component
public class MealTypeRule implements Rule<MealContext, Meal> {

    @Override
    public boolean isSatisfiedBy(MealContext context, Meal candidate) {
        return context.targetMealType() == candidate.mealType();
    }

    @Override
    public String description() {
        return "Meal must match the meal type slot being filled (breakfast/lunch/dinner/snack)";
    }
}
