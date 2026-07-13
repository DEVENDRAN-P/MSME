package com.idbi.msme.dto;

import java.math.BigDecimal;

public class ApproveLoanRequest {

    private String businessId;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer tenureMonths;

    public ApproveLoanRequest() {}

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
}
