package com.idbi.msme.controller;

import com.idbi.msme.dto.UserProfileResponse;
import com.idbi.msme.security.FirebaseUserPrincipal;
import com.idbi.msme.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        logger.info("Profile request for user: {}", principal.getUid());
        UserProfileResponse response = authService.getUserProfile(principal.getUid());
        return ResponseEntity.ok(response);
    }
}
