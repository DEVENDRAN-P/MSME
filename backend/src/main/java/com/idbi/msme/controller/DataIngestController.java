package com.idbi.msme.controller;

import com.idbi.msme.dto.IngestSummaryResponse;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.BusinessProfile;
import com.idbi.msme.repository.FirestoreDataAccess;
import com.idbi.msme.security.FirebaseUserPrincipal;
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
@RequestMapping("/data-ingest")
public class DataIngestController {

    private static final Logger logger = LoggerFactory.getLogger(DataIngestController.class);

    private final DataIngestService dataIngestService;
    private final FirestoreDataAccess db;
    private final ConsentService consentService;

    public DataIngestController(
            DataIngestService dataIngestService,
            FirestoreDataAccess db,
            ConsentService consentService) {
        this.dataIngestService = dataIngestService;
        this.db = db;
        this.consentService = consentService;
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<IngestSummaryResponse> syncData(
            @RequestParam(defaultValue = "ALL") String streamType,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        logger.info("Alternate data sync triggered for owner: {} | Stream: {}", principal.getEmail(), streamType);

        BusinessProfile business = db.findBusinessByOwnerId(principal.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Please register your business profile before syncing alternate data."));

        IngestSummaryResponse response = dataIngestService.syncAlternateData(business.getId(), streamType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<IngestSummaryResponse> getMySummary(
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        logger.info("Fetching alternate data summary for owner: {}", principal.getEmail());

        BusinessProfile business = db.findBusinessByOwnerId(principal.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Please register your business profile to view alternate data."));

        IngestSummaryResponse response = dataIngestService.getIngestSummary(business.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/{businessId}")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<IngestSummaryResponse> getBusinessSummary(
            @PathVariable String businessId,
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        logger.info("Underwriter {} querying alternate data summary for business ID: {}", principal.getEmail(), businessId);

        boolean isAdmin = principal.getRole().equals("ROLE_ADMIN");
        if (!isAdmin && !consentService.hasActiveConsent(businessId, principal.getUid())) {
            throw new AccessDeniedException("Access denied: You do not have approved consent to view alternate data for this business.");
        }

        IngestSummaryResponse response = dataIngestService.getIngestSummary(businessId);
        return ResponseEntity.ok(response);
    }
}
