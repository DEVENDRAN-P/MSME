package com.idbi.msme.model;

import java.math.BigDecimal;

public class AaBankTransactionDocument {

    private String id;
    private String businessId;
    private String month;
    private BigDecimal avgBalance;
    private BigDecimal inwardRemittances;
    private BigDecimal outwardRemittances;
    private String createdAt;

    public AaBankTransactionDocument() {
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

    public BigDecimal getAvgBalance() {
        return avgBalance;
    }

    public void setAvgBalance(BigDecimal avgBalance) {
        this.avgBalance = avgBalance;
    }

    public BigDecimal getInwardRemittances() {
        return inwardRemittances;
    }

    public void setInwardRemittances(BigDecimal inwardRemittances) {
        this.inwardRemittances = inwardRemittances;
    }

    public BigDecimal getOutwardRemittances() {
        return outwardRemittances;
    }

    public void setOutwardRemittances(BigDecimal outwardRemittances) {
        this.outwardRemittances = outwardRemittances;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
