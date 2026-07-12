# MODULE 4: DATA PROCESSING & AI FEATURE ENGINEERING
## Complete Implementation Guide

---

## 4.1 DATABASE SCHEMA

```sql
-- Feature Store
CREATE TABLE feature_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    feature_category VARCHAR(50) NOT NULL,
    feature_name VARCHAR(100) NOT NULL,
    feature_value DECIMAL(15,4),
    feature_value_text VARCHAR(255),
    feature_metadata JSONB,
    computed_at TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ NOT NULL,
    UNIQUE(msme_id, feature_name)
);

CREATE INDEX idx_feature_store_lookup 
ON feature_store(msme_id, feature_name, computed_at DESC);

-- Processed Data
CREATE TABLE processed_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    data_type VARCHAR(50) NOT NULL,
    processing_status VARCHAR(20) NOT NULL,
    input_records INTEGER,
    output_records INTEGER,
    cleaning_stats JSONB,
    validation_stats JSONB,
    processing_time_ms INTEGER,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Data Quality Metrics
CREATE TABLE data_quality_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(10,4),
    threshold DECIMAL(10,4),
    status VARCHAR(20),
    details JSONB,
    computed_at TIMESTAMPTZ DEFAULT NOW()
);

-- Feature Definitions
CREATE TABLE feature_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feature_name VARCHAR(100) UNIQUE NOT NULL,
    feature_category VARCHAR(50) NOT NULL,
    description TEXT,
    data_type VARCHAR(20) NOT NULL,
    computation_logic TEXT,
    version INTEGER DEFAULT 1,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ML Feature Versioning
CREATE TABLE ml_feature_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version_name VARCHAR(50) NOT NULL,
    feature_list JSONB NOT NULL,
    model_performance JSONB,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

## 4.2 DATA PROCESSING PIPELINE

```python
# app/services/data_processor.py
from typing import Dict, List, Optional
from datetime import datetime
import pandas as pd
import numpy as np
from sqlalchemy.orm import Session
from app.models import ProcessedData, FeatureStore, DataQualityMetric

