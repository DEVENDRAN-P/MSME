package com.idbi.msme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthCardResponse {

    @JsonProperty("unified_score")
    private int unifiedScore;

    private String grade;
    private String description;

    @JsonProperty("dimension_scores")
    private DimensionScores dimensionScores;

    public HealthCardResponse() {
    }

    // Getters and Setters
    public int getUnifiedScore() {
        return unifiedScore;
    }

    public void setUnifiedScore(int unifiedScore) {
        this.unifiedScore = unifiedScore;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DimensionScores getDimensionScores() {
        return dimensionScores;
    }

    public void setDimensionScores(DimensionScores dimensionScores) {
        this.dimensionScores = dimensionScores;
    }

    // Nested DTO
    public static class DimensionScores {

        @JsonProperty("revenue_health")
        private int revenueHealth;

        @JsonProperty("compliance_health")
        private int complianceHealth;

        @JsonProperty("liquidity_health")
        private int liquidityHealth;

        @JsonProperty("workforce_health")
        private int workforceHealth;

        public DimensionScores() {
        }

        // Getters and Setters
        public int getRevenueHealth() {
            return revenueHealth;
        }

        public void setRevenueHealth(int revenueHealth) {
            this.revenueHealth = revenueHealth;
        }

        public int getComplianceHealth() {
            return complianceHealth;
        }

        public void setComplianceHealth(int complianceHealth) {
            this.complianceHealth = complianceHealth;
        }

        public int getLiquidityHealth() {
            return liquidityHealth;
        }

        public void setLiquidityHealth(int liquidityHealth) {
            this.liquidityHealth = liquidityHealth;
        }

        public int getWorkforceHealth() {
            return workforceHealth;
        }

        public void setWorkforceHealth(int workforceHealth) {
            this.workforceHealth = workforceHealth;
        }
    }
}
