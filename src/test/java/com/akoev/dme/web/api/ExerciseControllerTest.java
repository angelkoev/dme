package com.akoev.dme.web.api;

import com.akoev.dme.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ExerciseControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listAndGetAreAnonymouslyAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists());

        mockMvc.perform(get("/api/v1/exercises/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createIsForbiddenForRegularUser() throws Exception {
        mockMvc.perform(post("/api/v1/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test Exercise","primaryMuscleGroup":"CHEST","movementPattern":"PUSH",
                                 "difficultyLevel":"BEGINNER","exerciseType":"COMPOUND","equipmentIds":[]}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSucceedsForAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cable Crossover","primaryMuscleGroup":"CHEST","movementPattern":"PUSH",
                                 "difficultyLevel":"INTERMEDIATE","exerciseType":"ISOLATION","equipmentIds":[]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cable Crossover"));
    }

    @Test
    void createRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Anon Exercise","primaryMuscleGroup":"CHEST","movementPattern":"PUSH",
                                 "difficultyLevel":"BEGINNER","exerciseType":"COMPOUND","equipmentIds":[]}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
