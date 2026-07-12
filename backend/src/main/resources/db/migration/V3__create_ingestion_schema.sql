-- V3__create_ingestion_schema.sql
-- Database schema for Alternate Data Ingestion Module

CREATE TABLE gst_filings (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    filing_month VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    turnover NUMERIC(15, 2) NOT NULL,
    tax_paid NUMERIC(15, 2) NOT NULL,
    filing_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE(business_id, filing_month)
);

CREATE TABLE upi_transactions (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    month VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    total_credit_volume NUMERIC(15, 2) NOT NULL,
    total_credit_count INT NOT NULL,
    total_debit_volume NUMERIC(15, 2) NOT NULL,
    total_debit_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE(business_id, month)
);

CREATE TABLE aa_bank_transactions (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    month VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    avg_balance NUMERIC(15, 2) NOT NULL,
    inward_remittances NUMERIC(15, 2) NOT NULL,
    outward_remittances NUMERIC(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE(business_id, month)
);

CREATE TABLE epfo_records (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    month VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    employee_count INT NOT NULL,
    contribution_amount NUMERIC(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE(business_id, month)
);

CREATE TABLE utility_payments (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    utility_type VARCHAR(50) NOT NULL, -- E.g. ELECTRICITY, TELECOM, WATER
    billing_month VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    amount NUMERIC(15, 2) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE(business_id, utility_type, billing_month)
);

CREATE TABLE ecommerce_sales (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    platform VARCHAR(50) NOT NULL, -- E.g. AMAZON, FLIPKART, ONDC
    month VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    sales_volume NUMERIC(15, 2) NOT NULL,
    order_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    UNIQUE(business_id, platform, month)
);

-- Indices for fast aggregation query lookups by business_id
CREATE INDEX idx_gst_filings_biz ON gst_filings(business_id);
CREATE INDEX idx_upi_tx_biz ON upi_transactions(business_id);
CREATE INDEX idx_aa_tx_biz ON aa_bank_transactions(business_id);
CREATE INDEX idx_epfo_rec_biz ON epfo_records(business_id);
CREATE INDEX idx_utility_pay_biz ON utility_payments(business_id);
CREATE INDEX idx_ecomm_sales_biz ON ecommerce_sales(business_id);
