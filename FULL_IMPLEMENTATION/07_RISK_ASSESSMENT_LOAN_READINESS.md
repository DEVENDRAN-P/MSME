# MODULE 7: RISK ASSESSMENT & LOAN READINESS
## Complete Implementation Guide

---

## 7.1 DATABASE SCHEMA

```sql
-- Risk Assessment
CREATE TABLE risk_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msME_profiles(id),
    score_id UUID REFERENCES financial_health_scores(id),
    
    -- Overall Risk
    overall_risk_score DECIMAL(5,2) NOT NULL,
    risk_category VARCHAR(20) NOT NULL,
    
    -- Risk Dimensions
    revenue_risk DECIMAL(5,2),
    cash_flow_risk DECIMAL(5,2),
    compliance_risk DECIMAL(5,2),
    liquidity_risk DECIMAL(5,2),
    employee_risk DECIMAL(5,2),
    transaction_risk DECIMAL(5,2),
    market_risk DECIMAL(5,2),
    operational_risk DECIMAL(5,2),
    
    -- Risk Factors
    risk_factors JSONB,
    mitigations JSONB,
    
    -- Stress Test Results
    stress_test_results JSONB,
    
    -- Timestamps
    assessed_at TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Loan Readiness Assessment
CREATE TABLE loan_readiness_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    score_id UUID REFERENCES financial_health_scores(id),
    risk_assessment_id UUID REFERENCES risk_assessments(id),
    
    -- Readiness Score
    readiness_score DECIMAL(5,2) NOT NULL,
    readiness_status VARCHAR(20) NOT NULL,
    
    -- Loan Details
    recommended_amount DECIMAL(15,2),
    max_amount DECIMAL(15,2),
    safe_amount DECIMAL(15,2),
    recommended_tenure INTEGER,
    recommended_interest_rate DECIMAL(5,2),
    
    -- Approval Probability
    approval_probability DECIMAL(5,2),
    
    -- Eligible Products
    eligible_products JSONB,
    
    -- Conditions
    conditions JSONB,
    
    -- Timestamps
    assessed_at TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Loan Applications
CREATE TABLE loan_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    readiness_id UUID REFERENCES loan_readiness_assessments(id),
    
    -- Loan Details
    loan_type VARCHAR(50) NOT NULL,
    loan_amount DECIMAL(15,2) NOT NULL,
    loan_purpose TEXT,
    tenure_months INTEGER,
    interest_rate DECIMAL(5,2),
    
    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'draft',
    submitted_at TIMESTAMPTZ,
    approved_at TIMESTAMPTZ,
    rejected_at TIMESTAMPTZ,
    disbursed_at TIMESTAMPTZ,
    
    -- Decision
    approved_amount DECIMAL(15,2),
    approved_interest_rate DECIMAL(5,2),
    rejection_reason TEXT,
    
    -- AI Recommendation
    ai_recommendation VARCHAR(20),
    ai_confidence DECIMAL(3,2),
    
    -- Officer Notes
    officer_notes TEXT,
    officer_id UUID REFERENCES users(id),
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Approval Queue
CREATE TABLE approval_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES loan_applications(id),
    priority VARCHAR(10) NOT NULL,
    assigned_to UUID REFERENCES users(id),
    assigned_at TIMESTAMPTZ,
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Decision History
CREATE TABLE decision_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES loan_applications(id),
    decision VARCHAR(20) NOT NULL,
    decision_maker UUID REFERENCES users(id),
    decision_reason TEXT,
    ai_recommendation VARCHAR(20),
    ai_confidence DECIMAL(3,2),
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

## 7.2 RISK ASSESSMENT SERVICE

```python
# app/services/risk_assessment.py
from typing import Dict, List
from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from app.models import RiskAssessment, FinancialHealthScore

