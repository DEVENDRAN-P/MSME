# MODULE 1: AUTHENTICATION & USER MANAGEMENT
## Complete Implementation Guide

---

## 1.1 DATABASE SCHEMA

```sql
-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN (
        'msme_owner', 'loan_officer', 'credit_manager', 
        'risk_analyst', 'admin', 'super_admin'
    )),
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN (
        'active', 'inactive', 'suspended', 'pending_verification'
    )),
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    last_login TIMESTAMPTZ,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMPTZ,
    password_changed_at TIMESTAMPTZ DEFAULT NOW(),
    password_reset_token VARCHAR(255),
    password_reset_expires TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- User Profiles (MSME Owners)
CREATE TABLE msme_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_name VARCHAR(255) NOT NULL,
    business_type VARCHAR(50) NOT NULL,
    industry_classification VARCHAR(100) NOT NULL,
    udyam_number VARCHAR(20),
    pan_number VARCHAR(10),
    gstin VARCHAR(15),
    cin VARCHAR(21),
    business_address JSONB NOT NULL,
    years_in_business INTEGER,
    number_of_employees INTEGER,
    annual_turnover DECIMAL(15,2),
    bank_account_details JSONB,
    existing_loan_details JSONB,
    owner_details JSONB NOT NULL,
    verification_status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Bank Staff Profiles
CREATE TABLE bank_staff_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    employee_id VARCHAR(20) UNIQUE NOT NULL,
    department VARCHAR(50) NOT NULL,
    branch VARCHAR(100),
    designation VARCHAR(100),
    reporting_to UUID REFERENCES users(id),
    permissions JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- KYC Verification Records
CREATE TABLE kyc_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    document_type VARCHAR(50) NOT NULL,
    document_number VARCHAR(100) NOT NULL,
    verification_status VARCHAR(20) DEFAULT 'pending',
    verification_source VARCHAR(50),
    verification_data JSONB,
    verified_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Sessions Table
CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    refresh_token_hash VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    device_info JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    last_active_at TIMESTAMPTZ DEFAULT NOW(),
    revoked_at TIMESTAMPTZ
);

-- Audit Logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100),
    old_value JSONB,
    new_value JSONB,
    ip_address INET,
    user_agent TEXT,
    request_id VARCHAR(100),
    status_code INTEGER,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user ON audit_logs(user_id, created_at DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action, created_at DESC);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);

-- Password History
CREATE TABLE password_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- MFA Backup Codes
CREATE TABLE mfa_backup_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code_hash VARCHAR(255) NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

## 1.2 BACKEND IMPLEMENTATION

### Auth Service

```python
# app/services/auth_service.py
from datetime import datetime, timedelta
from typing import Optional, Dict
import bcrypt
import jwt
import pyotp
import secrets
from sqlalchemy.orm import Session
from app.models import User, Session as UserSession, AuditLog
from app.core.config import settings

