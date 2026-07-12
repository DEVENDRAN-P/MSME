# features.py
# Python Alternate Data Feature Extraction Engine

import pandas as pd
import numpy as np

def calculate_features(data: dict) -> dict:
    """
    Extract multi-dimensional credit indicators from alternate data streams.
    Handles empty arrays gracefully for credit-invisible businesses.
    """
    gst_df = pd.DataFrame(data.get("gstRecords", []))
    upi_df = pd.DataFrame(data.get("upiRecords", []))
    bank_df = pd.DataFrame(data.get("bankRecords", []))
    epfo_df = pd.DataFrame(data.get("epfoRecords", []))
    utility_df = pd.DataFrame(data.get("utilityRecords", []))
    ecomm_df = pd.DataFrame(data.get("ecommRecords", []))

    features = {}

    # 1. GST & Revenue Features
    if not gst_df.empty and "turnover" in gst_df.columns:
        gst_df["turnover"] = gst_df["turnover"].astype(float)
        turnover_vals = gst_df["turnover"].values
        mean_turnover = float(np.mean(turnover_vals))
        std_turnover = float(np.std(turnover_vals))

        # Coefficient of Variation (Volatility proxy)
        features["gst_turnover_volatility"] = std_turnover / mean_turnover if mean_turnover > 0 else 0.0

        # MoM Growth rate average
        if len(turnover_vals) > 1:
            mom_growth = []
            for i in range(1, len(turnover_vals)):
                prev = turnover_vals[i-1]
                curr = turnover_vals[i]
                growth = (curr - prev) / prev if prev > 0 else 0.0
                mom_growth.append(growth)
            features["gst_turnover_growth_rate"] = float(np.mean(mom_growth))
        else:
            features["gst_turnover_growth_rate"] = 0.0

        # Filing Discipline (Filed / Total)
        if "status" in gst_df.columns:
            filed_count = int(np.sum(gst_df["status"] == "FILED"))
            features["gst_filing_discipline_ratio"] = filed_count / len(gst_df)
        else:
            features["gst_filing_discipline_ratio"] = 1.0
    else:
        mean_turnover = 0.0
        features["gst_turnover_volatility"] = 0.0
        features["gst_turnover_growth_rate"] = 0.0
        features["gst_filing_discipline_ratio"] = 0.0

    # 2. UPI Transaction Features
    if not upi_df.empty and "creditVolume" in upi_df.columns:
        upi_df["creditVolume"] = upi_df["creditVolume"].astype(float)
        upi_df["creditCount"] = upi_df["creditCount"].astype(int)
        
        mean_upi_credit = float(np.mean(upi_df["creditVolume"].values))
        total_upi_volume = float(np.sum(upi_df["creditVolume"].values))
        total_upi_count = int(np.sum(upi_df["creditCount"].values))

        # UPI Penetration Ratio
        features["upi_penetration_ratio"] = mean_upi_credit / mean_turnover if mean_turnover > 0 else 0.0
        # Average ticket size
        features["upi_avg_credit_tx_value"] = total_upi_volume / total_upi_count if total_upi_count > 0 else 0.0
    else:
        features["upi_penetration_ratio"] = 0.0
        features["upi_avg_credit_tx_value"] = 0.0

    # 3. Account Aggregator Bank Cash Flow Features
    if not bank_df.empty and "inflows" in bank_df.columns:
        bank_df["inflows"] = bank_df["inflows"].astype(float)
        bank_df["outflows"] = bank_df["outflows"].astype(float)
        bank_df["avgBalance"] = bank_df["avgBalance"].astype(float)

        total_inflows = float(np.sum(bank_df["inflows"].values))
        total_outflows = float(np.sum(bank_df["outflows"].values))
        mean_balance = float(np.mean(bank_df["avgBalance"].values))

        # Cash Coverage ratio (Inward / Outward)
        features["bank_cash_coverage_ratio"] = total_inflows / total_outflows if total_outflows > 0 else 1.0
        # MAB to monthly turnover ratio
        features["bank_mab_to_turnover_ratio"] = mean_balance / mean_turnover if mean_turnover > 0 else 0.0
    else:
        features["bank_cash_coverage_ratio"] = 1.0
        features["bank_mab_to_turnover_ratio"] = 0.0

    # 4. EPFO Workforce Features
    if not epfo_df.empty and "employeeCount" in epfo_df.columns:
        epfo_df["employeeCount"] = epfo_df["employeeCount"].astype(int)
        epfo_df["contribution"] = epfo_df["contribution"].astype(float)
        
        emp_counts = epfo_df["employeeCount"].values
        mean_contribution = float(np.mean(epfo_df["contribution"].values))
        std_contribution = float(np.std(epfo_df["contribution"].values))

        # MoM Headcount growth
        if len(emp_counts) > 1:
            first_month_emp = emp_counts[0]
            last_month_emp = emp_counts[-1]
            features["epfo_employee_growth_rate"] = (last_month_emp - first_month_emp) / first_month_emp if first_month_emp > 0 else 0.0
        else:
            features["epfo_employee_growth_rate"] = 0.0

        # Contribution Volatility
        features["epfo_payroll_consistency"] = std_contribution / mean_contribution if mean_contribution > 0 else 0.0
    else:
        features["epfo_employee_growth_rate"] = 0.0
        features["epfo_payroll_consistency"] = 0.0

    # 5. Utility Bills Payment Discipline Features
    if not utility_df.empty and "status" in utility_df.columns:
        total_payments = len(utility_df)
        late_payments = int(np.sum(utility_df["status"].isin(["PAID_LATE", "UNPAID"])))
        features["utility_late_payment_ratio"] = late_payments / total_payments if total_payments > 0 else 0.0
    else:
        features["utility_late_payment_ratio"] = 0.0

    # 6. E-Commerce Sales Features
    if not ecomm_df.empty and "sales" in ecomm_df.columns:
        ecomm_df["sales"] = ecomm_df["sales"].astype(float)
        mean_ecomm_sales = float(np.mean(ecomm_df["sales"].values))
        
        features["ecomm_sales_contribution_ratio"] = mean_ecomm_sales / mean_turnover if mean_turnover > 0 else 0.0
    else:
        features["ecomm_sales_contribution_ratio"] = 0.0

    return features
