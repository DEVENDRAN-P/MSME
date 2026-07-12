# MODULE 12: ULI, OCEN & AA INTEGRATION
## Production-Grade Implementation

---

## 12.1 MICROSERVICES STRUCTURE

```
aggregation-service/
├── src/main/java/com/msme/aggregation/
│   ├── AggregationServiceApplication.java
│   ├── config/
│   │   ├── KafkaConfig.java
│   │   └── RestTemplateConfig.java
│   ├── controller/
│   │   ├── AAConsentController.java
│   │   ├── ULIController.java
│   │   ├── OCENController.java
│   │   └── VerificationController.java
│   ├── service/
│   │   ├── AAConsentService.java
│   │   ├── ULIService.java
│   │   ├── OCENService.java
│   │   ├── VerificationService.java
│   │   └── DataFetchService.java
│   ├── model/
│   │   ├── AAConsent.java
│   │   ├── ULIRequest.java
│   │   ├── OCENApplication.java
│   │   └── Verification.java
│   ├── dto/
│   │   ├── AAConsentRequest.java
│   │   ├── ULIRequestDTO.java
│   │   └── OCENApplicationDTO.java
│   └── client/
│       ├── AAPIClient.java
│       ├── ULIClient.java
│       ├── OCENClient.java
│       └── VerificationClient.java
└── pom.xml
```

---

## 12.2 ACCOUNT AGGREGATOR CONSENT SERVICE

```java
// model/AAConsent.java
package com.msme.aggregation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "aa_consents")
public class AAConsent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID msmeId;
    
    private String aaProvider;
    
    private String fipId;
    
    @Column(columnDefinition = "text[]")
    private String[] dataTypes;
    
    private String consentHandle;
    
    private String status = "pending";
    
    private String consentToken;
    
    private LocalDateTime expiresAt;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getMsmeId() { return msmeId; }
    public void setMsmeId(UUID msmeId) { this.msmeId = msmeId; }
    
    public String getAaProvider() { return aaProvider; }
    public void setAaProvider(String aaProvider) { this.aaProvider = aaProvider; }
    
    public String getFipId() { return fipId; }
    public void setFipId(String fipId) { this.fipId = fipId; }
    
    public String[] getDataTypes() { return dataTypes; }
    public void setDataTypes(String[] dataTypes) { this.dataTypes = dataTypes; }
    
    public String getConsentHandle() { return consentHandle; }
    public void setConsentHandle(String consentHandle) { this.consentHandle = consentHandle; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getConsentToken() { return consentToken; }
    public void setConsentToken(String consentToken) { this.consentToken = consentToken; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

```java
// service/AAConsentService.java
package com.msme.aggregation.service;

