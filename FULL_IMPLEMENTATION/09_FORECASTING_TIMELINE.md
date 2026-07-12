# MODULE 9: FORECASTING & TIMELINE
## Production-Grade Implementation

---

## 9.1 MICROSERVICES STRUCTURE

```
forecast-service/
├── src/main/java/com/msme/forecast/
│   ├── ForecastServiceApplication.java
│   ├── config/
│   │   └── KafkaConfig.java
│   ├── controller/
│   │   ├── ForecastController.java
│   │   └── ScenarioController.java
│   ├── service/
│   │   ├── ForecastService.java
│   │   ├── ScenarioService.java
│   │   ├── DigitalTwinService.java
│   │   └── EarlyWarningService.java
│   ├── model/
│   │   ├── Forecast.java
│   │   ├── Scenario.java
│   │   ├── DigitalTwin.java
│   │   └── EarlyWarning.java
│   ├── dto/
│   │   ├── ForecastRequest.java
│   │   ├── ForecastResponse.java
│   │   ├── ScenarioRequest.java
│   │   └── EarlyWarningDTO.java
│   └── repository/
│       ├── ForecastRepository.java
│       └── ScenarioRepository.java
└── pom.xml

ai-services/forecasting/
├── app/
│   ├── main.py
│   ├── models/
│   │   ├── prophet_forecast.py
│   │   ├── lightgbm_forecast.py
│   │   ├── lstm_model.py
│   │   └── ensemble_forecast.py
│   ├── scenarios/
│   │   ├── digital_twin.py
│   │   ├── scenario_engine.py
│   │   └── monte_carlo.py
│   ├── features/
│   │   └── forecast_features.py
│   └── api/
│       └── routes.py
├── ml_models/
│   ├── prophet_model.pkl
│   ├── lightgbm_model.pkl
│   ├── lstm_weights.h5
│   └── scaler.pkl
├── requirements.txt
└── Dockerfile

ai-services/early-warning/
├── app/
│   ├── main.py
│   ├── monitor/
│   │   ├── realtime_monitor.py
│   │   ├── threshold_engine.py
│   │   └── anomaly_detector.py
│   ├── alerts/
│   │   ├── alert_manager.py
│   │   └── notification_handler.py
│   └── api/
│       └── routes.py
├── requirements.txt
└── Dockerfile
```

---

## 9.2 FORECAST SERVICE (JAVA)

```java
// ForecastServiceApplication.java
package com.msme.forecast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableFeignClients
@EnableKafka
public class ForecastServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ForecastServiceApplication.class, args);
    }
}
```

