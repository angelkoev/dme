package com.akoev.dme.web.mvc;

import com.akoev.dme.AbstractIntegrationTest;
import com.akoev.dme.application.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class WebRegisterFlowTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Test
    void registerPageIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    void submittingValidRegistrationRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "web.register.user")
                        .param("email", "web.register.user@example.com")
                        .param("password", "WebPassword1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    @Test
    void submittingWithTakenUsernameRedisplaysFormWithError() throws Exception {
        authService.register("web.duplicate", "web.duplicate@example.com", "WebPassword1");

        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "web.duplicate")
                        .param("email", "someone.else@example.com")
                        .param("password", "AnotherPass1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Username already taken")));
    }

    @Test
    void submittingWithTooShortPasswordRedisplaysFormWithValidationError() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "web.shortpass")
                        .param("email", "web.shortpass@example.com")
                        .param("password", "short"))
                .andExpect(status().isOk());
    }
}
