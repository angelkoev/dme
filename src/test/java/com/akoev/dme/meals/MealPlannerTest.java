package com.akoev.dme.meals;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MealPlannerTest {

    private final MealScorer scorer = new MealScorer();

    @Test
    void weightLossGoalFavorsLowerCalorieMeal() {
        MealContext context = new MealContext(DietGoal.WEIGHT_LOSS, MealType.LUNCH, Set.of());
        Meal lowCalorie = new Meal(1L, "A", MealType.LUNCH, 350, 20, Set.of());
        Meal highCalorie = new Meal(2L, "B", MealType.LUNCH, 700, 20, Set.of());

        assertThat(scorer.score(context, lowCalorie)).isGreaterThan(scorer.score(context, highCalorie));
    }

    @Test
    void muscleGainGoalFavorsHigherProteinMeal() {
        MealContext context = new MealContext(DietGoal.MUSCLE_GAIN, MealType.LUNCH, Set.of());
        Meal highProtein = new Meal(1L, "A", MealType.LUNCH, 450, 40, Set.of());
        Meal lowProtein = new Meal(2L, "B", MealType.LUNCH, 450, 5, Set.of());

        assertThat(scorer.score(context, highProtein)).isGreaterThan(scorer.score(context, lowProtein));
    }

    @Test
    void allergenRuleExcludesMealWithFlaggedAllergen() {
        AllergenRule rule = new AllergenRule();
        MealContext context = new MealContext(DietGoal.MAINTENANCE, MealType.BREAKFAST, Set.of(Allergen.DAIRY));
        Meal dairyMeal = new Meal(1L, "A", MealType.BREAKFAST, 300, 15, Set.of(Allergen.DAIRY));

        assertThat(rule.isSatisfiedBy(context, dairyMeal)).isFalse();
    }

    @Test
    void dailyPlanHasOneMealPerSlotAndNeverRepeats() {
        MealCatalog catalog = new MealCatalog();
        MealPlannerService service = new MealPlannerService(
                catalog, List.of(new AllergenRule(), new MealTypeRule()), new MealScorer());

        List<Meal> plan = service.recommendDailyPlan(DietGoal.MAINTENANCE, Set.of());

        assertThat(plan).hasSize(MealType.values().length);
        assertThat(plan.stream().map(Meal::id)).doesNotHaveDuplicates();
    }
}
