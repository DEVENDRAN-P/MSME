# MODULE 3: ALTERNATE DATA AGGREGATION
## Complete Implementation Guide

---

## 3.1 DATABASE SCHEMA

```sql
-- GST Data
CREATE TABLE gst_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    gstin VARCHAR(15) NOT NULL,
    filing_period VARCHAR(10) NOT NULL,
    filing_date DATE,
    total_turnover DECIMAL(15,2),
    taxable_turnover DECIMAL(15,2),
    tax_paid DECIMAL(15,2),
    input_tax_credit DECIMAL(15,2),
    compliance_score DECIMAL(5,2),
    filing_status VARCHAR(20),
    gstr1_filed BOOLEAN DEFAULT FALSE,
    gstr3b_filed BOOLEAN DEFAULT FALSE,
    data_source VARCHAR(50) DEFAULT 'gst_api',
    raw_data JSONB,
    fetched_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_gst_msme_period ON gst_data(msme_id, filing_period DESC);

-- UPI Transaction Data
CREATE TABLE upi_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    transaction_id VARCHAR(100) UNIQUE,
    transaction_date DATE NOT NULL,
    transaction_time TIME,
    amount DECIMAL(15,2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- credit/debit
    sender_vpa VARCHAR(100),
    receiver_vpa VARCHAR(100),
    sender_name VARCHAR(255),
    receiver_name VARCHAR(255),
    description TEXT,
    status VARCHAR(20) DEFAULT 'success',
    data_source VARCHAR(50) DEFAULT 'upi_api',
    raw_data JSONB,
    fetched_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_upi_msme_date ON upi_transactions(msme_id, transaction_date DESC);
CREATE INDEX idx_upi_type ON upi_transactions(transaction_type);

-- Account Aggregator Data
CREATE TABLE aa_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    consent_id VARCHAR(100),
    data_provider VARCHAR(100),
    account_id VARCHAR(100),
    account_type VARCHAR(50),
    balance DECIMAL(15,2),
    transactions JSONB,
    income_pattern JSONB,
    expense_pattern JSONB,
    data_start_date DATE,
    data_end_date DATE,
    fetched_at TIMESTAMPTZ DEFAULT NOW()
);

-- EPFO Data
CREATE TABLE epfo_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msME_profiles(id),
    establishment_code VARCHAR(20),
    reporting_month VARCHAR(7) NOT NULL, -- YYYY-MM
    total_employees INTEGER,
    new_employees INTEGER,
    exited_employees INTEGER,
    total_contribution DECIMAL(15,2),
    employer_contribution DECIMAL(15,2),
    employee_contribution DECIMAL(15,2),
    average_salary DECIMAL(15,2),
    data_source VARCHAR(50) DEFAULT 'epfo_api',
    raw_data JSONB,
    fetched_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_epfo_msme_month ON epfo_data(msme_id, reporting_month DESC);

-- Bank Statement Data
CREATE TABLE bank_statements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    account_id VARCHAR(100),
    statement_month VARCHAR(7) NOT NULL, -- YYYY-MM
    opening_balance DECIMAL(15,2),
    closing_balance DECIMAL(15,2),
    total_credits DECIMAL(15,2),
    total_debits DECIMAL(15,2),
    average_balance DECIMAL(15,2),
    min_balance DECIMAL(15,2),
    max_balance DECIMAL(15,2),
    transaction_count INTEGER,
    bounced_checks INTEGER,
    emi_deductions INTEGER,
    salary_payments INTEGER,
    data_source VARCHAR(50) DEFAULT 'bank_api',
    raw_data JSONB,
    fetched_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_bank_msme_month ON bank_statements(msme_id, statement_month DESC);

-- Utility Bills Data
CREATE TABLE utility_bills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    utility_type VARCHAR(50) NOT NULL, -- electricity, water, internet
    provider VARCHAR(100),
    billing_month VARCHAR(7) NOT NULL,
    amount DECIMAL(10,2),
    due_date DATE,
    payment_date DATE,
    payment_status VARCHAR(20),
    payment_delay_days INTEGER,
    data_source VARCHAR(50) DEFAULT 'utility_api',
    raw_data JSONB,
    fetched_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_utility_msme_type ON utility_bills(msme_id, utility_type, billing_month DESC);

-- E-Commerce Data (Optional)
CREATE TABLE ecommerce_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    platform VARCHAR(50) NOT NULL, -- amazon, flipkart, etc.
    reporting_month VARCHAR(7) NOT NULL,
    total_sales DECIMAL(15,2),
    total_orders INTEGER,
    average_order_value DECIMAL(10,2),
    returns INTEGER,
    cancellations INTEGER,
    customer_ratings DECIMAL(3,2),
    data_source VARCHAR(50) DEFAULT 'ecommerce_api',
    raw_data JSONB,
    fetched_at TIMESTAMPTZ DEFAULT NOW()
);

-- POS Machine Data (Optional)
CREATE TABLE pos_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    terminal_id VARCHAR(50),
    reporting_date DATE NOT NULL,
    total_transactions INTEGER,
    total_amount DECIMAL(15,2),
    average_transaction DECIMAL(10,2),
    card_payments DECIMAL(15,2),
    upi_payments DECIMAL(15,2),
    data_source VARCHAR(50) DEFAULT 'pos_api',
    raw_data JSONB,
    fetched_at TIMESTAMPTZ DEFAULT NOW()
);

-- Invoice Data (Optional)
CREATE TABLE invoice_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    invoice_number VARCHAR(100),
    invoice_date DATE,
    due_date DATE,
    customer_name VARCHAR(255),
    customer_gstin VARCHAR(15),
    amount DECIMAL(15,2),
    tax_amount DECIMAL(15,2),
    total_amount DECIMAL(15,2),
    payment_status VARCHAR(20),
    payment_date DATE,
    payment_delay_days INTEGER,
    data_source VARCHAR(50) DEFAULT 'invoice_upload',
    raw_data JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Consent Records
CREATE TABLE consent_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    consent_type VARCHAR(50) NOT NULL,
    data_provider VARCHAR(100) NOT NULL,
    purpose VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    granted_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    consent_artifact JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Data Sync Logs
CREATE TABLE data_sync_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    data_source VARCHAR(50) NOT NULL,
    sync_status VARCHAR(20) NOT NULL,
    records_fetched INTEGER,
    records_processed INTEGER,
    error_message TEXT,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

## 3.2 BACKEND SERVICES

### GST Data Service

```python
# app/services/gst_service.py
from typing import Dict, List, Optional
from datetime import datetime, date
import httpx
from sqlalchemy.orm import Session
from app.models import GSTData

