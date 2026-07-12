package com.idbi.msme.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ConsentResponseDto {

    private UUID id;
    private UUID businessId;
    private String businessName;
    private UUID requestedBy;
    private String requestedByName;
    private String consentType;
    private LocalDateTime validUntil;
    private String status;
    private LocalDateTime createdAt;

    public ConsentResponseDto() {
    }

    public ConsentResponseDto(UUID id, UUID businessId, String businessName, UUID requestedBy,
                              String requestedByName, String consentType, LocalDateTime validUntil,
                              String status, LocalDateTime createdAt) {
        this.id = id;
        this.businessId = businessId;
        this.businessName = businessName;
        this.requestedBy = requestedBy;
        this.requestedByName = requestedByName;
        this.consentType = consentType;
        this.validUntil = validUntil;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBusinessId() {
        return businessId;
    }

    public void setBusinessId(UUID businessId) {
        this.businessId = businessId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public UUID getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(UUID requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
