package com.akoev.dme.web.mvc;

import com.akoev.dme.application.service.ProfileUpdateCommand;
import com.akoev.dme.application.service.UserProfileService;
import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.ExperienceLevel;
import com.akoev.dme.domain.model.Exercise;
import com.akoev.dme.domain.model.Location;
import com.akoev.dme.domain.model.MuscleGroup;
import com.akoev.dme.domain.model.Sex;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.UserProfile;
import com.akoev.dme.domain.repository.EquipmentRepository;
import com.akoev.dme.domain.repository.ExerciseRepository;
import com.akoev.dme.infrastructure.security.CustomUserDetails;
import com.akoev.dme.web.mvc.form.ProfileForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProfileViewController {

    private static final int BLANK_LIMITATION_ROWS = 3;

    private final UserProfileService userProfileService;
    private final EquipmentRepository equipmentRepository;
    private final ExerciseRepository exerciseRepository;

    @GetMapping("/profile")
    public String view(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
        ProfileForm form;
        boolean hasProfile;
        try {
            form = toForm(userProfileService.getProfile(principal.getId()));
            hasProfile = true;
        } catch (ResponseStatusException ex) {
            form = new ProfileForm();
            hasProfile = false;
        }
        padLimitationRows(form);

        model.addAttribute("profileForm", form);
        model.addAttribute("hasProfile", hasProfile);
        addReferenceData(model);
        return "profile";
    }

    @PostMapping("/profile")
    public String update(@Valid @ModelAttribute("profileForm") ProfileForm form, BindingResult bindingResult,
                          Model model, @AuthenticationPrincipal CustomUserDetails principal,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addReferenceData(model);
            return "profile";
        }
        userProfileService.updateProfile(principal.getId(), toCommand(form));
        redirectAttributes.addFlashAttribute("profileSaved", true);
        return "redirect:/profile";
    }

    private void addReferenceData(Model model) {
        model.addAttribute("allEquipment", equipmentRepository.findAll());
        model.addAttribute("allExercises", exerciseRepository.findAll());
        model.addAttribute("experienceLevels", ExperienceLevel.values());
        model.addAttribute("trainingGoals", TrainingGoal.values());
        model.addAttribute("locations", Location.values());
        model.addAttribute("sexes", Sex.values());
        model.addAttribute("muscleGroups", MuscleGroup.values());
        model.addAttribute("daysOfWeek", DayOfWeek.values());
    }

    private void padLimitationRows(ProfileForm form) {
        for (int i = 0; i < BLANK_LIMITATION_ROWS; i++) {
            form.getLimitations().add(new ProfileForm.LimitationRow());
        }
    }

    private ProfileForm toForm(UserProfile profile) {
        ProfileForm form = new ProfileForm();
        form.setBirthDate(profile.getBirthDate());
        form.setSex(profile.getSex());
        form.setHeightCm(profile.getHeightCm());
        form.setWeightKg(profile.getWeightKg());
        form.setExperienceLevel(profile.getExperienceLevel());
        form.setPrimaryGoal(profile.getPrimaryGoal());
        form.setDaysPerWeek(profile.getDaysPerWeek());
        form.setSessionDurationMinutes(profile.getSessionDurationMinutes());
        form.setNotes(profile.getNotes());
        form.setLocation(profile.getLocation());
        form.setEquipmentIds(profile.getAvailableEquipment().stream()
                .map(Equipment::getId).collect(Collectors.toCollection(ArrayList::new)));
        form.setFavoriteExerciseIds(profile.getFavoriteExercises().stream()
                .map(Exercise::getId).collect(Collectors.toCollection(ArrayList::new)));
        form.setDislikedExerciseIds(profile.getDislikedExercises().stream()
                .map(Exercise::getId).collect(Collectors.toCollection(ArrayList::new)));
        form.setPreferredCategories(new ArrayList<>(profile.getPreferredCategories()));
        form.setUnwantedCategories(new ArrayList<>(profile.getUnwantedCategories()));
        form.setRestDays(new ArrayList<>(profile.getRestDays()));

        List<ProfileForm.LimitationRow> rows = profile.getLimitations().stream()
                .map(limitation -> {
                    ProfileForm.LimitationRow row = new ProfileForm.LimitationRow();
                    row.setMuscleGroup(limitation.getMuscleGroup());
                    row.setNote(limitation.getNote());
                    return row;
                })
                .collect(Collectors.toCollection(ArrayList::new));
        form.setLimitations(rows);
        return form;
    }

    // Deliberately kept in the web layer, not a static factory on
    // ProfileUpdateCommand itself — same layering rationale as
    // ProfileController.toCommand (the REST controller): Command types live
    // in application.service, which must not depend on web-layer form/DTO
    // classes.
    private ProfileUpdateCommand toCommand(ProfileForm form) {
        List<ProfileUpdateCommand.LimitationCommand> limitations = form.getLimitations().stream()
                .filter(row -> row.getNote() != null && !row.getNote().isBlank())
                .map(row -> new ProfileUpdateCommand.LimitationCommand(row.getMuscleGroup(), row.getNote().trim()))
                .toList();

        return new ProfileUpdateCommand(
                form.getBirthDate(),
                form.getSex(),
                form.getHeightCm(),
                form.getWeightKg(),
                form.getExperienceLevel(),
                form.getPrimaryGoal(),
                form.getDaysPerWeek(),
                form.getSessionDurationMinutes(),
                form.getNotes(),
                form.getLocation(),
                Set.copyOf(form.getEquipmentIds()),
                Set.copyOf(form.getFavoriteExerciseIds()),
                Set.copyOf(form.getDislikedExerciseIds()),
                Set.copyOf(form.getPreferredCategories()),
                Set.copyOf(form.getUnwantedCategories()),
                limitations,
                Set.copyOf(form.getRestDays())
        );
    }
}
