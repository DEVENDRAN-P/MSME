package com.idbi.msme.dto;

public class ConsentRequestDto {

    private String businessId;
    private String consentType;

    public ConsentRequestDto() {}

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getConsentType() { return consentType; }
    public void setConsentType(String consentType) { this.consentType = consentType; }
}
