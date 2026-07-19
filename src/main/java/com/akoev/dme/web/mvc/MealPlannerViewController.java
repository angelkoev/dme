package com.akoev.dme.web.mvc;

import com.akoev.dme.meals.Allergen;
import com.akoev.dme.meals.DietGoal;
import com.akoev.dme.meals.MealPlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class MealPlannerViewController {

    private final MealPlannerService mealPlannerService;

    @GetMapping("/meals")
    public String view(Model model) {
        model.addAttribute("meals", mealPlannerService.listMeals());
        model.addAttribute("dietGoals", DietGoal.values());
        model.addAttribute("allergens", Allergen.values());
        return "meals";
    }

    @PostMapping("/meals/plan")
    public String plan(@RequestParam DietGoal dietGoal,
                        @RequestParam(required = false) Set<Allergen> allergies,
                        Model model) {
        model.addAttribute("dailyPlan", mealPlannerService.recommendDailyPlan(dietGoal, allergies));
        model.addAttribute("selectedDietGoal", dietGoal);
        model.addAttribute("meals", mealPlannerService.listMeals());
        model.addAttribute("dietGoals", DietGoal.values());
        model.addAttribute("allergens", Allergen.values());
        return "meals";
    }
}
