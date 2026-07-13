package com.idbi.msme.dto;

public class ConsentResponseDto {
    private String id;
    private String businessId;
    private String businessName;
    private String requestedById;
    private String requestedByName;
    private String consentType;
    private String validUntil;
    private String status;
    private String createdAt;

    public ConsentResponseDto() {}

    public ConsentResponseDto(String id, String businessId, String businessName, String requestedById,
                              String requestedByName, String consentType, String validUntil,
                              String status, String createdAt) {
        this.id = id;
        this.businessId = businessId;
        this.businessName = businessName;
        this.requestedById = requestedById;
        this.requestedByName = requestedByName;
        this.consentType = consentType;
        this.validUntil = validUntil;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getRequestedById() { return requestedById; }
    public void setRequestedById(String requestedById) { this.requestedById = requestedById; }
    public String getRequestedByName() { return requestedByName; }
    public void setRequestedByName(String requestedByName) { this.requestedByName = requestedByName; }
    public String getConsentType() { return consentType; }
    public void setConsentType(String consentType) { this.consentType = consentType; }
    public String getValidUntil() { return validUntil; }
    public void setValidUntil(String validUntil) { this.validUntil = validUntil; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
