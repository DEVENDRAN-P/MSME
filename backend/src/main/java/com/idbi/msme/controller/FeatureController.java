package com.idbi.msme.controller;

import com.idbi.msme.dto.FeatureResponse;
import com.idbi.msme.dto.IngestSummaryResponse;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.Business;
import com.idbi.msme.repository.BusinessRepository;
import com.idbi.msme.security.CustomUserDetails;
import com.idbi.msme.client.AiServiceClient;
import com.idbi.msme.service.ConsentService;
import com.idbi.msme.service.DataIngestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/features")
public class FeatureController {

    private static final Logger logger = LoggerFactory.getLogger(FeatureController.class);

    private final DataIngestService dataIngestService;
    private final BusinessRepository businessRepository;
    private final AiServiceClient aiServiceClient;
    private final ConsentService consentService;

    public FeatureController(
            DataIngestService dataIngestService,
            BusinessRepository businessRepository,
            AiServiceClient aiServiceClient,
            ConsentService consentService) {
        this.dataIngestService = dataIngestService;
        this.businessRepository = businessRepository;
        this.aiServiceClient = aiServiceClient;
        this.consentService = consentService;
    }

    @GetMapping("/my-features")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<FeatureResponse> getMyFeatures(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Computing credit features for owner: {}", userDetails.getUsername());
        
        Business business = businessRepository.findByOwnerId(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Please register your business profile to compute credit features."));
        
        IngestSummaryResponse rawData = dataIngestService.getIngestSummary(business.getId());
        FeatureResponse response = aiServiceClient.extractFeatures(rawData);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{businessId}")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<FeatureResponse> getBusinessFeatures(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Underwriter {} querying credit features for business ID: {}", userDetails.getUsername(), businessId);

        // Verify active consent for Loan Officers and Credit Managers (Admins can inspect for diagnostics)
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !consentService.hasActiveConsent(businessId, userDetails.getId())) {
            throw new AccessDeniedException("Access denied: You do not have approved consent to view credit details for this business.");
        }
        
        IngestSummaryResponse rawData = dataIngestService.getIngestSummary(businessId);
        FeatureResponse response = aiServiceClient.extractFeatures(rawData);
        
        return ResponseEntity.ok(response);
    }
}
