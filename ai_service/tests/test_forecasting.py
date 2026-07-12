# test_forecasting.py
# Python pytest module to verify cash flow forecasting projections

from app.services.forecasting import calculate_emi, forecast_cash_flows
import pytest

def test_calculate_emi():
    # Principal: 10 Lakhs, Interest: 12% per annum, Tenure: 12 months
    emi = calculate_emi(1000000.0, 12.0, 12)
    # Monthly interest rate = 0.01
    # EMI = 1000000 * 0.01 * (1.01^12) / (1.01^12 - 1) = 88848.79
    assert emi == pytest.approx(88848.79, 0.01)

    # principal 0 check
    assert calculate_emi(0.0, 10.0, 10) == 0.0

def test_forecast_cash_flows():
    mock_payload = {
        "gstRecords": [
            {"month": "2025-01", "turnover": 1000000.0},
            {"month": "2025-02", "turnover": 1050000.0},
        ],
        "loan": {
            "amount": 500000.0,
            "interestRate": 12.0,
            "tenureMonths": 6
        }
    }

    forecast = forecast_cash_flows(mock_payload)

    assert len(forecast) == 6
    for item in forecast:
        assert "month" in item
        assert "projectedSales" in item
        assert "emi" in item
        assert "netSurplus" in item
        assert item["emi"] > 0
