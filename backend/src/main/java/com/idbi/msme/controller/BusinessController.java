package com.idbi.msme.controller;

import com.idbi.msme.dto.BusinessResponse;
import com.idbi.msme.dto.RegisterBusinessRequest;
import com.idbi.msme.security.CustomUserDetails;
import com.idbi.msme.service.BusinessService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/business")
public class BusinessController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessController.class);

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<BusinessResponse> registerBusiness(
            @Valid @RequestBody RegisterBusinessRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Registering business for owner: {}", userDetails.getUsername());
        BusinessResponse response = businessService.registerBusiness(request, userDetails.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my-business")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<BusinessResponse> getMyBusiness(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Retrieving business details for owner: {}", userDetails.getUsername());
        BusinessResponse response = businessService.getBusinessByOwner(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<BusinessResponse> getBusinessById(
            @PathVariable UUID id) {
        logger.info("Underwriter querying business profile by ID: {}", id);
        BusinessResponse response = businessService.getBusinessById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER')")
    public ResponseEntity<java.util.List<BusinessResponse>> getAllBusinesses() {
        logger.info("Underwriter querying all registered business profiles");
        java.util.List<BusinessResponse> response = businessService.getAllBusinesses();
        return ResponseEntity.ok(response);
    }
}

