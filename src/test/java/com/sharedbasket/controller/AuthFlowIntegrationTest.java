package com.sharedbasket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharedbasket.dto.LoginRequestDTO;
import com.sharedbasket.dto.ProfileUpdateRequestDTO;
import com.sharedbasket.dto.RegisterRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void registerAndLoginAndProfileFlowWorks() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("phase2@test.local");
        registerRequest.setUsername("phase2user");
        registerRequest.setPassword("secret123");
        registerRequest.setFirstName("Phase");
        registerRequest.setLastName("Two");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("phase2@test.local"))
            .andExpect(jsonPath("$.fullName").value("Phase Two"));

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("phase2@test.local");
        loginRequest.setPassword("secret123");

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/auth/login")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("phase2@test.local"));

        mockMvc.perform(get("/api/auth/me").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("phase2@test.local"));

        ProfileUpdateRequestDTO profileUpdate = new ProfileUpdateRequestDTO();
        profileUpdate.setFirstName("Updated");
        profileUpdate.setLastName("Name");
        profileUpdate.setUsername("updateduser");

        mockMvc.perform(put("/api/profile")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdate)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Updated Name"));
    }

    @Test
    public void duplicateRegistrationIsRejected() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("dup@test.local");
        request.setUsername("dupuser");
        request.setPassword("secret123");
        request.setFirstName("Dup");
        request.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(content().string(containsString("already exists")));
    }
}
