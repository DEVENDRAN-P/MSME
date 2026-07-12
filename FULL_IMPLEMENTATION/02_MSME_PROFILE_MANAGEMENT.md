# MODULE 2: MSME PROFILE MANAGEMENT
## Complete Implementation Guide

---

## 2.1 DATABASE SCHEMA

```sql
-- Enhanced MSME Profiles
CREATE TABLE msme_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Business Registration
    business_name VARCHAR(255) NOT NULL,
    business_type VARCHAR(50) NOT NULL,
    industry_classification VARCHAR(100) NOT NULL,
    industry_subcategory VARCHAR(100),
    udyam_number VARCHAR(20),
    pan_number VARCHAR(10),
    gstin VARCHAR(15),
    cin VARCHAR(21),
    
    -- Business Address
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    country VARCHAR(50) DEFAULT 'India',
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    
    -- Business Details
    years_in_business INTEGER,
    date_of_establishment DATE,
    number_of_employees INTEGER,
    annual_turnover DECIMAL(15,2),
    business_description TEXT,
    
    -- Bank Account Details
    bank_account_number VARCHAR(20),
    bank_ifsc VARCHAR(11),
    bank_name VARCHAR(100),
    bank_branch VARCHAR(100),
    account_type VARCHAR(20),
    
    -- Existing Loan Details
    existing_loans JSONB DEFAULT '[]',
    
    -- Owner Details
    owner_name VARCHAR(255) NOT NULL,
    owner_pan VARCHAR(10),
    owner_aadhaar VARCHAR(12),
    owner_phone VARCHAR(20),
    owner_email VARCHAR(255),
    owner_dob DATE,
    owner_address JSONB,
    
    -- Verification Status
    verification_status VARCHAR(20) DEFAULT 'pending',
    pan_verified BOOLEAN DEFAULT FALSE,
    gstin_verified BOOLEAN DEFAULT FALSE,
    udyam_verified BOOLEAN DEFAULT FALSE,
    bank_verified BOOLEAN DEFAULT FALSE,
    aadhaar_verified BOOLEAN DEFAULT FALSE,
    
    -- Profile Completion
    profile_completion_score INTEGER DEFAULT 0,
    profile_completed_at TIMESTAMPTZ,
    
    -- Metadata
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    last_data_sync TIMESTAMPTZ
);

-- Business Documents
CREATE TABLE business_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size INTEGER,
    mime_type VARCHAR(100),
    upload_status VARCHAR(20) DEFAULT 'pending',
    verification_status VARCHAR(20) = 'pending',
    metadata JSONB,
    uploaded_at TIMESTAMPTZ DEFAULT NOW(),
    verified_at TIMESTAMPTZ
);

-- Business Contacts
CREATE TABLE business_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id) ON DELETE CASCADE,
    contact_type VARCHAR(50) NOT NULL,
    name VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255),
    designation VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Business Certifications
CREATE TABLE business_certifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id) ON DELETE CASCADE,
    certification_type VARCHAR(100) NOT NULL,
    certification_number VARCHAR(100),
    issued_by VARCHAR(255),
    issued_date DATE,
    expiry_date DATE,
    status VARCHAR(20) DEFAULT 'active',
    document_id UUID REFERENCES business_documents(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Business Financials (Historical)
CREATE TABLE business_financials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id) ON DELETE CASCADE,
    financial_year VARCHAR(10) NOT NULL,
    quarter VARCHAR(5),
    revenue DECIMAL(15,2),
    expenses DECIMAL(15,2),
    profit_loss DECIMAL(15,2),
    assets DECIMAL(15,2),
    liabilities DECIMAL(15,2),
    equity DECIMAL(15,2),
    cash_flow DECIMAL(15,2),
    data_source VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Business Compliance
CREATE TABLE business_compliance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id) ON DELETE CASCADE,
    compliance_type VARCHAR(100) NOT NULL,
    compliance_status VARCHAR(20) NOT NULL,
    last_filed_date DATE,
    next_due_date DATE,
    filing_frequency VARCHAR(20),
    compliance_score DECIMAL(5,2),
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Profile Audit Trail
CREATE TABLE profile_audit_trail (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by UUID REFERENCES users(id),
    change_reason TEXT,
    changed_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

## 2.2 BACKEND SERVICE

```python
# app/services/profile_service.py
from typing import Dict, List, Optional
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import and_
from app.models import MSMEProfile, BusinessDocument, ProfileAuditTrail

