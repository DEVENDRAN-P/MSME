package com.idbi.msme.controller;

import com.idbi.msme.dto.IngestSummaryResponse;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.Business;
import com.idbi.msme.repository.BusinessRepository;
import com.idbi.msme.security.CustomUserDetails;
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
@RequestMapping("/data-ingest")
public class DataIngestController {

    private static final Logger logger = LoggerFactory.getLogger(DataIngestController.class);

    private final DataIngestService dataIngestService;
    private final BusinessRepository businessRepository;
    private final ConsentService consentService;

    public DataIngestController(
            DataIngestService dataIngestService,
            BusinessRepository businessRepository,
            ConsentService consentService) {
        this.dataIngestService = dataIngestService;
        this.businessRepository = businessRepository;
        this.consentService = consentService;
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<IngestSummaryResponse> syncData(
            @RequestParam(defaultValue = "ALL") String streamType,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Alternate data sync triggered for owner: {} | Stream: {}", userDetails.getUsername(), streamType);
        
        Business business = businessRepository.findByOwnerId(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Please register your business profile before syncing alternate data."));
        
        IngestSummaryResponse response = dataIngestService.syncAlternateData(business.getId(), streamType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<IngestSummaryResponse> getMySummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Fetching alternate data summary for owner: {}", userDetails.getUsername());
        
        Business business = businessRepository.findByOwnerId(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Please register your business profile to view alternate data."));
        
        IngestSummaryResponse response = dataIngestService.getIngestSummary(business.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/{businessId}")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<IngestSummaryResponse> getBusinessSummary(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Underwriter {} querying alternate data summary for business ID: {}", userDetails.getUsername(), businessId);

        // Verify active consent for Loan Officers and Credit Managers
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !consentService.hasActiveConsent(businessId, userDetails.getId())) {
            throw new AccessDeniedException("Access denied: You do not have approved consent to view alternate data for this business.");
        }

        IngestSummaryResponse response = dataIngestService.getIngestSummary(businessId);
        return ResponseEntity.ok(response);
    }
}
