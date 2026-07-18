package com.akoev.dme.web.api;

import com.akoev.dme.application.service.CompleteSessionCommand;
import com.akoev.dme.application.service.WorkoutPlanService;
import com.akoev.dme.infrastructure.security.CustomUserDetails;
import com.akoev.dme.web.api.dto.CompleteSessionRequest;
import com.akoev.dme.web.api.dto.CompleteSessionResponse;
import com.akoev.dme.web.api.dto.GenerateWorkoutPlanRequest;
import com.akoev.dme.web.api.dto.GenerateWorkoutPlanResponse;
import com.akoev.dme.web.api.dto.WorkoutPlanResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workout-plans")
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @PostMapping("/generate")
    public GenerateWorkoutPlanResponse generate(@AuthenticationPrincipal CustomUserDetails principal,
                                                 @RequestBody(required = false) GenerateWorkoutPlanRequest request) {
        var goalOverride = request != null ? request.goalOverride() : null;
        return GenerateWorkoutPlanResponse.from(workoutPlanService.generate(principal.getId(), goalOverride));
    }

    @GetMapping("/active")
    public WorkoutPlanResponse active(@AuthenticationPrincipal CustomUserDetails principal) {
        return WorkoutPlanResponse.from(workoutPlanService.getActivePlan(principal.getId()));
    }

    @GetMapping
    public List<WorkoutPlanResponse> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return workoutPlanService.listAll(principal.getId()).stream().map(WorkoutPlanResponse::from).toList();
    }

    @GetMapping("/{id}")
    public WorkoutPlanResponse get(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        return WorkoutPlanResponse.from(workoutPlanService.getById(principal.getId(), id));
    }

    @PostMapping("/{planId}/sessions/{sessionId}/complete")
    public CompleteSessionResponse complete(@AuthenticationPrincipal CustomUserDetails principal,
                                             @PathVariable Long planId, @PathVariable Long sessionId,
                                             @Valid @RequestBody CompleteSessionRequest request) {
        return CompleteSessionResponse.from(
                workoutPlanService.completeSession(principal.getId(), planId, sessionId, toCommand(request)));
    }

    private CompleteSessionCommand toCommand(CompleteSessionRequest request) {
        List<CompleteSessionCommand.ExercisePerformanceCommand> performances = request.exercisePerformances() == null
                ? List.of()
                : request.exercisePerformances().stream()
                        .map(p -> new CompleteSessionCommand.ExercisePerformanceCommand(p.exerciseId(), p.weightKg(), p.reps()))
                        .toList();
        return new CompleteSessionCommand(request.completionPercentage(), request.rating(),
                request.perceivedIntensity(), request.notes(), performances);
    }
}
