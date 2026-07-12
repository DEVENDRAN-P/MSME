package com.idbi.msme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class HealthCardResponse {

    @JsonProperty("unified_score")
    private int unifiedScore;

    private String grade;
    private String description;

    @JsonProperty("dimension_scores")
    private DimensionScores dimensionScores;

    private List<String> reasons;

    @JsonProperty("positive_contributors")
    private List<String> positiveContributors;

    @JsonProperty("negative_contributors")
    private List<String> negativeContributors;

    private double confidence;

    @JsonProperty("improvement_suggestions")
    private List<ImprovementSuggestion> improvementSuggestions;

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

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public List<String> getPositiveContributors() {
        return positiveContributors;
    }

    public void setPositiveContributors(List<String> positiveContributors) {
        this.positiveContributors = positiveContributors;
    }

    public List<String> getNegativeContributors() {
        return negativeContributors;
    }

    public void setNegativeContributors(List<String> negativeContributors) {
        this.negativeContributors = negativeContributors;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<ImprovementSuggestion> getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(List<ImprovementSuggestion> improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions;
    }

    // Nested DTOs
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

    public static class ImprovementSuggestion {
        private String suggestion;

        @JsonProperty("expected_improvement")
        private int expectedImprovement;

        public ImprovementSuggestion() {
        }

        public String getSuggestion() {
            return suggestion;
        }

        public void setSuggestion(String suggestion) {
            this.suggestion = suggestion;
        }

        public int getExpectedImprovement() {
            return expectedImprovement;
        }

        public void setExpectedImprovement(int expectedImprovement) {
            this.expectedImprovement = expectedImprovement;
        }
    }
}