class GSTService:
    def __init__(self, db: Session):
        self.db = db
        self.api_base_url = "https://api.gst.gov.in"  # Simulated
    
    async def fetch_gst_data(
        self, 
        msme_id: str, 
        gstin: str,
        months: int = 12
    ) -> Dict:
        """Fetch GST data for MSME"""
        
        # In production, call actual GST API
        # Simulated data fetch
        
        gst_records = []
        for i in range(months):
            filing_date = date(2024, 12 - i, 1) if 12 - i > 0 else date(2024, 12 - i + 12, 1)
            
            record = GSTData(
                msme_id=msme_id,
                gstin=gstin,
                filing_period=filing_date.strftime('%Y-%m'),
                filing_date=filing_date,
                total_turnover=self._simulate_turnover(),
                taxable_turnover=self._simulate_taxable_turnover(),
                tax_paid=self._simulate_tax_paid(),
                input_tax_credit=self._simulate_itc(),
                compliance_score=self._simulate_compliance_score(),
                filing_status='filed',
                gstr1_filed=True,
                gstr3b_filed=True,
                data_source='gst_api',
                raw_data=self._simulate_raw_gst_data()
            )
            gst_records.append(record)
        
        # Bulk insert
        self.db.bulk_save_objects(gst_records)
        self.db.commit()
        
        return {
            'status': 'success',
            'records_fetched': len(gst_records),
            'period_range': f"{gst_records[-1].filing_period} to {gst_records[0].filing_period}"
        }
    
    def get_gst_summary(self, msme_id: str) -> Dict:
        """Get GST summary for MSME"""
        
        records = self.db.query(GSTData).filter(
            GSTData.msme_id == msme_id
        ).order_by(GSTData.filing_period.desc()).limit(12).all()
        
        if not records:
            return {'status': 'no_data'}
        
        # Calculate summary
        total_turnover = sum(float(r.total_turnover) for r in records)
        avg_monthly_turnover = total_turnover / len(records)
        total_tax_paid = sum(float(r.tax_paid) for r in records)
        
        # Filing regularity
        filed_count = sum(1 for r in records if r.filing_status == 'filed')
        filing_regularity = (filed_count / len(records)) * 100
        
        # Growth trend
        if len(records) >= 2:
            latest_turnover = float(records[0].total_turnover)
            previous_turnover = float(records[1].total_turnover)
            growth_rate = ((latest_turnover - previous_turnover) / previous_turnover) * 100
        else:
            growth_rate = 0
        
        return {
            'total_turnover': total_turnover,
            'avg_monthly_turnover': avg_monthly_turnover,
            'total_tax_paid': total_tax_paid,
            'filing_regularity': filing_regularity,
            'growth_rate': growth_rate,
            'compliance_score': float(records[0].compliance_score) if records else 0,
            'months_data': len(records)
        }
    
    def _simulate_turnover(self) -> float:
        import random
        return random.uniform(500000, 5000000)
    
    def _simulate_taxable_turnover(self) -> float:
        import random
        return random.uniform(400000, 4500000)
    
    def _simulate_tax_paid(self) -> float:
        import random
        return random.uniform(50000, 500000)
    
    def _simulate_itc(self) -> float:
        import random
        return random.uniform(30000, 300000)
    
    def _simulate_compliance_score(self) -> float:
        import random
        return random.uniform(70, 100)
    
    def _simulate_raw_gst_data(self) -> dict:
        return {
            'api_response': 'simulated',
            'timestamp': datetime.utcnow().isoformat()
        }
