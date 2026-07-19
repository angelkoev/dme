package com.akoev.dme.meals;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class MealCatalog {

    private static final List<Meal> MEALS = List.of(
            new Meal(1L, "Oatmeal with Berries", MealType.BREAKFAST, 320, 10, Set.of()),
            new Meal(2L, "Greek Yogurt & Granola", MealType.BREAKFAST, 280, 18, Set.of(Allergen.DAIRY, Allergen.GLUTEN)),
            new Meal(3L, "Veggie Scramble", MealType.BREAKFAST, 350, 22, Set.of(Allergen.EGGS)),
            new Meal(4L, "Grilled Chicken Salad", MealType.LUNCH, 420, 35, Set.of()),
            new Meal(5L, "Lentil Soup", MealType.LUNCH, 380, 20, Set.of()),
            new Meal(6L, "Tuna Sandwich", MealType.LUNCH, 450, 28, Set.of(Allergen.GLUTEN, Allergen.SHELLFISH)),
            new Meal(7L, "Salmon & Quinoa", MealType.DINNER, 520, 40, Set.of()),
            new Meal(8L, "Steak & Vegetables", MealType.DINNER, 600, 45, Set.of()),
            new Meal(9L, "Vegetable Stir Fry", MealType.DINNER, 380, 15, Set.of()),
            new Meal(10L, "Mixed Nuts", MealType.SNACK, 200, 7, Set.of(Allergen.NUTS)),
            new Meal(11L, "Protein Shake", MealType.SNACK, 180, 25, Set.of(Allergen.DAIRY)),
            new Meal(12L, "Apple Slices", MealType.SNACK, 95, 0, Set.of())
    );

    public List<Meal> findAll() {
        return MEALS;
    }
}
