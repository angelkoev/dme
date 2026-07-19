package com.akoev.dme.meals;

import java.util.Set;

public record MealContext(DietGoal dietGoal, MealType targetMealType, Set<Allergen> allergies) {

    public MealContext {
        allergies = allergies == null ? Set.of() : allergies;
    }
}
