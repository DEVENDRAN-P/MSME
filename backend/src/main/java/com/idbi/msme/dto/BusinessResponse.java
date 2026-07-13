package com.idbi.msme.dto;

import com.idbi.msme.model.BusinessProfile;
import java.time.LocalDateTime;

public class BusinessResponse {
    private String id;
    private String ownerId;
    private String legalName;
    private String tradeName;
    private String gstin;
    private String pan;
    private String udyamNumber;
    private String incorporationDate;
    private String constitution;
    private String industrySector;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BusinessResponse() {}

    public BusinessResponse(BusinessProfile b) {
        this.id = b.getId();
        this.ownerId = b.getOwnerId();
        this.legalName = b.getLegalName();
        this.tradeName = b.getTradeName();
        this.gstin = b.getGstin();
        this.pan = b.getPan();
        this.udyamNumber = b.getUdyamNumber();
        this.incorporationDate = b.getIncorporationDate();
        this.constitution = b.getConstitution();
        this.industrySector = b.getIndustrySector();
        this.addressLine1 = b.getAddressLine1();
        this.addressLine2 = b.getAddressLine2();
        this.city = b.getCity();
        this.state = b.getState();
        this.pincode = b.getPincode();
        this.createdAt = b.getCreatedAt() != null ? LocalDateTime.parse(b.getCreatedAt()) : null;
        this.updatedAt = b.getUpdatedAt() != null ? LocalDateTime.parse(b.getUpdatedAt()) : null;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }
    public String getUdyamNumber() { return udyamNumber; }
    public void setUdyamNumber(String udyamNumber) { this.udyamNumber = udyamNumber; }
    public String getIncorporationDate() { return incorporationDate; }
    public void setIncorporationDate(String incorporationDate) { this.incorporationDate = incorporationDate; }
    public String getConstitution() { return constitution; }
    public void setConstitution(String constitution) { this.constitution = constitution; }
    public String getIndustrySector() { return industrySector; }
    public void setIndustrySector(String industrySector) { this.industrySector = industrySector; }
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
