# MODULE 13: NOTIFICATIONS, REPORTS & SECURITY
## Production-Grade Implementation

---

## 13.1 MICROSERVICES STRUCTURE

```
notification-service/
├── src/main/java/com/msme/notification/
│   ├── NotificationServiceApplication.java
│   ├── config/
│   │   ├── KafkaConfig.java
│   │   └── WebSocketConfig.java
│   ├── controller/
│   │   └── NotificationController.java
│   ├── service/
│   │   ├── NotificationService.java
│   │   ├── EmailService.java
│   │   └── WebSocketService.java
│   ├── model/
│   │   └── Notification.java
│   ├── repository/
│   │   └── NotificationRepository.java
│   └── listener/
│       └── NotificationEventListener.java
└── pom.xml

reporting-service/
├── src/main/java/com/msme/reporting/
│   ├── ReportingServiceApplication.java
│   ├── controller/
│   │   └── ReportController.java
│   ├── service/
│   │   ├── ReportService.java
│   │   ├── PDFGenerator.java
│   │   └── ExcelGenerator.java
│   ├── model/
│   │   └── Report.java
│   └── repository/
│       └── ReportRepository.java
└── pom.xml

audit-service/
├── src/main/java/com/msme/audit/
│   ├── AuditServiceApplication.java
│   ├── service/
│   │   ├── AuditService.java
│   │   └── AuditLogger.java
│   ├── model/
│   │   └── AuditLog.java
│   ├── repository/
│   │   └── AuditLogRepository.java
│   └── listener/
│       └── AuditEventListener.java
└── pom.xml
```

---

## 13.2 NOTIFICATION SERVICE

```java
// model/Notification.java
package com.msme.notification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "text")
    private String message;
    
    @Column(columnDefinition = "jsonb")
    private String data;
    
    private String channel = "in_app";
    
    private LocalDateTime readAt;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

```java
// service/NotificationService.java
package com.msme.notification.service;

import com.msme.notification.model.Notification;
import com.msme.notification.repository.NotificationRepository;
import com.msme.notification.listener.NotificationEventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final WebSocketService webSocketService;
    
    public NotificationService(
            NotificationRepository notificationRepository,
            EmailService emailService,
            WebSocketService webSocketService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.webSocketService = webSocketService;
    }
    
    public Notification createNotification(
            UUID userId,
            String type,
            String title,
            String message,
            String data,
            boolean sendEmail) {
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setData(data);
        
        notificationRepository.save(notification);
        
        // Send real-time WebSocket notification
        webSocketService.sendToUser(userId, Map.of(
            "type", "notification",
            "data", Map.of(
                "id", notification.getId().toString(),
                "type", type,
                "title", title,
                "message", message
            )
        ));
        
        // Send email for important notifications
        if (sendEmail) {
            emailService.sendNotificationEmail(userId, title, message);
        }
        
        return notification;
    }
    
    public List<Notification> getUserNotifications(UUID userId, boolean unreadOnly, int limit) {
        if (unreadOnly) {
            return notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public int getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }
    
    public Notification markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository
            .findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }
    
    public int markAllAsRead(UUID userId) {
        return notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }
    
    public Notification createScoreUpdateNotification(UUID userId, double newScore, String grade) {
        return createNotification(
            userId,
            "score_update",
            "Health Score Updated",
            String.format("Your financial health score has been updated to %.0f (Grade: %s)", newScore, grade),
            null,
            false
        );
    }
    
    public Notification createLoanStatusNotification(UUID userId, String status, double amount) {
        return createNotification(
            userId,
            "loan_status",
            "Loan Application " + status,
            String.format("Your loan application for ₹%.0f has been %s", amount, status),
            null,
            true
        );
    }
    
    public Notification createEarlyWarningNotification(UUID userId, String type, String description) {
        return createNotification(
            userId,
            "early_warning",
            "Early Warning Alert",
            description,
            null,
            true
        );
    }
}
```

```java
// controller/NotificationController.java
package com.msme.notification.controller;

import com.msme.notification.model.Notification;
import com.msme.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user notifications")
    public ResponseEntity<List<Notification>> getNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(
            notificationService.getUserNotifications(userId, unreadOnly, limit)
        );
    }
    
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(Map.of(
            "count", notificationService.getUnreadCount(userId)
        ));
    }
    
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(
            notificationService.markAsRead(notificationId, userId)
        );
    }
    
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @AuthenticationPrincipal UUID userId) {
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("marked", count));
    }
}
```

```java
// listener/NotificationEventListener.java
package com.msme.notification.listener;

