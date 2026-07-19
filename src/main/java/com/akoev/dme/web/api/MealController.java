package com.akoev.dme.web.api;

import com.akoev.dme.meals.Allergen;
import com.akoev.dme.meals.DietGoal;
import com.akoev.dme.meals.Meal;
import com.akoev.dme.meals.MealPlannerService;
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
    public List<Meal> plan(@RequestBody DailyPlanRequest request) {
        return mealPlannerService.recommendDailyPlan(request.dietGoal(), request.allergies());
    }

    public record DailyPlanRequest(DietGoal dietGoal, Set<Allergen> allergies) {
    }
}
