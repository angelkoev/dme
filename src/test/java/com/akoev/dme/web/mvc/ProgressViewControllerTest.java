package com.akoev.dme.web.mvc;

import com.akoev.dme.AbstractIntegrationTest;
import com.akoev.dme.application.service.AuthService;
import com.akoev.dme.application.service.CompleteSessionCommand;
import com.akoev.dme.application.service.ProfileUpdateCommand;
import com.akoev.dme.application.service.UserProfileService;
import com.akoev.dme.application.service.WorkoutPlanService;
import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.ExperienceLevel;
import com.akoev.dme.domain.model.Location;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.model.WorkoutPlan;
import com.akoev.dme.domain.repository.EquipmentRepository;
import com.akoev.dme.domain.repository.UserRepository;
import com.akoev.dme.infrastructure.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ProgressViewControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private WorkoutPlanService workoutPlanService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Test
    void progressPageRendersStreakAndPersonalRecordsAfterCompletingASession() throws Exception {
        authService.register("progress.render", "progress.render@example.com", "ProgressPass1");
        User user = userRepository.findByUsername("progress.render").orElseThrow();

        Long bodyweightId = equipmentRepository.findAll().stream()
                .filter(e -> e.getName().equals("Bodyweight"))
                .map(Equipment::getId)
                .findFirst().orElseThrow();

        userProfileService.updateProfile(user.getId(), new ProfileUpdateCommand(
                null, null, null, null,
                ExperienceLevel.INTERMEDIATE, TrainingGoal.HYPERTROPHY, 3, 60,
                null, Location.GYM,
                Set.of(bodyweightId), Set.of(), Set.of(), Set.of(), Set.of(), List.of(), Set.of()));

        WorkoutPlan plan = workoutPlanService.generate(user.getId(), null).plan();
        Long firstExerciseId = plan.getSessions().get(0).getExercises().get(0).getExercise().getId();

        workoutPlanService.completeSession(user.getId(), plan.getId(), plan.getSessions().get(0).getId(),
                new CompleteSessionCommand(90, 5, 7, "Great session",
                        List.of(new CompleteSessionCommand.ExercisePerformanceCommand(
                                firstExerciseId, new BigDecimal("50.0"), 10))));

        CustomUserDetails principal = new CustomUserDetails(userRepository.findByUsername("progress.render").orElseThrow());
        mockMvc.perform(get("/progress").with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Текуща поредица")))
                .andExpect(content().string(containsString("Лични рекорди")));
    }
}