```

### UPI Transaction Service

```python
# app/services/upi_service.py
from typing import Dict, List
from datetime import datetime, date, timedelta
import random
from sqlalchemy.orm import Session
from sqlalchemy import func
from app.models import UPITransaction

class UPIService:
    def __init__(self, db: Session):
        self.db = db
    
    async def fetch_upi_data(
        self, 
        msme_id: str, 
        vpa: str,
        months: int = 6
    ) -> Dict:
        """Fetch UPI transaction data"""
        
        transactions = []
        end_date = date.today()
        start_date = end_date - timedelta(days=months * 30)
        
        current_date = start_date
        while current_date <= end_date:
            # Simulate daily transactions
            daily_count = random.randint(5, 20)
            
            for _ in range(daily_count):
                is_credit = random.random() > 0.4  # 60% credits
                
                transaction = UPITransaction(
                    msme_id=msme_id,
                    transaction_id=f"UPI{current_date.strftime('%Y%m%d')}{random.randint(1000, 9999)}",
                    transaction_date=current_date,
                    transaction_time=f"{random.randint(9, 18):02d}:{random.randint(0, 59):02d}:00",
                    amount=random.uniform(1000, 100000),
                    transaction_type='credit' if is_credit else 'debit',
                    sender_vpa=f"{'customer' if is_credit else vpa}@bank",
                    receiver_vpa=f"{vpa}@bank" if is_credit else f"{'vendor' if not is_credit else vpa}@bank",
                    sender_name=f"Customer {random.randint(1, 100)}" if is_credit else vpa,
                    receiver_name=vpa if is_credit else f"Vendor {random.randint(1, 50)}",
                    description=f"{'Payment received' if is_credit else 'Payment sent'}",
                    status='success',
                    data_source='upi_api'
                )
                transactions.append(transaction)
            
            current_date += timedelta(days=1)
        
        # Bulk insert
        self.db.bulk_save_objects(transactions)
        self.db.commit()
        
        return {
            'status': 'success',
            'records_fetched': len(transactions),
            'period': f"{start_date} to {end_date}"
        }
    
    def get_upi_summary(self, msme_id: str) -> Dict:
        """Get UPI transaction summary"""
        
        # Get last 6 months data
        six_months_ago = date.today() - timedelta(days=180)
        
        transactions = self.db.query(UPITransaction).filter(
            UPITransaction.msme_id == msme_id,
            UPITransaction.transaction_date >= six_months_ago
        ).all()
        
        if not transactions:
            return {'status': 'no_data'}
        
        # Calculate metrics
        credits = [t for t in transactions if t.transaction_type == 'credit']
        debits = [t for t in transactions if t.transaction_type == 'debit']
        
        total_credits = sum(float(t.amount) for t in credits)
        total_debits = sum(float(t.amount) for t in debits)
        
        # Monthly breakdown
        monthly_data = {}
        for t in transactions:
            month_key = t.transaction_date.strftime('%Y-%m')
            if month_key not in monthly_data:
                monthly_data[month_key] = {'credits': 0, 'debits': 0, 'count': 0}
            
            if t.transaction_type == 'credit':
                monthly_data[month_key]['credits'] += float(t.amount)
            else:
                monthly_data[month_key]['debits'] += float(t.amount)
            monthly_data[month_key]['count'] += 1
        
        # Calculate trends
        months = sorted(monthly_data.keys())
        if len(months) >= 2:
            current_month = monthly_data[months[-1]]['credits']
            previous_month = monthly_data[months[-2]]['credits']
            credit_trend = ((current_month - previous_month) / previous_month) * 100 if previous_month > 0 else 0
        else:
            credit_trend = 0
        
        # Unique counterparties
        unique_receivers = set(t.receiver_name for t in credits)
        unique_senders = set(t.sender_name for t in debits)
        
        return {
            'total_transactions': len(transactions),
            'total_credits': total_credits,
            'total_debits': total_debits,
            'net_flow': total_credits - total_debits,
            'avg_transaction_amount': (total_credits + total_debits) / len(transactions),
            'credit_count': len(credits),
            'debit_count': len(debits),
            'unique_customers': len(unique_receivers),
            'unique_vendors': len(unique_senders),
            'credit_trend': credit_trend,
            'monthly_data': monthly_data,
            'digital_adoption_score': self._calculate_digital_score(transactions)
        }
    
    def _calculate_digital_score(self, transactions: list) -> float:
        """Calculate digital payment adoption score"""
        
        if not transactions:
            return 0
        
        # Factors: volume, consistency, diversity
        total_volume = sum(float(t.amount) for t in transactions)
        consistency = len(set(t.transaction_date for t in transactions))
        
        score = min(100, (total_volume / 100000) * 30 + consistency * 2)
        return round(score, 2)
