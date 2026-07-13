package com.idbi.msme.controller;

import com.idbi.msme.dto.FeatureResponse;
import com.idbi.msme.dto.HealthCardResponse;
import com.idbi.msme.dto.IngestSummaryResponse;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.BusinessProfile;
import com.idbi.msme.repository.FirestoreDataAccess;
import com.idbi.msme.security.FirebaseUserPrincipal;
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

@RestController
@RequestMapping("/health-card")
public class HealthCardController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCardController.class);

    private final DataIngestService dataIngestService;
    private final FirestoreDataAccess db;
    private final AiServiceClient aiServiceClient;
    private final ConsentService consentService;

    public HealthCardController(
            DataIngestService dataIngestService,
            FirestoreDataAccess db,
            AiServiceClient aiServiceClient,
            ConsentService consentService) {
        this.dataIngestService = dataIngestService;
        this.db = db;
        this.aiServiceClient = aiServiceClient;
        this.consentService = consentService;
    }

    @GetMapping("/my-card")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<HealthCardResponse> getMyHealthCard(
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        logger.info("Computing financial health scorecard for owner: {}", principal.getEmail());

        BusinessProfile business = db.findBusinessByOwnerId(principal.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Please register your business profile to view your credit scorecard."));

        IngestSummaryResponse rawData = dataIngestService.getIngestSummary(business.getId());
        FeatureResponse features = aiServiceClient.extractFeatures(rawData);
        HealthCardResponse response = aiServiceClient.getCreditScore(features);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{businessId}")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<HealthCardResponse> getBusinessHealthCard(
            @PathVariable String businessId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        logger.info("Underwriter {} querying financial health scorecard for business ID: {}", principal.getEmail(), businessId);

        boolean isAdmin = principal.getRole().equals("ROLE_ADMIN");
        if (!isAdmin && !consentService.hasActiveConsent(businessId, principal.getUid())) {
            throw new AccessDeniedException("Access denied: You do not have approved consent to view credit scorecard for this business.");
        }

        IngestSummaryResponse rawData = dataIngestService.getIngestSummary(businessId);
        FeatureResponse features = aiServiceClient.extractFeatures(rawData);
        HealthCardResponse response = aiServiceClient.getCreditScore(features);

        return ResponseEntity.ok(response);
    }
}