class DataProcessor:
    def __init__(self, db: Session):
        self.db = db
    
    async def process_gst_data(self, msme_id: str) -> Dict:
        """Process and clean GST data"""
        
        # Fetch raw data
        from app.models import GSTData
        raw_data = self.db.query(GSTData).filter(
            GSTData.msme_id == msme_id
        ).all()
        
        if not raw_data:
            return {'status': 'no_data'}
        
        # Convert to DataFrame
        df = pd.DataFrame([{
            'filing_period': r.filing_period,
            'total_turnover': float(r.total_turnover) if r.total_turnover else 0,
            'taxable_turnover': float(r.taxable_turnover) if r.taxable_turnover else 0,
            'tax_paid': float(r.tax_paid) if r.tax_paid else 0,
            'input_tax_credit': float(r.input_tax_credit) if r.input_tax_credit else 0,
            'compliance_score': float(r.compliance_score) if r.compliance_score else 0
        } for r in raw_data])
        
        # Data Cleaning
        cleaning_stats = self._clean_data(df)
        
        # Data Validation
        validation_stats = self._validate_data(df, 'gst')
        
        # Feature Engineering
        features = self._compute_gst_features(df)
        
        # Store features
        await self._store_features(msme_id, 'gst', features)
        
        # Log processing
        processing_log = ProcessedData(
            msme_id=msme_id,
            data_type='gst',
            processing_status='completed',
            input_records=len(raw_data),
            output_records=len(df),
            cleaning_stats=cleaning_stats,
            validation_stats=validation_stats,
            started_at=datetime.utcnow(),
            completed_at=datetime.utcnow()
        )
        self.db.add(processing_log)
        self.db.commit()
        
        return {
            'status': 'success',
            'features_computed': len(features),
            'cleaning_stats': cleaning_stats,
            'validation_stats': validation_stats
        }
    
    async def process_upi_data(self, msme_id: str) -> Dict:
        """Process and clean UPI data"""
        
        from app.models import UPITransaction
        raw_data = self.db.query(UPITransaction).filter(
            UPITransaction.msme_id == msme_id
        ).all()
        
        if not raw_data:
            return {'status': 'no_data'}
        
        df = pd.DataFrame([{
            'transaction_date': r.transaction_date,
            'amount': float(r.amount),
            'transaction_type': r.transaction_type,
            'sender_vpa': r.sender_vpa,
            'receiver_vpa': r.receiver_vpa
        } for r in raw_data])
        
        # Process
        cleaning_stats = self._clean_data(df)
        validation_stats = self._validate_data(df, 'upi')
        features = self._compute_upi_features(df)
        
        await self._store_features(msme_id, 'upi', features)
        
        return {
            'status': 'success',
            'features_computed': len(features)
        }
    
    async def process_bank_data(self, msme_id: str) -> Dict:
        """Process and clean bank statement data"""
        
        from app.models import BankStatement
        raw_data = self.db.query(BankStatement).filter(
            BankStatement.msme_id == msme_id
        ).all()
        
        if not raw_data:
            return {'status': 'no_data'}
        
        df = pd.DataFrame([{
            'statement_month': r.statement_month,
            'opening_balance': float(r.opening_balance) if r.opening_balance else 0,
            'closing_balance': float(r.closing_balance) if r.closing_balance else 0,
            'total_credits': float(r.total_credits) if r.total_credits else 0,
            'total_debits': float(r.total_debits) if r.total_debits else 0,
            'average_balance': float(r.average_balance) if r.average_balance else 0,
            'transaction_count': r.transaction_count,
            'bounced_checks': r.bounced_checks
        } for r in raw_data])
        
        cleaning_stats = self._clean_data(df)
        validation_stats = self._validate_data(df, 'bank')
        features = self._compute_bank_features(df)
        
        await self._store_features(msme_id, 'bank', features)
        
        return {
            'status': 'success',
            'features_computed': len(features)
        }
    
    def _clean_data(self, df: pd.DataFrame) -> Dict:
        """Clean data - handle missing values, duplicates, outliers"""
        
        initial_rows = len(df)
        
        # Remove duplicates
        df.drop_duplicates(inplace=True)
        duplicates_removed = initial_rows - len(df)
        
        # Handle missing values
        missing_before = df.isnull().sum().sum()
        
        # Fill numeric columns with median
        numeric_cols = df.select_dtypes(include=[np.number]).columns
        df[numeric_cols] = df[numeric_cols].fillna(df[numeric_cols].median())
        
        # Fill categorical columns with mode
        categorical_cols = df.select_dtypes(include=['object']).columns
        for col in categorical_cols:
            if not df[col].mode().empty:
                df[col].fillna(df[col].mode()[0], inplace=True)
        
        missing_after = df.isnull().sum().sum()
        
        # Handle outliers using IQR
        outliers_removed = 0
        for col in numeric_cols:
            Q1 = df[col].quantile(0.25)
            Q3 = df[col].quantile(0.75)
            IQR = Q3 - Q1
            lower_bound = Q1 - 1.5 * IQR
            upper_bound = Q3 + 1.5 * IQR
            
            outliers = ((df[col] < lower_bound) | (df[col] > upper_bound)).sum()
            outliers_removed += outliers
        
        return {
            'initial_rows': initial_rows,
            'final_rows': len(df),
            'duplicates_removed': duplicates_removed,
            'missing_values_handled': int(missing_before - missing_after),
            'outliers_detected': outliers_removed
        }
    
    def _validate_data(self, df: pd.DataFrame, data_type: str) -> Dict:
        """Validate data quality"""
        
        validations = {}
        
        if data_type == 'gst':
            validations['turnover_positive'] = (df['total_turnover'] >= 0).all()
            validations['tax_paid_valid'] = (df['tax_paid'] >= 0).all()
            validations['compliance_score_range'] = (
                (df['compliance_score'] >= 0) & 
                (df['compliance_score'] <= 100)
            ).all()
        
        elif data_type == 'upi':
            validations['amount_positive'] = (df['amount'] > 0).all()
            validations['valid_transaction_types'] = (
                df['transaction_type'].isin(['credit', 'debit'])
            ).all()
        
        elif data_type == 'bank':
            validations['balance_reasonable'] = (
                (df['closing_balance'] > -1000000) &  # Allow small overdraft
                (df['closing_balance'] < 100000000)
            ).all()
            validations['credits_debits_positive'] = (
                (df['total_credits'] >= 0) & 
                (df['total_debits'] >= 0)
            ).all()
        
        passed = sum(validations.values())
        total = len(validations)
        
        return {
            'validations': validations,
            'passed': passed,
            'total': total,
            'score': (passed / total) * 100 if total > 0 else 0
        }
    
    async def _store_features(
        self, 
        msme_id: str, 
        category: str, 
        features: Dict
    ):
        """Store computed features"""
        
        for feature_name, feature_value in features.items():
            # Check if feature exists
            existing = self.db.query(FeatureStore).filter(
                FeatureStore.msme_id == msme_id,
                FeatureStore.feature_name == feature_name
            ).first()
            
            if existing:
                existing.feature_value = feature_value
                existing.computed_at = datetime.utcnow()
                existing.valid_until = datetime.utcnow() + pd.Timedelta(days=7)
            else:
                feature = FeatureStore(
                    msme_id=msme_id,
                    feature_category=category,
                    feature_name=feature_name,
                    feature_value=feature_value,
                    computed_at=datetime.utcnow(),
                    valid_until=datetime.utcnow() + pd.Timedelta(days=7)
                )
                self.db.add(feature)
        
        self.db.commit()
