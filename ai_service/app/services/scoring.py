# scoring.py
# Python MSME Credit Scoring Engine (300-900 Scorecard)

def calculate_credit_score(features: dict) -> dict:
    """
    Computes a CIBIL-like credit score from 300 to 900 based on multi-dimensional alternate features,
    complemented by credit rule-based risk overlays.
    """
    # Extract calculated features with defaults
    volatility = float(features.get("gst_turnover_volatility", 0.0))
    growth = float(features.get("gst_turnover_growth_rate", 0.0))
    filing_ratio = float(features.get("gst_filing_discipline_ratio", 1.0))
    upi_ratio = float(features.get("upi_penetration_ratio", 0.0))
    coverage_ratio = float(features.get("bank_cash_coverage_ratio", 1.0))
    mab_ratio = float(features.get("bank_mab_to_turnover_ratio", 0.0))
    emp_growth = float(features.get("epfo_employee_growth_rate", 0.0))
    payroll_cv = float(features.get("epfo_payroll_consistency", 0.0))
    utility_late_ratio = float(features.get("utility_late_payment_ratio", 0.0))
    ecomm_ratio = float(features.get("ecomm_sales_contribution_ratio", 0.0))

    # 1. Dimension 1: Revenue Health (0 - 100)
    # Volatility impact (variance): lower is better
    rev_vol_points = (1.0 - min(volatility, 1.0)) * 60.0
    # Growth rate impact: scale -15% MoM to +15% MoM to 0-40 points
    rev_growth_points = min(max((growth + 0.15) / 0.30 * 40.0, 0.0), 40.0)
    revenue_score = min(max(rev_vol_points + rev_growth_points, 0.0), 100.0)

    # 2. Dimension 2: Compliance Health (0 - 100)
    # GST Filing punctuality: 70 points max
    gst_points = filing_ratio * 70.0
    # Utility Bill defaults penalty: 30 points max
    utility_points = (1.0 - min(utility_late_ratio, 1.0)) * 30.0
    compliance_score = min(max(gst_points + utility_points, 0.0), 100.0)

    # 3. Dimension 3: Liquidity Health (0 - 100)
    # Cash coverage (Inflow/Outflow): scale 0.8x to 1.1x into 0-60 points
    cash_coverage_points = min(max((coverage_ratio - 0.80) / 0.30 * 60.0, 0.0), 60.0)
    # MAB to Turnover: scale 0% to 25% into 0-40 points
    mab_points = min(mab_ratio * 160.0, 40.0)
    liquidity_score = min(max(cash_coverage_points + mab_points, 0.0), 100.0)

    # 4. Dimension 4: Workforce & Digital Quotient (0 - 100)
    # EPFO employee growth: scale -10% to +20% to 0-40 points
    emp_growth_points = min(max((emp_growth + 0.10) / 0.30 * 40.0, 0.0), 40.0)
    # Payroll consistency: 40 points
    payroll_points = (1.0 - min(payroll_cv, 1.0)) * 40.0
    # Digital Adoption bonus (UPI + E-commerce): 20 points max
    digital_points = min((upi_ratio + ecomm_ratio) * 10.0, 20.0)
    workforce_score = min(max(emp_growth_points + payroll_points + digital_points, 0.0), 100.0)

    # 5. Base Score Aggregation
    # Weights: Compliance (30%), Liquidity (30%), Revenue (25%), Workforce (15%)
    weighted_average = (
        compliance_score * 0.30 +
        liquidity_score * 0.30 +
        revenue_score * 0.25 +
        workforce_score * 0.15
    )

    # Map 0-100 average to 300-900 credit score
    base_score = 300.0 + (weighted_average / 100.0) * 600.0

    # 6. Rule-Based Overlays (Red Flags)
    penalties = 0.0
    if utility_late_ratio >= 0.30:
        penalties += 40.0  # Frequent late utility payments
    if filing_ratio <= 0.60:
        penalties += 60.0  # Severe tax filing delay
    if coverage_ratio < 0.90:
        penalties += 80.0  # Cash deficit (insolvency indicator)

    final_score = int(round(max(base_score - penalties, 300.0)))
    final_score = min(final_score, 900)

    # Grade Classification
    if final_score >= 750:
        grade = "PRIME_PLUS"
        description = "Excellent credit profile. Low risk of default."
    elif final_score >= 680:
        grade = "PRIME"
        description = "Strong credit profile. Normal risk checks apply."
    elif final_score >= 600:
        grade = "NEAR_PRIME"
        description = "Moderate risk. Enhancements or collateral might be requested."
    else:
        grade = "SUB_PRIME"
        description = "High risk. High probability of payment delay or cash constraints."

    return {
        "unified_score": final_score,
        "grade": grade,
        "description": description,
        "dimension_scores": {
            "revenue_health": int(round(revenue_score)),
            "compliance_health": int(round(compliance_score)),
            "liquidity_health": int(round(liquidity_score)),
            "workforce_health": int(round(workforce_score))
        }
    }
