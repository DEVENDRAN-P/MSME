# ENHANCEMENT 5: API DESIGN & SECURITY
## RESTful API Architecture with Enterprise Security

---

### Feature Name
**Comprehensive API Gateway with OAuth 2.0, RBAC, and Audit Logging**

---

### Problem It Solves
Banking applications require enterprise-grade security, rate limiting, and audit trails. APIs must be standardized for integration with multiple stakeholders.

---

### Why It Is Needed
- RBI mandates data security and privacy
- Multiple consumers (MSMEs, loan officers, regulators)
- API versioning for backward compatibility
- Comprehensive audit trail for compliance

---

### Relation to Problem Statement
Enables secure, scalable APIs for all platform features including ULI, OCEN, and AA integrations.

---

### Business Benefits
- 99.99% API availability
- Sub-100ms response time
- Full audit trail for compliance
- Easy integration for partners

---

### Technical Benefits
- Microservice-ready architecture
- Automatic API documentation
- Rate limiting and throttling
- Circuit breaker pattern

---

### API Endpoints

```yaml
# Financial Health Card APIs
/api/v1/health-card:
  GET /{msmeId}:
    description: Get financial health card
    response: FinancialHealthCard
    
  POST /compute:
    description: Compute health score
    request: ComputeRequest
    response: HealthCard
    
  GET /{msmeId}/history:
    description: Get score history
    response: ScoreHistory[]

# Scoring APIs
/api/v1/scoring:
  POST /predict:
    description: Get credit score
    request: ScoringRequest
    response: ScoringResponse
    
  POST /explain:
    description: Get explainable AI output
    request: ExplainRequest
    response: Explanation

# Consent Management
/api/v1/consent:
  POST /create:
    description: Create consent request
    request: ConsentRequest
    response: ConsentResponse
    
  GET /{consentId}/status:
    description: Check consent status
    response: ConsentStatus
    
  DELETE /{consentId}:
    description: Revoke consent

# Account Aggregator
/api/v1/aa:
  POST /initiate:
    description: Initiate AA data fetch
    request: AARequest
    response: AAResponse
    
  GET /callback:
    description: AA callback handler
    
# ULI Integration
/api/v1/uli:
  POST /submit:
    description: Submit loan application to ULI
    request: ULIRequest
    response: ULIResponse
    
  GET /{applicationId}/status:
    description: Get ULI application status
    
# OCEN Integration
/api/v1/ocen:
  POST /create-offer:
    description: Create loan offer via OCEN
    request: OCENOfferRequest
    response: OCENOffer
    
  POST /accept-offer:
    description: Accept loan offer
    request: AcceptOfferRequest

# Loan Management
/api/v1/loans:
  GET /:
    description: List loans
    query: [status, msmeId, lenderId]
    
  POST /apply:
    description: Apply for loan
    request: LoanApplication
    
  GET /{loanId}:
    description: Get loan details
    
  PUT /{loanId}/status:
    description: Update loan status

# User Management
/api/v1/users:
  POST /register:
    description: Register new user
    
  POST /login:
    description: User login
    response: AuthTokens
    
  GET /profile:
    description: Get user profile
    
  PUT /profile:
    description: Update profile

# Admin APIs
/api/v1/admin:
  GET /system-health:
    description: Get system health
    
  GET /model-performance:
    description: Get model metrics
    
  POST /retrain-model:
    description: Trigger model retraining
    
  GET /audit-logs:
    description: Get audit logs
```

---

### Security Implementation

```python
# Authentication Middleware
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from datetime import datetime, timedelta

app = FastAPI()

# JWT Configuration
SECRET_KEY = config.SECRET_KEY
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

class AuthenticationMiddleware:
    def __init__(self):
        self.token_verifier = TokenVerifier()
        self.rbac = RBACManager()
    
    async def verify_token(self, token: str = Depends(oauth2_scheme)):
        credentials_exception = HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials",
            headers={"WWW-Authenticate": "Bearer"},
        )
        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
            user_id: str = payload.get("sub")
            if user_id is None:
                raise credentials_exception
            return payload
        except JWTError:
            raise credentials_exception
    
    async def check_permission(
        self, 
        user: dict, 
        resource: str, 
        action: str
    ):
        if not self.rbac.has_permission(user['role'], resource, action):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Insufficient permissions"
            )

# Rate Limiting
from slowapi import Limiter
from slowapi.util import get_remote_address

limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter

@app.get("/api/v1/health-card/{msmeId}")
@limiter.limit("100/minute")
async def get_health_card(
    msmeId: str,
    current_user: dict = Depends(auth.verify_token)
):
    # Check permission
    await auth.check_permission(current_user, 'health-card', 'read')
    
    # Log access
    await audit_log.log(
        user_id=current_user['sub'],
        action='read_health_card',
        resource=msmeId
    )
    
    # Fetch and return
    return await health_card_service.get(msmeId)

# Audit Logging
class AuditLogger:
    async def log(self, user_id: str, action: str, resource: str, details: dict = None):
        await self.db.audit_logs.insert_one({
            'user_id': user_id,
            'action': action,
            'resource': resource,
            'details': details,
            'ip_address': request.client.host,
            'user_agent': request.headers.get('user-agent'),
            'timestamp': datetime.utcnow()
        })

# Input Validation
from pydantic import BaseModel, Field, validator

class ScoringRequest(BaseModel):
    msme_id: str = Field(..., min_length=1, max_length=100)
    data_sources: List[str] = Field(..., min_items=1)
    include_explanation: bool = True
    
    @validator('msme_id')
    def validate_msme_id(cls, v):
        if not re.match(r'^[A-Z0-9]{8,12}$', v):
            raise ValueError('Invalid MSME ID format')
        return v

# Encryption
from cryptography.fernet import Fernet

class EncryptionService:
    def __init__(self):
        self.key = config.ENCRYPTION_KEY
        self.cipher = Fernet(self.key)
    
    def encrypt(self, data: str) -> str:
        return self.cipher.encrypt(data.encode()).decode()
    
    def decrypt(self, encrypted: str) -> str:
        return self.cipher.decrypt(encrypted.encode()).decode()
```

