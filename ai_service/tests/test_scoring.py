# test_scoring.py
# Python pytest module to verify credit scoring calculations

from app.services.scoring import calculate_credit_score
import pytest

def test_scoring_prime_plus():
    mock_features = {
        "gst_turnover_volatility": 0.05,
        "gst_turnover_growth_rate": 0.12,
        "gst_filing_discipline_ratio": 1.0,
        "upi_penetration_ratio": 0.60,
        "bank_cash_coverage_ratio": 1.08,
        "bank_mab_to_turnover_ratio": 0.20,
        "epfo_employee_growth_rate": 0.15,
        "epfo_payroll_consistency": 0.05,
        "utility_late_payment_ratio": 0.0,
        "ecomm_sales_contribution_ratio": 0.25
    }

    result = calculate_credit_score(mock_features)

    assert result["unified_score"] >= 750
    assert result["grade"] == "PRIME_PLUS"
    assert result["dimension_scores"]["revenue_health"] > 80
    assert result["dimension_scores"]["compliance_health"] == 100
    assert result["dimension_scores"]["liquidity_health"] > 80

def test_scoring_red_flags_and_subprime():
    mock_features = {
        "gst_turnover_volatility": 0.50,
        "gst_turnover_growth_rate": -0.10,
        "gst_filing_discipline_ratio": 0.50, # Red flag penalty
        "upi_penetration_ratio": 0.05,
        "bank_cash_coverage_ratio": 0.85, # Red flag penalty
        "bank_mab_to_turnover_ratio": 0.02,
        "epfo_employee_growth_rate": -0.20,
        "epfo_payroll_consistency": 0.40,
        "utility_late_payment_ratio": 0.40, # Red flag penalty
        "ecomm_sales_contribution_ratio": 0.0
    }

    result = calculate_credit_score(mock_features)

    assert result["unified_score"] < 600
    assert result["grade"] == "SUB_PRIME"
    assert result["dimension_scores"]["compliance_health"] == 53
    assert result["dimension_scores"]["liquidity_health"] < 50