class AuthService:
    def __init__(self, db: Session):
        self.db = db
    
    async def register_msme(
        self, 
        email: str, 
        password: str, 
        phone: str,
        business_data: dict
    ) -> Dict:
        """Register new MSME user"""
        
        # Check if user exists
        existing = self.db.query(User).filter(
            (User.email == email) | (User.phone == phone)
        ).first()
        
        if existing:
            raise ValueError("User already exists")
        
        # Create user
        password_hash = bcrypt.hashpw(
            password.encode(), 
            bcrypt.gensalt()
        ).decode()
        
        user = User(
            email=email,
            phone=phone,
            password_hash=password_hash,
            role='msme_owner',
            status='pending_verification'
        )
        self.db.add(user)
        self.db.flush()
        
        # Create MSME profile
        from app.models import MSMEProfile
        profile = MSMEProfile(
            user_id=user.id,
            business_name=business_data['business_name'],
            business_type=business_data['business_type'],
            industry_classification=business_data['industry'],
            udyam_number=business_data.get('udyam_number'),
            pan_number=business_data.get('pan'),
            gstin=business_data.get('gstin'),
            business_address=business_data['address'],
            years_in_business=business_data.get('years_in_business'),
            number_of_employees=business_data.get('employees'),
            annual_turnover=business_data.get('turnover'),
            bank_account_details=business_data.get('bank_details'),
            owner_details=business_data['owner_details']
        )
        self.db.add(profile)
        
        # Send verification email
        await self.send_verification_email(user)
        
        # Audit log
        self.audit_log(user.id, 'REGISTER', 'user', user.id)
        
        self.db.commit()
        
        return {
            'user_id': str(user.id),
            'email': user.email,
            'status': 'pending_verification'
        }
    
    async def login(
        self, 
        email: str, 
        password: str,
        ip_address: str,
        user_agent: str
    ) -> Dict:
        """Authenticate user and return tokens"""
        
        user = self.db.query(User).filter(User.email == email).first()
        
        if not user:
            raise ValueError("Invalid credentials")
        
        # Check if account is locked
        if user.locked_until and user.locked_until > datetime.utcnow():
            raise ValueError("Account is locked. Try again later.")
        
        # Verify password
        if not bcrypt.checkpw(password.encode(), user.password_hash.encode()):
            user.failed_login_attempts += 1
            if user.failed_login_attempts >= 5:
                user.locked_until = datetime.utcnow() + timedelta(minutes=30)
            self.db.commit()
            raise ValueError("Invalid credentials")
        
        # Check MFA if enabled
        if user.mfa_enabled:
            return {
                'requires_mfa': True,
                'temp_token': self.generate_temp_token(user.id)
            }
        
        # Generate tokens
        tokens = self.generate_tokens(user)
        
        # Create session
        session = UserSession(
            user_id=user.id,
            token_hash=self.hash_token(tokens['access_token']),
            refresh_token_hash=self.hash_token(tokens['refresh_token']),
            ip_address=ip_address,
            user_agent=user_agent,
            expires_at=datetime.utcnow() + timedelta(days=7)
        )
        self.db.add(session)
        
        # Update user
        user.last_login = datetime.utcnow()
        user.failed_login_attempts = 0
        
        # Audit log
        self.audit_log(user.id, 'LOGIN', 'user', user.id)
        
        self.db.commit()
        
        return {
            'access_token': tokens['access_token'],
            'refresh_token': tokens['refresh_token'],
            'token_type': 'bearer',
            'expires_in': settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
            'user': {
                'id': str(user.id),
                'email': user.email,
                'role': user.role
            }
        }
    
    async def verify_mfa(
        self, 
        temp_token: str, 
        mfa_code: str
    ) -> Dict:
        """Verify MFA code and complete login"""
        
        user_id = self.verify_temp_token(temp_token)
        user = self.db.query(User).filter(User.id == user_id).first()
        
        # Verify TOTP
        totp = pyotp.TOTP(user.mfa_secret)
        if not totp.verify(mfa_code, valid_window=1):
            raise ValueError("Invalid MFA code")
        
        # Generate tokens
        tokens = self.generate_tokens(user)
        
        # Create session
        session = UserSession(
            user_id=user.id,
            token_hash=self.hash_token(tokens['access_token']),
            refresh_token_hash=self.hash_token(tokens['refresh_token']),
            expires_at=datetime.utcnow() + timedelta(days=7)
        )
        self.db.add(session)
        
        user.last_login = datetime.utcnow()
        user.failed_login_attempts = 0
        
        self.db.commit()
        
        return {
            'access_token': tokens['access_token'],
            'refresh_token': tokens['refresh_token'],
            'token_type': 'bearer'
        }
    
    def generate_tokens(self, user: User) -> Dict:
        """Generate JWT access and refresh tokens"""
        
        access_payload = {
            'sub': str(user.id),
            'email': user.email,
            'role': user.role,
            'type': 'access',
            'exp': datetime.utcnow() + timedelta(
                minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES
            ),
            'iat': datetime.utcnow()
        }
        
        refresh_payload = {
            'sub': str(user.id),
            'type': 'refresh',
            'exp': datetime.utcnow() + timedelta(days=7),
            'iat': datetime.utcnow()
        }
        
        return {
            'access_token': jwt.encode(
                access_payload, 
                settings.SECRET_KEY, 
                algorithm=settings.ALGORITHM
            ),
            'refresh_token': jwt.encode(
                refresh_payload, 
                settings.SECRET_KEY, 
                algorithm=settings.ALGORITHM
            )
        }
    
    def verify_token(self, token: str) -> Dict:
        """Verify and decode JWT token"""
        
        try:
            payload = jwt.decode(
                token, 
                settings.SECRET_KEY, 
                algorithms=[settings.ALGORITHM]
            )
            
            # Check if session is revoked
            session = self.db.query(UserSession).filter(
                UserSession.token_hash == self.hash_token(token),
                UserSession.revoked_at.is_(None)
            ).first()
            
            if not session:
                raise ValueError("Session revoked")
            
            if session.expires_at < datetime.utcnow():
                raise ValueError("Session expired")
            
            return payload
            
        except jwt.ExpiredSignatureError:
            raise ValueError("Token expired")
        except jwt.InvalidTokenError:
            raise ValueError("Invalid token")
    
    async def refresh_tokens(self, refresh_token: str) -> Dict:
        """Refresh access token using refresh token"""
        
        payload = self.verify_token(refresh_token)
        
        if payload.get('type') != 'refresh':
            raise ValueError("Invalid token type")
        
        user = self.db.query(User).filter(
            User.id == payload['sub']
        ).first()
        
        if not user or user.status != 'active':
            raise ValueError("User not found or inactive")
        
        # Generate new tokens
        tokens = self.generate_tokens(user)
        
        # Update session
        session = self.db.query(UserSession).filter(
            UserSession.token_hash == self.hash_token(refresh_token)
        ).first()
        
        if session:
            session.token_hash = self.hash_token(tokens['access_token'])
            session.refresh_token_hash = self.hash_token(tokens['refresh_token'])
            session.last_active_at = datetime.utcnow()
        
        self.db.commit()
        
        return {
            'access_token': tokens['access_token'],
            'refresh_token': tokens['refresh_token'],
            'token_type': 'bearer'
        }
    
    async def logout(self, user_id: str, token: str):
        """Logout user and revoke session"""
        
        session = self.db.query(UserSession).filter(
            UserSession.user_id == user_id,
            UserSession.token_hash == self.hash_token(token)
        ).first()
        
        if session:
            session.revoked_at = datetime.utcnow()
            self.db.commit()
        
        self.audit_log(user_id, 'LOGOUT', 'user', user_id)
    
    async def setup_mfa(self, user_id: str) -> Dict:
        """Setup MFA for user"""
        
        user = self.db.query(User).filter(User.id == user_id).first()
        
        # Generate secret
        secret = pyotp.random_base32()
        user.mfa_secret = secret
        
        # Generate backup codes
        backup_codes = []
        for _ in range(10):
            code = secrets.token_hex(4)
            backup_codes.append(code)
        
        # Create TOTP URI
        totp = pyotp.TOTP(secret)
        provisioning_uri = totp.provisioning_uri(
            name=user.email,
            issuer_name="MSME Financial Intelligence"
        )
        
        self.db.commit()
        
        return {
            'secret': secret,
            'provisioning_uri': provisioning_uri,
            'backup_codes': backup_codes
        }
    
    async def enable_mfa(self, user_id: str, verification_code: str):
        """Enable MFA after verification"""
        
        user = self.db.query(User).filter(User.id == user_id).first()
        
        totp = pyotp.TOTP(user.mfa_secret)
        if not totp.verify(verification_code, valid_window=1):
            raise ValueError("Invalid verification code")
        
        user.mfa_enabled = True
        self.db.commit()
        
        return {'message': 'MFA enabled successfully'}
    
    async def request_password_reset(self, email: str):
        """Send password reset email"""
        
        user = self.db.query(User).filter(User.email == email).first()
        
        if user:
            # Generate reset token
            reset_token = secrets.token_urlsafe(32)
            user.password_reset_token = self.hash_token(reset_token)
            user.password_reset_expires = datetime.utcnow() + timedelta(hours=1)
            
            # Send email (simulated)
            await self.send_password_reset_email(user, reset_token)
            
            self.db.commit()
        
        # Always return success to prevent email enumeration
        return {'message': 'If email exists, reset link sent'}
    
    async def reset_password(
        self, 
        token: str, 
        new_password: str
    ):
        """Reset password using token"""
        
        user = self.db.query(User).filter(
            User.password_reset_token == self.hash_token(token),
            User.password_reset_expires > datetime.utcnow()
        ).first()
        
        if not user:
            raise ValueError("Invalid or expired reset token")
        
        # Update password
        user.password_hash = bcrypt.hashpw(
            new_password.encode(), 
            bcrypt.gensalt()
        ).decode()
        user.password_reset_token = None
        user.password_reset_expires = None
        user.password_changed_at = datetime.utcnow()
        
        # Revoke all sessions
        self.db.query(UserSession).filter(
            UserSession.user_id == user.id
        ).update({'revoked_at': datetime.utcnow()})
        
        self.db.commit()
        
        return {'message': 'Password reset successful'}
    
    def hash_token(self, token: str) -> str:
        """Hash token for storage"""
        import hashlib
        return hashlib.sha256(token.encode()).hexdigest()
    
    def generate_temp_token(self, user_id: str) -> str:
        """Generate temporary token for MFA"""
        payload = {
            'sub': str(user_id),
            'type': 'temp_mfa',
            'exp': datetime.utcnow() + timedelta(minutes=5)
        }
        return jwt.encode(
            payload, 
            settings.SECRET_KEY, 
            algorithm=settings.ALGORITHM
        )
    
    def verify_temp_token(self, token: str) -> str:
        """Verify temporary MFA token"""
        payload = jwt.decode(
            token, 
            settings.SECRET_KEY, 
            algorithms=[settings.ALGORITHM]
        )
        if payload.get('type') != 'temp_mfa':
            raise ValueError("Invalid token type")
        return payload['sub']
    
    def audit_log(
        self, 
        user_id: str, 
        action: str, 
        resource_type: str, 
        resource_id: str,
        old_value: dict = None,
        new_value: dict = None
    ):
        """Create audit log entry"""
        log = AuditLog(
            user_id=user_id,
            action=action,
            resource_type=resource_type,
            resource_id=str(resource_id),
            old_value=old_value,
            new_value=new_value
        )
        self.db.add(log)