class RiskAssessmentService:
    def __init__(self, db: Session):
        self.db = db
    
    async def assess_risk(
        self,
        msme_id: str,
        score_data: Dict,
        features: Dict
    ) -> Dict:
        """Comprehensive risk assessment"""
        
        # 1. Compute risk dimensions
        risk_dimensions = self._compute_risk_dimensions(score_data, features)
        
        # 2. Compute overall risk score
        overall_risk = self._compute_overall_risk(risk_dimensions)
        
        # 3. Determine risk category
        risk_category = self._determine_risk_category(overall_risk)
        
        # 4. Identify risk factors
        risk_factors = self._identify_risk_factors(risk_dimensions, features)
        
        # 5. Suggest mitigations
        mitigations = self._suggest_mitigations(risk_factors)
        
        # 6. Stress test
        stress_test = await self._run_stress_test(features, score_data)
        
        # 7. Save assessment
        assessment = RiskAssessment(
            msme_id=msme_id,
            score_id=score_data.get('score_id'),
            overall_risk_score=overall_risk,
            risk_category=risk_category,
            revenue_risk=risk_dimensions['revenue'],
            cash_flow_risk=risk_dimensions['cash_flow'],
            compliance_risk=risk_dimensions['compliance'],
            liquidity_risk=risk_dimensions['liquidity'],
            employee_risk=risk_dimensions['employee'],
            transaction_risk=risk_dimensions['transaction'],
            market_risk=risk_dimensions['market'],
            operational_risk=risk_dimensions['operational'],
            risk_factors=risk_factors,
            mitigations=mitigations,
            stress_test_results=stress_test,
            assessed_at=datetime.utcnow(),
            valid_until=datetime.utcnow() + timedelta(days=30)
        )
        
        self.db.add(assessment)
        self.db.commit()
        
        return {
            'assessment_id': str(assessment.id),
            'overall_risk': overall_risk,
            'risk_category': risk_category,
            'risk_dimensions': risk_dimensions,
            'risk_factors': risk_factors,
            'mitigations': mitigations,
            'stress_test': stress_test
        }
    
    def _compute_risk_dimensions(
        self, 
        score_data: Dict, 
        features: Dict
    ) -> Dict:
        """Compute risk for each dimension"""
        
        dimensions = score_data.get('dimension_scores', {})
        
        # Risk is inverse of health score
        risk_dimensions = {
            'revenue': 100 - dimensions.get('revenue', 50),
            'cash_flow': 100 - dimensions.get('cash_flow', 50),
            'compliance': 100 - dimensions.get('compliance', 50),
            'liquidity': 100 - dimensions.get('liquidity', 50),
            'employee': 100 - dimensions.get('employee_stability', 50),
            'transaction': 100 - dimensions.get('digital_transaction', 50),
            'market': self._compute_market_risk(features),
            'operational': self._compute_operational_risk(features)
        }
        
        return risk_dimensions
    
    def _compute_market_risk(self, features: Dict) -> float:
        """Compute market risk"""
        
        risk = 50  # Base risk
        
        # Revenue volatility
        revenue_growth = abs(features.get('revenue_growth_rate', 0))
        if revenue_growth > 30:
            risk += 20  # High volatility
        elif revenue_growth > 15:
            risk += 10
        
        # Customer concentration
        # (would need customer data)
        
        return min(100, risk)
    
    def _compute_operational_risk(self, features: Dict) -> float:
        """Compute operational risk"""
        
        risk = 50
        
        # Bounced checks indicate operational issues
        bounced = features.get('bounced_checks', 0)
        risk += bounced * 5
        
        # Employee turnover affects operations
        turnover = features.get('employee_turnover_rate', 0)
        if turnover > 30:
            risk += 15
        elif turnover > 20:
            risk += 10
        
        return min(100, risk)
    
    def _compute_overall_risk(self, risk_dimensions: Dict) -> float:
        """Compute overall risk score"""
        
        # Weighted average
        weights = {
            'revenue': 0.15,
            'cash_flow': 0.20,
            'compliance': 0.15,
            'liquidity': 0.15,
            'employee': 0.10,
            'transaction': 0.10,
            'market': 0.10,
            'operational': 0.05
        }
        
        overall = sum(
            risk_dimensions[dim] * weights.get(dim, 0.1)
            for dim in risk_dimensions
        )
        
        return round(overall, 2)
    
    def _determine_risk_category(self, risk_score: float) -> str:
        """Determine risk category"""
        
        if risk_score < 25:
            return 'Low'
        elif risk_score < 50:
            return 'Moderate'
        elif risk_score < 75:
            return 'High'
        else:
            return 'Critical'
    
    def _identify_risk_factors(
        self, 
        risk_dimensions: Dict, 
        features: Dict
    ) -> List[Dict]:
        """Identify key risk factors"""
        
        factors = []
        
        for dimension, risk_score in risk_dimensions.items():
            if risk_score > 50:
                factors.append({
                    'dimension': dimension,
                    'risk_score': risk_score,
                    'severity': 'High' if risk_score > 75 else 'Medium',
                    'description': self._get_risk_description(dimension, risk_score),
                    'impact': self._estimate_impact(dimension, risk_score)
                })
        
        return sorted(factors, key=lambda x: x['risk_score'], reverse=True)
    
    def _suggest_mitigations(self, risk_factors: List[Dict]) -> List[Dict]:
        """Suggest risk mitigations"""
        
        mitigations = []
        
        for factor in risk_factors:
            mitigation = self._get_mitigation_suggestion(factor)
            if mitigation:
                mitigations.append(mitigation)
        
        return mitigations
    
    async def _run_stress_test(
        self, 
        features: Dict, 
        score_data: Dict
    ) -> Dict:
        """Run stress test scenarios"""
        
        scenarios = [
            {
                'name': 'Revenue Drop 20%',
                'adjustment': {'revenue_growth_rate': -20},
                'impact': 'Moderate risk increase'
            },
            {
                'name': 'Interest Rate Hike 3%',
                'adjustment': {'interest_rate': 3},
                'impact': 'Low to moderate impact'
            },
            {
                'name': 'Customer Loss 30%',
                'adjustment': {'unique_customers': -0.3},
                'impact': 'High impact on revenue'
            },
            {
                'name': 'Supply Chain Disruption',
                'adjustment': {'costs': 0.25},
                'impact': 'High impact on margins'
            }
        ]
        
        results = []
        for scenario in scenarios:
            # Simulate impact
            impact_score = self._simulate_stress_impact(
                features, scenario['adjustment']
            )
            results.append({
                'scenario': scenario['name'],
                'impact_score': impact_score,
                'description': scenario['impact'],
                'default_probability': min(50, impact_score * 0.5)
            })
        
        return results
    
    def _get_risk_description(self, dimension: str, risk_score: float) -> str:
        """Get risk description"""
        
        descriptions = {
            'revenue': 'Revenue volatility or decline detected',
            'cash_flow': 'Cash flow management needs attention',
            'compliance': 'Compliance gaps present regulatory risk',
            'liquidity': 'Liquidity position is concerning',
            'employee': 'Employee turnover affecting stability',
            'transaction': 'Digital transaction adoption is low',
            'market': 'Market conditions pose risks',
            'operational': 'Operational inefficiencies detected'
        }
        
        return descriptions.get(dimension, 'Risk identified in this area')
    
    def _estimate_impact(self, dimension: str, risk_score: float) -> str:
        """Estimate impact of risk"""
        
        if risk_score > 75:
            return 'Could lead to significant financial stress'
        elif risk_score > 50:
            return 'May affect business operations'
        else:
            return 'Manageable with proper attention'
    
    def _get_mitigation_suggestion(self, factor: Dict) -> Dict:
        """Get mitigation suggestion for risk factor"""
        
        mitigations_map = {
            'revenue': {
                'action': 'Diversify revenue streams',
                'timeline': '6-12 months',
                'priority': 'High'
            },
            'cash_flow': {
                'action': 'Implement better cash flow management',
                'timeline': '3-6 months',
                'priority': 'High'
            },
            'compliance': {
                'action': 'Ensure timely regulatory filings',
                'timeline': 'Immediate',
                'priority': 'Critical'
            },
            'liquidity': {
                'action': 'Build cash reserves',
                'timeline': '3-6 months',
                'priority': 'Medium'
            },
            'employee': {
                'action': 'Improve employee retention programs',
                'timeline': '6-12 months',
                'priority': 'Medium'
            },
            'transaction': {
                'action': 'Increase digital payment adoption',
                'timeline': '1-3 months',
                'priority': 'Medium'
            }
        }
        
        mitigation = mitigations_map.get(factor['dimension'])
        if mitigation:
            mitigation['dimension'] = factor['dimension']
            mitigation['risk_score'] = factor['risk_score']
        
        return mitigation
    
    def _simulate_stress_impact(
        self, 
        features: Dict, 
        adjustment: Dict
    ) -> float:
        """Simulate stress test impact"""
        
        # Simplified impact calculation
        base_impact = 30
        
        for key, value in adjustment.items():
            if key == 'revenue_growth_rate':
                base_impact += abs(value) * 0.5
            elif key == 'interest_rate':
                base_impact += value * 3
            elif key == 'unique_customers':
                base_impact += abs(value) * 20
        
        return min(100, base_impact)
