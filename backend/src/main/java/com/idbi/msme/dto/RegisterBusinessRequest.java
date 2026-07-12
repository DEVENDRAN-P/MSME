package com.idbi.msme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class RegisterBusinessRequest {

    @NotBlank(message = "Legal name is required")
    private String legalName;

    private String tradeName;

    @NotBlank(message = "GSTIN is required")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", message = "Invalid GSTIN format (e.g. 22AAAAA1111A1Z1)")
    private String gstin;

    @NotBlank(message = "PAN is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN format (e.g. ABCDE1234F)")
    private String pan;

    @NotBlank(message = "Udyam registration number is required")
    private String udyamNumber;

    @NotNull(message = "Incorporation date is required")
    private LocalDate incorporationDate;

    @NotBlank(message = "Constitution type is required")
    private String constitution;

    @NotBlank(message = "Industry sector is required")
    private String industrySector;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Size(min = 6, max = 6, message = "Pincode must be exactly 6 digits")
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must contain only digits")
    private String pincode;

    public RegisterBusinessRequest() {
    }

    public RegisterBusinessRequest(String legalName, String tradeName, String gstin, String pan, String udyamNumber,
                                   LocalDate incorporationDate, String constitution, String industrySector,
                                   String addressLine1, String addressLine2, String city, String state, String pincode) {
        this.legalName = legalName;
        this.tradeName = tradeName;
        this.gstin = gstin;
        this.pan = pan;
        this.udyamNumber = udyamNumber;
        this.incorporationDate = incorporationDate;
        this.constitution = constitution;
        this.industrySector = industrySector;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
    }

    // Getters and Setters
    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getUdyamNumber() {
        return udyamNumber;
    }

    public void setUdyamNumber(String udyamNumber) {
        this.udyamNumber = udyamNumber;
    }

    public LocalDate getIncorporationDate() {
        return incorporationDate;
    }

    public void setIncorporationDate(LocalDate incorporationDate) {
        this.incorporationDate = incorporationDate;
    }

    public String getConstitution() {
        return constitution;
    }

    public void setConstitution(String constitution) {
        this.constitution = constitution;
    }

    public String getIndustrySector() {
        return industrySector;
    }

    public void setIndustrySector(String industrySector) {
        this.industrySector = industrySector;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }
}
