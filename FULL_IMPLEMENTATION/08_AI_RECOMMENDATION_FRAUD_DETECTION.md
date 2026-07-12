# MODULE 8: AI RECOMMENDATION & FRAUD DETECTION
## Production-Grade Implementation

---

## 8.1 MICROSERVICES STRUCTURE

```
fraud-service/
├── src/main/java/com/msme/fraud/
│   ├── FraudServiceApplication.java
│   ├── config/
│   │   ├── KafkaConfig.java
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── FraudCheckController.java
│   │   └── FraudAlertController.java
│   ├── service/
│   │   ├── FraudDetectionService.java
│   │   ├── FraudAlertService.java
│   │   └── FraudRuleEngine.java
│   ├── model/
│   │   ├── FraudCheck.java
│   │   ├── FraudAlert.java
│   │   └── FraudRule.java
│   ├── repository/
│   │   ├── FraudCheckRepository.java
│   │   └── FraudAlertRepository.java
│   ├── dto/
│   │   ├── FraudCheckRequest.java
│   │   ├── FraudCheckResponse.java
│   │   └── FraudAlertDTO.java
│   └── client/
│       └── AIServiceClient.java
└── pom.xml

recommendation-service/
├── src/main/java/com/msme/recommendation/
│   ├── RecommendationServiceApplication.java
│   ├── controller/
│   │   └── RecommendationController.java
│   ├── service/
│   │   ├── RecommendationService.java
│   │   └── RuleEngineService.java
│   ├── model/
│   │   ├── Recommendation.java
│   │   └── RecommendationRule.java
│   └── repository/
│       └── RecommendationRepository.java
└── pom.xml

ai-services/fraud/
├── app/
│   ├── main.py
│   ├── models/
│   │   ├── isolation_forest.py
│   │   ├── local_outlier.py
│   │   ├── autoencoder.py
│   │   └── ensemble.py
│   ├── rules/
│   │   ├── rule_engine.py
│   │   └── business_rules.py
│   ├── features/
│   │   └── fraud_features.py
│   └── api/
│       └── routes.py
├── ml_models/
│   ├── isolation_forest_model.pkl
│   ├── scaler.pkl
│   └── feature_columns.pkl
├── requirements.txt
└── Dockerfile

ai-services/recommendation/
├── app/
│   ├── main.py
│   ├── engine/
│   │   ├── rule_engine.py
│   │   ├── ml_recommender.py
│   │   └── content_based.py
│   ├── templates/
│   │   └── recommendation_templates.json
│   └── api/
│       └── routes.py
├── requirements.txt
└── Dockerfile
```

---

## 8.2 FRAUD DETECTION SERVICE (JAVA)

```java
// FraudServiceApplication.java
package com.msme.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableFeignClients
@EnableKafka
public class FraudServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FraudServiceApplication.class, args);
    }
}
```

