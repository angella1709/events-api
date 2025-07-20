package com.example.angella.eventsapi.security;

import com.example.angella.eventsapi.TestConfig;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class SecurityIT extends TestConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void accessPublicEndpoint_ShouldBePermitted() throws Exception {
        mockMvc.perform(get("/api/v1/public/event"))
                .andExpect(status().isOk());
    }

    @Test
    void accessProtectedEndpointWithoutAuth_ShouldBeDenied() throws Exception {
        mockMvc.perform(get("/api/v1/event"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void accessProtectedEndpointWithAuth_ShouldBePermitted() throws Exception {
        mockMvc.perform(get("/api/v1/event"))
                .andExpect(status().isOk());
    }
}