package com.idbi.msme.controller;

import com.idbi.msme.dto.ApproveLoanRequest;
import com.idbi.msme.dto.ForecastResponse;
import com.idbi.msme.dto.IngestSummaryResponse;
import com.idbi.msme.dto.LoanResponse;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.Business;
import com.idbi.msme.repository.BusinessRepository;
import com.idbi.msme.security.CustomUserDetails;
import com.idbi.msme.client.AiServiceClient;
import com.idbi.msme.service.ConsentService;
import com.idbi.msme.service.DataIngestService;
import com.idbi.msme.service.LoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

    private final LoanService loanService;
    private final ConsentService consentService;
    private final BusinessRepository businessRepository;
    private final DataIngestService dataIngestService;
    private final AiServiceClient aiServiceClient;

    public LoanController(
            LoanService loanService,
            ConsentService consentService,
            BusinessRepository businessRepository,
            DataIngestService dataIngestService,
            AiServiceClient aiServiceClient) {
        this.loanService = loanService;
        this.consentService = consentService;
        this.businessRepository = businessRepository;
        this.dataIngestService = dataIngestService;
        this.aiServiceClient = aiServiceClient;
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER')")
    public ResponseEntity<LoanResponse> approveLoan(
            @RequestBody ApproveLoanRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Underwriter {} approving loan for business ID: {} | Amount: {}", userDetails.getUsername(), request.getBusinessId(), request.getAmount());

        // Verify active consent before allowing loan approval
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !consentService.hasActiveConsent(request.getBusinessId(), userDetails.getId())) {
            throw new AccessDeniedException("Access denied: You do not have approved consent to underwrite loans for this business.");
        }

        LoanResponse response = loanService.approveLoan(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-loans")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<List<LoanResponse>> getMyLoans(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Fetching active loans for owner: {}", userDetails.getUsername());
        List<LoanResponse> loans = loanService.getMyLoans(userDetails.getId());
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER')")
    public ResponseEntity<List<LoanResponse>> getBusinessLoans(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Underwriter {} checking loan histories for business ID: {}", userDetails.getUsername(), businessId);

        // Verify active consent
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !consentService.hasActiveConsent(businessId, userDetails.getId())) {
            throw new AccessDeniedException("Access denied: You do not have approved consent to view loan data for this business.");
        }

        List<LoanResponse> loans = loanService.getBusinessLoans(businessId);
        return ResponseEntity.ok(loans);
    }

    @PostMapping("/simulate-forecast")
    @PreAuthorize("hasRole('ROLE_MSME')")
    public ResponseEntity<List<ForecastResponse>> simulateForecast(
            @RequestBody Map<String, Object> loanOptions,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("MSME {} simulating future cash flows forecast", userDetails.getUsername());

        Business business = businessRepository.findByOwnerId(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Please register your business profile to simulate forecasts."));

        IngestSummaryResponse rawData = dataIngestService.getIngestSummary(business.getId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("gstRecords", rawData.getGstRecords());
        payload.put("loan", loanOptions);

        List<ForecastResponse> forecast = aiServiceClient.getForecastProjections(payload);
        return ResponseEntity.ok(forecast);
    }

    @PostMapping("/simulate-forecast/{businessId}")
    @PreAuthorize("hasAnyRole('ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER')")
    public ResponseEntity<List<ForecastResponse>> simulateBusinessForecast(
            @PathVariable UUID businessId,
            @RequestBody Map<String, Object> loanOptions,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Underwriter {} simulating future cash flows forecast for business ID: {}", userDetails.getUsername(), businessId);

        // Verify active consent
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !consentService.hasActiveConsent(businessId, userDetails.getId())) {
            throw new AccessDeniedException("Access denied: You do not have approved consent to forecast cash flows for this business.");
        }

        IngestSummaryResponse rawData = dataIngestService.getIngestSummary(businessId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("gstRecords", rawData.getGstRecords());
        payload.put("loan", loanOptions);

        List<ForecastResponse> forecast = aiServiceClient.getForecastProjections(payload);
        return ResponseEntity.ok(forecast);
    }
}