```

### RBAC Middleware

```python
# app/core/rbac.py
from functools import wraps
from fastapi import HTTPException, status
from app.services.auth_service import AuthService

# Role permissions mapping
ROLE_PERMISSIONS = {
    'msme_owner': [
        'read_own_profile',
        'update_own_profile',
        'view_own_health_card',
        'view_own_recommendations',
        'apply_for_loan',
        'view_own_loan_status'
    ],
    'loan_officer': [
        'read_msme_profiles',
        'view_health_cards',
        'view_loan_applications',
        'update_loan_status',
        'view_reports',
        'add_notes'
    ],
    'credit_manager': [
        'read_msme_profiles',
        'view_health_cards',
        'view_all_loans',
        'approve_loans',
        'reject_loans',
        'view_portfolio',
        'view_analytics',
        'generate_reports'
    ],
    'risk_analyst': [
        'read_msme_profiles',
        'view_health_cards',
        'view_risk_reports',
        'view_fraud_alerts',
        'update_risk_scores',
        'view_analytics'
    ],
    'admin': [
        'manage_users',
        'manage_msme_profiles',
        'view_all_data',
        'manage_system',
        'view_audit_logs',
        'manage_models'
    ],
    'super_admin': [
        '*'
    ]
}

def require_permission(permission: str):
    """Decorator to check user permission"""
    def decorator(func):
        @wraps(func)
        async def wrapper(*args, **kwargs):
            # Get current user from request
            current_user = kwargs.get('current_user')
            
            if not current_user:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Not authenticated"
                )
            
            user_role = current_user.get('role')
            user_permissions = ROLE_PERMISSIONS.get(user_role, [])
            
            # Super admin has all permissions
            if '*' in user_permissions:
                return await func(*args, **kwargs)
            
            if permission not in user_permissions:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail="Insufficient permissions"
                )
            
            return await func(*args, **kwargs)
        return wrapper
    return decorator

