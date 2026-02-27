package com.example.pos.controller;

import com.example.pos.dto.LoginRequest;
import com.example.pos.dto.SignupRequest;
import com.example.pos.entity.Role;
import com.example.pos.repository.RoleRepository;
import com.example.pos.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "JWT_SECRET=test-secret-key-for-testing-only-32-characters!",
        "ACCESS_TOKEN_EXPIRY_SECONDS=900",
        "REFRESH_TOKEN_EXPIRY_DAYS=30",
        "FRONTEND_ORIGIN=http://localhost:5173"
})
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Create roles
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        }
        if (roleRepository.findByName("ROLE_MANAGER").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_MANAGER").build());
        }
    }

    @Test
    void testSignupAndLogin() throws Exception {
        // 1. Signup
        SignupRequest signupReq = new SignupRequest();
        signupReq.setUsername("newuser");
        signupReq.setEmail("newuser@example.com");
        signupReq.setPassword("SecurePassword123!");

        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.email", is("newuser@example.com")))
                .andReturn();

        // 2. Login
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsernameOrEmail("newuser");
        loginReq.setPassword("SecurePassword123!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", notNullValue()))
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // 3. Use access token to get /api/users/me (not yet implemented, but simulate protected)
        mockMvc.perform(get("/api/health")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginWithEmail() throws Exception {
        // Signup
        SignupRequest signupReq = new SignupRequest();
        signupReq.setUsername("emailtest");
        signupReq.setEmail("emailtest@example.com");
        signupReq.setPassword("SecurePassword123!");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupReq)))
                .andExpect(status().isCreated());

        // Login with email
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsernameOrEmail("emailtest@example.com");
        loginReq.setPassword("SecurePassword123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));
    }

    @Test
    void testUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDuplicateUsernameRejected() throws Exception {
        SignupRequest req1 = new SignupRequest();
        req1.setUsername("duplicate");
        req1.setEmail("user1@example.com");
        req1.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        SignupRequest req2 = new SignupRequest();
        req2.setUsername("duplicate");
        req2.setEmail("user2@example.com");
        req2.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest());
    }
}
