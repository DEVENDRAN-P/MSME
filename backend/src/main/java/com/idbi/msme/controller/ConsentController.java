package com.idbi.msme.controller;

import com.idbi.msme.dto.ConsentRequestDto;
import com.idbi.msme.dto.ConsentResponseDto;
import com.idbi.msme.security.FirebaseUserPrincipal;
import com.idbi.msme.service.ConsentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        logger.info("Lender: {} requesting consent for business ID: {}", principal.getEmail(), request.getBusinessId());

        ConsentResponseDto response = consentService.requestConsent(
                request, principal.getUid(), principal.getEmail(), principal.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-pending")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<List<ConsentResponseDto>> getMyPendingConsents(
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        logger.info("Fetching pending consents for MSME: {}", principal.getEmail());
        List<ConsentResponseDto> consents = consentService.getMyPendingConsents(principal.getUid());
        return ResponseEntity.ok(consents);
    }

    @GetMapping("/my-all")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<List<ConsentResponseDto>> getMyAllConsents(
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        logger.info("Fetching all consents history for MSME: {}", principal.getEmail());
        List<ConsentResponseDto> consents = consentService.getAllMyConsents(principal.getUid());
        return ResponseEntity.ok(consents);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<ConsentResponseDto> updateStatus(
            @PathVariable String id,
            @RequestParam String status,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        logger.info("Updating consent ID: {} status to: {} by owner: {}", id, status, principal.getEmail());
        ConsentResponseDto response = consentService.updateConsentStatus(id, status, principal.getUid());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lender-requests")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER')")
    public ResponseEntity<List<ConsentResponseDto>> getLenderRequests(
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        logger.info("Fetching requested consents for lender: {}", principal.getEmail());
        List<ConsentResponseDto> consents = consentService.getLenderRequestedConsents(principal.getUid());
        return ResponseEntity.ok(consents);
    }

    @GetMapping("/check/{businessId}")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER')")
    public ResponseEntity<Boolean> checkConsent(
            @PathVariable String businessId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        boolean active = consentService.hasActiveConsent(businessId, principal.getUid());
        logger.info("Checking consent for business ID: {} by lender: {} | Result: {}", businessId, principal.getEmail(), active);
        return ResponseEntity.ok(active);
    }
}