def require_role(roles: list):
    """Decorator to check user role"""
    def decorator(func):
        @wraps(func)
        async def wrapper(*args, **kwargs):
            current_user = kwargs.get('current_user')
            
            if not current_user:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Not authenticated"
                )
            
            if current_user.get('role') not in roles:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail="Insufficient role"
                )
            
            return await func(*args, **kwargs)
        return wrapper
    return decorator
```

### Auth API Endpoints

```python
# app/api/v1/auth.py
from fastapi import APIRouter, Depends, HTTPException, Request
from app.core.deps import get_current_user, get_db
from app.services.auth_service import AuthService
from app.schemas.auth import (
    RegisterRequest, LoginRequest, MFARequest,
    PasswordResetRequest, PasswordResetConfirm
)

router = APIRouter(prefix="/auth", tags=["Authentication"])

@router.post("/register")
async def register(
    request: RegisterRequest,
    db = Depends(get_db)
):
    service = AuthService(db)
    result = await service.register_msme(
        email=request.email,
        password=request.password,
        phone=request.phone,
        business_data=request.business_data
    )
    return result

@router.post("/login")
async def login(
    request: LoginRequest,
    request_obj: Request,
    db = Depends(get_db)
):
    service = AuthService(db)
    result = await service.login(
        email=request.email,
        password=request.password,
        ip_address=request_obj.client.host,
        user_agent=request_obj.headers.get("user-agent", "")
    )
    return result

@router.post("/mfa/verify")
async def verify_mfa(
    request: MFARequest,
    db = Depends(get_db)
):
    service = AuthService(db)
    result = await service.verify_mfa(
        temp_token=request.temp_token,
        mfa_code=request.mfa_code
    )
    return result