```

---

## 7.3 LOAN READINESS SERVICE

```python
# app/services/loan_readiness.py
from typing import Dict, List
from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from app.models import LoanReadinessAssessment, LoanApplication

class LoanReadinessService:
    def __init__(self, db: Session):
        self.db = db
    
    async def assess_loan_readiness(
        self,
        msme_id: str,
        score_data: Dict,
        risk_data: Dict,
        profile: Dict
    ) -> Dict:
        """Assess loan readiness"""
        
        # 1. Compute readiness score
        readiness_score = self._compute_readiness_score(
            score_data, risk_data, profile
        )
        
        # 2. Determine status
        status = self._determine_status(readiness_score)
        
        # 3. Calculate loan amounts
        loan_amounts = self._calculate_loan_amounts(
            readiness_score, profile, score_data
        )
        
        # 4. Determine products
        eligible_products = self._determine_eligible_products(
            readiness_score, profile
        )
        
        # 5. Calculate approval probability
        approval_probability = self._calculate_approval_probability(
            readiness_score, risk_data
        )
        
        # 6. Determine conditions
        conditions = self._determine_conditions(
            readiness_score, risk_data
        )
        
        # 7. Save assessment
        assessment = LoanReadinessAssessment(
            msme_id=msme_id,
            score_id=score_data.get('score_id'),
            risk_assessment_id=risk_data.get('assessment_id'),
            readiness_score=readiness_score,
            readiness_status=status,
            recommended_amount=loan_amounts['recommended'],
            max_amount=loan_amounts['max'],
            safe_amount=loan_amounts['safe'],
            recommended_tenure=loan_amounts['tenure'],
            recommended_interest_rate=loan_amounts['interest_rate'],
            approval_probability=approval_probability,
            eligible_products=eligible_products,
            conditions=conditions,
            assessed_at=datetime.utcnow(),
            valid_until=datetime.utcnow() + timedelta(days=30)
        )
        
        self.db.add(assessment)
        self.db.commit()
        
        return {
            'assessment_id': str(assessment.id),
            'readiness_score': readiness_score,
            'status': status,
            'loan_amounts': loan_amounts,
            'eligible_products': eligible_products,
            'approval_probability': approval_probability,
            'conditions': conditions
        }
    
    def _compute_readiness_score(
        self,
        score_data: Dict,
        risk_data: Dict,
        profile: Dict
    ) -> float:
        """Compute loan readiness score"""
        
        # Base on health score
        health_score = score_data.get('overall_score', 50)
        
        # Adjust for risk
        risk_score = risk_data.get('overall_risk', 50)
        risk_adjustment = risk_score * 0.3
        
        # Adjust for profile completeness
        profile_completeness = self._compute_profile_completeness(profile)
        profile_adjustment = (profile_completeness - 50) * 0.2
        
        readiness = health_score - risk_adjustment + profile_adjustment
        
        return max(0, min(100, round(readiness, 2)))
    
    def _determine_status(self, readiness_score: float) -> str:
        """Determine readiness status"""
        
        if readiness_score >= 80:
            return 'Highly Ready'
        elif readiness_score >= 60:
            return 'Ready'
        elif readiness_score >= 40:
            return 'Partially Ready'
        else:
            return 'Not Ready'
    
    def _calculate_loan_amounts(
        self,
        readiness_score: float,
        profile: Dict,
        score_data: Dict
    ) -> Dict:
        """Calculate recommended loan amounts"""
        
        # Based on turnover
        annual_turnover = profile.get('annual_turnover', 0)
        
        # Maximum amount (typically 20-30% of turnover)
        max_amount = annual_turnover * 0.25
        
        # Recommended amount (based on readiness)
        recommended = max_amount * (readiness_score / 100)
        
        # Safe amount (conservative estimate)
        safe = recommended * 0.7
        
        # Interest rate (higher risk = higher rate)
        base_rate = 10.5
        risk_premium = (100 - readiness_score) * 0.05
        interest_rate = base_rate + risk_premium
        
        # Tenure
        tenure = 36 if readiness_score >= 70 else 24
        
        return {
            'max': round(max_amount / 100000) * 100000,
            'recommended': round(recommended / 100000) * 100000,
            'safe': round(safe / 100000) * 100000,
            'tenure': tenure,
            'interest_rate': round(interest_rate, 2)
        }
    
    def _determine_eligible_products(
        self,
        readiness_score: float,
        profile: Dict
    ) -> List[Dict]:
        """Determine eligible loan products"""
        
        products = []
        
        if readiness_score >= 40:
            products.append({
                'name': 'Working Capital Loan',
                'description': 'Short-term financing for operational needs',
                'amount_range': '₹5-25 Lakhs',
                'tenure': 'Up to 12 months',
                'interest_range': '9.5-11.5%'
            })
        
        if readiness_score >= 50:
            products.append({
                'name': 'Overdraft Facility',
                'description': 'Flexible credit line for cash flow management',
                'amount_range': '₹5-20 Lakhs',
                'tenure': '12 months (renewable)',
                'interest_range': '10-12%'
            })
        
        if readiness_score >= 60:
            products.append({
                'name': 'Term Loan',
                'description': 'Medium-term financing for expansion',
                'amount_range': '₹10-50 Lakhs',
                'tenure': '12-60 months',
                'interest_range': '10.5-12.5%'
            })
        
        if readiness_score >= 70:
            products.append({
                'name': 'Equipment Finance',
                'description': 'Financing for machinery and equipment',
                'amount_range': '₹5-30 Lakhs',
                'tenure': '24-60 months',
                'interest_range': '11-13%'
            })
        
        if readiness_score >= 80:
            products.append({
                'name': 'Business Expansion Loan',
                'description': 'Long-term financing for growth',
                'amount_range': '₹25-100 Lakhs',
                'tenure': '36-84 months',
                'interest_range': '10-12%'
            })
        
        return products
    
    def _calculate_approval_probability(
        self,
        readiness_score: float,
        risk_data: Dict
    ) -> float:
        """Calculate approval probability"""
        
        # Base probability from readiness
        base_prob = readiness_score
        
        # Adjust for risk
        risk_score = risk_data.get('overall_risk', 50)
        risk_adjustment = risk_score * 0.2
        
        probability = base_prob - risk_adjustment
        
        return max(0, min(100, round(probability, 2)))
    
    def _determine_conditions(
        self,
        readiness_score: float,
        risk_data: Dict
    ) -> List[Dict]:
        """Determine loan conditions"""
        
        conditions = []
        
        if readiness_score < 70:
            conditions.append({
                'type': 'collateral',
                'description': 'Collateral may be required',
                'mandatory': True
            })
        
        if readiness_score < 60:
            conditions.append({
                'type': 'guarantor',
                'description': 'Personal guarantee may be required',
                'mandatory': True
            })
        
        if risk_data.get('overall_risk', 0) > 50:
            conditions.append({
                'type': 'monitoring',
                'description': 'Monthly financial reporting required',
                'mandatory': True
            })
        
        conditions.append({
            'type': 'documentation',
            'description': 'Standard documentation required',
            'mandatory': True
        })
        
        return conditions
    
    def _compute_profile_completeness(self, profile: Dict) -> float:
        """Compute profile completeness"""
        
        required_fields = [
            'business_name', 'pan_number', 'gstin',
            'address_line1', 'city', 'state',
            'annual_turnover', 'bank_account_number'
        ]
        
        present = sum(1 for f in required_fields if profile.get(f))
        
        return (present / len(required_fields)) * 100
    
    async def apply_for_loan(
        self,
        msme_id: str,
        loan_details: Dict
    ) -> Dict:
        """Submit loan application"""
        
        # Get latest readiness assessment
        assessment = self.db.query(LoanReadinessAssessment).filter(
            LoanReadinessAssessment.msme_id == msme_id
        ).order_by(LoanReadinessAssessment.assessed_at.desc()).first()
        
        if not assessment:
            raise ValueError("No loan readiness assessment found")
        
        # Create application
        application = LoanApplication(
            msme_id=msme_id,
            readiness_id=str(assessment.id),
            loan_type=loan_details['loan_type'],
            loan_amount=loan_details['amount'],
            loan_purpose=loan_details.get('purpose'),
            tenure_months=loan_details.get('tenure', 36),
            interest_rate=assessment.recommended_interest_rate,
            status='submitted',
            submitted_at=datetime.utcnow(),
            ai_recommendation=self._get_ai_recommendation(assessment),
            ai_confidence=assessment.approval_probability / 100
        )
        
        self.db.add(application)
        
        # Add to approval queue
        from app.models import ApprovalQueue
        queue_item = ApprovalQueue(
            application_id=str(application.id),
            priority=self._determine_priority(assessment),
            status='pending'
        )
        self.db.add(queue_item)
        
        self.db.commit()
        
        return {
            'application_id': str(application.id),
            'status': 'submitted',
            'ai_recommendation': application.ai_recommendation,
            'estimated_decision_time': '2-3 business days'
        }
    
    def _get_ai_recommendation(self, assessment: LoanReadinessAssessment) -> str:
        """Get AI recommendation"""
        
        if assessment.approval_probability >= 75:
            return 'Approve'
        elif assessment.approval_probability >= 50:
            return 'Review'
        else:
            return 'Reject'
    
    def _determine_priority(self, assessment: LoanReadinessAssessment) -> str:
        """Determine queue priority"""
        
        if assessment.readiness_score >= 80:
            return 'high'
        elif assessment.readiness_score >= 60:
            return 'medium'
        else:
            return 'low'