```java
// model/FraudCheck.java
package com.msme.fraud.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_checks")
public class FraudCheck {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID msmeId;
    
    private UUID applicationId;
    
    @Column(nullable = false)
    private String checkType;
    
    @Column(nullable = false)
    private BigDecimal riskScore;
    
    @Column(columnDefinition = "jsonb")
    private String riskFactors;
    
    @Column(columnDefinition = "jsonb")
    private String modelOutput;
    
    private Boolean isSuspicious = false;
    
    private String modelVersion;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getMsmeId() { return msmeId; }
    public void setMsmeId(UUID msmeId) { this.msmeId = msmeId; }
    
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    
    public String getCheckType() { return checkType; }
    public void setCheckType(String checkType) { this.checkType = checkType; }
    
    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }
    
    public String getRiskFactors() { return riskFactors; }
    public void setRiskFactors(String riskFactors) { this.riskFactors = riskFactors; }
    
    public String getModelOutput() { return modelOutput; }
    public void setModelOutput(String modelOutput) { this.modelOutput = modelOutput; }
    
    public Boolean getIsSuspicious() { return isSuspicious; }
    public void setIsSuspicious(Boolean isSuspicious) { this.isSuspicious = isSuspicious; }
    
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

```java
// model/FraudAlert.java
package com.msme.fraud.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_alerts")
public class FraudAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID fraudCheckId;
    
    private String alertType;
    
    private String severity;
    
    @Column(columnDefinition = "text")
    private String description;
    
    @Column(columnDefinition = "jsonb")
    private String details;
    
    private String status = "open";
    
    private UUID assignedTo;
    
    private LocalDateTime resolvedAt;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getFraudCheckId() { return fraudCheckId; }
    public void setFraudCheckId(UUID fraudCheckId) { this.fraudCheckId = fraudCheckId; }
    
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public UUID getAssignedTo() { return assignedTo; }
    public void setAssignedTo(UUID assignedTo) { this.assignedTo = assignedTo; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

```java
// service/FraudDetectionService.java
package com.msme.fraud.service;

import com.msme.fraud.client.AIServiceClient;
import com.msme.fraud.dto.FraudCheckRequest;
import com.msme.fraud.dto.FraudCheckResponse;
import com.msme.fraud.model.FraudCheck;
import com.msme.fraud.model.FraudAlert;
import com.msme.fraud.repository.FraudCheckRepository;
import com.msme.fraud.repository.FraudAlertRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FraudDetectionService {
    
    private final FraudCheckRepository fraudCheckRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final AIServiceClient aiServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public FraudDetectionService(
            FraudCheckRepository fraudCheckRepository,
            FraudAlertRepository fraudAlertRepository,
            AIServiceClient aiServiceClient,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.fraudCheckRepository = fraudCheckRepository;
        this.fraudAlertRepository = fraudAlertRepository;
        this.aiServiceClient = aiServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public FraudCheckResponse performFraudCheck(FraudCheckRequest request) {
        // 1. Create fraud check record
        FraudCheck fraudCheck = new FraudCheck();
        fraudCheck.setMsmeId(request.getMsmeId());
        fraudCheck.setApplicationId(request.getApplicationId());
        fraudCheck.setCheckType("comprehensive");
        
        // 2. Call AI Fraud Detection Service
        try {
            var aiResponse = aiServiceClient.analyzeFraud(request);
            
            fraudCheck.setRiskScore(BigDecimal.valueOf(aiResponse.getRiskScore()));
            fraudCheck.setRiskFactors(aiResponse.getRiskFactors());
            fraudCheck.setModelOutput(aiResponse.getRawOutput());
            fraudCheck.setModelVersion(aiResponse.getModelVersion());
            fraudCheck.setIsSuspicious(aiResponse.getRiskScore() > 0.7);
            
        } catch (Exception e) {
            // Fallback to rule-based check
            var ruleResult = performRuleBasedCheck(request);
            fraudCheck.setRiskScore(BigDecimal.valueOf(ruleResult.getRiskScore()));
            fraudCheck.setRiskFactors(ruleResult.getRiskFactors());
            fraudCheck.setIsSuspicious(ruleResult.getRiskScore() > 0.7);
        }
        
        // 3. Save fraud check
        fraudCheckRepository.save(fraudCheck);
        
        // 4. Create alerts if suspicious
        if (fraudCheck.getIsSuspicious()) {
            createFraudAlert(fraudCheck, request);
        }
        
        // 5. Publish event
        kafkaTemplate.send("fraud.events", "fraud.check.completed", 
            Map.of(
                "msmeId", request.getMsmeId(),
                "riskScore", fraudCheck.getRiskScore(),
                "isSuspicious", fraudCheck.getIsSuspicious()
            )
        );
        
        // 6. Build response
        return FraudCheckResponse.builder()
            .checkId(fraudCheck.getId())
            .msmeId(request.getMsmeId())
            .riskScore(fraudCheck.getRiskScore().doubleValue())
            .isSuspicious(fraudCheck.getIsSuspicious())
            .riskFactors(fraudCheck.getRiskFactors())
            .recommendations(generateRecommendations(fraudCheck))
            .build();
    }
    
    private FraudCheckResponse performRuleBasedCheck(FraudCheckRequest request) {
        double riskScore = 0.0;
        StringBuilder riskFactors = new StringBuilder("[");
        
        // Rule 1: PAN verification
        if (request.getPanNumber() == null || request.getPanNumber().isEmpty()) {
            riskScore += 0.2;
            riskFactors.append("\"missing_pan\",");
        }
        
        // Rule 2: GST filing consistency
        if (request.getGstFilingMonths() != null && request.getGstFilingMonths() < 6) {
            riskScore += 0.15;
            riskFactors.append("\"insufficient_gst_history\",");
        }
        
        // Rule 3: Employee count vs turnover ratio
        if (request.getEmployeeCount() != null && request.getAnnualTurnover() != null) {
            double turnoverPerEmployee = request.getAnnualTurnover() / request.getEmployeeCount();
            if (turnoverPerEmployee < 100000 || turnoverPerEmployee > 10000000) {
                riskScore += 0.25;
                riskFactors.append("\"abnormal_turnover_ratio\",");
            }
        }
        
        // Rule 4: Business age
        if (request.getBusinessAge() != null && request.getBusinessAge() < 1) {
            riskScore += 0.15;
            riskFactors.append(\"new_business\",");
        }
        
        // Rule 5: Document consistency
        if (request.getDocumentMismatch() != null && request.getDocumentMismatch()) {
            riskScore += 0.3;
            riskFactors.append("\"document_mismatch\",");
        }
        
        // Close JSON array
        if (riskFactors.length() > 1) {
            riskFactors.setLength(riskFactors.length() - 1);
        }
        riskFactors.append("]");
        
        FraudCheckResponse response = new FraudCheckResponse();
        response.setRiskScore(Math.min(riskScore, 1.0));
        response.setRiskFactors(riskFactors.toString());
        return response;
    }
    
    private void createFraudAlert(FraudCheck fraudCheck, FraudCheckRequest request) {
        FraudAlert alert = new FraudAlert();
        alert.setFraudCheckId(fraudCheck.getId());
        alert.setAlertType(determineAlertType(fraudCheck));
        alert.setSeverity(determineSeverity(fraudCheck.getRiskScore()));
        alert.setDescription(generateAlertDescription(fraudCheck, request));
        alert.setDetails(fraudCheck.getRiskFactors());
        
        fraudAlertRepository.save(alert);
        
        // Publish alert event
        kafkaTemplate.send("fraud.events", "fraud.alert.created",
            Map.of(
                "alertId", alert.getId(),
                "msmeId", request.getMsmeId(),
                "severity", alert.getSeverity()
            )
        );
    }
    
    private String determineAlertType(FraudCheck fraudCheck) {
        if (fraudCheck.getRiskScore().doubleValue() > 0.9) {
            return "critical_fraud_risk";
        } else if (fraudCheck.getRiskScore().doubleValue() > 0.7) {
            return "high_fraud_risk";
        }
        return "medium_fraud_risk";
    }
    
    private String determineSeverity(BigDecimal riskScore) {
        if (riskScore.doubleValue() > 0.9) return "critical";
        if (riskScore.doubleValue() > 0.7) return "high";
        if (riskScore.doubleValue() > 0.5) return "medium";
        return "low";
    }
    
    private String generateAlertDescription(FraudCheck fraudCheck, FraudCheckRequest request) {
        return String.format(
            "High fraud risk detected for MSME %s. Risk Score: %.2f. Immediate review recommended.",
            request.getMsmeId(), fraudCheck.getRiskScore()
        );
    }
    
    private List<String> generateRecommendations(FraudCheck fraudCheck) {
        if (fraudCheck.getRiskScore().doubleValue() > 0.8) {
            return List.of(
                "Reject application",
                "Request additional documentation",
                "Conduct in-person verification",
                "Escalate to fraud investigation team"
            );
        } else if (fraudCheck.getRiskScore().doubleValue() > 0.5) {
            return List.of(
                "Request additional documents",
                "Verify with third-party data",
                "Manual review recommended"
            );
        }
        return List.of("Standard processing recommended");
    }
    
    public List<FraudAlert> getOpenAlerts() {
        return fraudAlertRepository.findByStatus("open");
    }
    
    public FraudAlert resolveAlert(UUID alertId, UUID resolvedBy) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found"));
        
        alert.setStatus("resolved");
        alert.setAssignedTo(resolvedBy);
        alert.setResolvedAt(LocalDateTime.now());
        
        return fraudAlertRepository.save(alert);
    }
}
```

```java
// controller/FraudCheckController.java
package com.msme.fraud.controller;

import com.msme.fraud.dto.FraudCheckRequest;
import com.msme.fraud.dto.FraudCheckResponse;
import com.msme.fraud.service.FraudDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fraud")
@Tag(name = "Fraud Detection", description = "Fraud detection and analysis endpoints")
public class FraudCheckController {
    
    private final FraudDetectionService fraudDetectionService;
    
    public FraudCheckController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }
    
    @PostMapping("/analyze")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Perform fraud analysis on MSME")
    public ResponseEntity<FraudCheckResponse> analyzeFraud(@RequestBody FraudCheckRequest request) {
        FraudCheckResponse response = fraudDetectionService.performFraudCheck(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Get open fraud alerts")
    public ResponseEntity<?> getAlerts() {
        return ResponseEntity.ok(fraudDetectionService.getOpenAlerts());
    }
    
    @PutMapping("/alerts/{alertId}/resolve")
    @PreAuthorize("hasAnyRole('CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Resolve a fraud alert")
    public ResponseEntity<?> resolveAlert(
            @PathVariable UUID alertId,
            @RequestParam UUID resolvedBy) {
        return ResponseEntity.ok(fraudDetectionService.resolveAlert(alertId, resolvedBy));
    }
}
```

```java
// dto/FraudCheckRequest.java
package com.msme.fraud.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class FraudCheckRequest {
    
    @NotNull(message = "MSME ID is required")
    private UUID msmeId;
    
    private UUID applicationId;
    
    private String panNumber;
    private String gstin;
    private String udyamNumber;
    
    private Integer gstFilingMonths;
    private Integer employeeCount;
    private BigDecimal annualTurnover;
    private Integer businessAge;
    
    private Boolean documentMismatch;
    
    // Getters and Setters
    public UUID getMsmeId() { return msmeId; }
    public void setMsmeId(UUID msmeId) { this.msmeId = msmeId; }
    
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    
    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }
    
    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    
    public String getUdyamNumber() { return udyamNumber; }
    public void setUdyamNumber(String udyamNumber) { this.udyamNumber = udyamNumber; }
    
    public Integer getGstFilingMonths() { return gstFilingMonths; }
    public void setGstFilingMonths(Integer gstFilingMonths) { this.gstFilingMonths = gstFilingMonths; }
    
    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }
    
    public BigDecimal getAnnualTurnover() { return annualTurnover; }
    public void setAnnualTurnover(BigDecimal annualTurnover) { this.annualTurnover = annualTurnover; }
    
    public Integer getBusinessAge() { return businessAge; }
    public void setBusinessAge(Integer businessAge) { this.businessAge = businessAge; }
    
    public Boolean getDocumentMismatch() { return documentMismatch; }
    public void setDocumentMismatch(Boolean documentMismatch) { this.documentMismatch = documentMismatch; }
}
```

```java
// dto/FraudCheckResponse.java
package com.msme.fraud.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FraudCheckResponse {
    private UUID checkId;
    private UUID msmeId;
    private double riskScore;
    private boolean isSuspicious;
    private String riskFactors;
    private String modelVersion;
    private List<String> recommendations;
}
```

```java
// client/AIServiceClient.java
package com.msme.fraud.client;

import com.msme.fraud.dto.FraudCheckRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-fraud", url = "${ai.fraud.url:http://ai-fraud:5003}")
public interface AIServiceClient {
    
    @PostMapping("/api/v1/fraud/analyze")
    AI FraudResponse analyzeFraud(@RequestBody FraudCheckRequest request);
}
```

---

## 8.3 AI FRAUD DETECTION SERVICE (PYTHON)

```python
# ai-services/fraud/app/main.py
from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional
import joblib
import numpy as np
from datetime import datetime

app = FastAPI(title="AI Fraud Detection Service", version="1.0.0")

# Load models
isolation_forest = joblib.load("ml_models/isolation_forest_model.pkl")
scaler = joblib.load("ml_models/scaler.pkl")
feature_columns = joblib.load("ml_models/feature_columns.pkl")

class FraudAnalysisRequest(BaseModel):
    msme_id: str
    pan_number: Optional[str] = None
    gstin: Optional[str] = None
    gst_filing_months: Optional[int] = None
    employee_count: Optional[int] = None
    annual_turnover: Optional[float] = None
    business_age: Optional[int] = None
    transaction_data: Optional[dict] = None
    document_data: Optional[dict] = None

class FraudAnalysisResponse(BaseModel):
    risk_score: float
    risk_level: str
    risk_factors: list
    anomaly_detected: bool
    model_version: str
    recommendations: list
    raw_output: dict

class IsolationForestDetector:
    def __init__(self):
        self.model = isolation_forest
        self.scaler = scaler
        self.feature_cols = feature_columns
        self.model_version = "1.0.0"
    
    def predict(self, features: np.ndarray) -> dict:
        """Predict fraud using Isolation Forest"""
        
        # Scale features
        features_scaled = self.scaler.transform(features.reshape(1, -1))
        
        # Get anomaly score (-1 for anomaly, 1 for normal)
        prediction = self.model.predict(features_scaled)[0]
        anomaly_score = self.model.decision_function(features_scaled)[0]
        
        # Convert to risk score (0-1)
        risk_score = 1 - (anomaly_score + 0.5)  # Normalize to 0-1
        risk_score = max(0, min(1, risk_score))
        
        return {
            "prediction": int(prediction),
            "anomaly_score": float(anomaly_score),
            "risk_score": float(risk_score),
            "is_anomaly": prediction == -1
        }

class LocalOutlierDetector:
    def __init__(self):
        from sklearn.neighbors import LocalOutlierFactor
        self.model = None  # Loaded on demand
        self.version = "1.0.0"
    
    def detect(self, features: np.ndarray, contamination: float = 0.1) -> dict:
        """Detect outliers using LOF"""
        
        from sklearn.neighbors import LocalOutlierFactor
        
        lof = LocalOutlierFactor(n_neighbors=20, contamination=contamination)
        prediction = lof.fit_predict(features.reshape(1, -1))
        scores = lof.negative_outlier_factor_
        
        return {
            "is_outlier": prediction[0] == -1,
            "outlier_score": float(abs(scores[0])),
            "risk_score": float(min(1, abs(scores[0]) / 2))
        }

class RuleEngine:
    """Business rule-based fraud detection"""
    
    def __init__(self):
        self.rules = self._load_rules()
    
    def _load_rules(self) -> list:
        return [
            {
                "id": "RULE_001",
                "name": "PAN Verification",
                "description": "Check if PAN is valid format",
                "weight": 0.2,
                "check": lambda data: self._check_pan(data)
            },
            {
                "id": "RULE_002",
                "name": "GST Filing Consistency",
                "description": "Check GST filing history",
                "weight": 0.15,
                "check": lambda data: self._check_gst_filing(data)
            },
            {
                "id": "RULE_003",
                "name": "Turnover-Employee Ratio",
                "description": "Check abnormal turnover per employee",
                "weight": 0.25,
                "check": lambda data: self._check_turnover_ratio(data)
            },
            {
                "id": "RULE_004",
                "name": "Business Age Risk",
                "description": "New businesses have higher risk",
                "weight": 0.15,
                "check": lambda data: self._check_business_age(data)
            },
            {
                "id": "RULE_005",
                "name": "Document Consistency",
                "description": "Check document data consistency",
                "weight": 0.3,
                "check": lambda data: self._check_document_consistency(data)
            }
        ]
    
    def evaluate(self, data: dict) -> dict:
        """Evaluate all rules"""
        
        triggered_rules = []
        total_risk = 0.0
        
        for rule in self.rules:
            result = rule["check"](data)
            if result["triggered"]:
                triggered_rules.append({
                    "rule_id": rule["id"],
                    "rule_name": rule["name"],
                    "description": rule["description"],
                    "risk_contribution": result["risk_score"] * rule["weight"]
                })
                total_risk += result["risk_score"] * rule["weight"]
        
        return {
            "total_risk_score": min(1.0, total_risk),
            "triggered_rules": triggered_rules,
            "rules_evaluated": len(self.rules)
        }
    
    def _check_pan(self, data: dict) -> dict:
        pan = data.get("pan_number", "")
        if not pan or len(pan) != 10:
            return {"triggered": True, "risk_score": 0.5}
        # Basic PAN format check
        import re
        if not re.match(r'^[A-Z]{5}[0-9]{4}[A-Z]$', pan):
            return {"triggered": True, "risk_score": 0.7}
        return {"triggered": False, "risk_score": 0}
    
    def _check_gst_filing(self, data: dict) -> dict:
        months = data.get("gst_filing_months", 0)
        if months < 6:
            return {"triggered": True, "risk_score": 0.4}
        if months < 12:
            return {"triggered": True, "risk_score": 0.2}
        return {"triggered": False, "risk_score": 0}
    
    def _check_turnover_ratio(self, data: dict) -> dict:
        turnover = data.get("annual_turnover", 0)
        employees = data.get("employee_count", 1)
        
        if employees == 0:
            return {"triggered": True, "risk_score": 0.5}
        
        ratio = turnover / employees
        
        if ratio < 50000 or ratio > 5000000:
            return {"triggered": True, "risk_score": 0.6}
        if ratio < 100000 or ratio > 2000000:
            return {"triggered": True, "risk_score": 0.3}
        
        return {"triggered": False, "risk_score": 0}
    
    def _check_business_age(self, data: dict) -> dict:
        age = data.get("business_age", 0)
        if age < 1:
            return {"triggered": True, "risk_score": 0.5}
        if age < 2:
            return {"triggered": True, "risk_score": 0.2}
        return {"triggered": False, "risk_score": 0}
    
    def _check_document_consistency(self, data: dict) -> dict:
        docs = data.get("document_data", {})
        
        # Check for mismatches
        if docs.get("pan_gst_mismatch"):
            return {"triggered": True, "risk_score": 0.8}
        if docs.get("address_mismatch"):
            return {"triggered": True, "risk_score": 0.4}
        if docs.get("name_mismatch"):
            return {"triggered": True, "risk_score": 0.6}
        
        return {"triggered": False, "risk_score": 0}

class FraudEnsemble:
    """Ensemble of multiple fraud detection methods"""
    
    def __init__(self):
        self.isolation_forest = IsolationForestDetector()
        self.lof_detector = LocalOutlierDetector()
        self.rule_engine = RuleEngine()
        self.model_version = "1.0.0"
    
    def analyze(self, request: FraudAnalysisRequest) -> FraudAnalysisResponse:
        """Comprehensive fraud analysis"""
        
        # 1. Extract features
        features = self._extract_features(request)
        
        # 2. Run Isolation Forest
        if_result = self.isolation_forest.predict(features)
        
        # 3. Run Rule Engine
        rule_data = {
            "pan_number": request.pan_number,
            "gstin": request.gstin,
            "gst_filing_months": request.gst_filing_months,
            "employee_count": request.employee_count,
            "annual_turnover": request.annual_turnover,
            "business_age": request.business_age,
            "document_data": request.document_data or {}
        }
        rule_result = self.rule_engine.evaluate(rule_data)
        
        # 4. Combine results (weighted average)
        if_weight = 0.4
        rule_weight = 0.6
        
        combined_score = (
            if_result["risk_score"] * if_weight +
            rule_result["total_risk_score"] * rule_weight
        )
        
        # 5. Determine risk level
        risk_level = self._determine_risk_level(combined_score)
        
        # 6. Generate recommendations
        recommendations = self._generate_recommendations(
            combined_score, 
            rule_result["triggered_rules"]
        )
        
        # 7. Compile risk factors
        risk_factors = []
        for rule in rule_result["triggered_rules"]:
            risk_factors.append(rule["rule_name"])
        
        if if_result["is_anomaly"]:
            risk_factors.append("Statistical Anomaly Detected")
        
        return FraudAnalysisResponse(
            risk_score=round(combined_score, 4),
            risk_level=risk_level,
            risk_factors=risk_factors,
            anomaly_detected=if_result["is_anomaly"],
            model_version=self.model_version,
            recommendations=recommendations,
            raw_output={
                "isolation_forest": if_result,
                "rule_engine": rule_result,
                "combined_score": combined_score
            }
        )
    
    def _extract_features(self, request: FraudAnalysisRequest) -> np.ndarray:
        """Extract numerical features for ML models"""
        
        features = [
            request.gst_filing_months or 0,
            request.employee_count or 0,
            request.annual_turnover or 0,
            request.business_age or 0,
            1 if request.pan_number else 0,
            1 if request.gstin else 0,
            1 if request.udyam_number else 0,
            self._calculate_turnover_per_employee(request),
            self._calculate_risk_flags(request)
        ]
        
        return np.array(features)
    
    def _calculate_turnover_per_employee(self, request: FraudAnalysisRequest) -> float:
        if request.employee_count and request.employee_count > 0:
            return (request.annual_turnover or 0) / request.employee_count
        return 0
    
    def _calculate_risk_flags(self, request: FraudAnalysisRequest) -> float:
        flags = 0
        if not request.pan_number:
            flags += 1
        if not request.gstin:
            flags += 1
        if not request.udyam_number:
            flags += 1
        if request.business_age and request.business_age < 2:
            flags += 1
        return flags
    
    def _determine_risk_level(self, score: float) -> str:
        if score >= 0.8:
            return "critical"
        elif score >= 0.6:
            return "high"
        elif score >= 0.4:
            return "medium"
        elif score >= 0.2:
            return "low"
        return "minimal"
    
    def _generate_recommendations(self, score: float, triggered_rules: list) -> list:
        recommendations = []
        
        if score >= 0.8:
            recommendations.extend([
                "Immediately reject application",
                "Escalate to fraud investigation team",
                "File suspicious activity report"
            ])
        elif score >= 0.6:
            recommendations.extend([
                "Request additional documentation",
                "Conduct in-person verification",
                "Manual review by senior officer"
            ])
        elif score >= 0.4:
            recommendations.extend([
                "Request clarification on flagged items",
                "Verify with third-party data sources"
            ])
        else:
            recommendations.append("Standard processing recommended")
        
        # Add specific recommendations based on triggered rules
        for rule in triggered_rules:
            if rule["rule_id"] == "RULE_001":
                recommendations.append("Verify PAN card original")
            elif rule["rule_id"] == "RULE_005":
                recommendations.append("Cross-verify documents against government databases")
        
        return list(set(recommendations))  # Remove duplicates

# Initialize ensemble
fraud_ensemble = FraudEnsemble()

@app.post("/api/v1/fraud/analyze", response_model=FraudAnalysisResponse)
async def analyze_fraud(request: FraudAnalysisRequest):
    """Analyze MSME for fraud detection"""
    return fraud_ensemble.analyze(request)

@app.get("/api/v1/fraud/model-info")
async def get_model_info():
    """Get model information"""
    return {
        "model_version": fraud_ensemble.model_version,
        "models_used": ["Isolation Forest", "Rule Engine"],
        "last_trained": "2024-01-15",
        "accuracy": 0.92
    }

@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "ai-fraud"}
```

---

## 8.4 RECOMMENDATION SERVICE (JAVA)

```java
// service/RecommendationService.java
package com.msme.recommendation.service;

import com.msme.recommendation.model.Recommendation;
import com.msme.recommendation.repository.RecommendationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RecommendationService {
    
    private final RecommendationRepository recommendationRepository;
    private final RuleEngineService ruleEngineService;
    
    public RecommendationService(
            RecommendationRepository recommendationRepository,
            RuleEngineService ruleEngineService) {
        this.recommendationRepository = recommendationRepository;
        this.ruleEngineService = ruleEngineService;
    }
    
    public List<Recommendation> generateRecommendations(UUID msmeId, Map<String, Object> msmeData) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // 1. Run rule engine
        var ruleRecommendations = ruleEngineService.evaluate(msmeId, msmeData);
        recommendations.addAll(ruleRecommendations);
        
        // 2. Generate score-based recommendations
        var scoreRecommendations = generateScoreBasedRecommendations(msmeId, msmeData);
        recommendations.addAll(scoreRecommendations);
        
        // 3. Generate industry-specific recommendations
        var industryRecommendations = generateIndustryRecommendations(msmeId, msmeData);
        recommendations.addAll(industryRecommendations);
        
        // 4. Save and return
        return recommendationRepository.saveAll(recommendations);
    }
    
    private List<Recommendation> generateScoreBasedRecommendations(UUID msmeId, Map<String, Object> data) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        double healthScore = (double) data.getOrDefault("healthScore", 50.0);
        double cashFlowScore = (double) data.getOrDefault("cashFlowScore", 50.0);
        double complianceScore = (double) data.getOrDefault("complianceScore", 50.0);
        
        // Health Score Recommendations
        if (healthScore < 40) {
            recommendations.add(createRecommendation(
                msmeId,
                "CRITICAL",
                "IMMEDIATE_ACTION",
                "Financial health is critically low. Consider restructuring existing debts and exploring government MSME support schemes.",
                "health_score"
            ));
        } else if (healthScore < 60) {
            recommendations.add(createRecommendation(
                msmeId,
                "HIGH",
                "IMPROVEMENT",
                "Focus on improving cash flow management and reducing operational costs.",
                "health_score"
            ));
        }
        
        // Cash Flow Recommendations
        if (cashFlowScore < 40) {
            recommendations.add(createRecommendation(
                msmeId,
                "HIGH",
                "CASH_FLOW",
                "Implement stricter payment collection policies. Consider invoice factoring for immediate liquidity.",
                "cash_flow"
            ));
        }
        
        // Compliance Recommendations
        if (complianceScore < 50) {
            recommendations.add(createRecommendation(
                msmeId,
                "MEDIUM",
                "COMPLIANCE",
                "Ensure timely filing of GST returns and EPF contributions to avoid penalties.",
                "compliance"
            ));
        }
        
        return recommendations;
    }
    
    private List<Recommendation> generateIndustryRecommendations(UUID msmeId, Map<String, Object> data) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        String industry = (String) data.getOrDefault("industry", "default");
        
        switch (industry.toLowerCase()) {
            case "manufacturing":
                recommendations.add(createRecommendation(
                    msmeId,
                    "MEDIUM",
                    "INDUSTRY",
                    "Consider investing in automation to improve efficiency and reduce dependency on manual labor.",
                    "industry"
                ));
                break;
            case "services":
                recommendations.add(createRecommendation(
                    msmeId,
                    "MEDIUM",
                    "INDUSTRY",
                    "Focus on customer retention and recurring revenue models.",
                    "industry"
                ));
                break;
            case "trading":
                recommendations.add(createRecommendation(
                    msmeId,
                    "MEDIUM",
                    "INDUSTRY",
                    "Optimize inventory management and negotiate better payment terms with suppliers.",
                    "industry"
                ));
                break;
        }
        
        return recommendations;
    }
    
    private Recommendation createRecommendation(
            UUID msmeId,
            String priority,
            String category,
            String description,
            String source) {
        
        Recommendation rec = new Recommendation();
        rec.setMsmeId(msmeId);
        rec.setPriority(priority);
        rec.setCategory(category);
        rec.setDescription(description);
        rec.setSource(source);
        rec.setStatus("active");
        rec.setCreatedAt(LocalDateTime.now());
        
        return rec;
    }
    
    public List<Recommendation> getRecommendations(UUID msmeId) {
        return recommendationRepository.findByMsmeIdOrderByCreatedAtDesc(msmeId);
    }
    
    public Recommendation acknowledgeRecommendation(UUID recommendationId) {
        Recommendation rec = recommendationRepository.findById(recommendationId)
            .orElseThrow(() -> new RuntimeException("Recommendation not found"));
        
        rec.setStatus("acknowledged");
        return recommendationRepository.save(rec);
    }
}
```

```java
// service/RuleEngineService.java
package com.msme.recommendation.service;

import com.msme.recommendation.model.Recommendation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RuleEngineService {
    
    private final List<RecommendationRule> rules;
    
    public RuleEngineService() {
        this.rules = initializeRules();
    }
    
    private List<RecommendationRule> initializeRules() {
        List<RecommendationRule> rules = new ArrayList<>();
        
        // Rule 1: Low Liquidity Warning
        rules.add(new RecommendationRule(
            "RULE_LIQUIDITY_001",
            "Low Liquidity Alert",
            data -> {
                double liquidity = (double) data.getOrDefault("liquidityScore", 100.0);
                return liquidity < 40;
            },
            "HIGH",
            "LIQUIDITY",
            "Liquidity is critically low. Maintain at least 3 months of operating expenses as cash reserve."
        ));
        
        // Rule 2: Payment Delay Pattern
        rules.add(new RecommendationRule(
            "RULE_PAYMENT_001",
            "Payment Delay Pattern Detected",
            data -> {
                int avgDelayDays = (int) data.getOrDefault("avgPaymentDelayDays", 0);
                return avgDelayDays > 30;
            },
            "MEDIUM",
            "PAYMENT",
            "Consistent payment delays detected. Consider negotiating extended payment terms with suppliers."
        ));
        
        // Rule 3: Revenue Concentration Risk
        rules.add(new RecommendationRule(
            "RULE_REVENUE_001",
            "Revenue Concentration Risk",
            data -> {
                double topCustomerPercent = (double) data.getOrDefault("topCustomerRevenuePercent", 0.0);
                return topCustomerPercent > 50;
            },
            "MEDIUM",
            "REVENUE",
            "High dependency on single customer. Diversify revenue sources to reduce concentration risk."
        ));
        
        // Rule 4: Seasonal Cash Flow
        rules.add(new RecommendationRule(
            "RULE_SEASONAL_001",
            "Seasonal Cash Flow Preparation",
            data -> {
                boolean hasSeasonality = (boolean) data.getOrDefault("hasSeasonality", false);
                double currentMonth = (double) data.getOrDefault("currentMonth", 0);
                return hasSeasonality && (currentMonth >= 10 || currentMonth <= 2);
            },
            "LOW",
            "PLANNING",
            "Prepare for seasonal downturn. Build cash reserves during peak months."
        ));
        
        return rules;
    }
    
    public List<Recommendation> evaluate(UUID msmeId, Map<String, Object> data) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        for (RecommendationRule rule : rules) {
            if (rule.getCondition().test(data)) {
                Recommendation rec = new Recommendation();
                rec.setMsmeId(msmeId);
                rec.setPriority(rule.getPriority());
                rec.setCategory(rule.getCategory());
                rec.setDescription(rule.getDescription());
                rec.setSource("rule_engine");
                rec.setRuleId(rule.getRuleId());
                rec.setStatus("active");
                rec.setCreatedAt(LocalDateTime.now());
                
                recommendations.add(rec);
            }
        }
        
        return recommendations;
    }
}
```

---

## 8.5 KAFKA EVENT HANDLING

```java
// config/KafkaConfig.java
package com.msme.fraud.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    
    @Bean
    public NewTopic fraudEventsTopic() {
        return TopicBuilder.name("fraud.events")
            .partitions(6)
            .replicas(3)
            .build();
    }
    
    @Bean
    public NewTopic fraudAlertsTopic() {
        return TopicBuilder.name("fraud.alerts")
            .partitions(3)
            .replicas(3)
            .build();
    }
}
```

```java
// listener/FraudEventListener.java
package com.msme.fraud.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FraudEventListener {
    
    private final FraudDetectionService fraudDetectionService;
    
    public FraudEventListener(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }
    
    @KafkaListener(topics = "loan.events", groupId = "fraud-service")
    public void handleLoanApplication(LoanApplicationEvent event) {
        if ("submitted".equals(event.getStatus())) {
            // Trigger fraud check for new loan applications
            FraudCheckRequest request = FraudCheckRequest.builder()
                .msmeId(event.getMsmeId())
                .applicationId(event.getApplicationId())
                .build();
            
            fraudDetectionService.performFraudCheck(request);
        }
    }
    
    @KafkaListener(topics = "aggregation.events", groupId = "fraud-service")
    public void handleDataFetched(DataFetchedEvent event) {
        // Analyze newly fetched data for fraud indicators
        // This can trigger additional fraud checks based on AA data
    }
}
```

---

## 8.6 API ENDPOINTS SUMMARY

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/fraud/analyze` | POST | Analyze MSME for fraud |
| `/api/v1/fraud/check/{checkId}` | GET | Get fraud check result |
| `/api/v1/fraud/alerts` | GET | Get open fraud alerts |
| `/api/v1/fraud/alerts/{alertId}` | PUT | Resolve fraud alert |
| `/api/v1/recommendations/{msmeId}` | GET | Get MSME recommendations |
| `/api/v1/recommendations` | POST | Generate recommendations |
| `/api/v1/recommendations/{id}/acknowledge` | PUT | Acknowledge recommendation |
| `/api/v1/fraud/model-info` | GET | Get ML model info |
| `/api/v1/fraud/health` | GET | Health check |

---

## 8.7 DOCKERFILE

```dockerfile
# ai-services/fraud/Dockerfile
FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY . .

# Expose port
EXPOSE 5003

# Run the application
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "5003"]
```

---

## 8.8 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| Fraud Service (Java) | 3 days |
| AI Fraud Service (Python) | 3 days |
| Recommendation Service | 2 days |
| Rule Engine | 2 days |
| Kafka Integration | 1 day |
| API Integration | 1 day |
| Testing | 2 days |
| **Total** | **14 days** |

---

## 8.9 HACKATHON PRIORITY

**HIGH** - Core feature demonstrating AI/ML capabilities
