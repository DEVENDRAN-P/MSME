package com.idbi.msme.controller;

import com.idbi.msme.dto.ConsentRequestDto;
import com.idbi.msme.dto.ConsentResponseDto;
import com.idbi.msme.model.User;
import com.idbi.msme.security.CustomUserDetails;
import com.idbi.msme.service.ConsentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/consents")
public class ConsentController {

    private static final Logger logger = LoggerFactory.getLogger(ConsentController.class);

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER')")
    public ResponseEntity<ConsentResponseDto> requestConsent(
            @RequestBody ConsentRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Lender: {} requesting consent for business ID: {}", userDetails.getUsername(), request.getBusinessId());
        
        User lender = new User();
        lender.setId(userDetails.getId());
        lender.setFullName(userDetails.getFullName());
        lender.setEmail(userDetails.getUsername());

        ConsentResponseDto response = consentService.requestConsent(request, lender);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-pending")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<List<ConsentResponseDto>> getMyPendingConsents(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Fetching pending consents for MSME: {}", userDetails.getUsername());
        List<ConsentResponseDto> consents = consentService.getMyPendingConsents(userDetails.getId());
        return ResponseEntity.ok(consents);
    }

    @GetMapping("/my-all")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<List<ConsentResponseDto>> getMyAllConsents(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Fetching all consents history for MSME: {}", userDetails.getUsername());
        List<ConsentResponseDto> consents = consentService.getAllMyConsents(userDetails.getId());
        return ResponseEntity.ok(consents);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<ConsentResponseDto> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Updating consent ID: {} status to: {} by owner: {}", id, status, userDetails.getUsername());
        ConsentResponseDto response = consentService.updateConsentStatus(id, status, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lender-requests")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER')")
    public ResponseEntity<List<ConsentResponseDto>> getLenderRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Fetching requested consents for lender: {}", userDetails.getUsername());
        List<ConsentResponseDto> consents = consentService.getLenderRequestedConsents(userDetails.getId());
        return ResponseEntity.ok(consents);
    }

    @GetMapping("/check/{businessId}")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER')")
    public ResponseEntity<Boolean> checkConsent(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean active = consentService.hasActiveConsent(businessId, userDetails.getId());
        logger.info("Checking consent for business ID: {} by lender: {} | Result: {}", businessId, userDetails.getUsername(), active);
        return ResponseEntity.ok(active);
    }
}