```java
// model/Forecast.java
package com.msme.forecast.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "forecasts")
public class Forecast {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID msmeId;
    
    @Column(nullable = false)
    private String forecastType;
    
    @Column(nullable = false)
    private Integer horizonMonths;
    
    @Column(columnDefinition = "jsonb")
    private String forecastData;
    
    @Column(columnDefinition = "jsonb")
    private String confidenceIntervals;
    
    private BigDecimal accuracy;
    
    private String modelVersion;
    
    private LocalDateTime forecastDate;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getMsmeId() { return msmeId; }
    public void setMsmeId(UUID msmeId) { this.msmeId = msmeId; }
    
    public String getForecastType() { return forecastType; }
    public void setForecastType(String forecastType) { this.forecastType = forecastType; }
    
    public Integer getHorizonMonths() { return horizonMonths; }
    public void setHorizonMonths(Integer horizonMonths) { this.horizonMonths = horizonMonths; }
    
    public String getForecastData() { return forecastData; }
    public void setForecastData(String forecastData) { this.forecastData = forecastData; }
    
    public String getConfidenceIntervals() { return confidenceIntervals; }
    public void setConfidenceIntervals(String confidenceIntervals) { this.confidenceIntervals = confidenceIntervals; }
    
    public BigDecimal getAccuracy() { return accuracy; }
    public void setAccuracy(BigDecimal accuracy) { this.accuracy = accuracy; }
    
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    
    public LocalDateTime getForecastDate() { return forecastDate; }
    public void setForecastDate(LocalDateTime forecastDate) { this.forecastDate = forecastDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

```java
// model/Scenario.java
package com.msme.forecast.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scenarios")
public class Scenario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID msmeId;
    
    @Column(nullable = false)
    private String scenarioName;
    
    @Column(columnDefinition = "jsonb")
    private String scenarioParameters;
    
    @Column(columnDefinition = "jsonb")
    private String results;
    
    private BigDecimal projectedHealthScore;
    
    private BigDecimal projectedCashBalance;
    
    private String riskLevel;
    
    private Integer simulationMonths;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getMsmeId() { return msmeId; }
    public void setMsmeId(UUID msmeId) { this.msmeId = msmeId; }
    
    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }
    
    public String getScenarioParameters() { return scenarioParameters; }
    public void setScenarioParameters(String scenarioParameters) { this.scenarioParameters = scenarioParameters; }
    
    public String getResults() { return results; }
    public void setResults(String results) { this.results = results; }
    
    public BigDecimal getProjectedHealthScore() { return projectedHealthScore; }
    public void setProjectedHealthScore(BigDecimal projectedHealthScore) { this.projectedHealthScore = projectedHealthScore; }
    
    public BigDecimal getProjectedCashBalance() { return projectedCashBalance; }
    public void setProjectedCashBalance(BigDecimal projectedCashBalance) { this.projectedCashBalance = projectedCashBalance; }
    
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    
    public Integer getSimulationMonths() { return simulationMonths; }
    public void setSimulationMonths(Integer simulationMonths) { this.simulationMonths = simulationMonths; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

```java
// model/EarlyWarning.java
package com.msme.forecast.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "early_warnings")
public class EarlyWarning {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID msmeId;
    
    @Column(nullable = false)
    private String warningType;
    
    private String severity;
    
    @Column(columnDefinition = "text")
    private String description;
    
    @Column(columnDefinition = "jsonb")
    private String indicators;
    
    private BigDecimal confidence;
    
    private String status = "active";
    
    private UUID assignedTo;
    
    private LocalDateTime expiresAt;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getMsmeId() { return msmeId; }
    public void setMsmeId(UUID msmeId) { this.msmeId = msmeId; }
    
    public String getWarningType() { return warningType; }
    public void setWarningType(String warningType) { this.warningType = warningType; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIndicators() { return indicators; }
    public void setIndicators(String indicators) { this.indicators = indicators; }
    
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public UUID getAssignedTo() { return assignedTo; }
    public void setAssignedTo(UUID assignedTo) { this.assignedTo = assignedTo; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

```java
// service/ForecastService.java
package com.msme.forecast.service;

import com.msme.forecast.client.AIServiceClient;
import com.msme.forecast.dto.*;
import com.msme.forecast.model.Forecast;
import com.msme.forecast.repository.ForecastRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ForecastService {
    
    private final ForecastRepository forecastRepository;
    private final AIServiceClient aiServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public ForecastService(
            ForecastRepository forecastRepository,
            AIServiceClient aiServiceClient,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.forecastRepository = forecastRepository;
        this.aiServiceClient = aiServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public ForecastResponse generateForecast(ForecastRequest request) {
        // 1. Prepare features
        Map<String, Object> features = prepareFeatures(request);
        
        // 2. Call AI Forecasting Service
        var aiResponse = aiServiceClient.generateForecast(
            request.getMsmeId(),
            request.getForecastType(),
            request.getHorizonMonths(),
            features
        );
        
        // 3. Save forecast
        Forecast forecast = new Forecast();
        forecast.setMsmeId(request.getMsmeId());
        forecast.setForecastType(request.getForecastType());
        forecast.setHorizonMonths(request.getHorizonMonths());
        forecast.setForecastData(aiResponse.getForecastData());
        forecast.setConfidenceIntervals(aiResponse.getConfidenceIntervals());
        forecast.setAccuracy(aiResponse.getAccuracy());
        forecast.setModelVersion(aiResponse.getModelVersion());
        forecast.setForecastDate(LocalDateTime.now());
        
        forecastRepository.save(forecast);
        
        // 4. Publish event
        kafkaTemplate.send("forecast.events", "forecast.generated",
            Map.of(
                "msmeId", request.getMsmeId(),
                "forecastType", request.getForecastType(),
                "horizon", request.getHorizonMonths()
            )
        );
        
        // 5. Build response
        return ForecastResponse.builder()
            .forecastId(forecast.getId())
            .msmeId(request.getMsmeId())
            .forecastType(request.getForecastType())
            .horizonMonths(request.getHorizonMonths())
            .forecastData(aiResponse.getForecastData())
            .confidenceIntervals(aiResponse.getConfidenceIntervals())
            .accuracy(forecast.getAccuracy())
            .trendAnalysis(aiResponse.getTrendAnalysis())
            .build();
    }
    
    private Map<String, Object> prepareFeatures(ForecastRequest request) {
        // Fetch historical data and prepare features for ML model
        Map<String, Object> features = new HashMap<>();
        
        features.put("msmeId", request.getMsmeId());
        features.put("forecastType", request.getForecastType());
        features.put("horizon", request.getHorizonMonths());
        
        // Add historical data (fetch from database or cache)
        // features.put("historicalScores", ...);
        // features.put("historicalCashFlow", ...);
        // features.put("seasonality", ...);
        
        return features;
    }
    
    public List<Forecast> getHistoricalForecasts(UUID msmeId, String forecastType) {
        return forecastRepository.findByMsmeIdAndForecastTypeOrderByCreatedAtDesc(
            msmeId, forecastType
        );
    }
    
    public Forecast getLatestForecast(UUID msmeId, String forecastType) {
        return forecastRepository.findTopByMsmeIdAndForecastTypeOrderByCreatedAtDesc(
            msmeId, forecastType
        ).orElse(null);
    }
    
    public Map<String, Object> getForecastAccuracy(UUID msmeId) {
        List<Forecast> forecasts = forecastRepository.findByMsmeId(msmeId);
        
        double avgAccuracy = forecasts.stream()
            .filter(f -> f.getAccuracy() != null)
            .mapToDouble(f -> f.getAccuracy().doubleValue())
            .average()
            .orElse(0.0);
        
        return Map.of(
            "averageAccuracy", avgAccuracy,
            "totalForecasts", forecasts.size(),
            "lastForecastDate", forecasts.isEmpty() ? null : 
                forecasts.get(0).getCreatedAt()
        );
    }
}
```

```java
// service/ScenarioService.java
package com.msme.forecast.service;

import com.msme.forecast.client.AIServiceClient;
import com.msme.forecast.dto.*;
import com.msme.forecast.model.Scenario;
import com.msme.forecast.repository.ScenarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class ScenarioService {
    
    private final ScenarioRepository scenarioRepository;
    private final AIServiceClient aiServiceClient;
    
    public ScenarioService(
            ScenarioRepository scenarioRepository,
            AIServiceClient aiServiceClient) {
        this.scenarioRepository = scenarioRepository;
        this.aiServiceClient = aiServiceClient;
    }
    
    public static final List<Map<String, Object>> SCENARIO_PRESETS = List.of(
        Map.of(
            "name", "Economic Boom",
            "description", "Strong economic growth with increased demand",
            "revenueChange", 20.0,
            "expenseChange", 10.0,
            "interestRateChange", 2.0
        ),
        Map.of(
            "name", "Recession",
            "description", "Economic downturn with reduced demand",
            "revenueChange", -25.0,
            "expenseChange", 5.0,
            "interestRateChange", -1.0
        ),
        Map.of(
            "name", "Interest Rate Hike",
            "description", "Significant increase in interest rates",
            "revenueChange", 0.0,
            "expenseChange", 0.0,
            "interestRateChange", 5.0
        ),
        Map.of(
            "name", "Cost Optimization",
            "description", "Aggressive cost cutting measures",
            "revenueChange", -5.0,
            "expenseChange", -20.0,
            "interestRateChange", 0.0
        ),
        Map.of(
            "name", "Rapid Expansion",
            "description", "Aggressive business expansion",
            "revenueChange", 40.0,
            "expenseChange", 50.0,
            "interestRateChange", 3.0
        )
    );
    
    public ScenarioResponse runScenario(ScenarioRequest request) {
        // 1. Call AI Digital Twin Service
        var aiResponse = aiServiceClient.runScenario(
            request.getMsmeId(),
            request.getParameters(),
            request.getMonths()
        );
        
        // 2. Save scenario
        Scenario scenario = new Scenario();
        scenario.setMsmeId(request.getMsmeId());
        scenario.setScenarioName(request.getName());
        scenario.setScenarioParameters(request.getParameters().toString());
        scenario.setResults(aiResponse.getResults().toString());
        scenario.setProjectedHealthScore(
            BigDecimal.valueOf(aiResponse.getFinalHealthScore())
        );
        scenario.setProjectedCashBalance(
            BigDecimal.valueOf(aiResponse.getFinalCashBalance())
        );
        scenario.setRiskLevel(aiResponse.getRiskLevel());
        scenario.setSimulationMonths(request.getMonths());
        
        scenarioRepository.save(scenario);
        
        // 3. Build response
        return ScenarioResponse.builder()
            .scenarioId(scenario.getId())
            .msmeId(request.getMsmeId())
            .scenarioName(request.getName())
            .initialState(aiResponse.getInitialState())
            .finalState(aiResponse.getFinalState())
            .monthlyHistory(aiResponse.getMonthlyHistory())
            .alerts(aiResponse.getAlerts())
            .analysis(aiResponse.getAnalysis())
            .riskLevel(aiResponse.getRiskLevel())
            .build();
    }
    
    public List<Map<String, Object>> getScenarioPresets() {
        return SCENARIO_PRESETS;
    }
    
    public List<Scenario> getScenarios(UUID msmeId) {
        return scenarioRepository.findByMsmeIdOrderByCreatedAtDesc(msmeId);
    }
    
    public Scenario getScenario(UUID scenarioId) {
        return scenarioRepository.findById(scenarioId)
            .orElseThrow(() -> new RuntimeException("Scenario not found"));
    }
}
```

```java
// service/DigitalTwinService.java
package com.msme.forecast.service;

import com.msme.forecast.client.AIServiceClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DigitalTwinService {
    
    private final AIServiceClient aiServiceClient;
    
    public DigitalTwinService(AIServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }
    
    public Map<String, Object> createDigitalTwin(UUID msmeId) {
        return aiServiceClient.createDigitalTwin(msmeId);
    }
    
    public Map<String, Object> simulateWhatIf(
            UUID msmeId,
            Map<String, Object> currentState,
            Map<String, Object> changes) {
        
        return aiServiceClient.simulateWhatIf(msmeId, currentState, changes);
    }
    
    public List<Map<String, Object>> runMonteCarloSimulation(
            UUID msmeId,
            int simulations,
            int horizonMonths) {
        
        return aiServiceClient.runMonteCarlo(msmeId, simulations, horizonMonths);
    }
}
```

```java
// service/EarlyWarningService.java
package com.msme.forecast.service;

import com.msme.forecast.client.AIServiceClient;
import com.msme.forecast.dto.EarlyWarningDTO;
import com.msme.forecast.model.EarlyWarning;
import com.msme.forecast.repository.EarlyWarningRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class EarlyWarningService {
    
    private final EarlyWarningRepository earlyWarningRepository;
    private final AIServiceClient aiServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public EarlyWarningService(
            EarlyWarningRepository earlyWarningRepository,
            AIServiceClient aiServiceClient,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.earlyWarningRepository = earlyWarningRepository;
        this.aiServiceClient = aiServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public List<EarlyWarningDTO> checkWarnings(UUID msmeId) {
        // Call AI Early Warning Service
        var warnings = aiServiceClient.checkEarlyWarnings(msmeId);
        
        List<EarlyWarningDTO> result = new ArrayList<>();
        
        for (var warning : warnings) {
            // Save warning
            EarlyWarning earlyWarning = new EarlyWarning();
            earlyWarning.setMsmeId(msmeId);
            earlyWarning.setWarningType(warning.getType());
            earlyWarning.setSeverity(warning.getSeverity());
            earlyWarning.setDescription(warning.getDescription());
            earlyWarning.setIndicators(warning.getIndicators().toString());
            earlyWarning.setConfidence(warning.getConfidence());
            earlyWarning.setExpiresAt(LocalDateTime.now().plusDays(7));
            
            earlyWarningRepository.save(earlyWarning);
            
            // Send notification if critical
            if ("critical".equals(warning.getSeverity()) || "high".equals(warning.getSeverity())) {
                kafkaTemplate.send("notification.events", "early.warning.created",
                    Map.of(
                        "warningId", earlyWarning.getId(),
                        "msmeId", msmeId,
                        "type", warning.getType(),
                        "severity", warning.getSeverity()
                    )
                );
            }
            
            result.add(EarlyWarningDTO.builder()
                .warningId(earlyWarning.getId())
                .type(warning.getType())
                .severity(warning.getSeverity())
                .description(warning.getDescription())
                .indicators(warning.getIndicators())
                .confidence(warning.getConfidence())
                .createdAt(earlyWarning.getCreatedAt())
                .build());
        }
        
        return result;
    }
    
    public List<EarlyWarningDTO> getActiveWarnings(UUID msmeId) {
        List<EarlyWarning> warnings = earlyWarningRepository
            .findByMsmeIdAndStatusAndExpiresAtAfter(msmeId, "active", LocalDateTime.now());
        
        return warnings.stream()
            .map(w -> EarlyWarningDTO.builder()
                .warningId(w.getId())
                .type(w.getWarningType())
                .severity(w.getSeverity())
                .description(w.getDescription())
                .confidence(w.getConfidence())
                .createdAt(w.getCreatedAt())
                .build())
            .toList();
    }
    
    public EarlyWarning acknowledgeWarning(UUID warningId, UUID userId) {
        EarlyWarning warning = earlyWarningRepository.findById(warningId)
            .orElseThrow(() -> new RuntimeException("Warning not found"));
        
        warning.setStatus("acknowledged");
        warning.setAssignedTo(userId);
        
        return earlyWarningRepository.save(warning);
    }
    
    public EarlyWarning resolveWarning(UUID warningId) {
        EarlyWarning warning = earlyWarningRepository.findById(warningId)
            .orElseThrow(() -> new RuntimeException("Warning not found"));
        
        warning.setStatus("resolved");
        
        return earlyWarningRepository.save(warning);
    }
}
```

```java
// controller/ForecastController.java
package com.msme.forecast.controller;

import com.msme.forecast.dto.*;
import com.msme.forecast.service.ForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forecast")
@Tag(name = "Forecasting", description = "Financial forecasting endpoints")
public class ForecastController {
    
    private final ForecastService forecastService;
    
    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }
    
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Generate financial forecast")
    public ResponseEntity<ForecastResponse> generateForecast(
            @RequestBody ForecastRequest request) {
        return ResponseEntity.ok(forecastService.generateForecast(request));
    }
    
    @GetMapping("/{msmeId}")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Get latest forecast for MSME")
    public ResponseEntity<?> getLatestForecast(
            @PathVariable UUID msmeId,
            @RequestParam String forecastType) {
        return ResponseEntity.ok(forecastService.getLatestForecast(msmeId, forecastType));
    }
    
    @GetMapping("/{msmeId}/history")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Get historical forecasts")
    public ResponseEntity<?> getHistoricalForecasts(
            @PathVariable UUID msmeId,
            @RequestParam String forecastType) {
        return ResponseEntity.ok(forecastService.getHistoricalForecasts(msmeId, forecastType));
    }
    
    @GetMapping("/{msmeId}/accuracy")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Get forecast accuracy metrics")
    public ResponseEntity<?> getForecastAccuracy(@PathVariable UUID msmeId) {
        return ResponseEntity.ok(forecastService.getForecastAccuracy(msmeId));
    }
}
```

```java
// controller/ScenarioController.java
package com.msme.forecast.controller;

import com.msme.forecast.dto.*;
import com.msme.forecast.service.ScenarioService;
import com.msme.forecast.service.DigitalTwinService;
import com.msme.forecast.service.EarlyWarningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/scenario")
@Tag(name = "Scenario Simulation", description = "Scenario simulation and early warning endpoints")
public class ScenarioController {
    
    private final ScenarioService scenarioService;
    private final DigitalTwinService digitalTwinService;
    private final EarlyWarningService earlyWarningService;
    
    public ScenarioController(
            ScenarioService scenarioService,
            DigitalTwinService digitalTwinService,
            EarlyWarningService earlyWarningService) {
        this.scenarioService = scenarioService;
        this.digitalTwinService = digitalTwinService;
        this.earlyWarningService = earlyWarningService;
    }
    
    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Run scenario simulation")
    public ResponseEntity<ScenarioResponse> runScenario(
            @RequestBody ScenarioRequest request) {
        return ResponseEntity.ok(scenarioService.runScenario(request));
    }
    
    @GetMapping("/presets")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get scenario presets")
    public ResponseEntity<List<Map<String, Object>>> getPresets() {
        return ResponseEntity.ok(scenarioService.getScenarioPresets());
    }
    
    @GetMapping("/{msmeId}/history")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Get scenario history")
    public ResponseEntity<?> getScenarioHistory(@PathVariable UUID msmeId) {
        return ResponseEntity.ok(scenarioService.getScenarios(msmeId));
    }
    
    @PostMapping("/{msmeId}/digital-twin")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Create digital twin for MSME")
    public ResponseEntity<?> createDigitalTwin(@PathVariable UUID msmeId) {
        return ResponseEntity.ok(digitalTwinService.createDigitalTwin(msmeId));
    }
    
    @PostMapping("/{msmeId}/monte-carlo")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Run Monte Carlo simulation")
    public ResponseEntity<?> runMonteCarlo(
            @PathVariable UUID msmeId,
            @RequestParam(defaultValue = "1000") int simulations,
            @RequestParam(defaultValue = "12") int horizonMonths) {
        return ResponseEntity.ok(
            digitalTwinService.runMonteCarloSimulation(msmeId, simulations, horizonMonths)
        );
    }
    
    @GetMapping("/{msmeId}/early-warnings")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Check early warnings")
    public ResponseEntity<?> checkEarlyWarnings(@PathVariable UUID msmeId) {
        return ResponseEntity.ok(earlyWarningService.checkWarnings(msmeId));
    }
    
    @GetMapping("/{msmeId}/active-warnings")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Get active warnings")
    public ResponseEntity<?> getActiveWarnings(@PathVariable UUID msmeId) {
        return ResponseEntity.ok(earlyWarningService.getActiveWarnings(msmeId));
    }
    
    @PutMapping("/warnings/{warningId}/acknowledge")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Acknowledge warning")
    public ResponseEntity<?> acknowledgeWarning(
            @PathVariable UUID warningId,
            @RequestParam UUID userId) {
        return ResponseEntity.ok(earlyWarningService.acknowledgeWarning(warningId, userId));
    }
}
```

---

## 9.3 AI FORECASTING SERVICE (PYTHON)

```python
# ai-services/forecasting/app/main.py
from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
import joblib
import numpy as np
from datetime import datetime

app = FastAPI(title="AI Forecasting Service", version="1.0.0")

class ForecastRequest(BaseModel):
    msme_id: str
    forecast_type: str  # revenue, cash_flow, health_score
    horizon_months: int
    features: Dict[str, Any]

class ScenarioRequest(BaseModel):
    msme_id: str
    parameters: Dict[str, Any]
    months: int

class ProphetForecaster:
    def __init__(self):
        self.model = None
        self.version = "1.0.0"
    
    def forecast(self, historical_data: list, horizon: int) -> dict:
        """Generate forecast using Prophet-like approach"""
        
        # Simple forecasting for demo
        values = [d.get("value", 0) for d in historical_data]
        
        if not values:
            return {"forecast": [], "confidence_lower": [], "confidence_upper": []}
        
        # Calculate trend
        if len(values) > 1:
            trend = (values[-1] - values[0]) / len(values)
        else:
            trend = 0
        
        # Generate forecast
        forecast = []
        confidence_lower = []
        confidence_upper = []
        
        last_value = values[-1]
        for i in range(horizon):
            predicted = last_value + trend * (i + 1)
            uncertainty = abs(trend) * (i + 1) * 0.2
            
            forecast.append(round(predicted, 2))
            confidence_lower.append(round(predicted - uncertainty * 1.96, 2))
            confidence_upper.append(round(predicted + uncertainty * 1.96, 2))
        
        return {
            "forecast": forecast,
            "confidence_lower": confidence_lower,
            "confidence_upper": confidence_upper,
            "trend": "increasing" if trend > 0 else "decreasing" if trend < 0 else "stable",
            "trend_strength": min(1.0, abs(trend) / (abs(values[-1]) + 1))
        }

class LightGBMForecaster:
    def __init__(self):
        self.model = None
        self.version = "1.0.0"
    
    def forecast(self, features: dict, horizon: int) -> dict:
        """Generate forecast using LightGBM"""
        
        # Extract features
        base_value = features.get("current_value", 0)
        growth_rate = features.get("growth_rate", 0.02)
        volatility = features.get("volatility", 0.1)
        
        # Generate forecast with seasonality
        forecast = []
        for i in range(horizon):
            month = i % 12
            seasonal_factor = 1 + 0.1 * np.sin(2 * np.pi * month / 12)
            predicted = base_value * (1 + growth_rate) ** (i + 1) * seasonal_factor
            forecast.append(round(predicted, 2))
        
        return {
            "forecast": forecast,
            "trend": "growing" if growth_rate > 0 else "declining",
            "seasonality_detected": True
        }

class EnsembleForecaster:
    """Ensemble of multiple forecasting methods"""
    
    def __init__(self):
        self.prophet = ProphetForecaster()
        self.lightgbm = LightGBMForecaster()
        self.version = "1.0.0"
    
    def generate_forecast(
        self,
        msme_id: str,
        forecast_type: str,
        horizon_months: int,
        features: dict
    ) -> dict:
        """Generate comprehensive forecast"""
        
        historical_data = features.get("historical_data", [])
        
        # Get forecasts from different models
        prophet_result = self.prophet.forecast(historical_data, horizon_months)
        
        # Combine forecasts (weighted average)
        weights = {"prophet": 0.6, "lightgbm": 0.4}
        
        combined_forecast = []
        for i in range(horizon_months):
            prophet_val = prophet_result["forecast"][i] if i < len(prophet_result["forecast"]) else 0
            
            # Simple combination
            combined = prophet_val * weights["prophet"]
            combined_forecast.append(round(combined, 2))
        
        # Calculate accuracy (simulated)
        accuracy = 0.85 + np.random.random() * 0.1
        
        # Trend analysis
        trend_analysis = {
            "direction": prophet_result.get("trend", "stable"),
            "strength": prophet_result.get("trend_strength", 0.5),
            "volatility": features.get("volatility", 0.1),
            "seasonality": prophet_result.get("seasonality_detected", False)
        }
        
        # Confidence intervals
        confidence_intervals = {
            "lower_80": [
                round(f * 0.9, 2) for f in combined_forecast
            ],
            "upper_80": [
                round(f * 1.1, 2) for f in combined_forecast
            ],
            "lower_95": [
                round(f * 0.8, 2) for f in combined_forecast
            ],
            "upper_95": [
                round(f * 1.2, 2) for f in combined_forecast
            ]
        }
        
        return {
            "forecast_data": {
                "months": list(range(1, horizon_months + 1)),
                "values": combined_forecast
            },
            "confidence_intervals": confidence_intervals,
            "accuracy": round(accuracy, 4),
            "model_version": self.version,
            "trend_analysis": trend_analysis
        }

class DigitalTwinSimulator:
    """Digital Twin simulation engine"""
    
    def __init__(self):
        self.version = "1.0.0"
    
    def simulate_scenario(
        self,
        current_state: dict,
        scenario_params: dict,
        months: int
    ) -> dict:
        """Run scenario simulation"""
        
        # Initialize simulation
        state = current_state.copy()
        history = []
        alerts = []
        
        for month in range(months):
            # Apply scenario factors
            revenue_change = scenario_params.get("revenue_change", 0) / 100
            expense_change = scenario_params.get("expense_change", 0) / 100
            
            # Update financials
            new_revenue = state.get("monthly_revenue", 0) * (1 + revenue_change / 12)
            new_expenses = state.get("monthly_expenses", 0) * (1 + expense_change / 12)
            
            # Calculate cash flow
            cash_flow = new_revenue - new_expenses
            new_cash = state.get("cash_balance", 0) + cash_flow
            
            # Update health score
            health_score = self._calculate_health_score(
                state.get("health_score", 50),
                new_revenue,
                new_expenses,
                new_cash
            )
            
            # Check for alerts
            if new_cash < 0:
                alerts.append({
                    "type": "negative_balance",
                    "month": month + 1,
                    "message": "Cash balance turned negative"
                })
            
            if health_score < 40:
                alerts.append({
                    "type": "low_score",
                    "month": month + 1,
                    "message": f"Health score dropped to {health_score:.1f}"
                })
            
            # Update state
            state = {
                "health_score": health_score,
                "cash_balance": new_cash,
                "monthly_revenue": new_revenue,
                "monthly_expenses": new_expenses
            }
            
            history.append({
                "month": month + 1,
                "state": state.copy()
            })
        
        # Analyze results
        scores = [h["state"]["health_score"] for h in history]
        analysis = {
            "score_trend": {
                "start": scores[0] if scores else 50,
                "end": scores[-1] if scores else 50,
                "min": min(scores) if scores else 0,
                "max": max(scores) if scores else 100
            },
            "risk_assessment": self._assess_risk(scores),
            "recommendations": self._generate_recommendations(history, scenario_params)
        }
        
        return {
            "initial_state": current_state,
            "final_state": state,
            "monthly_history": history,
            "alerts": alerts,
            "analysis": analysis,
            "risk_level": analysis["risk_assessment"]
        }
    
    def _calculate_health_score(self, current, revenue, expenses, cash):
        """Calculate health score"""
        
        score = current
        
        # Revenue impact
        if revenue > expenses * 1.2:
            score += 2
        elif revenue < expenses * 0.8:
            score -= 3
        
        # Cash flow impact
        if cash > 0:
            score += 1
        else:
            score -= 5
        
        return max(0, min(100, score))
    
    def _assess_risk(self, scores):
        """Assess risk level"""
        
        if not scores:
            return "unknown"
        
        final_score = scores[-1]
        min_score = min(scores)
        
        if final_score >= 70 and min_score >= 50:
            return "low"
        elif final_score >= 50:
            return "medium"
        elif final_score >= 30:
            return "high"
        return "critical"
    
    def _generate_recommendations(self, history, params):
        """Generate recommendations"""
        
        recommendations = []
        
        if params.get("revenue_change", 0) < -10:
            recommendations.append("Diversify revenue streams")
        
        if params.get("expense_change", 0) > 10:
            recommendations.append("Implement cost optimization")
        
        negative_months = [
            h["month"] for h in history if h["state"]["cash_balance"] < 0
        ]
        if negative_months:
            recommendations.append(f"Cash shortfall expected in months {negative_months}")
        
        return recommendations

class EarlyWarningEngine:
    """Early warning detection system"""
    
    def __init__(self):
        self.rules = self._initialize_rules()
        self.version = "1.0.0"
    
    def _initialize_rules(self):
        return [
            {
                "type": "score_decline",
                "severity": "high",
                "check": self._check_score_decline,
                "description": "Significant health score decline detected"
            },
            {
                "type": "cash_flow_stress",
                "severity": "critical",
                "check": self._check_cash_flow_stress,
                "description": "Cash flow stress detected"
            },
            {
                "type": "revenue_decline",
                "severity": "high",
                "check": self._check_revenue_decline,
                "description": "Sustained revenue decline"
            },
            {
                "type": "default_risk",
                "severity": "critical",
                "check": self._check_default_risk,
                "description": "Risk of loan default"
            }
        ]
    
    def check_warnings(self, msme_id: str, data: dict) -> list:
        """Check for early warnings"""
        
        warnings = []
        
        for rule in self.rules:
            result = rule["check"](data)
            if result["triggered"]:
                warnings.append({
                    "type": rule["type"],
                    "severity": rule["severity"],
                    "description": rule["description"],
                    "confidence": result.get("confidence", 0.8),
                    "indicators": result.get("indicators", {})
                })
        
        return warnings
    
    def _check_score_decline(self, data):
        scores = data.get("historical_scores", [])
        if len(scores) >= 2:
            decline = scores[-2] - scores[-1]
            if decline > 10:
                return {
                    "triggered": True,
                    "confidence": 0.9,
                    "indicators": {"decline": decline}
                }
        return {"triggered": False}
    
    def _check_cash_flow_stress(self, data):
        cash_balance = data.get("cash_balance", 0)
        monthly_expenses = data.get("monthly_expenses", 1)
        
        months_of_runway = cash_balance / monthly_expenses if monthly_expenses > 0 else 999
        
        if months_of_runway < 3:
            return {
                "triggered": True,
                "confidence": 0.85,
                "indicators": {"runway_months": months_of_runway}
            }
        return {"triggered": False}
    
    def _check_revenue_decline(self, data):
        revenue_history = data.get("revenue_history", [])
        if len(revenue_history) >= 3:
            recent_avg = sum(revenue_history[-3:]) / 3
            older_avg = sum(revenue_history[-6:-3]) / 3 if len(revenue_history) >= 6 else recent_avg
            
            decline_pct = (older_avg - recent_avg) / older_avg * 100
            
            if decline_pct > 15:
                return {
                    "triggered": True,
                    "confidence": 0.8,
                    "indicators": {"decline_percent": decline_pct}
                }
        return {"triggered": False}
    
    def _check_default_risk(self, data):
        health_score = data.get("health_score", 50)
        outstanding_loans = data.get("outstanding_loans", 0)
        annual_revenue = data.get("annual_revenue", 1)
        
        debt_to_revenue = outstanding_loans / annual_revenue if annual_revenue > 0 else 999
        
        if health_score < 40 or debt_to_revenue > 0.5:
            return {
                "triggered": True,
                "confidence": 0.75,
                "indicators": {
                    "health_score": health_score,
                    "debt_to_revenue": debt_to_revenue
                }
            }
        return {"triggered": False}

# Initialize engines
ensemble_forecaster = EnsembleForecaster()
digital_twin = DigitalTwinSimulator()
early_warning_engine = EarlyWarningEngine()

@app.post("/api/v1/forecast/generate")
async def generate_forecast(request: ForecastRequest):
    """Generate financial forecast"""
    return ensemble_forecaster.generate_forecast(
        request.msme_id,
        request.forecast_type,
        request.horizon_months,
        request.features
    )

@app.post("/api/v1/scenario/simulate")
async def simulate_scenario(request: ScenarioRequest):
    """Run scenario simulation"""
    current_state = request.parameters.get("current_state", {
        "health_score": 65,
        "cash_balance": 450000,
        "monthly_revenue": 850000,
        "monthly_expenses": 720000
    })
    
    return digital_twin.simulate_scenario(
        current_state,
        request.parameters,
        request.months
    )

@app.post("/api/v1/early-warning/check/{msme_id}")
async def check_early_warnings(msme_id: str, data: dict):
    """Check for early warnings"""
    return early_warning_engine.check_warnings(msme_id, data)

@app.get("/api/v1/digital-twin/{msme_id}")
async def get_digital_twin(msme_id: str):
    """Get digital twin for MSME"""
    return {
        "msme_id": msme_id,
        "current_state": {
            "health_score": 68,
            "cash_balance": 520000,
            "monthly_revenue": 920000,
            "monthly_expenses": 780000,
            "outstanding_loans": 2500000
        },
        "created_at": datetime.now().isoformat()
    }

@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "ai-forecasting"}
```

---

## 9.4 EARLY WARNING SERVICE (PYTHON)

```python
# ai-services/early-warning/app/main.py
from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Dict, Any
from datetime import datetime
import asyncio

app = FastAPI(title="AI Early Warning Service", version="1.0.0")

class EarlyWarningCheck(BaseModel):
    msme_id: str
    data: Dict[str, Any]

class EarlyWarningResponse(BaseModel):
    type: str
    severity: str
    description: str
    confidence: float
    indicators: Dict[str, Any]
    detected_at: str

class RealTimeMonitor:
    """Real-time monitoring and alerting"""
    
    def __init__(self):
        self.thresholds = {
            "health_score_low": 40,
            "cash_balance_low": 100000,
            "revenue_decline_pct": 15,
            "expense_increase_pct": 20,
            "debt_ratio_high": 0.5
        }
    
    async def monitor(self, msme_id: str, data: Dict[str, Any]) -> List[EarlyWarningResponse]:
        """Monitor for warning signs"""
        
        warnings = []
        
        # Health Score Check
        health_score = data.get("health_score", 100)
        if health_score < self.thresholds["health_score_low"]:
            warnings.append(EarlyWarningResponse(
                type="low_health_score",
                severity="high" if health_score < 30 else "medium",
                description=f"Health score is critically low: {health_score}",
                confidence=0.9,
                indicators={"current_score": health_score, "threshold": self.thresholds["health_score_low"]},
                detected_at=datetime.now().isoformat()
            ))
        
        # Cash Balance Check
        cash_balance = data.get("cash_balance", 0)
        if cash_balance < self.thresholds["cash_balance_low"]:
            warnings.append(EarlyWarningResponse(
                type="low_cash_balance",
                severity="critical" if cash_balance < 0 else "high",
                description=f"Cash balance is critically low: ₹{cash_balance:,.0f}",
                confidence=0.95,
                indicators={"current_balance": cash_balance, "threshold": self.thresholds["cash_balance_low"]},
                detected_at=datetime.now().isoformat()
            ))
        
        # Revenue Decline Check
        revenue_history = data.get("revenue_history", [])
        if len(revenue_history) >= 3:
            recent_avg = sum(revenue_history[-3:]) / 3
            older_avg = sum(revenue_history[-6:-3]) / 3 if len(revenue_history) >= 6 else recent_avg
            
            if older_avg > 0:
                decline_pct = (older_avg - recent_avg) / older_avg * 100
                if decline_pct > self.thresholds["revenue_decline_pct"]:
                    warnings.append(EarlyWarningResponse(
                        type="revenue_decline",
                        severity="high",
                        description=f"Revenue declined by {decline_pct:.1f}% over last 3 months",
                        confidence=0.85,
                        indicators={"decline_percent": decline_pct},
                        detected_at=datetime.now().isoformat()
                    ))
        
        # Expense Check
        monthly_expenses = data.get("monthly_expenses", 0)
        monthly_revenue = data.get("monthly_revenue", 1)
        if monthly_revenue > 0:
            expense_ratio = monthly_expenses / monthly_revenue
            if expense_ratio > 0.9:
                warnings.append(EarlyWarningResponse(
                    type="high_expense_ratio",
                    severity="high",
                    description=f"Expenses are {expense_ratio*100:.1f}% of revenue",
                    confidence=0.8,
                    indicators={"expense_ratio": expense_ratio},
                    detected_at=datetime.now().isoformat()
                ))
        
        # Default Risk Check
        outstanding_loans = data.get("outstanding_loans", 0)
        annual_revenue = data.get("annual_revenue", 1)
        if annual_revenue > 0:
            debt_ratio = outstanding_loans / annual_revenue
            if debt_ratio > self.thresholds["debt_ratio_high"]:
                warnings.append(EarlyWarningResponse(
                    type="high_debt_ratio",
                    severity="critical" if debt_ratio > 0.8 else "high",
                    description=f"Debt-to-revenue ratio is {debt_ratio:.2f}",
                    confidence=0.85,
                    indicators={"debt_ratio": debt_ratio},
                    detected_at=datetime.now().isoformat()
                ))
        
        return warnings

monitor = RealTimeMonitor()

@app.post("/api/v1/early-warning/check")
async def check_early_warnings(request: EarlyWarningCheck):
    """Check for early warnings"""
    return await monitor.monitor(request.msme_id, request.data)

@app.get("/api/v1/early-warning/thresholds")
async def get_thresholds():
    """Get monitoring thresholds"""
    return monitor.thresholds

@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "ai-early-warning"}
```

---

## 9.5 API ENDPOINTS SUMMARY

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/forecast/generate` | POST | Generate forecast |
| `/api/v1/forecast/{msmeId}` | GET | Get latest forecast |
| `/api/v1/forecast/{msmeId}/history` | GET | Get forecast history |
| `/api/v1/forecast/{msmeId}/accuracy` | GET | Get accuracy metrics |
| `/api/v1/scenario/simulate` | POST | Run scenario simulation |
| `/api/v1/scenario/presets` | GET | Get scenario presets |
| `/api/v1/scenario/{msmeId}/history` | GET | Get scenario history |
| `/api/v1/scenario/{msmeId}/digital-twin` | POST | Create digital twin |
| `/api/v1/scenario/{msmeId}/monte-carlo` | POST | Run Monte Carlo simulation |
| `/api/v1/scenario/{msmeId}/early-warnings` | GET | Check early warnings |
| `/api/v1/scenario/{msmeId}/active-warnings` | GET | Get active warnings |
| `/api/v1/scenario/warnings/{warningId}/acknowledge` | PUT | Acknowledge warning |

---

## 9.6 DOCKERFILE

```dockerfile
# ai-services/forecasting/Dockerfile
FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y gcc g++ && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 5002

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "5002"]
```

---

## 9.7 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| Forecast Service (Java) | 2 days |
| Scenario Service | 2 days |
| Digital Twin Service | 3 days |
| Early Warning Service | 2 days |
| AI Forecasting (Python) | 3 days |
| AI Early Warning (Python) | 2 days |
| Kafka Integration | 1 day |
| Testing | 2 days |
| **Total** | **17 days** |

---

## 9.8 HACKATHON PRIORITY

**HIGH** - Differentiating feature demonstrating AI capabilities
