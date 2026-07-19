package com.akoev.dme.web.api;

import com.akoev.dme.productivity.EnergyLevel;
import com.akoev.dme.productivity.ProductivityContext;
import com.akoev.dme.productivity.Task;
import com.akoev.dme.productivity.TaskPrioritizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productivity")
@RequiredArgsConstructor
public class TaskPrioritizerController {

    private final TaskPrioritizerService taskPrioritizerService;

    @PostMapping("/prioritize")
    public List<Task> prioritize(@RequestBody PrioritizeRequest request) {
        ProductivityContext context = new ProductivityContext(request.availableMinutesToday(), request.energyLevel());
        return taskPrioritizerService.prioritize(context, request.tasks());
    }

    public record PrioritizeRequest(int availableMinutesToday, EnergyLevel energyLevel, List<Task> tasks) {
    }
}
