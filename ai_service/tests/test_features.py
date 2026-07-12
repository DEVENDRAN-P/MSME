# test_features.py
# Python pytest module to verify alternate data feature calculations

from app.services.features import calculate_features
import pytest

def test_calculate_features_success():
    # Setup mock GstRecords, UpiRecords, etc.
    mock_data = {
        "gstRecords": [
            {"month": "2025-01", "turnover": 100000.0, "taxPaid": 18000.0, "status": "FILED"},
            {"month": "2025-02", "turnover": 120000.0, "taxPaid": 21600.0, "status": "FILED"},
            {"month": "2025-03", "turnover": 150000.0, "taxPaid": 27000.0, "status": "DELAYED"},
        ],
        "upiRecords": [
            {"month": "2025-01", "creditVolume": 50000.0, "creditCount": 100, "debitVolume": 45000.0, "debitCount": 80},
            {"month": "2025-02", "creditVolume": 60000.0, "creditCount": 120, "debitVolume": 54000.0, "debitCount": 90},
            {"month": "2025-03", "creditVolume": 75000.0, "creditCount": 150, "debitVolume": 67500.0, "debitCount": 100},
        ],
        "bankRecords": [
            {"month": "2025-01", "avgBalance": 20000.0, "inflows": 110000.0, "outflows": 100000.0},
            {"month": "2025-02", "avgBalance": 25000.0, "inflows": 130000.0, "outflows": 120000.0},
            {"month": "2025-03", "avgBalance": 30000.0, "inflows": 160000.0, "outflows": 150000.0},
        ],
        "epfoRecords": [
            {"month": "2025-01", "employeeCount": 10, "contribution": 18000.0},
            {"month": "2025-02", "employeeCount": 10, "contribution": 18000.0},
            {"month": "2025-03", "employeeCount": 12, "contribution": 21600.0},
        ],
        "utilityRecords": [
            {"type": "ELECTRICITY", "month": "2025-01", "amount": 5000.0, "status": "PAID_ON_TIME"},
            {"type": "ELECTRICITY", "month": "2025-02", "amount": 5200.0, "status": "PAID_ON_TIME"},
            {"type": "ELECTRICITY", "month": "2025-03", "amount": 6000.0, "status": "PAID_LATE"},
        ],
        "ecommRecords": [
            {"platform": "ONDC", "month": "2025-01", "sales": 20000.0, "orders": 50},
            {"platform": "ONDC", "month": "2025-02", "sales": 24000.0, "orders": 60},
            {"platform": "ONDC", "month": "2025-03", "sales": 30000.0, "orders": 75},
        ]
    }

    features = calculate_features(mock_data)

    # 1. GST checks
    assert features["gst_filing_discipline_ratio"] == pytest.approx(0.666, 0.01)
    assert features["gst_turnover_growth_rate"] == pytest.approx(0.225, 0.01) # (0.2 + 0.25) / 2
    assert features["gst_turnover_volatility"] > 0.0

    # 2. UPI checks
    assert features["upi_penetration_ratio"] == pytest.approx(0.5, 0.01)
    assert features["upi_avg_credit_tx_value"] == pytest.approx(500.0, 0.01) # (50k+60k+75k) / (100+120+150) = 185k / 370 = 500

    # 3. Bank checks
    assert features["bank_cash_coverage_ratio"] == pytest.approx(1.081, 0.01) # (110k+130k+160k) / (100k+120k+150k) = 400k / 370k
    assert features["bank_mab_to_turnover_ratio"] == pytest.approx(0.2027, 0.01) # mean balance = 25k, mean turnover = 123.3k -> 25 / 123.3 = 0.2027

    # 4. EPFO checks
    assert features["epfo_employee_growth_rate"] == pytest.approx(0.2, 0.01) # (12 - 10) / 10 = 0.2

    # 5. Utility checks
    assert features["utility_late_payment_ratio"] == pytest.approx(0.333, 0.01) # 1 late / 3 total

    # 6. Ecomm checks
    assert features["ecomm_sales_contribution_ratio"] == pytest.approx(0.2, 0.01) # mean ecomm = 24.6k, mean turnover = 123.3k -> 24.6 / 123.3 = 0.2

def test_calculate_features_empty_data():
    empty_data = {}
    features = calculate_features(empty_data)

    # Verify defaults are returned without exception
    assert features["gst_turnover_growth_rate"] == 0.0
    assert features["gst_turnover_volatility"] == 0.0
    assert features["gst_filing_discipline_ratio"] == 0.0
    assert features["upi_penetration_ratio"] == 0.0
    assert features["upi_avg_credit_tx_value"] == 0.0
    assert features["bank_cash_coverage_ratio"] == 1.0
    assert features["bank_mab_to_turnover_ratio"] == 0.0
    assert features["epfo_employee_growth_rate"] == 0.0
    assert features["epfo_payroll_consistency"] == 0.0
    assert features["utility_late_payment_ratio"] == 0.0
    assert features["ecomm_sales_contribution_ratio"] == 0.0
