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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                            @RequestParam(required = false) String goalOverride,
                            RedirectAttributes redirectAttributes) {
        try {
            // Bound as a raw String, not TrainingGoal: unlike bean-property binding
            // (e.g. ProfileForm), @RequestParam's ConversionService has no
            // empty-string-is-null special case, so an unset <select> (value="")
            // would otherwise fail enum conversion instead of meaning "no override".
            // valueOf() must stay inside this try: a tampered/stale non-blank value
            // throws IllegalArgumentException, which — unlike ResponseStatusException
            // below — has no handler in web.mvc (ApiExceptionHandler only covers
            // web.api), so outside the try it would surface as a raw 500 page.
            TrainingGoal goal = (goalOverride == null || goalOverride.isBlank()) ? null : TrainingGoal.valueOf(goalOverride);
            workoutPlanService.generate(principal.getId(), goal);
        } catch (ResponseStatusException | IllegalArgumentException ex) {
            // Same expected condition dashboard() already handles for the GET
            // case (e.g. "no profile yet") — without this catch it escaped as
            // an unhandled 404/plain-text error page, since ApiExceptionHandler
            // is scoped to web.api only and never sees web.mvc requests.
            String message = ex instanceof ResponseStatusException rse ? rse.getReason() : "Invalid goal selection.";
            redirectAttributes.addFlashAttribute("generateError", message);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/sessions/{sessionId}/complete")
    public String complete(@AuthenticationPrincipal CustomUserDetails principal,
                            @PathVariable Long sessionId,
                            @RequestParam Long planId,
                            @RequestParam int completionPercentage,
                            @RequestParam(required = false) Integer rating,
                            @RequestParam(required = false) Integer perceivedIntensity,
                            RedirectAttributes redirectAttributes) {
        try {
            workoutPlanService.completeSession(principal.getId(), planId, sessionId,
                    new CompleteSessionCommand(completionPercentage, rating, perceivedIntensity, null, List.of()));
        } catch (ResponseStatusException ex) {
            // Separate flash attribute from generate()'s: these errors (plan/session
            // ownership) don't warrant the "fill in your profile" hint that message shows.
            redirectAttributes.addFlashAttribute("completeError", ex.getReason());
        }
        return "redirect:/dashboard";
    }
}