```

---

## 4.3 AI FEATURE ENGINEERING

```python
# app/services/feature_engineer.py
from typing import Dict, List
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

class FeatureEngineer:
    """Compute AI features from raw data"""
    
    def compute_all_features(self, msme_id: str, data: Dict) -> Dict:
        """Compute all features for an MSME"""
        
        features = {}
        
        # GST Features
        if 'gst' in data:
            features.update(self._compute_gst_features(data['gst']))
        
        # UPI Features
        if 'upi' in data:
            features.update(self._compute_upi_features(data['upi']))
        
        # Bank Features
        if 'bank' in data:
            features.update(self._compute_bank_features(data['bank']))
        
        # EPFO Features
        if 'epfo' in data:
            features.update(self._compute_epfo_features(data['epfo']))
        
        # Utility Features
        if 'utility' in data:
            features.update(self._compute_utility_features(data['utility']))
        
        # Cross-source Features
        features.update(self._compute_cross_source_features(data))
        
        return features
    
    def _compute_gst_features(self, gst_data: Dict) -> Dict:
        """Compute GST-related features"""
        
        features = {}
        
        # Monthly Revenue
        features['monthly_revenue'] = gst_data.get('avg_monthly_turnover', 0)
        
        # Revenue Growth Rate
        features['revenue_growth_rate'] = gst_data.get('growth_rate', 0)
        
        # GST Compliance Ratio
        total_turnover = gst_data.get('total_turnover', 0)
        tax_paid = gst_data.get('total_tax_paid', 0)
        features['gst_compliance_ratio'] = (tax_paid / total_turnover * 100) if total_turnover > 0 else 0
        
        # Filing Regularity
        features['filing_regularity'] = gst_data.get('filing_regularity', 0)
        
        # Tax Efficiency
        features['tax_efficiency'] = (tax_paid / total_turnover * 100) if total_turnover > 0 else 0
        
        return features
    
    def _compute_upi_features(self, upi_data: Dict) -> Dict:
        """Compute UPI-related features"""
        
        features = {}
        
        # Transaction Volume
        features['total_transactions_6m'] = upi_data.get('total_transactions', 0)
        
        # Average Transaction Value
        features['avg_transaction_value'] = upi_data.get('avg_transaction_amount', 0)
        
        # Net Cash Flow
        features['net_cash_flow'] = upi_data.get('net_flow', 0)
        
        # Credit-Debit Ratio
        credits = upi_data.get('total_credits', 0)
        debits = upi_data.get('total_debits', 0)
        features['credit_debit_ratio'] = (credits / debits) if debits > 0 else 1
        
        # Customer Diversity
        features['unique_customers'] = upi_data.get('unique_customers', 0)
        
        # Vendor Diversity
        features['unique_vendors'] = upi_data.get('unique_vendors', 0)
        
        # Digital Adoption Score
        features['digital_adoption_score'] = upi_data.get('digital_adoption_score', 0)
        
        # Credit Trend
        features['credit_trend'] = upi_data.get('credit_trend', 0)
        
        return features
    
    def _compute_bank_features(self, bank_data: Dict) -> Dict:
        """Compute bank-related features"""
        
        features = {}
        
        # Average Monthly Balance
        features['avg_monthly_balance'] = bank_data.get('avg_balance', 0)
        
        # Current Balance
        features['current_balance'] = bank_data.get('current_balance', 0)
        
        # Balance Trend (positive = growing)
        features['balance_trend'] = bank_data.get('cash_flow_score', 50)
        
        # Cash Flow Score
        features['cash_flow_score'] = bank_data.get('cash_flow_score', 50)
        
        # Bounced Checks (negative indicator)
        features['bounced_checks'] = bank_data.get('bounced_checks', 0)
        
        # EMI Deductions
        features['emi_count'] = bank_data.get('emi_deductions', 0)
        
        # Salary Payments (employee count proxy)
        features['salary_payments'] = bank_data.get('salary_payments', 0)
        
        return features
    
    def _compute_epfo_features(self, epfo_data: Dict) -> Dict:
        """Compute EPFO-related features"""
        
        features = {}
        
        # Employee Count
        features['employee_count'] = epfo_data.get('current_employees', 0)
        
        # Employee Growth Rate
        features['employee_growth_rate'] = epfo_data.get('employee_growth', 0)
        
        # Employee Turnover Rate
        features['employee_turnover_rate'] = epfo_data.get('turnover_rate', 0)
        
        # Average Salary
        features['avg_employee_salary'] = epfo_data.get('avg_salary', 0)
        
        # Total Contribution
        features['total_epf_contribution'] = epfo_data.get('total_contribution', 0)
        
        # Stability Score
        features['employee_stability_score'] = epfo_data.get('stability_score', 50)
        
        return features
    
    def _compute_utility_features(self, utility_data: Dict) -> Dict:
        """Compute utility-related features"""
        
        features = {}
        
        # Payment Score
        features['utility_payment_score'] = utility_data.get('payment_score', 50)
        
        # On-time Payment Rate
        total_bills = utility_data.get('total_bills', 0)
        paid_on_time = utility_data.get('paid_on_time', 0)
        features['on_time_payment_rate'] = (paid_on_time / total_bills * 100) if total_bills > 0 else 0
        
        return features
    
    def _compute_cross_source_features(self, data: Dict) -> Dict:
        """Compute features using multiple data sources"""
        
        features = {}
        
        # Working Capital Estimate
        avg_balance = data.get('bank', {}).get('avg_balance', 0)
        monthly_revenue = data.get('gst', {}).get('avg_monthly_turnover', 0)
        features['working_capital_months'] = (avg_balance / monthly_revenue) if monthly_revenue > 0 else 0
        
        # Liquidity Ratio
        current_assets = data.get('bank', {}).get('current_balance', 0)
        current_liabilities = monthly_revenue * 0.5  # Estimate
        features['liquidity_ratio'] = (current_assets / current_liabilities) if current_liabilities > 0 else 1
        
        # Revenue per Employee
        revenue = data.get('gst', {}).get('total_turnover', 0)
        employees = data.get('epfo', {}).get('current_employees', 1)
        features['revenue_per_employee'] = revenue / employees if employees > 0 else 0
        
        # Digital Transaction Ratio
        digital_volume = data.get('upi', {}).get('total_credits', 0)
        total_revenue = data.get('gst', {}).get('total_turnover', 1)
        features['digital_transaction_ratio'] = (digital_volume / total_revenue * 100) if total_revenue > 0 else 0
        
        # Overall Compliance Score
        gst_compliance = data.get('gst', {}).get('compliance_score', 0)
        utility_compliance = data.get('utility', {}).get('payment_score', 0)
        features['overall_compliance_score'] = (gst_compliance + utility_compliance) / 2
        
        return features
