package com.akoev.dme.web.api;

import com.akoev.dme.AbstractIntegrationTest;
import com.akoev.dme.application.service.AuthService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ProfileControllerTest extends AbstractIntegrationTest {

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
    void getProfileReturns404BeforeCreation() throws Exception {
        String token = issueTokenForNewUser("profile.new");

        mockMvc.perform(get("/api/v1/profile/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void putThenGetProfileRoundTrips() throws Exception {
        String token = issueTokenForNewUser("profile.full");

        String equipmentJson = mockMvc.perform(get("/api/v1/equipment"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        java.util.List<Integer> dumbbellIds = JsonPath.read(equipmentJson, "$[?(@.name=='Dumbbell')].id");
        Integer dumbbellId = dumbbellIds.get(0);

        String updateBody = """
                {
                  "experienceLevel":"INTERMEDIATE",
                  "primaryGoal":"HYPERTROPHY",
                  "daysPerWeek":4,
                  "sessionDurationMinutes":45,
                  "location":"GYM",
                  "equipmentIds":[%d],
                  "favoriteExerciseIds":[],
                  "dislikedExerciseIds":[],
                  "preferredCategories":["BACK"],
                  "unwantedCategories":["CALVES"],
                  "limitations":[{"muscleGroup":"SHOULDERS","note":"Old shoulder injury"}],
                  "restDays":["SATURDAY","SUNDAY"]
                }
                """.formatted(dumbbellId);

        mockMvc.perform(put("/api/v1/profile/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryGoal").value("HYPERTROPHY"))
                .andExpect(jsonPath("$.daysPerWeek").value(4))
                .andExpect(jsonPath("$.availableEquipment[0].name").value("Dumbbell"))
                .andExpect(jsonPath("$.limitations[0].muscleGroup").value("SHOULDERS"));

        mockMvc.perform(get("/api/v1/profile/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("GYM"))
                .andExpect(jsonPath("$.preferredCategories[0]").value("BACK"));
    }
}