```

### Account Aggregator Service

```python
# app/services/aa_service.py
from typing import Dict, List
from datetime import datetime, timedelta
import random
from sqlalchemy.orm import Session
from app.models import AAData, ConsentRecord

class AccountAggregatorService:
    def __init__(self, db: Session):
        self.db = db
    
    async def initiate_consent(
        self, 
        msme_id: str, 
        data_types: List[str],
        purpose: str = 'credit_assessment'
    ) -> Dict:
        """Initiate AA consent request"""
        
        consent = ConsentRecord(
            msme_id=msme_id,
            consent_type='financial_data',
            data_provider='bank_aggregator',
            purpose=purpose,
            status='pending',
            created_at=datetime.utcnow()
        )
        
        self.db.add(consent)
        self.db.commit()
        
        # In production, call AA gateway
        return {
            'consent_id': str(consent.id),
            'status': 'pending',
            'consent_url': f"https://aa.example.com/consent/{consent.id}",
            'expires_at': (datetime.utcnow() + timedelta(hours=24)).isoformat()
        }
    
    async def approve_consent(self, consent_id: str) -> Dict:
        """Simulate consent approval"""
        
        consent = self.db.query(ConsentRecord).filter(
            ConsentRecord.id == consent_id
        ).first()
        
        if not consent:
            raise ValueError("Consent not found")
        
        consent.status = 'approved'
        consent.granted_at = datetime.utcnow()
        consent.expires_at = datetime.utcnow() + timedelta(days=90)
        
        self.db.commit()
        
        return {
            'consent_id': str(consent.id),
            'status': 'approved',
            'expires_at': consent.expires_at.isoformat()
        }
    
    async def fetch_financial_data(self, consent_id: str) -> Dict:
        """Fetch financial data using consent"""
        
        consent = self.db.query(ConsentRecord).filter(
            ConsentRecord.id == consent_id,
            ConsentRecord.status == 'approved'
        ).first()
        
        if not consent:
            raise ValueError("Valid consent not found")
        
        # Simulate data fetch
        financial_data = {
            'accounts': self._simulate_accounts(),
            'transactions': self._simulate_transactions(),
            'balances': self._simulate_balances()
        }
        
        # Store data
        aa_data = AAData(
            msme_id=consent.msme_id,
            consent_id=str(consent.id),
            data_provider='bank_aggregator',
            account_id='ACC001',
            account_type='current',
            balance=financial_data['balances']['current'],
            transactions=financial_data['transactions'],
            income_pattern=financial_data['income_pattern'],
            expense_pattern=financial_data['expense_pattern'],
            data_start_date=datetime.utcnow() - timedelta(days=180),
            data_end_date=datetime.utcnow(),
            fetched_at=datetime.utcnow()
        )
        
        self.db.add(aa_data)
        self.db.commit()
        
        return {
            'status': 'success',
            'data': financial_data
        }
    
    async def revoke_consent(self, consent_id: str) -> Dict:
        """Revoke consent"""
        
        consent = self.db.query(ConsentRecord).filter(
            ConsentRecord.id == consent_id
        ).first()
        
        if not consent:
            raise ValueError("Consent not found")
        
        consent.status = 'revoked'
        consent.revoked_at = datetime.utcnow()
        
        self.db.commit()
        
        return {
            'consent_id': str(consent.id),
            'status': 'revoked'
        }
    
    def _simulate_accounts(self) -> list:
        return [
            {
                'account_id': 'ACC001',
                'account_type': 'current',
                'bank_name': 'Sample Bank',
                'balance': random.uniform(100000, 1000000)
            }
        ]
    
    def _simulate_transactions(self) -> list:
        transactions = []
        for i in range(30):
            transactions.append({
                'date': (datetime.utcnow() - timedelta(days=i)).isoformat(),
                'amount': random.uniform(5000, 50000),
                'type': 'credit' if random.random() > 0.5 else 'debit',
                'description': f'Transaction {i+1}'
            })
        return transactions
    
    def _simulate_balances(self) -> dict:
        return {
            'current': random.uniform(100000, 500000),
            'average': random.uniform(80000, 400000),
            'minimum': random.uniform(50000, 200000)
        }
