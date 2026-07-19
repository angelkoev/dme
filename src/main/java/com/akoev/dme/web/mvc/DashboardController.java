package com.akoev.dme.web.mvc;

import com.akoev.dme.application.service.CompleteSessionCommand;
import com.akoev.dme.application.service.WorkoutPlanService;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.repository.WorkoutStreakRepository;
import com.akoev.dme.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final WorkoutPlanService workoutPlanService;
    private final WorkoutStreakRepository workoutStreakRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
        try {
            model.addAttribute("plan", workoutPlanService.getActivePlan(principal.getId()));
        } catch (ResponseStatusException ex) {
            model.addAttribute("noPlan", true);
        }
        workoutStreakRepository.findByUserId(principal.getId()).ifPresent(streak -> model.addAttribute("streak", streak));
        model.addAttribute("trainingGoals", TrainingGoal.values());
        return "dashboard";
    }

    @PostMapping("/dashboard/generate")
    public String generate(@AuthenticationPrincipal CustomUserDetails principal,
                            @RequestParam(required = false) String goalOverride) {
        // Bound as a raw String, not TrainingGoal: unlike bean-property binding
        // (e.g. ProfileForm), @RequestParam's ConversionService has no
        // empty-string-is-null special case, so an unset <select> (value="")
        // would otherwise fail enum conversion instead of meaning "no override".
        TrainingGoal goal = (goalOverride == null || goalOverride.isBlank()) ? null : TrainingGoal.valueOf(goalOverride);
        workoutPlanService.generate(principal.getId(), goal);
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/sessions/{sessionId}/complete")
    public String complete(@AuthenticationPrincipal CustomUserDetails principal,
                            @PathVariable Long sessionId,
                            @RequestParam Long planId,
                            @RequestParam int completionPercentage,
                            @RequestParam(required = false) Integer rating) {
        workoutPlanService.completeSession(principal.getId(), planId, sessionId,
                new CompleteSessionCommand(completionPercentage, rating, null, null, List.of()));
        return "redirect:/dashboard";
    }
}
