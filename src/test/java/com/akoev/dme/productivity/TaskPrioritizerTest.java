package com.akoev.dme.productivity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskPrioritizerTest {

    private final TaskScorer scorer = new TaskScorer();

    @Test
    void highUrgencyAndImportanceScoresHigherThanLow() {
        ProductivityContext context = new ProductivityContext(240, EnergyLevel.HIGH);
        Task critical = new Task("Fix outage", Urgency.HIGH, Importance.HIGH, 60);
        Task minor = new Task("Tidy inbox", Urgency.LOW, Importance.LOW, 15);

        assertThat(scorer.score(context, critical)).isGreaterThan(scorer.score(context, minor));
    }

    @Test
    void lowEnergyFavorsQuickTaskOverSlowerEquallyRankedTask() {
        ProductivityContext context = new ProductivityContext(240, EnergyLevel.LOW);
        Task quick = new Task("Quick email", Urgency.MEDIUM, Importance.MEDIUM, 10);
        Task slow = new Task("Long report", Urgency.MEDIUM, Importance.MEDIUM, 90);

        assertThat(scorer.score(context, quick)).isGreaterThan(scorer.score(context, slow));
    }

    @Test
    void fitsAvailableTimeRuleExcludesTaskLongerThanAvailableTime() {
        FitsAvailableTimeRule rule = new FitsAvailableTimeRule();
        ProductivityContext context = new ProductivityContext(30, EnergyLevel.MEDIUM);
        Task tooLong = new Task("Big project", Urgency.HIGH, Importance.HIGH, 90);

        assertThat(rule.isSatisfiedBy(context, tooLong)).isFalse();
    }

    @Test
    void prioritizeExcludesTasksThatDoNotFitAndRanksTheRest() {
        TaskPrioritizerService service = new TaskPrioritizerService(List.of(new FitsAvailableTimeRule()), new TaskScorer());
        ProductivityContext context = new ProductivityContext(60, EnergyLevel.HIGH);
        List<Task> tasks = List.of(
                new Task("Fits, low priority", Urgency.LOW, Importance.LOW, 30),
                new Task("Fits, high priority", Urgency.HIGH, Importance.HIGH, 45),
                new Task("Too long", Urgency.HIGH, Importance.HIGH, 90));

        List<Task> result = service.prioritize(context, tasks);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Fits, high priority");
    }
}