```

### EPFO Service

```python
# app/services/epfo_service.py
from typing import Dict, List
from datetime import datetime
import random
from sqlalchemy.orm import Session
from app.models import EPFOData

class EPFOService:
    def __init__(self, db: Session):
        self.db = db
    
    async def fetch_epfo_data(
        self, 
        msme_id: str, 
        establishment_code: str,
        months: int = 12
    ) -> Dict:
        """Fetch EPFO data"""
        
        records = []
        base_employees = random.randint(10, 50)
        
        for i in range(months):
            month = 12 - i
            year = 2024 if month > 0 else 2023
            actual_month = month if month > 0 else 12
            
            # Simulate employee changes
            new_employees = random.randint(0, 3)
            exited_employees = random.randint(0, 2)
            total_employees = base_employees + new_employees - exited_employees
            base_employees = total_employees
            
            avg_salary = random.uniform(20000, 50000)
            employer_contribution = total_employees * avg_salary * 0.12
            employee_contribution = total_employees * avg_salary * 0.12
            
            record = EPFOData(
                msme_id=msme_id,
                establishment_code=establishment_code,
                reporting_month=f"{year}-{actual_month:02d}",
                total_employees=total_employees,
                new_employees=new_employees,
                exited_employees=exited_employees,
                total_contribution=employer_contribution + employee_contribution,
                employer_contribution=employer_contribution,
                employee_contribution=employee_contribution,
                average_salary=avg_salary,
                data_source='epfo_api'
            )
            records.append(record)
        
        self.db.bulk_save_objects(records)
        self.db.commit()
        
        return {
            'status': 'success',
            'records_fetched': len(records)
        }
    
    def get_epfo_summary(self, msme_id: str) -> Dict:
        """Get EPFO summary"""
        
        records = self.db.query(EPFOData).filter(
            EPFOData.msme_id == msme_id
        ).order_by(EPFOData.reporting_month.desc()).limit(12).all()
        
        if not records:
            return {'status': 'no_data'}
        
        # Calculate metrics
        current_employees = records[0].total_employees
        previous_employees = records[1].total_employees if len(records) > 1 else current_employees
        
        employee_growth = ((current_employees - previous_employees) / previous_employees) * 100 if previous_employees > 0 else 0
        
        total_new = sum(r.new_employees for r in records)
        total_exited = sum(r.exited_employees for r in records)
        turnover_rate = (total_exited / current_employees) * 100 if current_employees > 0 else 0
        
        avg_salary = sum(float(r.average_salary) for r in records) / len(records)
        
        return {
            'current_employees': current_employees,
            'employee_growth': employee_growth,
            'turnover_rate': turnover_rate,
            'avg_salary': avg_salary,
            'total_contribution': sum(float(r.total_contribution) for r in records),
            'stability_score': self._calculate_stability_score(records)
        }
    
    def _calculate_stability_score(self, records: list) -> float:
        """Calculate employee stability score"""
        
        if not records:
            return 0
        
        # Factors: growth, turnover, salary consistency
        growth_score = min(100, max(0, 50 + records[0].total_employees - records[-1].total_employees) * 5)
        
        total_exited = sum(r.exited_employees for r in records)
        total_employees = sum(r.total_employees for r in records) / len(records)
        turnover_score = max(0, 100 - (total_exited / total_employees) * 100) if total_employees > 0 else 50
        
        return round((growth_score + turnover_score) / 2, 2)
```

### Bank Statement Service

```python
# app/services/bank_service.py
from typing import Dict, List
from datetime import datetime, date, timedelta
import random
from sqlalchemy.orm import Session
from app.models import BankStatement

