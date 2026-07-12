package com.idbi.msme.dto;

import java.math.BigDecimal;

public class ForecastResponse {

    private String month;
    private BigDecimal projectedSales;
    private BigDecimal emi;
    private BigDecimal netSurplus;

    public ForecastResponse() {
    }

    public ForecastResponse(String month, BigDecimal projectedSales, BigDecimal emi, BigDecimal netSurplus) {
        this.month = month;
        this.projectedSales = projectedSales;
        this.emi = emi;
        this.netSurplus = netSurplus;
    }

    // Getters and Setters
    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getProjectedSales() {
        return projectedSales;
    }

    public void setProjectedSales(BigDecimal projectedSales) {
        this.projectedSales = projectedSales;
    }

    public BigDecimal getEmi() {
        return emi;
    }

    public void setEmi(BigDecimal emi) {
        this.emi = emi;
    }

    public BigDecimal getNetSurplus() {
        return netSurplus;
    }

    public void setNetSurplus(BigDecimal netSurplus) {
        this.netSurplus = netSurplus;
    }
}