```

---

## 4.4 FEATURE DEFINITIONS

```python
# app/services/feature_definitions.py

FEATURE_DEFINITIONS = {
    # GST Features
    'monthly_revenue': {
        'category': 'revenue',
        'description': 'Average monthly revenue from GST filings',
        'data_type': 'numeric',
        'computation': 'avg(gst.total_turnover)'
    },
    'revenue_growth_rate': {
        'category': 'revenue',
        'description': 'Month-over-month revenue growth rate',
        'data_type': 'numeric',
        'computation': '(current_month - previous_month) / previous_month * 100'
    },
    'gst_compliance_ratio': {
        'category': 'compliance',
        'description': 'Ratio of tax paid to total turnover',
        'data_type': 'numeric',
        'computation': 'tax_paid / total_turnover * 100'
    },
    'filing_regularity': {
        'category': 'compliance',
        'description': 'Percentage of GST returns filed on time',
        'data_type': 'numeric',
        'computation': 'filed_count / total_periods * 100'
    },
    
    # UPI Features
    'total_transactions_6m': {
        'category': 'transaction',
        'description': 'Total UPI transactions in last 6 months',
        'data_type': 'numeric',
        'computation': 'count(upi_transactions)'
    },
    'avg_transaction_value': {
        'category': 'transaction',
        'description': 'Average UPI transaction value',
        'data_type': 'numeric',
        'computation': 'avg(upi.amount)'
    },
    'net_cash_flow': {
        'category': 'cash_flow',
        'description': 'Net cash flow from UPI (credits - debits)',
        'data_type': 'numeric',
        'computation': 'sum(credits) - sum(debits)'
    },
    'credit_debit_ratio': {
        'category': 'cash_flow',
        'description': 'Ratio of credits to debits',
        'data_type': 'numeric',
        'computation': 'sum(credits) / sum(debits)'
    },
    'digital_adoption_score': {
        'category': 'digital',
        'description': 'Score indicating digital payment adoption',
        'data_type': 'numeric',
        'computation': 'composite_score(volume, consistency, diversity)'
    },
    
    # Bank Features
    'avg_monthly_balance': {
        'category': 'liquidity',
        'description': 'Average monthly bank balance',
        'data_type': 'numeric',
        'computation': 'avg(bank.average_balance)'
    },
    'cash_flow_score': {
        'category': 'cash_flow',
        'description': 'Score indicating cash flow health',
        'data_type': 'numeric',
        'computation': 'composite_score(balance_trend, credit_debit_ratio, consistency)'
    },
    'bounced_checks': {
        'category': 'risk',
        'description': 'Number of bounced checks in last 6 months',
        'data_type': 'numeric',
        'computation': 'sum(bounced_checks)'
    },
    
    # EPFO Features
    'employee_count': {
        'category': 'stability',
        'description': 'Current number of employees',
        'data_type': 'numeric',
        'computation': 'latest(epfo.total_employees)'
    },
    'employee_growth_rate': {
        'category': 'stability',
        'description': 'Month-over-month employee growth rate',
        'data_type': 'numeric',
        'computation': '(current - previous) / previous * 100'
    },
    'employee_turnover_rate': {
        'category': 'risk',
        'description': 'Employee turnover rate',
        'data_type': 'numeric',
        'computation': 'exited / avg_employees * 100'
    },
    'avg_employee_salary': {
        'category': 'compensation',
        'description': 'Average employee salary',
        'data_type': 'numeric',
        'computation': 'avg(epfo.average_salary)'
    },
    
    # Utility Features
    'utility_payment_score': {
        'category': 'compliance',
        'description': 'Score for utility bill payment timeliness',
        'data_type': 'numeric',
        'computation': 'on_time_payments / total_bills * 100'
    },
    
    # Cross-source Features
    'working_capital_months': {
        'category': 'liquidity',
        'description': 'Months of working capital available',
        'data_type': 'numeric',
        'computation': 'avg_balance / monthly_revenue'
    },
    'liquidity_ratio': {
        'category': 'liquidity',
        'description': 'Current assets to current liabilities ratio',
        'data_type': 'numeric',
        'computation': 'current_assets / current_liabilities'
    },
    'revenue_per_employee': {
        'category': 'efficiency',
        'description': 'Revenue generated per employee',
        'data_type': 'numeric',
        'computation': 'total_revenue / employee_count'
    },
    'digital_transaction_ratio': {
        'category': 'digital',
        'description': 'Percentage of revenue via digital transactions',
        'data_type': 'numeric',
        'computation': 'digital_volume / total_revenue * 100'
    },
    'overall_compliance_score': {
        'category': 'compliance',
        'description': 'Overall compliance score across all sources',
        'data_type': 'numeric',
        'computation': 'avg(gst_compliance, utility_compliance)'
    }
}
```

---

## 4.5 API ENDPOINTS

```python
# app/api/v1/features.py
from fastapi import APIRouter, Depends
from app.core.deps import get_current_user, get_db
from app.services.feature_engineer import FeatureEngineer
from app.services.data_processor import DataProcessor