class BankStatementService:
    def __init__(self, db: Session):
        self.db = db
    
    async def fetch_bank_data(
        self, 
        msme_id: str, 
        account_number: str,
        months: int = 6
    ) -> Dict:
        """Fetch bank statement data"""
        
        records = []
        current_balance = random.uniform(200000, 1000000)
        
        for i in range(months):
            month = 12 - i
            year = 2024 if month > 0 else 2023
            actual_month = month if month > 0 else 12
            
            # Simulate monthly data
            credits = random.uniform(200000, 1000000)
            debits = random.uniform(150000, 800000)
            
            opening_balance = current_balance
            closing_balance = opening_balance + credits - debits
            
            record = BankStatement(
                msme_id=msme_id,
                account_id=account_number,
                statement_month=f"{year}-{actual_month:02d}",
                opening_balance=opening_balance,
                closing_balance=closing_balance,
                total_credits=credits,
                total_debits=debits,
                average_balance=(opening_balance + closing_balance) / 2,
                min_balance=min(opening_balance, closing_balance) * 0.8,
                max_balance=max(opening_balance, closing_balance) * 1.2,
                transaction_count=random.randint(50, 200),
                bounced_checks=random.randint(0, 3),
                emi_deductions=random.randint(1, 5),
                salary_payments=random.randint(5, 30),
                data_source='bank_api'
            )
            
            records.append(record)
            current_balance = closing_balance
        
        self.db.bulk_save_objects(records)
        self.db.commit()
        
        return {
            'status': 'success',
            'records_fetched': len(records)
        }
    
    def get_bank_summary(self, msme_id: str) -> Dict:
        """Get bank statement summary"""
        
        records = self.db.query(BankStatement).filter(
            BankStatement.msme_id == msme_id
        ).order_by(BankStatement.statement_month.desc()).limit(6).all()
        
        if not records:
            return {'status': 'no_data'}
        
        total_credits = sum(float(r.total_credits) for r in records)
        total_debits = sum(float(r.total_debits) for r in records)
        avg_balance = sum(float(r.average_balance) for r in records) / len(records)
        
        return {
            'total_credits': total_credits,
            'total_debits': total_debits,
            'net_flow': total_credits - total_debits,
            'avg_balance': avg_balance,
            'current_balance': float(records[0].closing_balance),
            'bounced_checks': sum(r.bounced_checks for r in records),
            'cash_flow_score': self._calculate_cash_flow_score(records)
        }
    
    def _calculate_cash_flow_score(self, records: list) -> float:
        """Calculate cash flow score"""
        
        if not records:
            return 0
        
        # Factors: balance trend, credit-debit ratio, consistency
        balances = [float(r.closing_balance) for r in records]
        trend = (balances[0] - balances[-1]) / balances[-1] * 100 if balances[-1] > 0 else 0
        
        credits = sum(float(r.total_credits) for r in records)
        debits = sum(float(r.total_debits) for r in records)
        ratio = credits / debits if debits > 0 else 1
        
        score = min(100, max(0, 50 + trend * 2 + (ratio - 1) * 30))
        return round(score, 2)
```

### Utility Bills Service

```python
# app/services/utility_service.py
from typing import Dict, List
from datetime import datetime, date, timedelta
import random
from sqlalchemy.orm import Session
from app.models import UtilityBill

class UtilityService:
    def __init__(self, db: Session):
        self.db = db
    
    async def fetch_utility_data(
        self, 
        msme_id: str, 
        months: int = 12
    ) -> Dict:
        """Fetch utility bills data"""
        
        utilities = ['electricity', 'water', 'internet']
        records = []
        
        for utility in utilities:
            for i in range(months):
                month = 12 - i
                year = 2024 if month > 0 else 2023
                actual_month = month if month > 0 else 12
                
                amount = random.uniform(5000, 50000) if utility == 'electricity' else \
                         random.uniform(1000, 10000) if utility == 'water' else \
                         random.uniform(2000, 15000)
                
                due_date = date(year, actual_month, 15)
                payment_delay = random.randint(0, 10)
                payment_date = due_date + timedelta(days=payment_delay)
                
                record = UtilityBill(
                    msme_id=msme_id,
                    utility_type=utility,
                    provider=f"{utility.title()} Provider",
                    billing_month=f"{year}-{actual_month:02d}",
                    amount=amount,
                    due_date=due_date,
                    payment_date=payment_date if payment_delay < 10 else None,
                    payment_status='paid' if payment_delay < 10 else 'late',
                    payment_delay_days=payment_delay,
                    data_source='utility_api'
                )
                records.append(record)
        
        self.db.bulk_save_objects(records)
        self.db.commit()
        
        return {
            'status': 'success',
            'records_fetched': len(records)
        }
    
    def get_utility_summary(self, msme_id: str) -> Dict:
        """Get utility bills summary"""
        
        records = self.db.query(UtilityBill).filter(
            UtilityBill.msme_id == msme_id
        ).all()
        
        if not records:
            return {'status': 'no_data'}
        
        # Calculate metrics by utility type
        utility_summary = {}
        for utility in ['electricity', 'water', 'internet']:
            utility_records = [r for r in records if r.utility_type == utility]
            if utility_records:
                avg_amount = sum(float(r.amount) for r in utility_records) / len(utility_records)
                on_time_count = sum(1 for r in utility_records if r.payment_status == 'paid')
                on_time_rate = (on_time_count / len(utility_records)) * 100
                
                utility_summary[utility] = {
                    'avg_amount': avg_amount,
                    'on_time_rate': on_time_rate,
                    'total_bills': len(utility_records)
                }
        
        # Overall payment timeliness
        total_bills = len(records)
        paid_on_time = sum(1 for r in records if r.payment_status == 'paid')
        payment_score = (paid_on_time / total_bills) * 100 if total_bills > 0 else 0
        
        return {
            'utility_breakdown': utility_summary,
            'payment_score': payment_score,
            'total_bills': total_bills,
            'paid_on_time': paid_on_time
        }
