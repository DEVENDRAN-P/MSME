package com.idbi.msme.service;

import com.idbi.msme.dto.AuthResponse;
import com.idbi.msme.dto.LoginRequest;
import com.idbi.msme.dto.RegisterRequest;
import com.idbi.msme.dto.UserProfileResponse;
import com.idbi.msme.exception.ConflictException;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.AuditLog;
import com.idbi.msme.model.User;
import com.idbi.msme.model.UserStatus;
import com.idbi.msme.repository.AuditLogRepository;
import com.idbi.msme.repository.UserRepository;
import com.idbi.msme.security.CustomUserDetails;
import com.idbi.msme.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(
            UserRepository userRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public UserProfileResponse registerUser(RegisterRequest request, HttpServletRequest servletRequest) {
        if (userRepository.existsByEmail(request.getEmail())) {
            writeAuditLog(null, "REGISTER_USER_FAIL_DUPLICATE_EMAIL", "FAILURE", servletRequest);
            throw new ConflictException("Email address is already in use: " + request.getEmail());
        }

        User user = new User(
                UUID.randomUUID(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getRole(),
                request.getPhone(),
                UserStatus.ACTIVE
        );

        User savedUser = userRepository.save(user);
        writeAuditLog(savedUser, "USER_REGISTER", "SUCCESS", servletRequest);

        return new UserProfileResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole(),
                savedUser.getPhone(),
                savedUser.getStatus()
        );
    }

    @Override
    @Transactional
    public AuthResponse loginUser(LoginRequest request, HttpServletRequest servletRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String jwt = jwtTokenProvider.generateToken(userDetails);

            User user = optionalUser.orElseThrow(() -> new ResourceNotFoundException("User not found"));
            writeAuditLog(user, "USER_LOGIN", "SUCCESS", servletRequest);

            return new AuthResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getFullName(),
                    userDetails.getAuthorities().iterator().next().getAuthority()
            );

        } catch (Exception ex) {
            User failedUser = optionalUser.orElse(null);
            writeAuditLog(failedUser, "USER_LOGIN_FAIL", "FAILURE", servletRequest);
            throw new BadCredentialsException("Invalid email or password", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found with ID: " + userId));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getPhone(),
                user.getStatus()
        );
    }

    @Override
    @Transactional
    public void writeAuditLog(User user, String action, String status, HttpServletRequest servletRequest) {
        String ipAddress = "UNKNOWN";
        String userAgent = "UNKNOWN";

        if (servletRequest != null) {
            ipAddress = servletRequest.getRemoteAddr();
            String xfHeader = servletRequest.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isEmpty()) {
                ipAddress = xfHeader.split(",")[0];
            }
            String ua = servletRequest.getHeader("User-Agent");
            if (ua != null) {
                userAgent = ua;
            }
        }

        AuditLog auditLog = new AuditLog(
                UUID.randomUUID(),
                user,
                action,
                ipAddress,
                userAgent,
                status
        );
        auditLogRepository.save(auditLog);
    }
}