router = APIRouter(prefix="/features", tags=["Feature Engineering"])

@router.post("/compute/{msme_id}")
async def compute_features(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    processor = DataProcessor(db)
    engineer = FeatureEngineer()
    
    # Process data
    await processor.process_gst_data(msme_id)
    await processor.process_upi_data(msme_id)
    await processor.process_bank_data(msme_id)
    
    # Fetch summaries
    from app.services.data_aggregator import DataAggregator
    aggregator = DataAggregator(db)
    data = aggregator.get_all_summaries(msme_id)
    
    # Compute features
    features = engineer.compute_all_features(msme_id, data)
    
    return {
        'status': 'success',
        'features_computed': len(features),
        'features': features
    }

@router.get("/{msme_id}")
async def get_features(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    from app.models import FeatureStore
    
    features = db.query(FeatureStore).filter(
        FeatureStore.msme_id == msme_id,
        FeatureStore.valid_until > datetime.utcnow()
    ).all()
    
    return {
        'msme_id': msme_id,
        'feature_count': len(features),
        'features': {
            f.feature_name: f.feature_value 
            for f in features
        }
    }

@router.get("/definitions")
async def get_feature_definitions():
    return FEATURE_DEFINITIONS
```

---

## 4.6 UI DESIGN

```
┌─────────────────────────────────────────────────────────────────┐
│  FEATURE ENGINEERING & DATA PROCESSING                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  COMPUTED FEATURES                                       │    │
│  │                                                          │    │
│  │  Revenue Features:                                       │    │
│  │  • Monthly Revenue: ₹2,50,000                          │    │
│  │  • Revenue Growth: +15.2%                               │    │
│  │  • GST Compliance: 94.5%                                │    │
│  │                                                          │    │
│  │  Cash Flow Features:                                     │    │
│  │  • Net Cash Flow: ₹1,25,000                            │    │
│  │  • Credit-Debit Ratio: 1.35                             │    │
│  │  • Cash Flow Score: 78/100                              │    │
│  │                                                          │    │
│  │  Transaction Features:                                   │    │
│  │  • Total Transactions: 1,250                            │    │
│  │  • Avg Transaction: ₹8,500                              │    │
│  │  • Digital Adoption: 72%                                 │    │
│  │                                                          │    │
│  │  Stability Features:                                     │    │
│  │  • Employee Count: 25                                   │    │
│  │  • Employee Growth: +8%                                 │    │
│  │  • Stability Score: 85/100                              │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  DATA QUALITY                                           │    │
│  │                                                          │    │
│  │  Completeness: ████████████████████░░░░ 82%            │    │
│  │  Accuracy: ████████████████████████░░░ 92%             │    │
│  │  Timeliness: ██████████████████░░░░░░░ 75%             │    │
│  │  Consistency: ████████████████████████ 95%             │    │
│  │                                                          │    │
│  │  Overall Quality Score: 86/100                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  FEATURE HISTORY                                         │    │
│  │                                                          │    │
│  │  [Line Chart showing feature trends over time]          │    │
│  │                                                          │    │
│  │  Revenue: ▁▂▃▄▅▆▇█▇▆ (Growing)                        │    │
│  │  Cash Flow: ▁▁▂▂▃▃▄▄ (Stable)                          │    │
│  │  Compliance: ████████████ (Consistent)                  │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4.7 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| Database Schema | 1 day |
| Data Processor | 3 days |
| Feature Engineer | 3 days |
| Feature Definitions | 1 day |
| API Endpoints | 1 day |
| Frontend | 2 days |
| Testing | 2 days |
| **Total** | **13 days** |

---

## 4.8 HACKATHON PRIORITY

**CRITICAL** - Foundation for all AI/ML scoring