---

### API Gateway Configuration

```yaml
# Kong API Gateway Configuration
services:
  - name: financial-health-api
    url: http://health-service:8000
    routes:
      - name: health-card-routes
        paths: ["/api/v1/health-card"]
        strip_path: false
    plugins:
      - name: rate-limiting
        config:
          minute: 100
          policy: redis
      - name: jwt
        config:
          claims_to_verify: [exp, iss]
      - name: cors
        config:
          origins: ["https://msme-platform.idbi.com"]
          methods: ["GET", "POST", "PUT", "DELETE"]
      - name: request-transformer
        config:
          add:
            headers: ["X-Request-ID:$(uuid)"]
  
  - name: scoring-api
    url: http://scoring-service:8001
    routes:
      - name: scoring-routes
        paths: ["/api/v1/scoring"]
    plugins:
      - name: rate-limiting
        config:
          minute: 50
      - name: jwt
  
  - name: aa-integration-api
    url: http://aa-service:8002
    routes:
      - name: aa-routes
        paths: ["/api/v1/aa"]
    plugins:
      - name: rate-limiting
        config:
          minute: 30
      - name: jwt
```

---

### Database Changes

```sql
-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    last_login TIMESTAMPTZ,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Role-Based Access Control
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    permissions JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE user_roles (
    user_id UUID REFERENCES users(id),
    role_id UUID REFERENCES roles(id),
    assigned_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
);

-- API Keys
CREATE TABLE api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    key_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    permissions JSONB NOT NULL,
    rate_limit INTEGER DEFAULT 100,
    status VARCHAR(20) DEFAULT 'active',
    expires_at TIMESTAMPTZ,
    last_used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Audit Logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    resource_id VARCHAR(100),
    details JSONB,
    ip_address INET,
    user_agent TEXT,
    request_id VARCHAR(100),
    response_time_ms INTEGER,
    status_code INTEGER,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user ON audit_logs(user_id, created_at DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action, created_at DESC);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource, resource_id);

-- Rate Limiting (Redis-backed)
CREATE TABLE rate_limits (
    key VARCHAR(255) PRIMARY KEY,
    count INTEGER NOT NULL,
    window_start TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);

-- Session Management
CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

-- Encryption Keys
CREATE TABLE encryption_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_version INTEGER NOT NULL,
    key_hash VARCHAR(255) NOT NULL,
    algorithm VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    rotated_at TIMESTAMPTZ
);
```

---

### Frontend Changes

```
┌─────────────────────────────────────────────────────────────────┐
│                 SECURITY & CONSENT UI                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  CONSENT MANAGEMENT                                      │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  Data Access Consent                             │    │    │
│  │  │                                                  │    │    │
│  │  │  We need your consent to access:                │    │    │
│  │  │                                                  │    │    │
│  │  │  ☑️ GST Transaction Data (6 months)              │    │    │
│  │  │  ☑️ UPI Transaction History                      │    │    │
│  │  │  ☑️ Bank Statement (Optional)                    │    │    │
│  │  │  ☐ EPFO Employee Data                           │    │    │
│  │  │                                                  │    │    │
│  │  │  Purpose: Credit Assessment                     │    │    │
│  │  │  Validity: 90 days                              │    │    │
│  │  │                                                  │    │    │
│  │  │  [View Data Usage Policy]                       │    │    │
│  │  │  [Grant Consent] [Deny]                         │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  │  Active Consents:                                       │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │ GST Data - Granted 2 days ago - Expires in 88d │    │    │
│  │  │ UPI Data - Granted 5 days ago - Expires in 85d │    │    │
│  │  │ [Revoke] [View Details]                         │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  SECURITY SETTINGS                                       │    │
│  │                                                          │    │
│  │  Two-Factor Authentication: [Enabled] [Configure]       │    │
│  │  Login Notifications: [Enabled]                         │    │
│  │  Session Timeout: [30 minutes]                          │    │
│  │  API Key Management: [View Keys] [Generate New]         │    │
│  │                                                          │    │
│  │  Recent Activity:                                       │    │
│  │  • Login from Mumbai, India (2 hours ago)              │    │
│  │  • API access from 103.21.58.X (1 hour ago)            │    │
│  │  • Password changed (30 days ago)                       │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

### Estimated Development Time
- **API Gateway Setup**: 1 week
- **Authentication System**: 1 week
- **Rate Limiting**: 2 days
- **Audit Logging**: 2 days
- **Input Validation**: 2 days
- **Total**: 3 weeks

---

### Hackathon Priority
**HIGH** - Essential for banking compliance

---

### Difficulty Level
**MEDIUM** - Standard security implementation

---

### Expected Judge Impression
**EXCELLENT** - Demonstrates enterprise-grade security awareness
