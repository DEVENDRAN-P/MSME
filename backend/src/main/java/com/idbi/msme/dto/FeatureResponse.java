package com.idbi.msme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class FeatureResponse {

    @JsonProperty("gst_turnover_volatility")
    private BigDecimal gstTurnoverVolatility;

    @JsonProperty("gst_turnover_growth_rate")
    private BigDecimal gstTurnoverGrowthRate;

    @JsonProperty("gst_filing_discipline_ratio")
    private BigDecimal gstFilingDisciplineRatio;

    @JsonProperty("upi_penetration_ratio")
    private BigDecimal upiPenetrationRatio;

    @JsonProperty("upi_avg_credit_tx_value")
    private BigDecimal upiAvgCreditTxValue;

    @JsonProperty("bank_cash_coverage_ratio")
    private BigDecimal bankCashCoverageRatio;

    @JsonProperty("bank_mab_to_turnover_ratio")
    private BigDecimal bankMabToTurnoverRatio;

    @JsonProperty("epfo_employee_growth_rate")
    private BigDecimal epfoEmployeeGrowthRate;

    @JsonProperty("epfo_payroll_consistency")
    private BigDecimal epfoPayrollConsistency;

    @JsonProperty("utility_late_payment_ratio")
    private BigDecimal utilityLatePaymentRatio;

    @JsonProperty("ecomm_sales_contribution_ratio")
    private BigDecimal ecommSalesContributionRatio;

    public FeatureResponse() {
    }

    // Getters and Setters
    public BigDecimal getGstTurnoverVolatility() {
        return gstTurnoverVolatility;
    }

    public void setGstTurnoverVolatility(BigDecimal gstTurnoverVolatility) {
        this.gstTurnoverVolatility = gstTurnoverVolatility;
    }

    public BigDecimal getGstTurnoverGrowthRate() {
        return gstTurnoverGrowthRate;
    }

    public void setGstTurnoverGrowthRate(BigDecimal gstTurnoverGrowthRate) {
        this.gstTurnoverGrowthRate = gstTurnoverGrowthRate;
    }

    public BigDecimal getGstFilingDisciplineRatio() {
        return gstFilingDisciplineRatio;
    }

    public void setGstFilingDisciplineRatio(BigDecimal gstFilingDisciplineRatio) {
        this.gstFilingDisciplineRatio = gstFilingDisciplineRatio;
    }

    public BigDecimal getUpiPenetrationRatio() {
        return upiPenetrationRatio;
    }

    public void setUpiPenetrationRatio(BigDecimal upiPenetrationRatio) {
        this.upiPenetrationRatio = upiPenetrationRatio;
    }

    public BigDecimal getUpiAvgCreditTxValue() {
        return upiAvgCreditTxValue;
    }

    public void setUpiAvgCreditTxValue(BigDecimal upiAvgCreditTxValue) {
        this.upiAvgCreditTxValue = upiAvgCreditTxValue;
    }

    public BigDecimal getBankCashCoverageRatio() {
        return bankCashCoverageRatio;
    }

    public void setBankCashCoverageRatio(BigDecimal bankCashCoverageRatio) {
        this.bankCashCoverageRatio = bankCashCoverageRatio;
    }

    public BigDecimal getBankMabToTurnoverRatio() {
        return bankMabToTurnoverRatio;
    }

    public void setBankMabToTurnoverRatio(BigDecimal bankMabToTurnoverRatio) {
        this.bankMabToTurnoverRatio = bankMabToTurnoverRatio;
    }

    public BigDecimal getEpfoEmployeeGrowthRate() {
        return epfoEmployeeGrowthRate;
    }

    public void setEpfoEmployeeGrowthRate(BigDecimal epfoEmployeeGrowthRate) {
        this.epfoEmployeeGrowthRate = epfoEmployeeGrowthRate;
    }

    public BigDecimal getEpfoPayrollConsistency() {
        return epfoPayrollConsistency;
    }

    public void setEpfoPayrollConsistency(BigDecimal epfoPayrollConsistency) {
        this.epfoPayrollConsistency = epfoPayrollConsistency;
    }

    public BigDecimal getUtilityLatePaymentRatio() {
        return utilityLatePaymentRatio;
    }

    public void setUtilityLatePaymentRatio(BigDecimal utilityLatePaymentRatio) {
        this.utilityLatePaymentRatio = utilityLatePaymentRatio;
    }

    public BigDecimal getEcommSalesContributionRatio() {
        return ecommSalesContributionRatio;
    }

    public void setEcommSalesContributionRatio(BigDecimal ecommSalesContributionRatio) {
        this.ecommSalesContributionRatio = ecommSalesContributionRatio;
    }
}