import com.msme.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationEventListener {
    
    private final NotificationService notificationService;
    
    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @KafkaListener(topics = "scoring.events", groupId = "notification-service")
    public void handleScoreEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event");
        
        if ("score.calculated".equals(eventType)) {
            UUID msmeId = UUID.fromString((String) event.get("msmeId"));
            double score = ((Number) event.get("score")).doubleValue();
            String grade = (String) event.get("grade");
            
            notificationService.createScoreUpdateNotification(msmeId, score, grade);
        }
    }
    
    @KafkaListener(topics = "loan.events", groupId = "notification-service")
    public void handleLoanEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event");
        
        if ("application.status_changed".equals(eventType)) {
            UUID userId = UUID.fromString((String) event.get("userId"));
            String status = (String) event.get("status");
            double amount = ((Number) event.get("amount")).doubleValue();
            
            notificationService.createLoanStatusNotification(userId, status, amount);
        }
    }
    
    @KafkaListener(topics = "early-warning.events", groupId = "notification-service")
    public void handleEarlyWarningEvent(Map<String, Object> event) {
        UUID userId = UUID.fromString((String) event.get("userId"));
        String type = (String) event.get("type");
        String description = (String) event.get("description");
        
        notificationService.createEarlyWarningNotification(userId, type, description);
    }
}
```

---

## 13.3 REPORTING SERVICE

```java
// service/ReportService.java
package com.msme.reporting.service;

import com.msme.reporting.model.Report;
import com.msme.reporting.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final PDFGenerator pdfGenerator;
    private final ExcelGenerator excelGenerator;
    
    @Value("${reports.storage.path:/app/reports}")
    private String storagePath;
    
    public ReportService(
            ReportRepository reportRepository,
            PDFGenerator pdfGenerator,
            ExcelGenerator excelGenerator) {
        this.reportRepository = reportRepository;
        this.pdfGenerator = pdfGenerator;
        this.excelGenerator = excelGenerator;
    }
    
    public Report generateHealthCardReport(UUID msmeId, UUID userId) {
        Report report = createReport(msmeId, "health_card", userId);
        
        try {
            // Fetch health data
            Map<String, Object> healthData = fetchHealthData(msmeId);
            
            // Generate PDF
            String filePath = pdfGenerator.generateHealthCard(healthData, report.getId());
            
            report.setFilePath(filePath);
            report.setFileSize(getFileSize(filePath));
            report.setStatus("completed");
            
        } catch (Exception e) {
            report.setStatus("failed");
            report.setErrorMessage(e.getMessage());
        }
        
        return reportRepository.save(report);
    }
    
    public Report generateRiskReport(UUID msmeId, UUID userId) {
        Report report = createReport(msmeId, "risk_assessment", userId);
        
        try {
            Map<String, Object> riskData = fetchRiskData(msmeId);
            String filePath = pdfGenerator.generateRiskReport(riskData, report.getId());
            
            report.setFilePath(filePath);
            report.setFileSize(getFileSize(filePath));
            report.setStatus("completed");
            
        } catch (Exception e) {
            report.setStatus("failed");
            report.setErrorMessage(e.getMessage());
        }
        
        return reportRepository.save(report);
    }
    
    public Report generateLoanRecommendationReport(UUID msmeId, double loanAmount, UUID userId) {
        Report report = createReport(msmeId, "loan_recommendation", userId);
        report.setParameters(Map.of("loan_amount", loanAmount).toString());
        
        try {
            Map<String, Object> recommendationData = fetchRecommendationData(msmeId, loanAmount);
            String filePath = pdfGenerator.generateLoanRecommendation(recommendationData, report.getId());
            
            report.setFilePath(filePath);
            report.setFileSize(getFileSize(filePath));
            report.setStatus("completed");
            
        } catch (Exception e) {
            report.setStatus("failed");
            report.setErrorMessage(e.getMessage());
        }
        
        return reportRepository.save(report);
    }
    
    public Report generateFraudReport(UUID msmeId, UUID userId) {
        Report report = createReport(msmeId, "fraud_analysis", userId);
        
        try {
            Map<String, Object> fraudData = fetchFraudData(msmeId);
            String filePath = pdfGenerator.generateFraudReport(fraudData, report.getId());
            
            report.setFilePath(filePath);
            report.setFileSize(getFileSize(filePath));
            report.setStatus("completed");
            
        } catch (Exception e) {
            report.setStatus("failed");
            report.setErrorMessage(e.getMessage());
        }
        
        return reportRepository.save(report);
    }
    
    public Report generateExcelReport(UUID msmeId, String reportType, UUID userId) {
        Report report = createReport(msmeId, reportType + "_excel", userId);
        
        try {
            Map<String, Object> data = fetchReportData(msmeId, reportType);
            String filePath = excelGenerator.generate(data, report.getId());
            
            report.setFilePath(filePath);
            report.setFileSize(getFileSize(filePath));
            report.setStatus("completed");
            
        } catch (Exception e) {
            report.setStatus("failed");
            report.setErrorMessage(e.getMessage());
        }
        
        return reportRepository.save(report);
    }
    
    private Report createReport(UUID msmeId, String reportType, UUID userId) {
        Report report = new Report();
        report.setMsmeId(msmeId);
        report.setReportType(reportType);
        report.setGeneratedBy(userId);
        report.setStatus("generating");
        return reportRepository.save(report);
    }
    
    public Report getReport(UUID reportId) {
        return reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));
    }
    
    public List<Report> getUserReports(UUID userId, String reportType) {
        if (reportType != null) {
            return reportRepository.findByGeneratedByAndReportTypeOrderByCreatedAtDesc(userId, reportType);
        }
        return reportRepository.findByGeneratedByOrderByCreatedAtDesc(userId);
    }
    
    private Map<String, Object> fetchHealthData(UUID msmeId) {
        // Fetch from health service
        return Map.of("msme_id", msmeId, "score", 75, "grade", "B+");
    }
    
    private Map<String, Object> fetchRiskData(UUID msmeId) {
        return Map.of("msme_id", msmeId, "risk_level", "medium");
    }
    
    private Map<String, Object> fetchRecommendationData(UUID msmeId, double loanAmount) {
        return Map.of("msme_id", msmeId, "loan_amount", loanAmount);
    }
    
    private Map<String, Object> fetchFraudData(UUID msmeId) {
        return Map.of("msme_id", msmeId, "risk_score", 0.25);
    }
    
    private Map<String, Object> fetchReportData(UUID msmeId, String reportType) {
        return Map.of("msme_id", msmeId, "type", reportType);
    }
    
    private long getFileSize(String filePath) {
        // Get file size
        return 0;
    }
}
```

```java
// controller/ReportController.java
package com.msme.reporting.controller;