class MSMEProfileService:
    def __init__(self, db: Session):
        self.db = db
    
    async def get_profile(self, msme_id: str) -> Dict:
        """Get complete MSME profile"""
        
        profile = self.db.query(MSMEProfile).filter(
            MSMEProfile.id == msme_id
        ).first()
        
        if not profile:
            raise ValueError("Profile not found")
        
        return self.format_profile(profile)
    
    async def update_profile(
        self, 
        msme_id: str, 
        update_data: Dict,
        changed_by: str
    ) -> Dict:
        """Update MSME profile with audit trail"""
        
        profile = self.db.query(MSMEProfile).filter(
            MSMEProfile.id == msme_id
        ).first()
        
        if not profile:
            raise ValueError("Profile not found")
        
        # Track changes for audit
        changes = []
        for field, new_value in update_data.items():
            old_value = getattr(profile, field, None)
            if old_value != new_value:
                changes.append({
                    'field': field,
                    'old_value': str(old_value),
                    'new_value': str(new_value)
                })
                setattr(profile, field, new_value)
        
        # Create audit trail entries
        for change in changes:
            audit = ProfileAuditTrail(
                msme_id=msme_id,
                field_name=change['field'],
                old_value=change['old_value'],
                new_value=change['new_value'],
                changed_by=changed_by
            )
            self.db.add(audit)
        
        # Update profile completion score
        profile.profile_completion_score = self.calculate_completion_score(profile)
        
        # Update timestamp
        profile.updated_at = datetime.utcnow()
        
        self.db.commit()
        
        return self.format_profile(profile)
    
    async def upload_document(
        self,
        msme_id: str,
        document_type: str,
        file_name: str,
        file_content: bytes,
        mime_type: str
    ) -> Dict:
        """Upload business document"""
        
        # Generate file path
        file_path = f"documents/{msme_id}/{document_type}/{file_name}"
        
        # Store file (simulated - in production use S3/MinIO)
        # await self.storage.upload(file_path, file_content)
        
        # Create document record
        document = BusinessDocument(
            msme_id=msme_id,
            document_type=document_type,
            document_name=file_name,
            file_path=file_path,
            file_size=len(file_content),
            mime_type=mime_type,
            upload_status='completed'
        )
        
        self.db.add(document)
        self.db.commit()
        
        return {
            'document_id': str(document.id),
            'status': 'uploaded',
            'document_type': document_type
        }
    
    async def get_documents(self, msme_id: str) -> List[Dict]:
        """Get all documents for MSME"""
        
        documents = self.db.query(BusinessDocument).filter(
            BusinessDocument.msme_id == msme_id
        ).all()
        
        return [
            {
                'id': str(doc.id),
                'document_type': doc.document_type,
                'document_name': doc.document_name,
                'upload_status': doc.upload_status,
                'verification_status': doc.verification_status,
                'uploaded_at': doc.uploaded_at.isoformat()
            }
            for doc in documents
        ]
    
    async def get_compliance_status(self, msme_id: str) -> Dict:
        """Get compliance status for MSME"""
        
        from app.models import BusinessCompliance
        
        compliance_records = self.db.query(BusinessCompliance).filter(
            BusinessCompliance.msme_id == msme_id
        ).all()
        
        compliance_status = {
            'gst': {'status': 'pending', 'score': 0},
            'epf': {'status': 'pending', 'score': 0},
            'esi': {'status': 'pending', 'score': 0},
            'income_tax': {'status': 'pending', 'score': 0},
            'mca': {'status': 'pending', 'score': 0}
        }
        
        for record in compliance_records:
            if record.compliance_type in compliance_status:
                compliance_status[record.compliance_type] = {
                    'status': record.compliance_status,
                    'score': float(record.compliance_score or 0),
                    'last_filed': record.last_filed_date.isoformat() if record.last_filed_date else None,
                    'next_due': record.next_due_date.isoformat() if record.next_due_date else None
                }
        
        # Calculate overall compliance score
        scores = [v['score'] for v in compliance_status.values()]
        overall_score = sum(scores) / len(scores) if scores else 0
        
        return {
            'overall_score': round(overall_score, 2),
            'breakdown': compliance_status,
            'last_updated': datetime.utcnow().isoformat()
        }
    
    async def get_financial_history(
        self, 
        msme_id: str,
        years: int = 3
    ) -> List[Dict]:
        """Get financial history for MSME"""
        
        from app.models import BusinessFinancials
        
        financials = self.db.query(BusinessFinancials).filter(
            BusinessFinancials.msme_id == msme_id
        ).order_by(
            BusinessFinancials.financial_year.desc()
        ).limit(years * 4).all()  # Assuming quarterly data
        
        return [
            {
                'financial_year': f.financial_year,
                'quarter': f.quarter,
                'revenue': float(f.revenue) if f.revenue else 0,
                'expenses': float(f.expenses) if f.expenses else 0,
                'profit_loss': float(f.profit_loss) if f.profit_loss else 0,
                'assets': float(f.assets) if f.assets else 0,
                'liabilities': float(f.liabilities) if f.liabilities else 0,
                'cash_flow': float(f.cash_flow) if f.cash_flow else 0
            }
            for f in financials
        ]
    
    async def get_profile_completion_status(self, msme_id: str) -> Dict:
        """Get detailed profile completion status"""
        
        profile = self.db.query(MSMEProfile).filter(
            MSMEProfile.id == msme_id
        ).first()
        
        if not profile:
            raise ValueError("Profile not found")
        
        required_fields = {
            'business_name': bool(profile.business_name),
            'business_type': bool(profile.business_type),
            'industry_classification': bool(profile.industry_classification),
            'pan_number': bool(profile.pan_number),
            'gstin': bool(profile.gstin),
            'address_line1': bool(profile.address_line1),
            'city': bool(profile.city),
            'state': bool(profile.state),
            'pincode': bool(profile.pincode),
            'years_in_business': bool(profile.years_in_business),
            'number_of_employees': bool(profile.number_of_employees),
            'annual_turnover': bool(profile.annual_turnover),
            'bank_account_number': bool(profile.bank_account_number),
            'bank_ifsc': bool(profile.bank_ifsc),
            'owner_name': bool(profile.owner_name),
            'owner_pan': bool(profile.owner_pan)
        }
        
        optional_fields = {
            'udyam_number': bool(profile.udyam_number),
            'cin': bool(profile.cin),
            'business_description': bool(profile.business_description),
            'owner_aadhaar': bool(profile.owner_aadhaar)
        }
        
        verified_fields = {
            'pan_verified': profile.pan_verified,
            'gstin_verified': profile.gstin_verified,
            'udyam_verified': profile.udyam_verified,
            'bank_verified': profile.bank_verified,
            'aadhaar_verified': profile.aadhaar_verified
        }
        
        # Calculate completion
        required_completed = sum(1 for v in required_fields.values() if v)
        optional_completed = sum(1 for v in optional_fields.values() if v)
        verified_count = sum(1 for v in verified_fields.values() if v)
        
        total_score = (
            (required_completed / len(required_fields)) * 60 +
            (optional_completed / len(optional_fields)) * 20 +
            (verified_count / len(verified_fields)) * 20
        )
        
        return {
            'completion_score': round(total_score),
            'required_fields': required_fields,
            'optional_fields': optional_fields,
            'verification_status': verified_fields,
            'missing_required': [k for k, v in required_fields.items() if not v],
            'missing_optional': [k for k, v in optional_fields.items() if not v],
            'pending_verification': [k for k, v in verified_fields.items() if not v]
        }
    
    def calculate_completion_score(self, profile: MSMEProfile) -> int:
        """Calculate profile completion score"""
        
        fields = [
            bool(profile.business_name),
            bool(profile.business_type),
            bool(profile.industry_classification),
            bool(profile.pan_number),
            bool(profile.gstin),
            bool(profile.address_line1),
            bool(profile.city),
            bool(profile.state),
            bool(profile.pincode),
            bool(profile.years_in_business),
            bool(profile.number_of_employees),
            bool(profile.annual_turnover),
            bool(profile.bank_account_number),
            bool(profile.bank_ifsc),
            bool(profile.owner_name),
            bool(profile.owner_pan)
        ]
        
        return round((sum(fields) / len(fields)) * 100)
    
    def format_profile(self, profile: MSMEProfile) -> Dict:
        """Format profile for API response"""
        
        return {
            'id': str(profile.id),
            'business': {
                'name': profile.business_name,
                'type': profile.business_type,
                'industry': profile.industry_classification,
                'subcategory': profile.industry_subcategory,
                'udyam_number': profile.udyam_number,
                'pan': profile.pan_number,
                'gstin': profile.gstin,
                'cin': profile.cin,
                'description': profile.business_description,
                'establishment_date': profile.date_of_establishment.isoformat() if profile.date_of_establishment else None,
                'years_in_business': profile.years_in_business,
                'employees': profile.number_of_employees,
                'annual_turnover': float(profile.annual_turnover) if profile.annual_turnover else None
            },
            'address': {
                'line1': profile.address_line1,
                'line2': profile.address_line2,
                'city': profile.city,
                'state': profile.state,
                'pincode': profile.pincode,
                'country': profile.country,
                'coordinates': {
                    'latitude': float(profile.latitude) if profile.latitude else None,
                    'longitude': float(profile.longitude) if profile.longitude else None
                }
            },
            'bank_details': {
                'account_number': f"****{profile.bank_account_number[-4:]}" if profile.bank_account_number else None,
                'ifsc': profile.bank_ifsc,
                'bank_name': profile.bank_name,
                'branch': profile.bank_branch,
                'account_type': profile.account_type
            },
            'owner': {
                'name': profile.owner_name,
                'phone': profile.owner_phone,
                'email': profile.owner_email
            },
            'verification': {
                'status': profile.verification_status,
                'pan': profile.pan_verified,
                'gstin': profile.gstin_verified,
                'udyam': profile.udyam_verified,
                'bank': profile.bank_verified,
                'aadhaar': profile.aadhaar_verified
            },
            'completion_score': profile.profile_completion_score,
            'last_updated': profile.updated_at.isoformat() if profile.updated_at else None
        }