```

---

## 7.4 API ENDPOINTS

```python
# app/api/v1/risk_loan.py
from fastapi import APIRouter, Depends, HTTPException
from app.core.deps import get_current_user, get_db
from app.services.risk_assessment import RiskAssessmentService
from app.services.loan_readiness import LoanReadinessService

router = APIRouter(prefix="/risk-loan", tags=["Risk & Loan"])

@router.post("/assess-risk/{msme_id}")
async def assess_risk(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = RiskAssessmentService(db)
    
    # Get score data
    from app.models import FinancialHealthScore
    score = db.query(FinancialHealthScore).filter(
        FinancialHealthScore.msme_id == msme_id
    ).order_by(FinancialHealthScore.computed_at.desc()).first()
    
    if not score:
        raise HTTPException(status_code=404, detail="No score found")
    
    # Get features
    from app.models import FeatureStore
    features = {
        f.feature_name: f.feature_value 
        for f in db.query(FeatureStore).filter(
            FeatureStore.msme_id == msme_id
        ).all()
    }
    
    score_data = {
        'score_id': str(score.id),
        'overall_score': float(score.overall_score),
        'dimension_scores': {
            'cash_flow': float(score.cash_flow_score),
            'revenue': float(score.revenue_score),
            'compliance': float(score.compliance_score),
            'liquidity': float(score.liquidity_score),
            'payment_discipline': float(score.payment_discipline_score),
            'employee_stability': float(score.employee_stability_score),
            'business_stability': float(score.business_stability_score),
            'digital_transaction': float(score.digital_transaction_score),
            'working_capital': float(score.working_capital_score)
        }
    }
    
    return await service.assess_risk(msme_id, score_data, features)

@router.post("/assess-readiness/{msme_id}")
async def assess_loan_readiness(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    risk_service = RiskAssessmentService(db)
    readiness_service = LoanReadinessService(db)
    
    # Get risk assessment
    from app.models import RiskAssessment
    risk = db.query(RiskAssessment).filter(
        RiskAssessment.msme_id == msme_id
    ).order_by(RiskAssessment.assessed_at.desc()).first()
    
    if not risk:
        raise HTTPException(status_code=404, detail="No risk assessment found")
    
    risk_data = {
        'assessment_id': str(risk.id),
        'overall_risk': float(risk.overall_risk_score)
    }
    
    # Get score data
    from app.models import FinancialHealthScore
    score = db.query(FinancialHealthScore).filter(
        FinancialHealthScore.msme_id == msme_id
    ).order_by(FinancialHealthScore.computed_at.desc()).first()
    
    score_data = {
        'score_id': str(score.id) if score else None,
        'overall_score': float(score.overall_score) if score else 50
    }
    
    # Get profile
    from app.models import MSMEProfile
    profile = db.query(MSMEProfile).filter(
        MSMEProfile.id == msme_id
    ).first()
    
    profile_data = {
        'annual_turnover': float(profile.annual_turnover) if profile.annual_turnover else 0,
        'business_name': profile.business_name,
        'pan_number': profile.pan_number
    }
    
    return await readiness_service.assess_loan_readiness(
        msme_id, score_data, risk_data, profile_data
    )

@router.post("/apply/{msme_id}")
async def apply_for_loan(
    msme_id: str,
    loan_details: dict,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = LoanReadinessService(db)
    return await service.apply_for_loan(msme_id, loan_details)

@router.get("/applications/{msme_id}")
async def get_applications(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    from app.models import LoanApplication
    
    applications = db.query(LoanApplication).filter(
        LoanApplication.msme_id == msme_id
    ).order_by(LoanApplication.created_at.desc()).all()
    
    return [
        {
            'id': str(app.id),
            'loan_type': app.loan_type,
            'amount': float(app.loan_amount),
            'status': app.status,
            'ai_recommendation': app.ai_recommendation,
            'submitted_at': app.submitted_at.isoformat() if app.submitted_at else None
        }
        for app in applications
    ]
```

---

## 7.5 UI DESIGN

```
┌─────────────────────────────────────────────────────────────────┐
│  RISK ASSESSMENT & LOAN READINESS                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  RISK OVERVIEW                                          │    │
│  │                                                          │    │
│  │  Overall Risk: 35/100  |  Category: Low-Moderate       │    │
│  │                                                          │    │
│  │  Risk Dimensions:                                       │    │
│  │  Revenue Risk:        25  ██████░░░░░░░░░░░░░░         │    │
│  │  Cash Flow Risk:      35  █████████░░░░░░░░░░░         │    │
│  │  Compliance Risk:     10  ███░░░░░░░░░░░░░░░░░         │    │
│  │  Liquidity Risk:      45  ███████████░░░░░░░░░         │    │
│  │  Employee Risk:       20  █████░░░░░░░░░░░░░░░         │    │
│  │  Transaction Risk:    55  █████████████░░░░░░░         │    │
│  │  Market Risk:         40  ██████████░░░░░░░░░░         │    │
│  │  Operational Risk:    30  ███████░░░░░░░░░░░░░         │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  RISK FACTORS                                           │    │
│  │                                                          │    │
│  │  ⚠️  Transaction Risk (55)                              │    │
│  │      Low digital payment adoption                       │    │
│  │      Mitigation: Increase UPI/digital transactions      │    │
│  │                                                          │    │
│  │  ⚠️  Liquidity Risk (45)                                │    │
│  │      Working capital could be improved                  │    │
│  │      Mitigation: Maintain higher cash reserves          │    │
│  │                                                          │    │
│  │  ⚠️  Market Risk (40)                                   │    │
│  │      Revenue volatility detected                        │    │
│  │      Mitigation: Diversify customer base                │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  STRESS TEST RESULTS                                    │    │
│  │                                                          │    │
│  │  Scenario              Impact    Default Probability    │    │
│  │  ─────────────────────────────────────────────────────  │    │
│  │  Revenue Drop 20%      45/100    12%                    │    │
│  │  Interest Rate +3%     35/100    8%                     │    │
│  │  Customer Loss 30%     60/100    18%                    │    │
│  │  Supply Chain Issue    55/100    15%                    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  LOAN READINESS                                         │    │
│  │                                                          │    │
│  │  Readiness Score: 75%  |  Status: Ready                │    │
│  │                                                          │    │
│  │  Recommended Amount: ₹25,00,000                        │    │
│  │  Maximum Amount: ₹40,00,000                            │    │
│  │  Safe Amount: ₹17,50,000                               │    │
│  │                                                          │    │
│  │  Approval Probability: 72%                              │    │
│  │  Recommended Interest: 11.5%                            │    │
│  │  Recommended Tenure: 36 months                          │    │
│  │                                                          │    │
│  │  [Apply for Loan]                                       │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  ELIGIBLE PRODUCTS                                      │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  Working Capital Loan                            │    │    │
│  │  │  Amount: ₹5-25 Lakhs                            │    │    │
│  │  │  Tenure: Up to 12 months                        │    │    │
│  │  │  Interest: 9.5-11.5%                            │    │    │
│  │  │  [Apply] [Details]                               │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  Term Loan                                       │    │    │
│  │  │  Amount: ₹10-50 Lakhs                           │    │    │
│  │  │  Tenure: 12-60 months                           │    │    │
│  │  │  Interest: 10.5-12.5%                           │    │    │
│  │  │  [Apply] [Details]                               │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  Equipment Finance                              │    │    │
│  │  │  Amount: ₹5-30 Lakhs                            │    │    │
│  │  │  Tenure: 24-60 months                           │    │    │
│  │  │  Interest: 11-13%                                │    │    │
│  │  │  [Apply] [Details]                               │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7.6 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| Database Schema | 1 day |
| Risk Assessment Service | 3 days |
| Loan Readiness Service | 3 days |
| Loan Application Flow | 2 days |
| API Endpoints | 2 days |
| Frontend Components | 4 days |
| Testing | 2 days |
| **Total** | **17 days** |

---

## 7.7 HACKATHON PRIORITY

**HIGH** - Important for complete lending workflow
