package com.akoev.dme.web.mvc;

import com.akoev.dme.productivity.EnergyLevel;
import com.akoev.dme.productivity.Importance;
import com.akoev.dme.productivity.ProductivityContext;
import com.akoev.dme.productivity.Task;
import com.akoev.dme.productivity.TaskPrioritizerService;
import com.akoev.dme.productivity.Urgency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Bound via parallel same-named-parameter lists (taskName/taskUrgency/...)
 * rather than a mutable form-backing bean like ProfileForm/ExerciseForm —
 * simpler here since rows are fixed-count and flat (no nested object graph
 * to bind), so indexed th:field binding would be more machinery than this
 * domain's "basic" scope calls for.
 */
@Controller
@RequiredArgsConstructor
public class TaskPrioritizerViewController {

    private static final int TASK_ROW_COUNT = 5;

    private final TaskPrioritizerService taskPrioritizerService;

    @GetMapping("/tasks")
    public String view(Model model) {
        addReferenceData(model);
        return "productivity";
    }

    @PostMapping("/tasks/prioritize")
    public String prioritize(@RequestParam int availableMinutesToday,
                              @RequestParam EnergyLevel energyLevel,
                              @RequestParam List<String> taskName,
                              @RequestParam List<Urgency> taskUrgency,
                              @RequestParam List<Importance> taskImportance,
                              @RequestParam List<Integer> taskMinutes,
                              Model model) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < taskName.size(); i++) {
            if (taskName.get(i) != null && !taskName.get(i).isBlank()) {
                tasks.add(new Task(taskName.get(i), taskUrgency.get(i), taskImportance.get(i), taskMinutes.get(i)));
            }
        }

        ProductivityContext context = new ProductivityContext(availableMinutesToday, energyLevel);
        model.addAttribute("prioritized", taskPrioritizerService.prioritize(context, tasks));
        addReferenceData(model);
        return "productivity";
    }

    private void addReferenceData(Model model) {
        model.addAttribute("rowIndexes", java.util.stream.IntStream.range(0, TASK_ROW_COUNT).boxed().toList());
        model.addAttribute("energyLevels", EnergyLevel.values());
        model.addAttribute("urgencies", Urgency.values());
        model.addAttribute("importances", Importance.values());
    }
}
