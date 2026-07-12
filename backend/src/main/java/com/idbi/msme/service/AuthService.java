package com.idbi.msme.service;

import com.idbi.msme.dto.AuthResponse;
import com.idbi.msme.dto.LoginRequest;
import com.idbi.msme.dto.RegisterRequest;
import com.idbi.msme.dto.UserProfileResponse;
import com.idbi.msme.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface AuthService {
    UserProfileResponse registerUser(RegisterRequest request, HttpServletRequest servletRequest);
    AuthResponse loginUser(LoginRequest request, HttpServletRequest servletRequest);
    UserProfileResponse getUserProfile(UUID userId);
    void writeAuditLog(User user, String action, String status, HttpServletRequest servletRequest);
}
