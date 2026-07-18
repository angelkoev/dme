package com.akoev.dme.web.api;

import com.akoev.dme.application.service.ExerciseService;
import com.akoev.dme.application.service.ExerciseUpsertCommand;
import com.akoev.dme.web.api.dto.ExerciseRequest;
import com.akoev.dme.web.api.dto.ExerciseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public List<ExerciseResponse> list() {
        return exerciseService.listAll().stream().map(ExerciseResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ExerciseResponse get(@PathVariable Long id) {
        return ExerciseResponse.from(exerciseService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseResponse create(@Valid @RequestBody ExerciseRequest request) {
        return ExerciseResponse.from(exerciseService.create(toCommand(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ExerciseResponse update(@PathVariable Long id, @Valid @RequestBody ExerciseRequest request) {
        return ExerciseResponse.from(exerciseService.update(id, toCommand(request)));
    }

    private ExerciseUpsertCommand toCommand(ExerciseRequest request) {
        return new ExerciseUpsertCommand(
                request.name(),
                request.description(),
                request.primaryMuscleGroup(),
                request.movementPattern(),
                request.difficultyLevel(),
                request.exerciseType(),
                request.instructions(),
                request.videoUrl(),
                request.equipmentIds()
        );
    }
}
