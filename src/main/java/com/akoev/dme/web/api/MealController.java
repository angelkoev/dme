package com.akoev.dme.web.api;

import com.akoev.dme.meals.Allergen;
import com.akoev.dme.meals.DietGoal;
import com.akoev.dme.meals.Meal;
import com.akoev.dme.meals.MealPlannerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealPlannerService mealPlannerService;

    @GetMapping
    public List<Meal> meals() {
        return mealPlannerService.listMeals();
    }

    @PostMapping("/plan")
    public List<Meal> plan(@Valid @RequestBody DailyPlanRequest request) {
        return mealPlannerService.recommendDailyPlan(request.dietGoal(), request.allergies());
    }

    // dietGoal is @NotNull because MealScorer switches on it; a plain switch
    // throws on a null enum, which without this validation surfaced as an
    // opaque 500 instead of a 400 for a missing field.
    public record DailyPlanRequest(@NotNull DietGoal dietGoal, Set<Allergen> allergies) {
    }
}