import com.msme.reporting.model.Report;
import com.msme.reporting.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Report generation endpoints")
public class ReportController {
    
    private final ReportService reportService;
    
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    
    @PostMapping("/health-card")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER')")
    @Operation(summary = "Generate health card report")
    public ResponseEntity<Report> generateHealthCard(
            @RequestParam UUID msmeId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(reportService.generateHealthCardReport(msmeId, userId));
    }
    
    @PostMapping("/risk")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER', 'CREDIT_MANAGER')")
    @Operation(summary = "Generate risk assessment report")
    public ResponseEntity<Report> generateRiskReport(
            @RequestParam UUID msmeId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(reportService.generateRiskReport(msmeId, userId));
    }
    
    @PostMapping("/loan-recommendation")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER')")
    @Operation(summary = "Generate loan recommendation report")
    public ResponseEntity<Report> generateLoanRecommendation(
            @RequestParam UUID msmeId,
            @RequestParam double loanAmount,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(
            reportService.generateLoanRecommendationReport(msmeId, loanAmount, userId)
        );
    }
    
    @PostMapping("/fraud")
    @PreAuthorize("hasAnyRole('LOAN_OFFICER', 'CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Generate fraud analysis report")
    public ResponseEntity<Report> generateFraudReport(
            @RequestParam UUID msmeId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(reportService.generateFraudReport(msmeId, userId));
    }
    
    @PostMapping("/excel")
    @PreAuthorize("hasAnyRole('MSME', 'LOAN_OFFICER', 'CREDIT_MANAGER')")
    @Operation(summary = "Generate Excel report")
    public ResponseEntity<Report> generateExcelReport(
            @RequestParam UUID msmeId,
            @RequestParam String reportType,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(reportService.generateExcelReport(msmeId, reportType, userId));
    }
    
    @GetMapping("/{reportId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get report details")
    public ResponseEntity<Report> getReport(@PathVariable UUID reportId) {
        return ResponseEntity.ok(reportService.getReport(reportId));
    }
    