```

---

## 2.3 API ENDPOINTS

```python
# app/api/v1/profile.py
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from app.core.deps import get_current_user, get_db
from app.services.profile_service import MSMEProfileService
from app.schemas.profile import (
    ProfileUpdateRequest, DocumentUploadResponse
)

router = APIRouter(prefix="/profile", tags=["MSME Profile"])

@router.get("/msme/{msme_id}")
async def get_msme_profile(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = MSMEProfileService(db)
    return await service.get_profile(msme_id)

@router.put("/msme/{msme_id}")
async def update_msme_profile(
    msme_id: str,
    request: ProfileUpdateRequest,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = MSMEProfileService(db)
    return await service.update_profile(
        msme_id,
        request.dict(exclude_unset=True),
        current_user['sub']
    )

@router.get("/msme/{msme_id}/completion")
async def get_completion_status(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = MSMEProfileService(db)
    return await service.get_profile_completion_status(msme_id)

@router.post("/msme/{msme_id}/documents")
async def upload_document(
    msme_id: str,
    document_type: str,
    file: UploadFile = File(...),
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = MSMEProfileService(db)
    content = await file.read()
    return await service.upload_document(
        msme_id,
        document_type,
        file.filename,
        content,
        file.content_type
    )

@router.get("/msme/{msme_id}/documents")
async def get_documents(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = MSMEProfileService(db)
    return await service.get_documents(msme_id)

@router.get("/msme/{msme_id}/compliance")
async def get_compliance_status(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = MSMEProfileService(db)
    return await service.get_compliance_status(msme_id)

@router.get("/msme/{msme_id}/financials")
async def get_financial_history(
    msme_id: str,
    years: int = 3,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = MSMEProfileService(db)
    return await service.get_financial_history(msme_id, years)
```

---

## 2.4 FRONTEND COMPONENTS

### Profile Dashboard

```tsx
// src/pages/profile/ProfileDashboard.tsx
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useProfile } from '@/hooks/useProfile';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { 
  Building2, MapPin, CreditCard, User, 
  FileCheck, TrendingUp, AlertCircle 
} from 'lucide-react';

export const ProfileDashboard: React.FC = () => {
  const { msmeId } = useParams();
  const { profile, completion, loading, error } = useProfile(msmeId);
  
  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  
  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">{profile.business.name}</h1>
          <p className="text-gray-600">{profile.business.industry}</p>
        </div>
        <Button>Edit Profile</Button>
      </div>
      
      {/* Completion Score */}
      <Card>
        <CardHeader>
          <CardTitle>Profile Completion</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center space-x-4">
            <Progress value={completion.completion_score} className="flex-1" />
            <span className="text-2xl font-bold">{completion.completion_score}%</span>
          </div>
          
          {completion.missing_required.length > 0 && (
            <div className="mt-4 p-3 bg-yellow-50 rounded-lg">
              <div className="flex items-center text-yellow-800">
                <AlertCircle className="w-5 h-5 mr-2" />
                <span className="font-medium">Missing Required Fields</span>
              </div>
              <ul className="mt-2 list-disc list-inside text-sm text-yellow-700">
                {completion.missing_required.map(field => (
                  <li key={field}>{field.replace(/_/g, ' ')}</li>
                ))}
              </ul>
            </div>
          )}
        </CardContent>
      </Card>
      
      {/* Tabs */}
      <Tabs defaultValue="business">
        <TabsList>
          <TabsTrigger value="business">
            <Building2 className="w-4 h-4 mr-2" />
            Business
          </TabsTrigger>
          <TabsTrigger value="address">
            <MapPin className="w-4 h-4 mr-2" />
            Address
          </TabsTrigger>
          <TabsTrigger value="bank">
            <CreditCard className="w-4 h-4 mr-2" />
            Bank
          </TabsTrigger>
          <TabsTrigger value="owner">
            <User className="w-4 h-4 mr-2" />
            Owner
          </TabsTrigger>
          <TabsTrigger value="compliance">
            <FileCheck className="w-4 h-4 mr-2" />
            Compliance
          </TabsTrigger>
          <TabsTrigger value="financials">
            <TrendingUp className="w-4 h-4 mr-2" />
            Financials
          </TabsTrigger>
        </TabsList>
        
        <TabsContent value="business">
          <BusinessDetails profile={profile.business} />
        </TabsContent>
        
        <TabsContent value="address">
          <AddressDetails profile={profile.address} />
        </TabsContent>
        
        <TabsContent value="bank">
          <BankDetails profile={profile.bank_details} />
        </TabsContent>
        
        <TabsContent value="owner">
          <OwnerDetails profile={profile.owner} />
        </TabsContent>
        
        <TabsContent value="compliance">
          <ComplianceDetails msmeId={msmeId} />
        </TabsContent>
        
        <TabsContent value="financials">
          <FinancialDetails msmeId={msmeId} />
        </TabsContent>
      </Tabs>
    </div>
  );
};

// Sub-components
const BusinessDetails: React.FC<{ profile: any }> = ({ profile }) => (
  <Card>
    <CardContent className="pt-6">
      <div className="grid grid-cols-2 gap-6">
        <div>
          <label className="text-sm text-gray-500">Business Name</label>
          <p className="font-medium">{profile.name}</p>
        </div>
        <div>
          <label className="text-sm text-gray-500">Business Type</label>
          <p className="font-medium">{profile.type}</p>
        </div>
        <div>
          <label className="text-sm text-gray-500">Industry</label>
          <p className="font-medium">{profile.industry}</p>
        </div>
        <div>
          <label className="text-sm text-gray-500">Years in Business</label>
          <p className="font-medium">{profile.years_in_business} years</p>
        </div>
        <div>
          <label className="text-sm text-gray-500">PAN</label>
          <p className="font-medium">{profile.pan || 'Not provided'}</p>
        </div>
        <div>
          <label className="text-sm text-gray-500">GSTIN</label>
          <p className="font-medium">{profile.gstin || 'Not provided'}</p>
        </div>
        <div>
          <label className="text-sm text-gray-500">UDYAM Number</label>
          <p className="font-medium">{profile.udyam_number || 'Not provided'}</p>
        </div>
        <div>
          <label className="text-sm text-gray-500">Annual Turnover</label>
          <p className="font-medium">
            {profile.annual_turnover 
              ? `₹${profile.annual_turnover.toLocaleString()}`
              : 'Not provided'}
          </p>
        </div>
      </div>
    </CardContent>
  </Card>
);

const AddressDetails: React.FC<{ profile: any }> = ({ profile }) => (
  <Card>
    <CardContent className="pt-6">
      <div className="space-y-2">
        <p>{profile.line1}</p>
        {profile.line2 && <p>{profile.line2}</p>}
        <p>{profile.city}, {profile.state} - {profile.pincode}</p>
        <p>{profile.country}</p>
      </div>
    </CardContent>
  </Card>
);

const BankDetails: React.FC<{ profile: any }> = ({ profile }) => (
  <Card>
    <CardContent className="pt-6">
      <div className="grid grid-cols-2 gap-6">
        <div>
          <label className="text-sm text-gray-500">Account Number</label>
          <p className="font-medium">{profile.account_number || 'Not provided'}</p>
        </div>
        <div>
          <label className="text-sm text-gray-500">IFSC</label>
          <p className="font-medium">{profile.ifsc || 'Not provided'}</p>
        </div>
        <div>
          <label className="text-sm text-gray-500">Bank Name</label>
          <p className="font-medium">{profile.bank_name || 'Not provided'}</p>
        </div>
        <div>
          <label className="text-sm text-gray-500">Account Type</label>
          <p className="font-medium">{profile.account_type || 'Not provided'}</p>
        </div>
      </div>
    </CardContent>
  </Card>
);

const OwnerDetails: React.FC<{ profile: any }> = ({ profile }) => (
  <Card>
    <CardContent className="pt-6">
      <div className="grid grid-cols-2 gap-6">
        <div>
          <label className="text-sm text-gray-500">Name</label>
          <p className="font-medium">{profile.name}</p>
        </div>
        <div>
          <label className="text-sm text-gray-500">Phone</label>
          <p className="font-medium">{profile.phone}</p>
        </div>
        <div>
          <label className="text-sm text-gray-500">Email</label>
          <p className="font-medium">{profile.email}</p>
        </div>
      </div>
    </CardContent>
  </Card>
);
```

---

## 2.5 UI DESIGN

```
┌─────────────────────────────────────────────────────────────────┐
│  MSME PROFILE MANAGEMENT                                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  PROFILE COMPLETION                                      │    │
│  │                                                          │    │
│  │  ████████████████████████████████░░░░░░░░░  78%         │    │
│  │                                                          │    │
│  │  ✅ Business Name    ✅ PAN Number                       │    │
│  │  ✅ Business Type    ✅ GSTIN                            │    │
│  │  ✅ Industry         ✅ Address                          │    │
│  │  ✅ Employees        ⚠️  Bank Account                    │    │
│  │  ✅ Turnover         ⚠️  UDYAM Number                    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  [Business] [Address] [Bank] [Owner] [Compliance]       │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  BUSINESS DETAILS                                        │    │
│  │                                                          │    │
│  │  Business Name:    Rahul Textiles Pvt Ltd               │    │
│  │  Business Type:    Private Limited                      │    │
│  │  Industry:         Manufacturing > Textiles             │    │
│  │  PAN:              ABCDE1234F          [✅ Verified]     │    │
│  │  GSTIN:            27ABCDE1234F1Z5    [✅ Verified]     │    │
│  │  UDYAM:            UDYAM-MH-12-0001234 [⏳ Pending]     │    │
│  │  CIN:              U12345MH2015PTC123456                 │    │
│  │  Est. Date:        January 15, 2015                     │    │
│  │  Years:            9 years                              │    │
│  │  Employees:        25                                  │    │
│  │  Annual Turnover:  ₹2,50,00,000                        │    │
│  │                                                          │    │
│  │  [Edit Business Details]                                │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  DOCUMENTS                                              │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  PAN Card.pdf              [✅ Verified]         │    │    │
│  │  │  Uploaded: Jan 15, 2024                          │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  GST Certificate.pdf      [⏳ Pending]          │    │    │
│  │  │  Uploaded: Jan 15, 2024                          │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  ┌─────────────────────────────────────────┐    │    │    │
│  │  │  │  + Upload New Document                   │    │    │    │
│  │  │  └─────────────────────────────────────────┘    │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2.6 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| Database Schema | 1 day |
| Profile Service | 3 days |
| Document Service | 2 days |
| Compliance Service | 2 days |
| API Endpoints | 2 days |
| Frontend Components | 4 days |
| Testing | 2 days |
| **Total** | **16 days** |

---

## 2.7 HACKATHON PRIORITY

**CRITICAL** - Foundation for all data aggregation and scoring
