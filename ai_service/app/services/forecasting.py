# forecasting.py
# Python Cash Flow Forecasting & Loan Amortization Simulator

import numpy as np

def calculate_emi(principal: float, annual_rate: float, tenure_months: int) -> float:
    """
    Calculate Equated Monthly Installment (EMI) using the standard formula.
    """
    if principal <= 0 or annual_rate <= 0 or tenure_months <= 0:
        return 0.0
    
    monthly_rate = (annual_rate / 12.0) / 100.0
    emi = (principal * monthly_rate * ((1 + monthly_rate) ** tenure_months)) / (((1 + monthly_rate) ** tenure_months) - 1)
    return float(emi)

def forecast_cash_flows(payload: dict) -> list:
    """
    Forecast 6 months of future sales revenues, EMI liabilities, and net surplus cash balances.
    """
    gst_records = payload.get("gstRecords", [])
    loan = payload.get("loan", {})

    # Extract historical base turnover
    if gst_records:
        turnovers = [float(r.get("turnover", 0.0)) for r in gst_records]
        base_sales = float(np.mean(turnovers))
        
        # Calculate historical average growth rate
        if len(turnovers) > 1:
            growths = []
            for i in range(1, len(turnovers)):
                prev = turnovers[i-1]
                curr = turnovers[i]
                growths.append((curr - prev) / prev if prev > 0 else 0.0)
            avg_growth = float(np.mean(growths))
        else:
            avg_growth = 0.015 # default 1.5% growth
    else:
        base_sales = 500000.0  # default 5L
        avg_growth = 0.015

    # Extract loan parameters
    principal = float(loan.get("amount", 0.0))
    rate = float(loan.get("interestRate", 0.0))
    tenure = int(loan.get("tenureMonths", 0))

    monthly_emi = calculate_emi(principal, rate, tenure)

    # Project next 6 months
    forecast = []
    current_sales = base_sales

    for month_idx in range(1, 7):
        # Sales projection with growth trend and slight volatility
        noise = np.random.uniform(-0.04, 0.04)
        projected_sales = current_sales * (1 + avg_growth + noise)
        current_sales = projected_sales # roll forward

        # Base operational expenses (usually 85-90% of revenue)
        base_outflows = projected_sales * 0.88

        # Total outflows include EMI if loan is active
        total_outflows = base_outflows + monthly_emi
        net_surplus = projected_sales - total_outflows

        forecast.append({
            "month": f"Forecast M+{month_idx}",
            "projectedSales": round(projected_sales, 2),
            "emi": round(monthly_emi, 2),
            "netSurplus": round(net_surplus, 2)
        })

    return forecast
