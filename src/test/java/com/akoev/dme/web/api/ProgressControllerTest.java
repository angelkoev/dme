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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ProgressControllerTest extends AbstractIntegrationTest {

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

    @Test
    void streakDefaultsToZeroForNewUser() throws Exception {
        String token = issueTokenForNewUser("progress.api.new");

        mockMvc.perform(get("/api/v1/me/streak").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreak").value(0))
                .andExpect(jsonPath("$.longestStreak").value(0));
    }

    @Test
    void streakAndHistoryReflectACompletedSession() throws Exception {
        String token = issueTokenForNewUser("progress.api.flow");

        List<Integer> equipmentIds = JsonPath.read(
                mockMvc.perform(get("/api/v1/equipment")).andReturn().getResponse().getContentAsString(),
                "$[?(@.name=='Bodyweight')].id");

        mockMvc.perform(put("/api/v1/profile/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"experienceLevel":"BEGINNER","primaryGoal":"GENERAL_FITNESS","daysPerWeek":3,
                                 "sessionDurationMinutes":45,"location":"HOME","equipmentIds":%s}
                                """.formatted(equipmentIds)))
                .andExpect(status().isOk());

        String generateResponse = mockMvc.perform(post("/api/v1/workout-plans/generate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Integer planId = JsonPath.read(generateResponse, "$.plan.id");
        Integer sessionId = JsonPath.read(generateResponse, "$.plan.sessions[0].id");

        mockMvc.perform(post("/api/v1/workout-plans/%d/sessions/%d/complete".formatted(planId, sessionId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"completionPercentage":100,"rating":4}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/me/streak").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreak").value(1));

        mockMvc.perform(get("/api/v1/me/workout-history").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].completionPercentage").value(100));
    }
}
