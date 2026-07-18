package com.akoev.dme.web.api;

import com.akoev.dme.AbstractIntegrationTest;
import com.akoev.dme.application.service.AuthService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class WorkoutPlanControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    private String issueTokenForNewUser(String username) throws Exception {
        authService.register(username, username + "@example.com", "SecurePass1");
        String loginBody = """
                {"username":"%s","password":"SecurePass1"}
                """.formatted(username);
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private List<Integer> equipmentIdsByNames(String... names) throws Exception {
        String equipmentJson = mockMvc.perform(get("/api/v1/equipment"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String namesFilter = java.util.Arrays.stream(names)
                .map(n -> "@.name=='" + n + "'")
                .reduce((a, b) -> a + " || " + b)
                .orElseThrow();
        return JsonPath.read(equipmentJson, "$[?(" + namesFilter + ")].id");
    }

    private void createWellEquippedProfile(String token) throws Exception {
        List<Integer> equipmentIds = equipmentIdsByNames("Bodyweight", "Barbell", "Bench", "Dumbbell", "Pull-up Bar", "Kettlebell");
        String updateBody = """
                {
                  "experienceLevel":"INTERMEDIATE",
                  "primaryGoal":"HYPERTROPHY",
                  "daysPerWeek":3,
                  "sessionDurationMinutes":60,
                  "location":"GYM",
                  "equipmentIds":%s
                }
                """.formatted(equipmentIds);

        mockMvc.perform(put("/api/v1/profile/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());
    }

    @Test
    void generateViewAndCompleteSessionClosesTheFeedbackLoop() throws Exception {
        String token = issueTokenForNewUser("workout.flow");
        createWellEquippedProfile(token);

        String generateResponse = mockMvc.perform(post("/api/v1/workout-plans/generate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan.goal").value("HYPERTROPHY"))
                .andExpect(jsonPath("$.plan.sessions").isNotEmpty())
                .andExpect(jsonPath("$.explanation").isNotEmpty())
                .andExpect(jsonPath("$.motivationalMessage").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        Integer planId = JsonPath.read(generateResponse, "$.plan.id");
        Integer firstSessionId = JsonPath.read(generateResponse, "$.plan.sessions[0].id");

        mockMvc.perform(get("/api/v1/workout-plans/active").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planId));

        String completeBody = """
                {"completionPercentage":90,"rating":5,"perceivedIntensity":7,"notes":"Felt great"}
                """;
        mockMvc.perform(post("/api/v1/workout-plans/%d/sessions/%d/complete".formatted(planId, firstSessionId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completionPercentage").value(90))
                .andExpect(jsonPath("$.currentStreak").value(1))
                .andExpect(jsonPath("$.longestStreak").value(1));
    }

    @Test
    void anotherUserCannotAccessSomeoneElsesPlan() throws Exception {
        String ownerToken = issueTokenForNewUser("plan.owner.api");
        createWellEquippedProfile(ownerToken);
        String generateResponse = mockMvc.perform(post("/api/v1/workout-plans/generate")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Integer planId = JsonPath.read(generateResponse, "$.plan.id");

        String otherToken = issueTokenForNewUser("plan.intruder");
        mockMvc.perform(get("/api/v1/workout-plans/" + planId).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void generateWithoutProfileFails() throws Exception {
        String token = issueTokenForNewUser("no.profile.plan");

        mockMvc.perform(post("/api/v1/workout-plans/generate").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }
}
