package com.idbi.msme.dto;

import java.math.BigDecimal;

public class LoanResponse {
    private String id;
    private String businessId;
    private String businessName;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private String status;
    private String disbursedAt;

    public LoanResponse() {}

    public LoanResponse(String id, String businessId, String businessName, BigDecimal amount,
                        BigDecimal interestRate, Integer tenureMonths, String status, String disbursedAt) {
        this.id = id;
        this.businessId = businessId;
        this.businessName = businessName;
        this.amount = amount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.status = status;
        this.disbursedAt = disbursedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDisbursedAt() { return disbursedAt; }
    public void setDisbursedAt(String disbursedAt) { this.disbursedAt = disbursedAt; }
}
