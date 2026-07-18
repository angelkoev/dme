package com.akoev.dme.web.mvc;

import com.akoev.dme.AbstractIntegrationTest;
import com.akoev.dme.application.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class WebLoginFlowTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Test
    void loginPageIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void homePageIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void formLoginWithValidCredentialsRedirectsHome() throws Exception {
        authService.register("web.user", "web.user@example.com", "WebPassword1");

        mockMvc.perform(SecurityMockMvcRequestBuilders.formLogin().user("web.user").password("WebPassword1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void formLoginWithInvalidCredentialsRedirectsToLoginError() throws Exception {
        mockMvc.perform(SecurityMockMvcRequestBuilders.formLogin().user("nope").password("nope"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}
