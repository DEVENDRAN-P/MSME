package com.idbi.msme.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class LoanResponse {

    private UUID id;
    private UUID businessId;
    private String businessName;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private String status;
    private LocalDateTime disbursedAt;

    public LoanResponse() {
    }

    public LoanResponse(UUID id, UUID businessId, String businessName, BigDecimal amount,
                        BigDecimal interestRate, Integer tenureMonths, String status, LocalDateTime disbursedAt) {
        this.id = id;
        this.businessId = businessId;
        this.businessName = businessName;
        this.amount = amount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.status = status;
        this.disbursedAt = disbursedAt;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDisbursedAt() {
        return disbursedAt;
    }

    public void setDisbursedAt(LocalDateTime disbursedAt) {
        this.disbursedAt = disbursedAt;
    }
}
