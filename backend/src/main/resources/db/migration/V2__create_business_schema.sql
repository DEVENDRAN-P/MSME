-- V2__create_business_schema.sql
-- Schema migration for MSME Business Registration

CREATE TABLE businesses (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL UNIQUE,
    legal_name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255),
    gstin VARCHAR(15) NOT NULL UNIQUE,
    pan VARCHAR(10) NOT NULL UNIQUE,
    udyam_number VARCHAR(25) NOT NULL UNIQUE,
    incorporation_date DATE NOT NULL,
    constitution VARCHAR(100) NOT NULL,
    industry_sector VARCHAR(100) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index for searching businesses by owners
CREATE INDEX idx_businesses_owner ON businesses(owner_id);

-- Index for quick tax ID checks
CREATE INDEX idx_businesses_gstin_pan ON businesses(gstin, pan);
