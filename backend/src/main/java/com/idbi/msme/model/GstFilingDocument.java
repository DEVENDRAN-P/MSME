package com.idbi.msme.model;

import java.math.BigDecimal;

public class GstFilingDocument {

    private String id;
    private String businessId;
    private String filingMonth;
    private BigDecimal turnover;
    private BigDecimal taxPaid;
    private String filingStatus;
    private String createdAt;

    public GstFilingDocument() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getFilingMonth() {
        return filingMonth;
    }

    public void setFilingMonth(String filingMonth) {
        this.filingMonth = filingMonth;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public void setTurnover(BigDecimal turnover) {
        this.turnover = turnover;
    }

    public BigDecimal getTaxPaid() {
        return taxPaid;
    }

    public void setTaxPaid(BigDecimal taxPaid) {
        this.taxPaid = taxPaid;
    }

    public String getFilingStatus() {
        return filingStatus;
    }

    public void setFilingStatus(String filingStatus) {
        this.filingStatus = filingStatus;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
