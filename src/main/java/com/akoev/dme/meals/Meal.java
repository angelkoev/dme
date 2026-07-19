package com.akoev.dme.meals;

import java.util.Set;

public record Meal(Long id, String name, MealType mealType, int calories, int proteinGrams,
                    Set<Allergen> allergens) {
}