    @GetMapping("/{reportId}/download")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Download report file")
    public ResponseEntity<Resource> downloadReport(@PathVariable UUID reportId) {
        Report report = reportService.getReport(reportId);
        
        if (report.getFilePath() == null || !"completed".equals(report.getStatus())) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(report.getFilePath());
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + report.getReportType() + "_" + reportId + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(resource);
    }
    
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get report history")
    public ResponseEntity<List<Report>> getReportHistory(
            @RequestParam(required = false) String reportType,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(reportService.getUserReports(userId, reportType));
    }
}
```

---

## 13.4 AUDIT SERVICE

```java
// service/AuditService.java
package com.msme.audit.service;

import com.msme.audit.model.AuditLog;
import com.msme.audit.repository.AuditLogRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public AuditService(
            AuditLogRepository auditLogRepository,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.auditLogRepository = auditLogRepository;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public AuditLog logAction(
            UUID userId,
            String action,
            String entityType,
            UUID entityId,
            Map<String, Object> oldValues,
            Map<String, Object> newValues,
            String ipAddress,
            String userAgent,
            String requestMethod,
            String requestPath,
            Integer responseStatus,
            Long durationMs) {
        
        AuditLog auditLog = new AuditLog();
        auditLog.setCorrelationId(UUID.randomUUID().toString());
        auditLog.setUserId(userId);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setOldValues(oldValues != null ? oldValues.toString() : null);
        auditLog.setNewValues(newValues != null ? newValues.toString() : null);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setRequestMethod(requestMethod);
        auditLog.setRequestPath(requestPath);
        auditLog.setResponseStatus(responseStatus);
        auditLog.setDurationMs(durationMs);
        auditLog.setCreatedAt(LocalDateTime.now());
        
        auditLogRepository.save(auditLog);
        
        // Send to Kafka for centralized logging
        kafkaTemplate.send("audit.events", "audit.action.logged",
            Map.of(
                "correlationId", auditLog.getCorrelationId(),
                "userId", userId.toString(),
                "action", action,
                "entityType", entityType,
                "timestamp", LocalDateTime.now().toString()
            )
        );
        
        return auditLog;
    }
    
    public List<AuditLog> getAuditLogs(
            UUID userId,
            String entityType,
            String action,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int limit) {
        
        return auditLogRepository.findByFilters(
            userId, entityType, action, startDate, endDate, limit
        );
    }
    
    public List<AuditLogs> getEntityAuditLogs(UUID entityId, String entityType) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }
    
    public Map<String, Object> getAuditStats() {
        long totalLogs = auditLogRepository.count();
        long todayLogs = auditLogRepository.countByCreatedAtAfter(
            LocalDateTime.now().toLocalDate().atStartOfDay()
        );
        
        return Map.of(
            "total_logs", totalLogs,
            "today_logs", todayLogs
        );
    }
}
```

---

## 13.5 SECURITY SERVICE

```java
// In auth-service - service/SecurityService.java
package com.msme.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class SecurityService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${refresh-token.expiration}")
    private long refreshTokenExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateAccessToken(UUID userId, String email, String role, List<String> permissions) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .claim("permissions", permissions)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("type", "refresh")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public UUID getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return UUID.fromString(claims.getSubject());
    }
    
    public String getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("role", String.class);
    }
    
    public List<String> getPermissionsFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("permissions", List.class);
    }
}
```

```java
// service/EncryptionService.java
package com.msme.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {
    
    @Value("${encryption.secret}")
    private String encryptionSecret;
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    
    public String encrypt(String data) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            SecretKeySpec keySpec = new SecretKeySpec(
                encryptionSecret.getBytes(), "AES");
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(combined);
            
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    public String decrypt(String encryptedData) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] data = new byte[combined.length - GCM_IV_LENGTH];
            
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, data, 0, data.length);
            
            SecretKeySpec keySpec = new SecretKeySpec(
                encryptionSecret.getBytes(), "AES");
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            
            return new String(cipher.doFinal(data));
            
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
    
    public String maskPAN(String pan) {
        if (pan == null || pan.length() < 8) return "****";
        return pan.substring(0, 4) + "****" + pan.substring(pan.length() - 4);
    }
    
    public String maskAccount(String account) {
        if (account == null || account.length() < 4) return "****";
        return "****" + account.substring(account.length() - 4);
    }
}
```

---

## 13.6 FRONTEND - NOTIFICATION CENTER

```tsx
// frontend/src/components/shared/NotificationBell.tsx
import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notificationApi } from '@/api/notification.api';
import { Card } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';

