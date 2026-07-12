package com.idbi.msme.dto;

import java.util.UUID;

public class ConsentRequestDto {

    private UUID businessId;
    private String consentType; // GST, UPI, AA, ALL

    public ConsentRequestDto() {
    }

    public UUID getBusinessId() {
        return businessId;
    }

    public void setBusinessId(UUID businessId) {
        this.businessId = businessId;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }
}