@router.post("/mfa/setup")
async def setup_mfa(
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = AuthService(db)
    result = await service.setup_mfa(current_user['sub'])
    return result

@router.post("/mfa/enable")
async def enable_mfa(
    verification_code: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = AuthService(db)
    result = await service.enable_mfa(
        current_user['sub'], 
        verification_code
    )
    return result

@router.post("/refresh")
async def refresh_token(
    refresh_token: str,
    db = Depends(get_db)
):
    service = AuthService(db)
    result = await service.refresh_tokens(refresh_token)
    return result

@router.post("/logout")
async def logout(
    token: str = Depends(get_current_user),
    db = Depends(get_db)
):
    service = AuthService(db)
    await service.logout(token['sub'], token['token'])
    return {"message": "Logged out successfully"}

@router.post("/password-reset/request")
async def request_password_reset(
    request: PasswordResetRequest,
    db = Depends(get_db)
):
    service = AuthService(db)
    result = await service.request_password_reset(request.email)
    return result

@router.post("/password-reset/confirm")
async def reset_password(
    request: PasswordResetConfirm,
    db = Depends(get_db)
):
    service = AuthService(db)
    result = await service.reset_password(
        request.token,
        request.new_password
    )
    return result
```

---

## 1.3 KYC VERIFICATION SERVICE

```python
# app/services/kyc_service.py
from typing import Dict, Optional
from datetime import datetime, timedelta
import httpx
from sqlalchemy.orm import Session
from app.models import MSMEProfile, KYCVerification

class KYCVerificationService:
    def __init__(self, db: Session):
        self.db = db
    
    async def verify_pan(self, pan_number: str, name: str) -> Dict:
        """Simulate PAN verification"""
        
        # In production, integrate with NSDL/UTIITSL API
        # Simulated verification
        
        verification_result = {
            'status': 'verified',
            'pan_number': pan_number,
            'name_on_pan': name,
            'category': 'Company',  # or Individual
            'valid_from': '2015-04-01',
            'valid_until': '2025-03-31',
            'verification_id': f"PAN{pan_number}",
            'verified_at': datetime.utcnow().isoformat()
        }
        
        return verification_result
    
    async def verify_gstin(self, gstin: str) -> Dict:
        """Simulate GSTIN verification"""
        
        # In production, integrate with GST Portal API
        # Simulated verification
        
        verification_result = {
            'status': 'verified',
            'gstin': gstin,
            'legal_name': 'Sample Business',
            'trade_name': 'Sample Business',
            'registration_date': '2018-07-01',
            'status': 'Active',
            'business_type': 'Private Limited',
            'state': 'Maharashtra',
            'constitution': 'Private Limited Company',
            'verification_id': f"GST{gstin}",
            'verified_at': datetime.utcnow().isoformat()
        }
        
        return verification_result
    
    async def verify_udyam(self, udyam_number: str) -> Dict:
        """Simulate UDYAM verification"""
        
        # In production, integrate with UDYAM Portal API
        verification_result = {
            'status': 'verified',
            'udyam_number': udyam_number,
            'enterprise_name': 'Sample Enterprise',
            'date_of_registration': '2020-01-15',
            'enterprise_type': 'Micro',
            'activity': 'Manufacturing',
            'verification_id': f"UDYAM{udyam_number}",
            'verified_at': datetime.utcnow().isoformat()
        }
        
        return verification_result
    
    async def verify_bank_account(
        self, 
        account_number: str, 
        ifsc: str,
        account_holder_name: str
    ) -> Dict:
        """Simulate bank account verification"""
        
        # In production, use penny drop verification
        verification_result = {
            'status': 'verified',
            'account_number': account_number[-4:],  # Masked
            'ifsc': ifsc,
            'account_holder_name': account_holder_name,
            'bank_name': 'Sample Bank',
            'branch': 'Sample Branch',
            'account_type': 'Current',
            'verification_id': f"BANK{account_number[-4:]}",
            'verified_at': datetime.utcnow().isoformat()
        }
        
        return verification_result
    
    async def verify_aadhaar(self, aadhaar_number: str, name: str) -> Dict:
        """Simulate Aadhaar verification"""
        
        # In production, integrate with UIDAI API
        verification_result = {
            'status': 'verified',
            'aadhaar_last_4': aadhaar_number[-4:],
            'name': name,
            'age_band': '30-40',
            'gender': 'Male',
            'state': 'Maharashtra',
            'verification_id": f"AADHAR{aadhaar_number[-4:]}",
            'verified_at': datetime.utcnow().isoformat()
        }
        
        return verification_result
    
    async def complete_kyc(
        self, 
        msme_id: str, 
        documents: Dict
    ) -> Dict:
        """Complete full KYC verification"""
        
        results = {}
        
        # Verify PAN
        if 'pan' in documents:
            results['pan'] = await self.verify_pan(
                documents['pan']['number'],
                documents['pan']['name']
            )
        
        # Verify GSTIN
        if 'gstin' in documents:
            results['gstin'] = await self.verify_gstin(
                documents['gstin']
            )
        
        # Verify UDYAM
        if 'udyam' in documents:
            results['udyam'] = await self.verify_udyam(
                documents['udyam']
            )
        
        # Verify Bank Account
        if 'bank_account' in documents:
            results['bank_account'] = await self.verify_bank_account(
                documents['bank_account']['number'],
                documents['bank_account']['ifsc'],
                documents['bank_account']['name']
            )
        
        # Store verification records
        for doc_type, result in results.items():
            verification = KYCVerification(
                msme_id=msme_id,
                document_type=doc_type,
                document_number=documents.get(doc_type, {}).get('number', ''),
                verification_status=result['status'],
                verification_source='simulated',
                verification_data=result,
                verified_at=datetime.utcnow()
            )
            self.db.add(verification)
        
        # Update MSME profile verification status
        profile = self.db.query(MSMEProfile).filter(
            MSMEProfile.id == msme_id
        ).first()
        
        if profile:
            all_verified = all(
                r['status'] == 'verified' for r in results.values()
            )
            profile.verification_status = 'completed' if all_verified else 'partial'
        
        self.db.commit()
        
        return {
            'verification_results': results,
            'overall_status': 'completed' if all(
                r['status'] == 'verified' for r in results.values()
            ) else 'partial'
        }
```

---

## 1.4 SESSION MANAGEMENT

```python
# app/services/session_service.py
from datetime import datetime, timedelta
from typing import Optional, List
from sqlalchemy.orm import Session
from app.models import Session as UserSession, User

class SessionService:
    def __init__(self, db: Session):
        self.db = db
    
    async def get_active_sessions(self, user_id: str) -> List[dict]:
        """Get all active sessions for a user"""
        
        sessions = self.db.query(UserSession).filter(
            UserSession.user_id == user_id,
            UserSession.revoked_at.is_(None),
            UserSession.expires_at > datetime.utcnow()
        ).all()
        
        return [
            {
                'id': str(s.id),
                'ip_address': s.ip_address,
                'user_agent': s.user_agent,
                'device_info': s.device_info,
                'created_at': s.created_at.isoformat(),
                'last_active_at': s.last_active_at.isoformat()
            }
            for s in sessions
        ]
    
    async def revoke_session(
        self, 
        user_id: str, 
        session_id: str
    ) -> bool:
        """Revoke a specific session"""
        
        session = self.db.query(UserSession).filter(
            UserSession.id == session_id,
            UserSession.user_id == user_id
        ).first()
        
        if session:
            session.revoked_at = datetime.utcnow()
            self.db.commit()
            return True
        
        return False
    
    async def revoke_all_sessions(self, user_id: str):
        """Revoke all sessions for a user"""
        
        self.db.query(UserSession).filter(
            UserSession.user_id == user_id,
            UserSession.revoked_at.is_(None)
        ).update({'revoked_at': datetime.utcnow()})
        
        self.db.commit()
    
    async def cleanup_expired_sessions(self):
        """Clean up expired sessions"""
        
        self.db.query(UserSession).filter(
            UserSession.expires_at < datetime.utcnow()
        ).delete()
        
        self.db.commit()
    
    async def update_last_active(self, session_id: str):
        """Update last active timestamp"""
        
        session = self.db.query(UserSession).filter(
            UserSession.id == session_id
        ).first()
        
        if session:
            session.last_active_at = datetime.utcnow()
            self.db.commit()
```

---

## 1.5 FRONTEND COMPONENTS

### Login Page

```tsx
// src/pages/auth/Login.tsx
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [mfaCode, setMfaCode] = useState('');
  const [requiresMfa, setRequiresMfa] = useState(false);
  const [tempToken, setTempToken] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { login, verifyMfa } = useAuth();
  const navigate = useNavigate();
  
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    try {
      const result = await login(email, password);
      
      if (result.requires_mfa) {
        setRequiresMfa(true);
        setTempToken(result.temp_token);
      } else {
        navigate('/dashboard');
      }
    } catch (err: any) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };
  
  const handleMfaVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    try {
      await verifyMfa(tempToken, mfaCode);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'MFA verification failed');
    } finally {
      setLoading(false);
    }
  };
  
  if (requiresMfa) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Multi-Factor Authentication</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleMfaVerify} className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2">
                  Enter 6-digit code from your authenticator app
                </label>
                <Input
                  type="text"
                  value={mfaCode}
                  onChange={(e) => setMfaCode(e.target.value)}
                  placeholder="000000"
                  maxLength={6}
                  required
                />
              </div>
              
              {error && (
                <Alert variant="destructive">
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}
              
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? 'Verifying...' : 'Verify'}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    );
  }
  
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>MSME Financial Intelligence Platform</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-2">Email</label>
              <Input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium mb-2">Password</label>
              <Input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
              />
            </div>
            
            <div className="flex justify-between items-center">
              <Link 
                to="/forgot-password" 
                className="text-sm text-blue-600 hover:underline"
              >
                Forgot password?
              </Link>
              <Link 
                to="/register" 
                className="text-sm text-blue-600 hover:underline"
              >
                Register as MSME
              </Link>
            </div>
            
            {error && (
              <Alert variant="destructive">
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}
            
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign In'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};
```

### Registration Page

```tsx
// src/pages/auth/Register.tsx
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

export const RegisterPage: React.FC = () => {
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    businessName: '',
    businessType: '',
    industry: '',
    udyamNumber: '',
    pan: '',
    gstin: '',
    address: {
      line1: '',
      line2: '',
      city: '',
      state: '',
      pincode: ''
    },
    yearsInBusiness: '',
    employees: '',
    turnover: '',
    ownerDetails: {
      name: '',
      pan: '',
      aadhaar: '',
      phone: '',
      email: ''
    }
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { register } = useAuth();
  const navigate = useNavigate();
  
  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };
  
  const handleAddressChange = (field: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      address: { ...prev.address, [field]: value }
    }));
  };
  
  const handleOwnerChange = (field: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      ownerDetails: { ...prev.ownerDetails, [field]: value }
    }));
  };
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    
    setLoading(true);
    setError('');
    
    try {
      await register({
        email: formData.email,
        phone: formData.phone,
        password: formData.password,
        business_data: {
          business_name: formData.businessName,
          business_type: formData.businessType,
          industry: formData.industry,
          udyam_number: formData.udyamNumber,
          pan: formData.pan,
          gstin: formData.gstin,
          address: formData.address,
          years_in_business: parseInt(formData.yearsInBusiness),
          employees: parseInt(formData.employees),
          turnover: parseFloat(formData.turnover),
          owner_details: formData.ownerDetails
        }
      });
      
      navigate('/login', { 
        state: { message: 'Registration successful. Please verify your email.' }
      });
    } catch (err: any) {
      setError(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };
  
  const renderStep1 = () => (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold">Account Details</h3>
      
      <div>
        <label className="block text-sm font-medium mb-2">Email</label>
        <Input
          type="email"
          value={formData.email}
          onChange={(e) => handleInputChange('email', e.target.value)}
          required
        />
      </div>
      
      <div>
        <label className="block text-sm font-medium mb-2">Phone</label>
        <Input
          type="tel"
          value={formData.phone}
          onChange={(e) => handleInputChange('phone', e.target.value)}
          required
        />
      </div>
      
      <div>
        <label className="block text-sm font-medium mb-2">Password</label>
        <Input
          type="password"
          value={formData.password}
          onChange={(e) => handleInputChange('password', e.target.value)}
          required
        />
      </div>
      
      <div>
        <label className="block text-sm font-medium mb-2">Confirm Password</label>
        <Input
          type="password"
          value={formData.confirmPassword}
          onChange={(e) => handleInputChange('confirmPassword', e.target.value)}
          required
        />
      </div>
    </div>
  );
  
  const renderStep2 = () => (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold">Business Details</h3>
      
      <div>
        <label className="block text-sm font-medium mb-2">Business Name</label>
        <Input
          value={formData.businessName}
          onChange={(e) => handleInputChange('businessName', e.target.value)}
          required
        />
      </div>
      
      <div>
        <label className="block text-sm font-medium mb-2">Business Type</label>
        <Select
          value={formData.businessType}
          onValueChange={(value) => handleInputChange('businessType', value)}
        >
          <SelectTrigger>
            <SelectValue placeholder="Select business type" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="sole_proprietorship">Sole Proprietorship</SelectItem>
            <SelectItem value="partnership">Partnership</SelectItem>
            <SelectItem value="llp">LLP</SelectItem>
            <SelectItem value="private_limited">Private Limited</SelectItem>
            <SelectItem value="public_limited">Public Limited</SelectItem>
          </SelectContent>
        </Select>
      </div>
      
      <div>
        <label className="block text-sm font-medium mb-2">Industry</label>
        <Select
          value={formData.industry}
          onValueChange={(value) => handleInputChange('industry', value)}
        >
          <SelectTrigger>
            <SelectValue placeholder="Select industry" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="manufacturing">Manufacturing</SelectItem>
            <SelectItem value="trading">Trading</SelectItem>
            <SelectItem value="services">Services</SelectItem>
            <SelectItem value="textiles">Textiles</SelectItem>
            <SelectItem value="food_processing">Food Processing</SelectItem>
            <SelectItem value="electronics">Electronics</SelectItem>
            <SelectItem value="pharmaceuticals">Pharmaceuticals</SelectItem>
          </SelectContent>
        </Select>
      </div>
      
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium mb-2">PAN Number</label>
          <Input
            value={formData.pan}
            onChange={(e) => handleInputChange('pan', e.target.value.toUpperCase())}
            maxLength={10}
            required
          />
        </div>
        
        <div>
          <label className="block text-sm font-medium mb-2">GSTIN</label>
          <Input
            value={formData.gstin}
            onChange={(e) => handleInputChange('gstin', e.target.value.toUpperCase())}
            maxLength={15}
          />
        </div>
      </div>
      
      <div>
        <label className="block text-sm font-medium mb-2">UDYAM Number</label>
        <Input
          value={formData.udyamNumber}
          onChange={(e) => handleInputChange('udyamNumber', e.target.value)}
        />
      </div>
    </div>
  );
  
  const renderStep3 = () => (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold">Address & Details</h3>
      
      <div>
        <label className="block text-sm font-medium mb-2">Address Line 1</label>
        <Input
          value={formData.address.line1}
          onChange={(e) => handleAddressChange('line1', e.target.value)}
          required
        />
      </div>
      
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium mb-2">City</label>
          <Input
            value={formData.address.city}
            onChange={(e) => handleAddressChange('city', e.target.value)}
            required
          />
        </div>
        
        <div>
          <label className="block text-sm font-medium mb-2">State</label>
          <Input
            value={formData.address.state}
            onChange={(e) => handleAddressChange('state', e.target.value)}
            required
          />
        </div>
      </div>
      
      <div className="grid grid-cols-3 gap-4">
        <div>
          <label className="block text-sm font-medium mb-2">Years in Business</label>
          <Input
            type="number"
            value={formData.yearsInBusiness}
            onChange={(e) => handleInputChange('yearsInBusiness', e.target.value)}
            required
          />
        </div>
        
        <div>
          <label className="block text-sm font-medium mb-2">Employees</label>
          <Input
            type="number"
            value={formData.employees}
            onChange={(e) => handleInputChange('employees', e.target.value)}
            required
          />
        </div>
        
        <div>
          <label className="block text-sm font-medium mb-2">Annual Turnover (₹)</label>
          <Input
            type="number"
            value={formData.turnover}
            onChange={(e) => handleInputChange('turnover', e.target.value)}
            required
          />
        </div>
      </div>
    </div>
  );
  
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12">
      <Card className="w-full max-w-2xl">
        <CardHeader>
          <CardTitle>Register Your MSME</CardTitle>
          <div className="flex justify-center space-x-4 mt-4">
            {[1, 2, 3].map((s) => (
              <div
                key={s}
                className={`w-8 h-8 rounded-full flex items-center justify-center ${
                  step >= s ? 'bg-blue-600 text-white' : 'bg-gray-200'
                }`}
              >
                {s}
              </div>
            ))}
          </div>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {step === 1 && renderStep1()}
            {step === 2 && renderStep2()}
            {step === 3 && renderStep3()}
            
            {error && (
              <Alert variant="destructive">
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}
            
            <div className="flex justify-between">
              {step > 1 && (
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setStep(step - 1)}
                >
                  Previous
                </Button>
              )}
              
              {step < 3 ? (
                <Button
                  type="button"
                  onClick={() => setStep(step + 1)}
                  className="ml-auto"
                >
                  Next
                </Button>
              ) : (
                <Button type="submit" className="ml-auto" disabled={loading}>
                  {loading ? 'Registering...' : 'Register'}
                </Button>
              )}
            </div>
          </form>
          
          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="text-blue-600 hover:underline">
                Sign in
              </Link>
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
```

---

## 1.6 UI DESIGN

```
┌─────────────────────────────────────────────────────────────────┐
│                    MSME FINANCIAL INTELLIGENCE                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                                                          │    │
│  │              [LOGO]                                      │    │
│  │                                                          │    │
│  │         MSME Financial Intelligence                     │
│  │              Platform                                    │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  Email                                           │    │    │
│  │  │  ┌─────────────────────────────────────────┐    │    │    │
│  │  │  │ you@example.com                         │    │    │    │
│  │  │  └─────────────────────────────────────────┘    │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  Password                                        │    │    │
│  │  │  ┌─────────────────────────────────────────┐    │    │    │
│  │  │  │ ••••••••                              👁 │    │    │    │
│  │  │  └─────────────────────────────────────────┘    │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │                                                   │    │    │
│  │  │              SIGN IN                              │    │    │
│  │  │                                                   │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  ┌─────────┐  ┌─────────┐  ┌─────────┐        │    │    │
│  │  │  │  MSME   │  │  Bank   │  │  Admin  │        │    │    │
│  │  │  │ Portal  │  │ Portal  │  │ Portal  │        │    │    │
│  │  │  └─────────┘  └─────────┘  └─────────┘        │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  Forgot Password? | Register as MSME            │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1.7 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| Database Schema | 2 days |
| Auth Service | 3 days |
| KYC Service | 2 days |
| Session Management | 1 day |
| RBAC System | 2 days |
| API Endpoints | 2 days |
| Frontend (Login/Register) | 3 days |
| Frontend (Profile Management) | 3 days |
| Testing | 2 days |
| **Total** | **20 days** |

---

## 1.8 HACKATHON PRIORITY

**CRITICAL** - This is the foundation for all other features
