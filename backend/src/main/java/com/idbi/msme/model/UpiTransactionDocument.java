package com.idbi.msme.model;

import java.math.BigDecimal;

public class UpiTransactionDocument {

    private String id;
    private String businessId;
    private String month;
    private BigDecimal totalCreditVolume;
    private Integer totalCreditCount;
    private BigDecimal totalDebitVolume;
    private Integer totalDebitCount;
    private String createdAt;

    public UpiTransactionDocument() {
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

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getTotalCreditVolume() {
        return totalCreditVolume;
    }

    public void setTotalCreditVolume(BigDecimal totalCreditVolume) {
        this.totalCreditVolume = totalCreditVolume;
    }

    public Integer getTotalCreditCount() {
        return totalCreditCount;
    }

    public void setTotalCreditCount(Integer totalCreditCount) {
        this.totalCreditCount = totalCreditCount;
    }

    public BigDecimal getTotalDebitVolume() {
        return totalDebitVolume;
    }

    public void setTotalDebitVolume(BigDecimal totalDebitVolume) {
        this.totalDebitVolume = totalDebitVolume;
    }

    public Integer getTotalDebitCount() {
        return totalDebitCount;
    }

    public void setTotalDebitCount(Integer totalDebitCount) {
        this.totalDebitCount = totalDebitCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
