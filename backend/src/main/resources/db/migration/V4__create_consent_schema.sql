CREATE TABLE consents (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    requested_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    consent_type VARCHAR(50) NOT NULL, -- GST, UPI, AA, ALL
    valid_until TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL, -- PENDING, APPROVED, DENIED, REVOKED, EXPIRED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_consents_business ON consents(business_id);
CREATE INDEX idx_consents_requestor ON consents(requested_by);