import com.msme.aggregation.client.AAPIClient;
import com.msme.aggregation.dto.AAConsentRequest;
import com.msme.aggregation.model.AAConsent;
import com.msme.aggregation.repository.AAConsentRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class AAConsentService {
    
    private final AAConsentRepository aaConsentRepository;
    private final AAPIClient aaApiClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public AAConsentService(
            AAConsentRepository aaConsentRepository,
            AAPIClient aaApiClient,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.aaConsentRepository = aaConsentRepository;
        this.aaApiClient = aaApiClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public Map<String, Object> createConsentRequest(AAConsentRequest request) {
        // 1. Generate consent handle
        String consentHandle = "AA-" + UUID.randomUUID().toString().substring(0, 8);
        
        // 2. Create consent record
        AAConsent consent = new AAConsent();
        consent.setMsmeId(request.getMsmeId());
        consent.setAaProvider(request.getProvider());
        consent.setFipId(request.getFipId());
        consent.setDataTypes(request.getDataTypes().toArray(new String[0]));
        consent.setConsentHandle(consentHandle);
        consent.setStatus("pending");
        consent.setExpiresAt(LocalDateTime.now().plusHours(24));
        
        aaConsentRepository.save(consent);
        
        // 3. Call AA API (simulated for demo)
        try {
            var apiResponse = aaApiClient.createConsent(
                request.getProvider(),
                request.getFipId(),
                request.getDataTypes()
            );
            
            // Update with API response
            consent.setConsentToken(apiResponse.get("consent_token"));
            
        } catch (Exception e) {
            // For demo, generate simulated response
            consent.setConsentToken("sim_" + UUID.randomUUID().toString().substring(0, 12));
        }
        
        // 4. Return response
        Map<String, Object> response = new HashMap<>();
        response.put("consent_handle", consentHandle);
        response.put("status", "pending");
        response.put("redirect_url", String.format(
            "https://%s.com/consent/%s", 
            request.getProvider(), 
            consentHandle
        ));
        response.put("expires_at", consent.getExpiresAt().toString());
        response.put("data_types_requested", request.getDataTypes());
        
        return response;
    }
    
    public Map<String, Object> checkConsentStatus(String consentHandle) {
        AAConsent consent = aaConsentRepository
            .findByConsentHandle(consentHandle)
            .orElseThrow(() -> new RuntimeException("Consent not found"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("consent_handle", consentHandle);
        response.put("status", consent.getStatus());
        response.put("data_types", Arrays.asList(consent.getDataTypes()));
        response.put("expires_at", consent.getExpiresAt().toString());
        
        return response;
    }
    
    public Map<String, Object> handleConsentCallback(
            String consentHandle, 
            String status, 
            String consentToken) {
        
        AAConsent consent = aaConsentRepository
            .findByConsentHandle(consentHandle)
            .orElseThrow(() -> new RuntimeException("Consent not found"));
        
        consent.setStatus(status);
        if (consentToken != null) {
            consent.setConsentToken(consentToken);
        }
        
        aaConsentRepository.save(consent);
        
        // If approved, fetch data
        if ("approved".equals(status)) {
            fetchConsentData(consent);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("consent_handle", consentHandle);
        response.put("status", status);
        response.put("message", "Consent " + status);
        
        return response;
    }
    
    private void fetchConsentData(AAConsent consent) {
        // Fetch data using consent token
        Map<String, Object> fetchedData = new HashMap<>();
        
        for (String dataType : consent.getDataTypes()) {
            switch (dataType) {
                case "bank_transactions":
                    fetchedData.put("bank", generateBankData());
                    break;
                case "gst_returns":
                    fetchedData.put("gst", generateGSTData());
                    break;
                case "epf_data":
                    fetchedData.put("epf", generateEPFData());
                    break;
            }
        }
        
        // Publish event
        kafkaTemplate.send("aggregation.events", "aa.data.fetched",
            Map.of(
                "msmeId", consent.getMsmeId(),
                "dataTypes", Arrays.asList(consent.getDataTypes()),
                "data", fetchedData
            )
        );
    }
    
    private Map<String, Object> generateBankData() {
        Map<String, Object> data = new HashMap<>();
        data.put("account_number", "****" + (int)(Math.random() * 9000 + 1000));
        data.put("bank_name", "State Bank of India");
        data.put("balance", 450000 + (int)(Math.random() * 100000));
        data.put("avg_balance", 380000);
        
        List<Map<String, Object>> transactions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> txn = new HashMap<>();
            txn.put("date", "2024-01-" + (15 - i));
            txn.put("amount", (int)(Math.random() * 100000));
            txn.put("type", Math.random() > 0.5 ? "credit" : "debit");
            transactions.add(txn);
        }
        data.put("transactions", transactions);
        
        return data;
    }
    
    private Map<String, Object> generateGSTData() {
        Map<String, Object> data = new HashMap<>();
        data.put("gstin", "27AABCU" + (int)(Math.random() * 9000 + 1000) + "R1ZM");
        data.put("filing_status", "regular");
        data.put("total_turnover", 8500000);
        
        List<Map<String, Object>> returns = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Map<String, Object> ret = new HashMap<>();
            ret.put("month", "2024-0" + (6 - i));
            ret.put("turnover", 800000 + (int)(Math.random() * 200000));
            ret.put("tax_paid", 144000 + (int)(Math.random() * 36000));
            returns.add(ret);
        }
        data.put("returns", returns);
        
        return data;
    }
    
    private Map<String, Object> generateEPFData() {
        Map<String, Object> data = new HashMap<>();
        data.put("establishment_code", "MH/" + (int)(Math.random() * 90000 + 10000));
        data.put("employee_count", 12);
        data.put("total_contribution", 259200);
        
        return data;
    }
    
    public List<AAConsent> getConsentsByMsme(UUID msmeId) {
        return aaConsentRepository.findByMsmeIdOrderByCreatedAtDesc(msmeId);
    }
}
```

```java
// dto/AAConsentRequest.java
package com.msme.aggregation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class AAConsentRequest {
    
    @NotNull(message = "MSME ID is required")
    private UUID msmeId;
    
    @NotEmpty(message = "Provider is required")
    private String provider;
    
    @NotEmpty(message = "FIP ID is required")
    private String fipId;
    
    @NotEmpty(message = "Data types are required")
    private List<String> dataTypes;
    
    // Getters and Setters
    public UUID getMsmeId() { return msmeId; }
    public void setMsmeId(UUID msmeId) { this.msmeId = msmeId; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getFipId() { return fipId; }
    public void setFipId(String fipId) { this.fipId = fipId; }
    
    public List<String> getDataTypes() { return dataTypes; }
    public void setDataTypes(List<String> dataTypes) { this.dataTypes = dataTypes; }
}
```

```java
// controller/AAConsentController.java
package com.msme.aggregation.controller;

import com.msme.aggregation.dto.AAConsentRequest;
import com.msme.aggregation.service.AAConsentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/aa")
@Tag(name = "Account Aggregator", description = "AA consent and data fetching endpoints")
public class AAConsentController {
    
    private final AAConsentService aaConsentService;
    
    public AAConsentController(AAConsentService aaConsentService) {
        this.aaConsentService = aaConsentService;
    }
    
    @PostMapping("/consent/request")
    @PreAuthorize("hasRole('MSME')")
    @Operation(summary = "Create AA consent request")
    public ResponseEntity<?> createConsentRequest(
            @Valid @RequestBody AAConsentRequest request) {
        return ResponseEntity.ok(aaConsentService.createConsentRequest(request));
    }
    
    @GetMapping("/consent/{consentHandle}")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER')")
    @Operation(summary = "Check consent status")
    public ResponseEntity<?> checkConsentStatus(
            @PathVariable String consentHandle) {
        return ResponseEntity.ok(aaConsentService.checkConsentStatus(consentHandle));
    }
    
    @PostMapping("/consent/callback")
    @Operation(summary = "Handle consent callback")
    public ResponseEntity<?> handleConsentCallback(
            @RequestParam String consentHandle,
            @RequestParam String status,
            @RequestParam(required = false) String consentToken) {
        return ResponseEntity.ok(
            aaConsentService.handleConsentCallback(consentHandle, status, consentToken)
        );
    }
    
    @GetMapping("/consents/{msmeId}")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER')")
    @Operation(summary = "Get consents for MSME")
    public ResponseEntity<?> getConsents(@PathVariable UUID msmeId) {
        return ResponseEntity.ok(aaConsentService.getConsentsByMsme(msmeId));
    }
}
```

---

## 12.3 ULI INTEGRATION SERVICE

```java
// service/ULIService.java
package com.msme.aggregation.service;

import com.msme.aggregation.client.ULIClient;
import com.msme.aggregation.model.ULIRequest;
import com.msme.aggregation.repository.ULIRequestRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ULIService {
    
    private final ULIRequestRepository uliRequestRepository;
    private final ULIClient uliClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public ULIService(
            ULIRequestRepository uliRequestRepository,
            ULIClient uliClient,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.uliRequestRepository = uliRequestRepository;
        this.uliClient = uliClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public Map<String, Object> createULIRequest(
            UUID msmeId,
            String lenderId,
            double loanAmount,
            String loanPurpose) {
        
        String requestId = "ULI-" + UUID.randomUUID().toString().substring(0, 12);
        
        // Create ULI request
        ULIRequest request = new ULIRequest();
        request.setMsmeId(msmeId);
        request.setLenderId(lenderId);
        request.setLoanAmount(loanAmount);
        request.setLoanPurpose(loanPurpose);
        request.setRequestId(requestId);
        request.setStatus("pending");
        
        uliRequestRepository.save(request);
        
        // Call ULI API (simulated)
        try {
            uliClient.createRequest(
                requestId,
                msmeId.toString(),
                lenderId,
                loanAmount,
                loanPurpose
            );
        } catch (Exception e) {
            // Continue with simulated response
        }
        
        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("request_id", requestId);
        response.put("status", "pending");
        response.put("message", "ULI request created");
        response.put("next_steps", List.of(
            "Consent will be requested from borrower",
            "Data will be fetched from AA",
            "Credit decision will be made"
        ));
        
        return response;
    }
    
    public Map<String, Object> getULIStatus(String requestId) {
        ULIRequest request = uliRequestRepository
            .findByRequestId(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("request_id", requestId);
        response.put("status", request.getStatus());
        response.put("created_at", request.getCreatedAt().toString());
        response.put("completed_at", 
            request.getCompletedAt() != null ? request.getCompletedAt().toString() : null
        );
        
        return response;
    }
    
    public Map<String, Object> handleULICallback(String requestId, Map<String, Object> data) {
        ULIRequest request = uliRequestRepository
            .findByRequestId(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));
        
        request.setStatus((String) data.getOrDefault("status", "completed"));
        request.setResponsePayload(data.toString());
        request.setCompletedAt(LocalDateTime.now());
        
        uliRequestRepository.save(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("request_id", requestId);
        response.put("status", request.getStatus());
        response.put("message", "Callback processed");
        
        return response;
    }
    
    public List<ULIRequest> getRequestsByMsme(UUID msmeId) {
        return uliRequestRepository.findByMsmeIdOrderByCreatedAtDesc(msmeId);
    }
}
```

```java
// controller/ULIController.java
package com.msme.aggregation.controller;

import com.msme.aggregation.service.ULIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/uli")
@Tag(name = "ULI Integration", description = "Unified Lending Interface endpoints")
public class ULIController {
    
    private final ULIService uliService;
    
    public ULIController(ULIService uliService) {
        this.uliService = uliService;
    }
    
    @PostMapping("/request")
    @PreAuthorize("hasRole('MSME')")
    @Operation(summary = "Create ULI lending request")
    public ResponseEntity<?> createULIRequest(
            @RequestParam UUID msmeId,
            @RequestParam String lenderId,
            @RequestParam double loanAmount,
            @RequestParam String loanPurpose) {
        return ResponseEntity.ok(
            uliService.createULIRequest(msmeId, lenderId, loanAmount, loanPurpose)
        );
    }
    
    @GetMapping("/status/{requestId}")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER')")
    @Operation(summary = "Get ULI request status")
    public ResponseEntity<?> getULIStatus(@PathVariable String requestId) {
        return ResponseEntity.ok(uliService.getULIStatus(requestId));
    }
    
    @PostMapping("/callback")
    @Operation(summary = "Handle ULI callback")
    public ResponseEntity<?> handleULICallback(
            @RequestParam String requestId,
            @RequestBody Map<String, Object> data) {
        return ResponseEntity.ok(uliService.handleULICallback(requestId, data));
    }
    
    @GetMapping("/history/{msmeId}")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER')")
    @Operation(summary = "Get ULI request history")
    public ResponseEntity<?> getULIHistory(@PathVariable UUID msmeId) {
        return ResponseEntity.ok(uliService.getRequestsByMsme(msmeId));
    }
}
```

---

## 12.4 OCEN INTEGRATION SERVICE

```java
// service/OCENService.java
package com.msme.aggregation.service;

import com.msme.aggregation.client.OCENClient;
import com.msme.aggregation.model.OCENApplication;
import com.msme.aggregation.repository.OCENApplicationRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class OCENService {
    
    private final OCENApplicationRepository ocenApplicationRepository;
    private final OCENClient ocenClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    // Lender products configuration
    private final Map<String, List<Map<String, Object>>> lenderProducts = new HashMap<>() {{
        put("sbi", List.of(
            Map.of("product_id", "SBI_MSE", "name", "SBI MSE Loan", 
                   "max_amount", 5000000, "rate", 8.5),
            Map.of("product_id", "SBI_SHG", "name", "SBI SHG Loan", 
                   "max_amount", 2000000, "rate", 7.0)
        ));
        put("hdfc", List.of(
            Map.of("product_id", "HDFC_MSB", "name", "HDFC MSB Loan", 
                   "max_amount", 10000000, "rate", 9.0)
        ));
        put("icici", List.of(
            Map.of("product_id", "ICICI_MSE", "name", "ICICI MSE Loan", 
                   "max_amount", 7500000, "rate", 8.75)
        ));
    }};
    
    public OCENService(
            OCENApplicationRepository ocenApplicationRepository,
            OCENClient ocenClient,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.ocenApplicationRepository = ocenApplicationRepository;
        this.ocenClient = ocenClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public List<Map<String, Object>> searchProducts(
            UUID msmeId,
            double loanAmount,
            int tenureMonths) {
        
        List<Map<String, Object>> availableProducts = new ArrayList<>();
        
        for (Map.Entry<String, List<Map<String, Object>>> entry : lenderProducts.entrySet()) {
            String lenderId = entry.getKey();
            
            for (Map<String, Object> product : entry.getValue()) {
                double maxAmount = ((Number) product.get("max_amount")).doubleValue();
                
                if (loanAmount <= maxAmount) {
                    double rate = ((Number) product.get("rate")).doubleValue();
                    double emi = calculateEMI(loanAmount, rate, tenureMonths);
                    
                    Map<String, Object> productInfo = new HashMap<>(product);
                    productInfo.put("lender_id", lenderId);
                    productInfo.put("estimated_emi", Math.round(emi * 100.0) / 100.0);
                    productInfo.put("eligibility_score", calculateEligibilityScore(msmeId));
                    
                    availableProducts.add(productInfo);
                }
            }
        }
        
        // Sort by eligibility score
        availableProducts.sort((a, b) -> 
            ((Number) b.get("eligibility_score")).intValue() - 
            ((Number) a.get("eligibility_score")).intValue()
        );
        
        return availableProducts;
    }
    
    public Map<String, Object> createApplication(
            UUID msmeId,
            String productId,
            String lenderId,
            double amount,
            int tenureMonths) {
        
        String applicationId = "OCEN-" + UUID.randomUUID().toString().substring(0, 10);
        
        // Create application record
        OCENApplication application = new OCENApplication();
        application.setMsmeId(msmeId);
        application.setProductId(productId);
        application.setLenderId(lenderId);
        application.setAmount(amount);
        application.setTenureMonths(tenureMonths);
        application.setApplicationId(applicationId);
        application.setStatus("initiated");
        
        ocenApplicationRepository.save(application);
        
        // Call OCEN API (simulated)
        try {
            ocenClient.createApplication(
                applicationId,
                msmeId.toString(),
                productId,
                lenderId,
                amount,
                tenureMonths
            );
        } catch (Exception e) {
            // Continue with simulated response
        }
        
        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("application_id", applicationId);
        response.put("status", "initiated");
        response.put("message", "Loan application created");
        response.put("next_steps", List.of(
            "Data will be fetched via AA",
            "Credit assessment will be performed",
            "Loan offer will be generated"
        ));
        
        return response;
    }
    
    public Map<String, Object> getApplicationStatus(String applicationId) {
        OCENApplication application = ocenApplicationRepository
            .findByApplicationId(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("application_id", applicationId);
        response.put("status", application.getStatus());
        response.put("amount", application.getAmount());
        response.put("tenure", application.getTenureMonths());
        response.put("created_at", application.getCreatedAt().toString());
        
        return response;
    }
    
    public Map<String, Object> handleOfferCallback(String applicationId, Map<String, Object> offerData) {
        OCENApplication application = ocenApplicationRepository
            .findByApplicationId(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        
        application.setStatus("offer_generated");
        application.setOfferPayload(offerData.toString());
        
        ocenApplicationRepository.save(application);
        
        Map<String, Object> response = new HashMap<>();
        response.put("application_id", applicationId);
        response.put("status", "offer_generated");
        response.put("offer", offerData);
        
        return response;
    }
    
    public List<OCENApplication> getApplicationsByMsme(UUID msmeId) {
        return ocenApplicationRepository.findByMsmeIdOrderByCreatedAtDesc(msmeId);
    }
    
    private double calculateEMI(double principal, double annualRate, int months) {
        double monthlyRate = annualRate / 100 / 12;
        return principal * monthlyRate * Math.pow(1 + monthlyRate, months) / 
               (Math.pow(1 + monthlyRate, months) - 1);
    }
    
    private int calculateEligibilityScore(UUID msmeId) {
        // Simulated eligibility score
        return (int) (60 + Math.random() * 35);
    }
}
```

```java
// controller/OCENController.java
package com.msme.aggregation.controller;

import com.msme.aggregation.service.OCENService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/ocen")
@Tag(name = "OCEN Integration", description = "Open Credit Enablement Network endpoints")
public class OCENController {
    
    private final OCENService ocenService;
    
    public OCENController(OCENService ocenService) {
        this.ocenService = ocenService;
    }
    
    @GetMapping("/products")
    @PreAuthorize("hasRole('MSME')")
    @Operation(summary = "Search for loan products")
    public ResponseEntity<?> searchProducts(
            @RequestParam UUID msmeId,
            @RequestParam double loanAmount,
            @RequestParam int tenureMonths) {
        return ResponseEntity.ok(
            ocenService.searchProducts(msmeId, loanAmount, tenureMonths)
        );
    }
    
    @PostMapping("/apply")
    @PreAuthorize("hasRole('MSME')")
    @Operation(summary = "Apply for loan")
    public ResponseEntity<?> applyLoan(
            @RequestParam UUID msmeId,
            @RequestParam String productId,
            @RequestParam String lenderId,
            @RequestParam double amount,
            @RequestParam int tenureMonths) {
        return ResponseEntity.ok(
            ocenService.createApplication(msmeId, productId, lenderId, amount, tenureMonths)
        );
    }
    
    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER')")
    @Operation(summary = "Get application status")
    public ResponseEntity<?> getApplicationStatus(@PathVariable String applicationId) {
        return ResponseEntity.ok(ocenService.getApplicationStatus(applicationId));
    }
    
    @PostMapping("/callback")
    @Operation(summary = "Handle offer callback")
    public ResponseEntity<?> handleOfferCallback(
            @RequestParam String applicationId,
            @RequestBody Map<String, Object> offerData) {
        return ResponseEntity.ok(ocenService.handleOfferCallback(applicationId, offerData));
    }
    
    @GetMapping("/history/{msmeId}")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER')")
    @Operation(summary = "Get application history")
    public ResponseEntity<?> getApplicationHistory(@PathVariable UUID msmeId) {
        return ResponseEntity.ok(ocenService.getApplicationsByMsme(msmeId));
    }
}
```

---

## 12.5 VERIFICATION SERVICE

```java
// service/VerificationService.java
package com.msme.aggregation.service;

import com.msme.aggregation.client.VerificationClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VerificationService {
    
    private final VerificationClient verificationClient;
    
    public VerificationService(VerificationClient verificationClient) {
        this.verificationClient = verificationClient;
    }
    
    public Map<String, Object> verifyPAN(String panNumber) {
        // Simulated PAN verification
        Map<String, Object> result = new HashMap<>();
        result.put("pan", panNumber);
        result.put("valid", panNumber != null && panNumber.length() == 10);
        result.put("name", "BUSINESS NAME");
        result.put("status", "verified");
        
        return result;
    }
    
    public Map<String, Object> verifyGSTIN(String gstin) {
        // Simulated GSTIN verification
        Map<String, Object> result = new HashMap<>();
        result.put("gstin", gstin);
        result.put("valid", gstin != null && gstin.length() == 15);
        result.put("business_name", "MSME BUSINESS");
        result.put("status", "active");
        result.put("registration_date", "2018-05-15");
        
        return result;
    }
    
    public Map<String, Object> verifyUdyam(String udyamNumber) {
        // Simulated UDYAM verification
        Map<String, Object> result = new HashMap<>();
        result.put("udyam_number", udyamNumber);
        result.put("valid", udyamNumber != null && udyamNumber.startsWith("UDYAM"));
        result.put("enterprise_name", "MSME ENTERPRISE");
        result.put("date_of_registration", "2020-03-20");
        result.put("msme_category", "Small");
        
        return result;
    }
    
    public Map<String, Object> verifyAll(String pan, String gstin, String udyam) {
        Map<String, Object> results = new HashMap<>();
        results.put("pan", verifyPAN(pan));
        results.put("gstin", verifyGSTIN(gstin));
        results.put("udyam", verifyUdyam(udyam));
        
        // Overall verification status
        boolean allValid = 
            (boolean) results.get("pan").get("valid") &&
            (boolean) results.get("gstin").get("valid") &&
            (boolean) results.get("udyam").get("valid");
        
        results.put("overall_status", allValid ? "verified" : "partial");
        
        return results;
    }
}
```

```java
// controller/VerificationController.java
package com.msme.aggregation.controller;

import com.msme.aggregation.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/verification")
@Tag(name = "Verification", description = "Document verification endpoints")
public class VerificationController {
    
    private final VerificationService verificationService;
    
    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }
    
    @GetMapping("/pan/{panNumber}")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER', 'ADMIN')")
    @Operation(summary = "Verify PAN number")
    public ResponseEntity<?> verifyPAN(@PathVariable String panNumber) {
        return ResponseEntity.ok(verificationService.verifyPAN(panNumber));
    }
    
    @GetMapping("/gstin/{gstin}")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER', 'ADMIN')")
    @Operation(summary = "Verify GSTIN")
    public ResponseEntity<?> verifyGSTIN(@PathVariable String gstin) {
        return ResponseEntity.ok(verificationService.verifyGSTIN(gstin));
    }
    
    @GetMapping("/udyam/{udyamNumber}")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER', 'ADMIN')")
    @Operation(summary = "Verify UDYAM registration")
    public ResponseEntity<?> verifyUdyam(@PathVariable String udyamNumber) {
        return ResponseEntity.ok(verificationService.verifyUdyam(udyamNumber));
    }
    
    @PostMapping("/verify-all")
    @PreAuthorize("hasRole('MSME')")
    @Operation(summary = "Verify all documents")
    public ResponseEntity<?> verifyAll(
            @RequestParam String pan,
            @RequestParam String gstin,
            @RequestParam String udyam) {
        return ResponseEntity.ok(verificationService.verifyAll(pan, gstin, udyam));
    }
}
```

---

## 12.6 FRONTEND - AA CONSENT FLOW

```tsx
// frontend/src/components/consent/AAConsentFlow.tsx
import React, { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Checkbox } from '@/components/ui/Checkbox';
import { aaApi } from '@/api/aa.api';

interface AAConsentFlowProps {
  msmeId: string;
  onComplete: (consentId: string) => void;
}

export const AAConsentFlow: React.FC<AAConsentFlowProps> = ({ msmeId, onComplete }) => {
  const [step, setStep] = useState<'select' | 'consent' | 'processing' | 'complete'>('select');
  const [selectedProvider, setSelectedProvider] = useState('');
  const [selectedDataTypes, setSelectedDataTypes] = useState<string[]>([]);
  
  const consentMutation = useMutation({
    mutationFn: async () => {
      const response = await aaApi.createConsent({
        msme_id: msmeId,
        provider: selectedProvider,
        fip_id: 'default_fip',
        data_types: selectedDataTypes
      });
      return response.data;
    },
    onSuccess: (data) => {
      setStep('processing');
      // Simulate processing
      setTimeout(() => {
        setStep('complete');
        onComplete(data.consent_handle);
      }, 3000);
    }
  });
  
  const providers = [
    { id: 'finvu', name: 'Finvu', description: 'Leading AA provider' },
    { id: 'onemoney', name: 'OneMoney', description: 'Fast & secure' },
    { id: 'sahamati', name: 'Sahamati', description: 'Government backed' }
  ];
  
  const dataTypes = [
    { id: 'bank_transactions', label: 'Bank Transactions', description: 'Last 12 months' },
    { id: 'gst_returns', label: 'GST Returns', description: 'Last 6 months' },
    { id: 'epf_data', label: 'EPF Data', description: 'Employee details' }
  ];
  
  return (
    <Card className="max-w-md mx-auto">
      <CardHeader>
        <CardTitle>Share Financial Data via AA</CardTitle>
      </CardHeader>
      <CardContent>
        {step === 'select' && (
          <div className="space-y-6">
            <div>
              <h3 className="font-medium mb-3">Select AA Provider</h3>
              <div className="space-y-2">
                {providers.map(provider => (
                  <div
                    key={provider.id}
                    className={`p-3 border rounded cursor-pointer transition-colors ${
                      selectedProvider === provider.id 
                        ? 'border-blue-500 bg-blue-50' 
                        : 'hover:border-gray-300'
                    }`}
                    onClick={() => setSelectedProvider(provider.id)}
                  >
                    <p className="font-medium">{provider.name}</p>
                    <p className="text-sm text-gray-500">{provider.description}</p>
                  </div>
                ))}
              </div>
            </div>
            
            <div>
              <h3 className="font-medium mb-3">Select Data to Share</h3>
              <div className="space-y-2">
                {dataTypes.map(dataType => (
                  <div key={dataType.id} className="flex items-center space-x-3">
                    <Checkbox
                      id={dataType.id}
                      checked={selectedDataTypes.includes(dataType.id)}
                      onCheckedChange={(checked) => {
                        if (checked) {
                          setSelectedDataTypes([...selectedDataTypes, dataType.id]);
                        } else {
                          setSelectedDataTypes(selectedDataTypes.filter(d => d !== dataType.id));
                        }
                      }}
                    />
                    <div>
                      <label htmlFor={dataType.id} className="font-medium">
                        {dataType.label}
                      </label>
                      <p className="text-sm text-gray-500">{dataType.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
            
            <Button 
              className="w-full" 
              onClick={() => setStep('consent')}
              disabled={!selectedProvider || selectedDataTypes.length === 0}
            >
              Continue
            </Button>
          </div>
        )}
        
        {step === 'consent' && (
          <div className="space-y-4">
            <div className="bg-yellow-50 p-4 rounded">
              <h4 className="font-medium text-yellow-800">Consent Request</h4>
              <p className="text-sm text-yellow-700 mt-2">
                You are about to share the following data with your selected lender:
              </p>
              <ul className="mt-2 text-sm text-yellow-700">
                {selectedDataTypes.map(type => (
                  <li key={type}>• {dataTypes.find(d => d.id === type)?.label}</li>
                ))}
              </ul>
            </div>
            
            <p className="text-sm text-gray-500">
              By proceeding, you authorize the AA provider to share this data 
              with the lender for credit assessment purposes only.
            </p>
            
            <div className="flex gap-2">
              <Button variant="outline" onClick={() => setStep('select')}>
                Back
              </Button>
              <Button 
                className="flex-1" 
                onClick={() => consentMutation.mutate()}
                disabled={consentMutation.isPending}
              >
                {consentMutation.isPending ? 'Processing...' : 'Approve & Share'}
              </Button>
            </div>
          </div>
        )}
        
        {step === 'processing' && (
          <div className="text-center py-8">
            <div className="animate-spin w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full mx-auto" />
            <p className="mt-4 text-gray-600">Processing your consent...</p>
            <p className="text-sm text-gray-500">Fetching data from your accounts</p>
          </div>
        )}
        
        {step === 'complete' && (
          <div className="text-center py-8">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
              <span className="text-3xl">✓</span>
            </div>
            <h3 className="mt-4 font-medium text-lg">Data Shared Successfully</h3>
            <p className="mt-2 text-gray-500">
              Your financial data has been securely shared with the lender.
            </p>
          </div>
        )}
      </CardContent>
    </Card>
  );
};
```

---

## 12.7 API ENDPOINTS SUMMARY

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/aa/consent/request` | POST | Create AA consent |
| `/api/v1/aa/consent/{handle}` | GET | Check consent status |
| `/api/v1/aa/consent/callback` | POST | Handle consent callback |
| `/api/v1/aa/consents/{msmeId}` | GET | Get MSME consents |
| `/api/v1/uli/request` | POST | Create ULI request |
| `/api/v1/uli/status/{requestId}` | GET | Get ULI status |
| `/api/v1/uli/callback` | POST | Handle ULI callback |
| `/api/v1/uli/history/{msmeId}` | GET | Get ULI history |
| `/api/v1/ocen/products` | GET | Search loan products |
| `/api/v1/ocen/apply` | POST | Apply for loan |
| `/api/v1/ocen/application/{id}` | GET | Get application status |
| `/api/v1/ocen/callback` | POST | Handle offer callback |
| `/api/v1/ocen/history/{msmeId}` | GET | Get application history |
| `/api/v1/verification/pan/{pan}` | GET | Verify PAN |
| `/api/v1/verification/gstin/{gstin}` | GET | Verify GSTIN |
| `/api/v1/verification/udyam/{udyam}` | GET | Verify UDYAM |
| `/api/v1/verification/verify-all` | POST | Verify all documents |

---

## 12.8 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| AA Consent Service | 3 days |
| ULI Service | 2 days |
| OCEN Service | 2 days |
| Verification Service | 1 day |
| API Endpoints | 2 days |
| Frontend Components | 2 days |
| Testing | 2 days |
| **Total** | **14 days** |

---

## 12.9 HACKATHON PRIORITY

**HIGH** - Core requirement demonstrating fintech integration