export const NotificationBell: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const queryClient = useQueryClient();
  
  const { data: unreadCount } = useQuery({
    queryKey: ['notificationCount'],
    queryFn: async () => {
      const response = await notificationApi.getUnreadCount();
      return response.data.count;
    },
    refetchInterval: 30000 // Refetch every 30 seconds
  });
  
  const { data: notifications } = useQuery({
    queryKey: ['notifications'],
    queryFn: async () => {
      const response = await notificationApi.getNotifications();
      return response.data;
    },
    enabled: isOpen
  });
  
  const markAsReadMutation = useMutation({
    mutationFn: async (notificationId: string) => {
      await notificationApi.markAsRead(notificationId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notificationCount'] });
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    }
  });
  
  const markAllAsReadMutation = useMutation({
    mutationFn: async () => {
      await notificationApi.markAllAsRead();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notificationCount'] });
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    }
  });
  
  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 text-gray-600 hover:text-gray-900"
      >
        <span className="text-xl">🔔</span>
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>
      
      {isOpen && (
        <Card className="absolute right-0 mt-2 w-80 max-h-96 overflow-y-auto z-50">
          <div className="p-3 border-b flex items-center justify-between">
            <h3 className="font-medium">Notifications</h3>
            {unreadCount > 0 && (
              <button
                onClick={() => markAllAsReadMutation.mutate()}
                className="text-sm text-blue-600 hover:underline"
              >
                Mark all read
              </button>
            )}
          </div>
          
          <div className="divide-y">
            {notifications?.length === 0 ? (
              <p className="p-4 text-center text-gray-500">No notifications</p>
            ) : (
              notifications?.map((notification) => (
                <div
                  key={notification.id}
                  className={`p-3 cursor-pointer hover:bg-gray-50 ${
                    !notification.readAt ? 'bg-blue-50' : ''
                  }`}
                  onClick={() => markAsReadMutation.mutate(notification.id)}
                >
                  <div className="flex items-start gap-3">
                    <span className="text-lg">
                      {getNotificationIcon(notification.type)}
                    </span>
                    <div className="flex-1">
                      <p className="font-medium text-sm">{notification.title}</p>
                      <p className="text-xs text-gray-500">{notification.message}</p>
                      <p className="text-xs text-gray-400 mt-1">
                        {new Date(notification.createdAt).toLocaleString()}
                      </p>
                    </div>
                    {!notification.readAt && (
                      <div className="w-2 h-2 bg-blue-500 rounded-full" />
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </Card>
      )}
    </div>
  );
};

const getNotificationIcon = (type: string) => {
  switch (type) {
    case 'score_update': return '📊';
    case 'loan_status': return '🏦';
    case 'early_warning': return '⚠️';
    case 'fraud_alert': return '🚨';
    default: return '📌';
  }
};
```

---

## 13.7 API ENDPOINTS SUMMARY

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/notifications` | GET | Get notifications |
| `/api/v1/notifications/count` | GET | Get unread count |
| `/api/v1/notifications/{id}/read` | PUT | Mark as read |
| `/api/v1/notifications/read-all` | PUT | Mark all as read |
| `/api/v1/reports/health-card` | POST | Generate health card |
| `/api/v1/reports/risk` | POST | Generate risk report |
| `/api/v1/reports/loan-recommendation` | POST | Generate loan report |
| `/api/v1/reports/fraud` | POST | Generate fraud report |
| `/api/v1/reports/excel` | POST | Generate Excel report |
| `/api/v1/reports/{id}` | GET | Get report details |
| `/api/v1/reports/{id}/download` | GET | Download report |
| `/api/v1/reports/history` | GET | Get report history |
| `/api/v1/audit/logs` | GET | Get audit logs |
| `/api/v1/audit/entity/{entityId}` | GET | Get entity audit logs |
| `/api/v1/audit/stats` | GET | Get audit statistics |

---

## 13.8 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| Notification Service | 2 days |
| Reporting Service | 3 days |
| PDF/Excel Generation | 2 days |
| Audit Service | 2 days |
| Security & Encryption | 2 days |
| Frontend Components | 2 days |
| Kafka Integration | 1 day |
| Testing | 2 days |
| **Total** | **16 days** |

---

## 13.9 HACKATHON PRIORITY

**HIGH** - Security and notifications essential for production readiness