```

---

## 3.3 DATA AGGREGATION ORCHESTRATOR

```python
# app/services/data_aggregator.py
from typing import Dict, List
from datetime import datetime
from sqlalchemy.orm import Session
from app.services.gst_service import GSTService
from app.services.upi_service import UPIService
from app.services.aa_service import AccountAggregatorService
from app.services.epfo_service import EPFOService
from app.services.bank_service import BankStatementService
from app.services.utility_service import UtilityService
from app.models import DataSyncLog

class DataAggregator:
    def __init__(self, db: Session):
        self.db = db
        self.gst_service = GSTService(db)
        self.upi_service = UPIService(db)
        self.aa_service = AccountAggregatorService(db)
        self.epfo_service = EPFOService(db)
        self.bank_service = BankStatementService(db)
        self.utility_service = UtilityService(db)
    
    async def aggregate_all_data(
        self, 
        msme_id: str, 
        profile: Dict
    ) -> Dict:
        """Aggregate all available data for MSME"""
        
        results = {}
        
        # GST Data
        if profile.get('gstin'):
            try:
                gst_result = await self.gst_service.fetch_gst_data(
                    msme_id, 
                    profile['gstin']
                )
                results['gst'] = gst_result
            except Exception as e:
                results['gst'] = {'status': 'error', 'error': str(e)}
        
        # UPI Data
        if profile.get('upi_vpa'):
            try:
                upi_result = await self.upi_service.fetch_upi_data(
                    msme_id, 
                    profile['upi_vpa']
                )
                results['upi'] = upi_result
            except Exception as e:
                results['upi'] = {'status': 'error', 'error': str(e)}
        
        # EPFO Data
        if profile.get('establishment_code'):
            try:
                epfo_result = await self.epfo_service.fetch_epfo_data(
                    msme_id, 
                    profile['establishment_code']
                )
                results['epfo'] = epfo_result
            except Exception as e:
                results['epfo'] = {'status': 'error', 'error': str(e)}
        
        # Bank Statement
        if profile.get('bank_account_number'):
            try:
                bank_result = await self.bank_service.fetch_bank_data(
                    msme_id, 
                    profile['bank_account_number']
                )
                results['bank'] = bank_result
            except Exception as e:
                results['bank'] = {'status': 'error', 'error': str(e)}
        
        # Utility Bills
        try:
            utility_result = await self.utility_service.fetch_utility_data(msme_id)
            results['utility'] = utility_result
        except Exception as e:
            results['utility'] = {'status': 'error', 'error': str(e)}
        
        # Log sync
        sync_log = DataSyncLog(
            msme_id=msme_id,
            data_source='all',
            sync_status='completed',
            records_fetched=sum(
                r.get('records_fetched', 0) 
                for r in results.values() 
                if isinstance(r, dict)
            ),
            started_at=datetime.utcnow(),
            completed_at=datetime.utcnow()
        )
        self.db.add(sync_log)
        self.db.commit()
        
        return results
    
    def get_all_summaries(self, msme_id: str) -> Dict:
        """Get summaries from all data sources"""
        
        return {
            'gst': self.gst_service.get_gst_summary(msme_id),
            'upi': self.upi_service.get_upi_summary(msme_id),
            'epfo': self.epfo_service.get_epfo_summary(msme_id),
            'bank': self.bank_service.get_bank_summary(msme_id),
            'utility': self.utility_service.get_utility_summary(msme_id)
        }
