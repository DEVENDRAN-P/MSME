package com.idbi.msme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idbi.msme.dto.AuthResponse;
import com.idbi.msme.dto.LoginRequest;
import com.idbi.msme.dto.RegisterRequest;
import com.idbi.msme.dto.UserProfileResponse;
import com.idbi.msme.model.UserRole;
import com.idbi.msme.model.UserStatus;
import com.idbi.msme.security.CustomUserDetailsService;
import com.idbi.msme.security.JwtAuthenticationFilter;
import com.idbi.msme.security.JwtTokenProvider;
import com.idbi.msme.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disables spring security filters for unit test convenience
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void testRegisterUser_Success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "test@idbi.com",
                "password123",
                "John Doe",
                UserRole.ROLE_MSME,
                "9876543210"
        );

        UUID userId = UUID.randomUUID();
        UserProfileResponse response = new UserProfileResponse(
                userId,
                "test@idbi.com",
                "John Doe",
                UserRole.ROLE_MSME,
                "9876543210",
                UserStatus.ACTIVE
        );

        Mockito.when(authService.registerUser(any(RegisterRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@idbi.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.role").value("ROLE_MSME"));
    }

    @Test
    public void testRegisterUser_ValidationFailure() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest(
                "invalid-email",
                "short", // min is 6
                "",      // blank
                null,    // role required
                "123"
        );

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.role").exists());
    }

    @Test
    public void testLoginUser_Success() throws Exception {
        LoginRequest request = new LoginRequest("test@idbi.com", "password123");
        UUID userId = UUID.randomUUID();
        AuthResponse response = new AuthResponse(
                "mock-jwt-token",
                userId,
                "test@idbi.com",
                "John Doe",
                UserRole.ROLE_MSME.name()
        );

        Mockito.when(authService.loginUser(any(LoginRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.email").value("test@idbi.com"))
                .andExpect(jsonPath("$.role").value("ROLE_MSME"));
    }

    @Test
    public void testLoginUser_BadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("test@idbi.com", "wrong-password");

        Mockito.when(authService.loginUser(any(LoginRequest.class), any()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
