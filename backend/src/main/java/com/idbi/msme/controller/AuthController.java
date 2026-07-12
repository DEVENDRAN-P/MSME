package com.idbi.msme.controller;

import com.idbi.msme.dto.AuthResponse;
import com.idbi.msme.dto.LoginRequest;
import com.idbi.msme.dto.RegisterRequest;
import com.idbi.msme.dto.UserProfileResponse;
import com.idbi.msme.security.CustomUserDetails;
import com.idbi.msme.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> registerUser(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest) {
        logger.info("Register request received for email: {}", request.getEmail());
        UserProfileResponse response = authService.registerUser(request, servletRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        logger.info("Login request received for email: {}", request.getEmail());
        AuthResponse response = authService.loginUser(request, servletRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Profile request received for logged-in user: {}", userDetails.getUsername());
        UserProfileResponse response = authService.getUserProfile(userDetails.getId());
        return ResponseEntity.ok(response);
    }
}
