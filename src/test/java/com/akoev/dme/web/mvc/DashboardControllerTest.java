package com.akoev.dme.web.mvc;

import com.akoev.dme.AbstractIntegrationTest;
import com.akoev.dme.application.service.AuthService;
import com.akoev.dme.application.service.ProfileUpdateCommand;
import com.akoev.dme.application.service.UserProfileService;
import com.akoev.dme.application.service.WorkoutPlanService;
import com.akoev.dme.domain.model.Equipment;
import com.akoev.dme.domain.model.ExperienceLevel;
import com.akoev.dme.domain.model.Location;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.User;
import com.akoev.dme.domain.repository.EquipmentRepository;
import com.akoev.dme.domain.repository.UserRepository;
import com.akoev.dme.infrastructure.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression test: an earlier version of dashboard.html used "session" as a
 * th:each loop variable name, which Thymeleaf rejects as a reserved word
 * only once a real WorkoutPlan (with sessions) reaches the template — a gap
 * none of the other MockMvc tests exercised.
 */
@AutoConfigureMockMvc
class DashboardControllerTest extends AbstractIntegrationTest {

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
    void dashboardRendersAGeneratedPlanWithoutError() throws Exception {
        authService.register("dashboard.render", "dashboard.render@example.com", "DashPass1");
        User user = userRepository.findByUsername("dashboard.render").orElseThrow();

        Long bodyweightId = equipmentRepository.findAll().stream()
                .filter(e -> e.getName().equals("Bodyweight"))
                .map(Equipment::getId)
                .findFirst().orElseThrow();

        userProfileService.updateProfile(user.getId(), new ProfileUpdateCommand(
                null, null, null, null,
                ExperienceLevel.INTERMEDIATE, TrainingGoal.HYPERTROPHY, 3, 60,
                null, Location.GYM,
                Set.of(bodyweightId), Set.of(), Set.of(), Set.of(), Set.of(), List.of(), Set.of()));
        workoutPlanService.generate(user.getId(), null);

        CustomUserDetails principal = new CustomUserDetails(userRepository.findByUsername("dashboard.render").orElseThrow());
        mockMvc.perform(get("/dashboard").with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Active Plan")));
    }
}
