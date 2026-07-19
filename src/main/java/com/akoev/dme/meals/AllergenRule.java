package com.akoev.dme.meals;

import com.akoev.dme.decisionengine.Rule;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class AllergenRule implements Rule<MealContext, Meal> {

    @Override
    public boolean isSatisfiedBy(MealContext context, Meal candidate) {
        return Collections.disjoint(context.allergies(), candidate.allergens());
    }

    @Override
    public String description() {
        return "Meal must not contain any allergen the user has flagged";
    }
}
