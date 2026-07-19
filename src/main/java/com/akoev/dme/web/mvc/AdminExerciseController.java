package com.akoev.dme.web.mvc;

import com.akoev.dme.application.service.ExerciseService;
import com.akoev.dme.application.service.ExerciseUpsertCommand;
import com.akoev.dme.domain.model.DifficultyLevel;
import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.ExerciseType;
import com.akoev.dme.domain.model.MovementPattern;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.repository.EquipmentRepository;
import com.akoev.dme.web.mvc.form.ExerciseForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Web (session-authenticated) admin pages for the exercise catalog.
 * Equivalent, for the browser, of the ROLE_ADMIN-guarded
 * POST/PUT /api/v1/exercises endpoints — same ExerciseService underneath,
 * so catalog rules (validation, mapping) live in exactly one place.
 */
@Controller
@RequestMapping("/admin/exercises")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminExerciseController {

    private final ExerciseService exerciseService;
    private final EquipmentRepository equipmentRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("exercises", exerciseService.listAll());
        return "admin-exercises";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("exerciseForm")) {
            model.addAttribute("exerciseForm", new ExerciseForm());
        }
        model.addAttribute("isNew", true);
        addReferenceData(model);
        return "admin-exercise-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("exerciseForm") ExerciseForm form, BindingResult bindingResult,
                          Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isNew", true);
            addReferenceData(model);
            return "admin-exercise-form";
        }
        exerciseService.create(toCommand(form));
        redirectAttributes.addFlashAttribute("exerciseSaved", true);
        return "redirect:/admin/exercises";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("exerciseForm")) {
            model.addAttribute("exerciseForm", toForm(exerciseService.getById(id)));
        }
        model.addAttribute("isNew", false);
        model.addAttribute("exerciseId", id);
        addReferenceData(model);
        return "admin-exercise-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("exerciseForm") ExerciseForm form,
                          BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isNew", false);
            model.addAttribute("exerciseId", id);
            addReferenceData(model);
            return "admin-exercise-form";
        }
        exerciseService.update(id, toCommand(form));
        redirectAttributes.addFlashAttribute("exerciseSaved", true);
        return "redirect:/admin/exercises";
    }

    private void addReferenceData(Model model) {
        model.addAttribute("allEquipment", equipmentRepository.findAll());
        model.addAttribute("muscleGroups", MuscleGroup.values());
        model.addAttribute("movementPatterns", MovementPattern.values());
        model.addAttribute("difficultyLevels", DifficultyLevel.values());
        model.addAttribute("exerciseTypes", ExerciseType.values());
    }

    private ExerciseForm toForm(Exercise exercise) {
        ExerciseForm form = new ExerciseForm();
        form.setName(exercise.getName());
        form.setDescription(exercise.getDescription());
        form.setPrimaryMuscleGroup(exercise.getPrimaryMuscleGroup());
        form.setMovementPattern(exercise.getMovementPattern());
        form.setDifficultyLevel(exercise.getDifficultyLevel());
        form.setExerciseType(exercise.getExerciseType());
        form.setInstructions(exercise.getInstructions());
        form.setVideoUrl(exercise.getVideoUrl());
        form.setEquipmentIds(exercise.getRequiredEquipment().stream()
                .map(Equipment::getId).collect(Collectors.toCollection(ArrayList::new)));
        return form;
    }

    // Deliberately kept in the web layer — Command types live in
    // application.service, which must not depend on web-layer form classes
    // (same rationale as ProfileViewController.toCommand).
    private ExerciseUpsertCommand toCommand(ExerciseForm form) {
        return new ExerciseUpsertCommand(
                form.getName(),
                form.getDescription(),
                form.getPrimaryMuscleGroup(),
                form.getMovementPattern(),
                form.getDifficultyLevel(),
                form.getExerciseType(),
                form.getInstructions(),
                form.getVideoUrl(),
                Set.copyOf(form.getEquipmentIds())
        );
    }
}