```

---

## 3.4 API ENDPOINTS

```python
# app/api/v1/data_sources.py
from fastapi import APIRouter, Depends, HTTPException
from app.core.deps import get_current_user, get_db
from app.services.data_aggregator import DataAggregator

router = APIRouter(prefix="/data", tags=["Data Sources"])

@router.post("/aggregate/{msme_id}")
async def aggregate_data(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    aggregator = DataAggregator(db)
    return await aggregator.aggregate_all_data(msme_id, {})

@router.get("/summary/{msme_id}")
async def get_data_summary(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    aggregator = DataAggregator(db)
    return aggregator.get_all_summaries(msme_id)

@router.post("/gst/{msme_id}")
async def fetch_gst_data(
    msme_id: str,
    gstin: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    from app.services.gst_service import GSTService
    service = GSTService(db)
    return await service.fetch_gst_data(msme_id, gstin)

@router.post("/upi/{msme_id}")
async def fetch_upi_data(
    msme_id: str,
    vpa: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    from app.services.upi_service import UPIService
    service = UPIService(db)
    return await service.fetch_upi_data(msme_id, vpa)

@router.post("/consent/initiate")
async def initiate_consent(
    msme_id: str,
    data_types: list,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    from app.services.aa_service import AccountAggregatorService
    service = AccountAggregatorService(db)
    return await service.initiate_consent(msme_id, data_types)

@router.post("/consent/{consent_id}/approve")
async def approve_consent(
    consent_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    from app.services.aa_service import AccountAggregatorService
    service = AccountAggregatorService(db)
    return await service.approve_consent(consent_id)

@router.post("/consent/{consent_id}/revoke")
async def revoke_consent(
    consent_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    from app.services.aa_service import AccountAggregatorService
    service = AccountAggregatorService(db)
    return await service.revoke_consent(consent_id)
```

---

## 3.5 UI DESIGN

```
┌─────────────────────────────────────────────────────────────────┐
│  DATA SOURCES                                                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  CONNECTED SOURCES                                       │    │
│  │                                                          │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐      │    │
│  │  │  GST    │ │   UPI   │ │  EPFO   │ │  Bank   │      │    │
│  │  │   ✅    │ │   ✅    │ │   ✅    │ │   ⏳    │      │    │
│  │  │ Synced  │ │ Synced  │ │ Synced  │ │Pending  │      │    │
│  │  │ 2 hrs   │ │ 1 hr    │ │ 3 hrs   │ │ -       │      │    │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘      │    │
│  │                                                          │    │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐      │    │
│  │  │Utility  │ │  AA     │ │  MCA    │ │ Invoice │      │    │
│  │  │   ✅    │ │   ⚠️    │ │   ⏳    │ │   ⏳    │      │    │
│  │  │ Synced  │ │Consent  │ │Not      │ │Not      │      │    │
│  │  │ 5 hrs   │ │Needed   │ │Connected│ │Connected│      │    │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘      │    │
│  │                                                          │    │
│  │  [Sync All Sources] [View Sync History]                 │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  CONSENT MANAGEMENT                                      │    │
│  │                                                          │    │
│  │  Active Consents:                                       │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │ GST Data - Granted 2 days ago - Expires in 88d │    │    │
│  │  │ UPI Data - Granted 5 days ago - Expires in 85d │    │    │
│  │  │ EPFO Data - Granted 1 day ago - Expires in 89d │    │    │
│  │  │                                                  │    │    │
│  │  │ [Revoke] [View Details]                          │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  │  [Request New Consent]                                  │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  DATA QUALITY                                           │    │
│  │                                                          │    │
│  │  GST: ████████████████████░░░░ 85% (12/14 months)      │    │
│  │  UPI: ████████████████████████ 95% (180 days)          │    │
│  │  EPFO: ██████████████████░░░░░ 75% (9/12 months)       │    │
│  │  Bank: ████████████░░░░░░░░░░ 55% (3/6 months)         │    │
│  │                                                          │    │
│  │  Overall Data Completeness: 78%                         │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3.6 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| Database Schema | 2 days |
| GST Service | 2 days |
| UPI Service | 2 days |
| AA Service | 3 days |
| EPFO Service | 1 day |
| Bank Statement Service | 2 days |
| Utility Service | 1 day |
| Data Aggregator | 2 days |
| API Endpoints | 2 days |
| Frontend | 3 days |
| Testing | 2 days |
| **Total** | **22 days** |

---

## 3.7 HACKATHON PRIORITY

**CRITICAL** - Foundation for all scoring and analysis
