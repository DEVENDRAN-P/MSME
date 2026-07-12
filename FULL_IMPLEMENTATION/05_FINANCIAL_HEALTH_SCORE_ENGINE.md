# MODULE 5: FINANCIAL HEALTH SCORE ENGINE
## Complete Implementation Guide

---

## 5.1 DATABASE SCHEMA

```sql
-- Financial Health Scores
CREATE TABLE financial_health_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    
    -- Overall Score
    overall_score DECIMAL(5,2) NOT NULL,
    overall_grade VARCHAR(5) NOT NULL,
    
    -- Dimension Scores
    cash_flow_score DECIMAL(5,2) NOT NULL,
    revenue_score DECIMAL(5,2) NOT NULL,
    compliance_score DECIMAL(5,2) NOT NULL,
    liquidity_score DECIMAL(5,2) NOT NULL,
    payment_discipline_score DECIMAL(5,2) NOT NULL,
    employee_stability_score DECIMAL(5,2) NOT NULL,
    business_stability_score DECIMAL(5,2) NOT NULL,
    digital_transaction_score DECIMAL(5,2) NOT NULL,
    working_capital_score DECIMAL(5,2) NOT NULL,
    
    -- Dynamic Weights
    weights_used JSONB NOT NULL,
    
    -- Confidence & Metadata
    confidence_score DECIMAL(3,2) NOT NULL,
    data_completeness DECIMAL(3,2),
    model_version VARCHAR(20),
    
    -- Risk Classification
    risk_category VARCHAR(20) NOT NULL,
    risk_score DECIMAL(5,2) NOT NULL,
    
    -- Timestamps
    computed_at TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_health_scores_msme ON financial_health_scores(msme_id, computed_at DESC);

-- Score History (Time Series)
CREATE TABLE score_history (
    time TIMESTAMPTZ NOT NULL,
    msme_id UUID NOT NULL,
    overall_score DECIMAL(5,2),
    cash_flow_score DECIMAL(5,2),
    revenue_score DECIMAL(5,2),
    compliance_score DECIMAL(5,2),
    liquidity_score DECIMAL(5,2),
    payment_discipline_score DECIMAL(5,2),
    employee_stability_score DECIMAL(5,2),
    business_stability_score DECIMAL(5,2),
    digital_transaction_score DECIMAL(5,2),
    working_capital_score DECIMAL(5,2)
);
SELECT create_hypertable('score_history', 'time');

-- Score Explanations
CREATE TABLE score_explanations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    score_id UUID NOT NULL REFERENCES financial_health_scores(id),
    dimension VARCHAR(50) NOT NULL,
    shap_values JSONB,
    positive_factors JSONB,
    negative_factors JSONB,
    explanation_text TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Industry Benchmarks
CREATE TABLE industry_benchmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    industry VARCHAR(100) NOT NULL,
    subcategory VARCHAR(100),
    overall_score_avg DECIMAL(5,2),
    overall_score_p25 DECIMAL(5,2),
    overall_score_p75 DECIMAL(5,2),
    cash_flow_avg DECIMAL(5,2),
    revenue_avg DECIMAL(5,2),
    compliance_avg DECIMAL(5,2),
    liquidity_avg DECIMAL(5,2),
    payment_discipline_avg DECIMAL(5,2),
    employee_stability_avg DECIMAL(5,2),
    business_stability_avg DECIMAL(5,2),
    digital_transaction_avg DECIMAL(5,2),
    working_capital_avg DECIMAL(5,2),
    sample_size INTEGER,
    period VARCHAR(10),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Dynamic Weight Configurations
CREATE TABLE weight_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    industry VARCHAR(100) NOT NULL,
    business_age_min INTEGER,
    business_age_max INTEGER,
    data_richness VARCHAR(20),
    weights JSONB NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Scoring Model Metadata
CREATE TABLE scoring_models (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    model_name VARCHAR(100) NOT NULL,
    version VARCHAR(20) NOT NULL,
    algorithm VARCHAR(50) NOT NULL,
    hyperparameters JSONB,
    training_metrics JSONB,
    feature_importance JSONB,
    training_date TIMESTAMPTZ,
    status VARCHAR(20) DEFAULT 'staging',
    deployed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

## 5.2 SCORING ENGINE

```python
# app/services/scoring_engine.py
from typing import Dict, List, Optional
from datetime import datetime, timedelta
import numpy as np
from sqlalchemy.orm import Session
from app.models import (
    FinancialHealthScore, ScoreHistory, ScoreExplanation,
    IndustryBenchmark, WeightConfiguration
)

class FinancialHealthScoringEngine:
    def __init__(self, db: Session):
        self.db = db
    
    async def compute_health_score(
        self, 
        msme_id: str,
        features: Dict,
        profile: Dict
    ) -> Dict:
        """Compute complete financial health score"""
        
        # 1. Get dynamic weights based on industry and profile
        weights = self._get_dynamic_weights(profile)
        
        # 2. Compute dimension scores
        dimension_scores = self._compute_dimension_scores(features)
        
        # 3. Compute overall score
        overall_score = self._compute_overall_score(dimension_scores, weights)
        
        # 4. Determine grade and risk category
        grade = self._determine_grade(overall_score)
        risk_category = self._determine_risk_category(overall_score)
        risk_score = self._compute_risk_score(dimension_scores)
        
        # 5. Compute confidence score
        confidence = self._compute_confidence(features, profile)
        
        # 6. Compute data completeness
        data_completeness = self._compute_data_completeness(features)
        
        # 7. Save score
        score_record = FinancialHealthScore(
            msme_id=msme_id,
            overall_score=overall_score,
            overall_grade=grade,
            cash_flow_score=dimension_scores['cash_flow'],
            revenue_score=dimension_scores['revenue'],
            compliance_score=dimension_scores['compliance'],
            liquidity_score=dimension_scores['liquidity'],
            payment_discipline_score=dimension_scores['payment_discipline'],
            employee_stability_score=dimension_scores['employee_stability'],
            business_stability_score=dimension_scores['business_stability'],
            digital_transaction_score=dimension_scores['digital_transaction'],
            working_capital_score=dimension_scores['working_capital'],
            weights_used=weights,
            confidence_score=confidence,
            data_completeness=data_completeness,
            model_version='1.0',
            risk_category=risk_category,
            risk_score=risk_score,
            computed_at=datetime.utcnow(),
            valid_until=datetime.utcnow() + timedelta(days=7)
        )
        
        self.db.add(score_record)
        
        # 8. Save to time series
        history = ScoreHistory(
            time=datetime.utcnow(),
            msme_id=msme_id,
            overall_score=overall_score,
            cash_flow_score=dimension_scores['cash_flow'],
            revenue_score=dimension_scores['revenue'],
            compliance_score=dimension_scores['compliance'],
            liquidity_score=dimension_scores['liquidity'],
            payment_discipline_score=dimension_scores['payment_discipline'],
            employee_stability_score=dimension_scores['employee_stability'],
            business_stability_score=dimension_scores['business_stability'],
            digital_transaction_score=dimension_scores['digital_transaction'],
            working_capital_score=dimension_scores['working_capital']
        )
        self.db.add(history)
        
        self.db.commit()
        
        return {
            'overall_score': overall_score,
            'grade': grade,
            'dimension_scores': dimension_scores,
            'weights': weights,
            'risk_category': risk_category,
            'risk_score': risk_score,
            'confidence': confidence,
            'data_completeness': data_completeness,
            'computed_at': datetime.utcnow().isoformat()
        }
    
    def _compute_dimension_scores(self, features: Dict) -> Dict:
        """Compute scores for each dimension"""
        
        scores = {}
        
        # Cash Flow Score
        scores['cash_flow'] = self._compute_cash_flow_score(features)
        
        # Revenue Score
        scores['revenue'] = self._compute_revenue_score(features)
        
        # Compliance Score
        scores['compliance'] = self._compute_compliance_score(features)
        
        # Liquidity Score
        scores['liquidity'] = self._compute_liquidity_score(features)
        
        # Payment Discipline Score
        scores['payment_discipline'] = self._compute_payment_discipline_score(features)
        
        # Employee Stability Score
        scores['employee_stability'] = self._compute_employee_stability_score(features)
        
        # Business Stability Score
        scores['business_stability'] = self._compute_business_stability_score(features)
        
        # Digital Transaction Score
        scores['digital_transaction'] = self._compute_digital_transaction_score(features)
        
        # Working Capital Score
        scores['working_capital'] = self._compute_working_capital_score(features)
        
        return scores
    
    def _compute_cash_flow_score(self, features: Dict) -> float:
        """Compute cash flow health score"""
        
        score = 50  # Base score
        
        # Net cash flow
        net_flow = features.get('net_cash_flow', 0)
        if net_flow > 0:
            score += min(20, net_flow / 10000)
        else:
            score += max(-20, net_flow / 10000)
        
        # Credit-debit ratio
        ratio = features.get('credit_debit_ratio', 1)
        if ratio > 1.2:
            score += 15
        elif ratio > 1:
            score += 10
        elif ratio > 0.8:
            score += 5
        else:
            score -= 10
        
        # Cash flow consistency (from bank data)
        cash_flow_score = features.get('cash_flow_score', 50)
        score = (score + cash_flow_score) / 2
        
        return self._normalize_score(score)
    
    def _compute_revenue_score(self, features: Dict) -> float:
        """Compute revenue health score"""
        
        score = 50
        
        # Revenue growth rate
        growth = features.get('revenue_growth_rate', 0)
        if growth > 20:
            score += 25
        elif growth > 10:
            score += 20
        elif growth > 5:
            score += 15
        elif growth > 0:
            score += 10
        elif growth > -5:
            score += 0
        else:
            score -= 15
        
        # Monthly revenue amount
        monthly_revenue = features.get('monthly_revenue', 0)
        if monthly_revenue > 1000000:
            score += 15
        elif monthly_revenue > 500000:
            score += 10
        elif monthly_revenue > 100000:
            score += 5
        
        return self._normalize_score(score)
    
    def _compute_compliance_score(self, features: Dict) -> float:
        """Compute compliance score"""
        
        score = 50
        
        # GST compliance
        gst_compliance = features.get('gst_compliance_ratio', 0)
        score += (gst_compliance - 50) * 0.3
        
        # Filing regularity
        filing = features.get('filing_regularity', 0)
        score += (filing - 50) * 0.3
        
        # Utility payment score
        utility_score = features.get('utility_payment_score', 50)
        score += (utility_score - 50) * 0.2
        
        # Overall compliance
        overall = features.get('overall_compliance_score', 50)
        score += (overall - 50) * 0.2
        
        return self._normalize_score(score)
    
    def _compute_liquidity_score(self, features: Dict) -> float:
        """Compute liquidity score"""
        
        score = 50
        
        # Working capital months
        wc_months = features.get('working_capital_months', 0)
        if wc_months > 6:
            score += 25
        elif wc_months > 3:
            score += 20
        elif wc_months > 1:
            score += 10
        else:
            score -= 10
        
        # Liquidity ratio
        liquidity = features.get('liquidity_ratio', 1)
        if liquidity > 2:
            score += 20
        elif liquidity > 1.5:
            score += 15
        elif liquidity > 1:
            score += 10
        else:
            score -= 10
        
        # Average balance
        avg_balance = features.get('avg_monthly_balance', 0)
        if avg_balance > 500000:
            score += 10
        elif avg_balance > 200000:
            score += 5
        
        return self._normalize_score(score)
    
    def _compute_payment_discipline_score(self, features: Dict) -> float:
        """Compute payment discipline score"""
        
        score = 50
        
        # On-time payment rate
        on_time = features.get('on_time_payment_rate', 50)
        score += (on_time - 50) * 0.4
        
        # Utility payment score
        utility = features.get('utility_payment_score', 50)
        score += (utility - 50) * 0.3
        
        # Bounced checks (negative indicator)
        bounced = features.get('bounced_checks', 0)
        score -= bounced * 5
        
        return self._normalize_score(score)
    
    def _compute_employee_stability_score(self, features: Dict) -> float:
        """Compute employee stability score"""
        
        score = 50
        
        # Employee growth
        growth = features.get('employee_growth_rate', 0)
        if growth > 10:
            score += 20
        elif growth > 5:
            score += 15
        elif growth > 0:
            score += 10
        elif growth > -5:
            score += 0
        else:
            score -= 15
        
        # Turnover rate (negative)
        turnover = features.get('employee_turnover_rate', 0)
        if turnover < 10:
            score += 15
        elif turnover < 20:
            score += 10
        elif turnover < 30:
            score += 0
        else:
            score -= 10
        
        # Stability score from EPFO
        stability = features.get('employee_stability_score', 50)
        score = (score + stability) / 2
        
        return self._normalize_score(score)
    
    def _compute_business_stability_score(self, features: Dict) -> float:
        """Compute business stability score"""
        
        score = 50
        
        # Years in business (from profile)
        # This would come from profile data
        
        # Revenue consistency
        revenue_growth = features.get('revenue_growth_rate', 0)
        if abs(revenue_growth) < 10:
            score += 20  # Stable
        elif abs(revenue_growth) < 20:
            score += 10
        else:
            score -= 10  # Volatile
        
        # Employee stability contribution
        employee_stability = features.get('employee_stability_score', 50)
        score += (employee_stability - 50) * 0.3
        
        return self._normalize_score(score)
    
    def _compute_digital_transaction_score(self, features: Dict) -> float:
        """Compute digital transaction adoption score"""
        
        score = 50
        
        # Digital adoption score
        adoption = features.get('digital_adoption_score', 0)
        score += adoption * 0.3
        
        # Digital transaction ratio
        ratio = features.get('digital_transaction_ratio', 0)
        if ratio > 50:
            score += 25
        elif ratio > 30:
            score += 20
        elif ratio > 10:
            score += 10
        
        return self._normalize_score(score)
    
    def _compute_working_capital_score(self, features: Dict) -> float:
        """Compute working capital score"""
        
        score = 50
        
        # Working capital months
        wc_months = features.get('working_capital_months', 0)
        if wc_months > 6:
            score += 30
        elif wc_months > 3:
            score += 20
        elif wc_months > 1:
            score += 10
        else:
            score -= 15
        
        # Liquidity ratio
        liquidity = features.get('liquidity_ratio', 1)
        if liquidity > 1.5:
            score += 20
        elif liquidity > 1:
            score += 10
        else:
            score -= 10
        
        return self._normalize_score(score)
    
    def _compute_overall_score(
        self, 
        dimension_scores: Dict, 
        weights: Dict
    ) -> float:
        """Compute weighted overall score"""
        
        overall = 0
        for dimension, score in dimension_scores.items():
            weight = weights.get(dimension, 0.1)
            overall += score * weight
        
        return round(overall, 2)
    
    def _get_dynamic_weights(self, profile: Dict) -> Dict:
        """Get dynamic weights based on industry and profile"""
        
        industry = profile.get('industry', 'default')
        business_age = profile.get('years_in_business', 5)
        
        # Check for specific weight configuration
        config = self.db.query(WeightConfiguration).filter(
            WeightConfiguration.industry == industry,
            WeightConfiguration.business_age_min <= business_age,
            WeightConfiguration.business_age_max >= business_age
        ).first()
        
        if config:
            return config.weights
        
        # Default weights
        return {
            'cash_flow': 0.18,
            'revenue': 0.15,
            'compliance': 0.12,
            'liquidity': 0.10,
            'payment_discipline': 0.14,
            'employee_stability': 0.08,
            'business_stability': 0.12,
            'digital_transaction': 0.06,
            'working_capital': 0.05
        }
    
    def _determine_grade(self, score: float) -> str:
        """Determine grade from score"""
        
        if score >= 90:
            return 'A+'
        elif score >= 80:
            return 'A'
        elif score >= 70:
            return 'B+'
        elif score >= 60:
            return 'B'
        elif score >= 50:
            return 'C+'
        elif score >= 40:
            return 'C'
        elif score >= 30:
            return 'D'
        else:
            return 'F'
    
    def _determine_risk_category(self, score: float) -> str:
        """Determine risk category from score"""
        
        if score >= 75:
            return 'Low'
        elif score >= 50:
            return 'Moderate'
        elif score >= 30:
            return 'High'
        else:
            return 'Critical'
    
    def _compute_risk_score(self, dimension_scores: Dict) -> float:
        """Compute risk score (inverse of health score)"""
        
        # Risk is higher when scores are low
        avg_score = sum(dimension_scores.values()) / len(dimension_scores)
        risk_score = 100 - avg_score
        
        # Increase risk if any dimension is very low
        min_score = min(dimension_scores.values())
        if min_score < 30:
            risk_score += 10
        
        return min(100, risk_score)
    
    def _compute_confidence(self, features: Dict, profile: Dict) -> float:
        """Compute confidence in the score"""
        
        # Factors affecting confidence
        data_points = len(features)
        profile_completeness = len([v for v in profile.values() if v]) / len(profile) if profile else 0
        
        # Base confidence
        confidence = 0.5
        
        # Add for more data points
        confidence += min(0.3, data_points * 0.01)
        
        # Add for profile completeness
        confidence += profile_completeness * 0.2
        
        return min(1.0, confidence)
    
    def _compute_data_completeness(self, features: Dict) -> float:
        """Compute data completeness"""
        
        # Expected features
        expected_features = [
            'monthly_revenue', 'net_cash_flow', 'credit_debit_ratio',
            'digital_adoption_score', 'employee_count', 'utility_payment_score'
        ]
        
        available = sum(1 for f in expected_features if f in features and features[f] != 0)
        
        return available / len(expected_features)
    
    def _normalize_score(self, score: float) -> float:
        """Normalize score to 0-100 range"""
        return max(0, min(100, round(score, 2)))
    
    def get_score_history(
        self, 
        msme_id: str, 
        months: int = 12
    ) -> List[Dict]:
        """Get historical scores"""
        
        history = self.db.query(ScoreHistory).filter(
            ScoreHistory.msme_id == msme_id
        ).order_by(ScoreHistory.time.desc()).limit(months).all()
        
        return [
            {
                'date': h.time.isoformat(),
                'overall': float(h.overall_score) if h.overall_score else None,
                'cash_flow': float(h.cash_flow_score) if h.cash_flow_score else None,
                'revenue': float(h.revenue_score) if h.revenue_score else None,
                'compliance': float(h.compliance_score) if h.compliance_score else None
            }
            for h in reversed(history)
        ]
    
    def get_industry_benchmark(
        self, 
        industry: str
    ) -> Dict:
        """Get industry benchmark data"""
        
        benchmark = self.db.query(IndustryBenchmark).filter(
            IndustryBenchmark.industry == industry
        ).order_by(IndustryBenchmark.created_at.desc()).first()
        
        if not benchmark:
            return None
        
        return {
            'industry': benchmark.industry,
            'overall_avg': float(benchmark.overall_score_avg),
            'overall_p25': float(benchmark.overall_score_p25),
            'overall_p75': float(benchmark.overall_score_p75),
            'dimensions': {
                'cash_flow': float(benchmark.cash_flow_avg),
                'revenue': float(benchmark.revenue_avg),
                'compliance': float(benchmark.compliance_avg),
                'liquidity': float(benchmark.liquidity_avg),
                'payment_discipline': float(benchmark.payment_discipline_avg),
                'employee_stability': float(benchmark.employee_stability_avg),
                'business_stability': float(benchmark.business_stability_avg),
                'digital_transaction': float(benchmark.digital_transaction_avg),
                'working_capital': float(benchmark.working_capital_avg)
            },
            'sample_size': benchmark.sample_size
        }
```

---

## 5.3 API ENDPOINTS

```python
# app/api/v1/scoring.py
from fastapi import APIRouter, Depends
from app.core.deps import get_current_user, get_db
from app.services.scoring_engine import FinancialHealthScoringEngine

router = APIRouter(prefix="/scoring", tags=["Financial Health Scoring"])

@router.post("/compute/{msme_id}")
async def compute_health_score(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    engine = FinancialHealthScoringEngine(db)
    
    # Get features
    from app.models import FeatureStore
    features = {
        f.feature_name: f.feature_value 
        for f in db.query(FeatureStore).filter(
            FeatureStore.msme_id == msme_id
        ).all()
    }
    
    # Get profile
    from app.services.profile_service import MSMEProfileService
    profile_service = MSMEProfileService(db)
    profile = await profile_service.get_profile(msme_id)
    
    return await engine.compute_health_score(msme_id, features, profile)

@router.get("/{msme_id}")
async def get_health_score(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    from app.models import FinancialHealthScore
    
    score = db.query(FinancialHealthScore).filter(
        FinancialHealthScore.msme_id == msme_id
    ).order_by(FinancialHealthScore.computed_at.desc()).first()
    
    if not score:
        return {'status': 'no_score'}
    
    return {
        'overall_score': float(score.overall_score),
        'grade': score.overall_grade,
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
        },
        'risk_category': score.risk_category,
        'risk_score': float(score.risk_score),
        'confidence': float(score.confidence_score),
        'computed_at': score.computed_at.isoformat()
    }

@router.get("/{msme_id}/history")
async def get_score_history(
    msme_id: str,
    months: int = 12,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    engine = FinancialHealthScoringEngine(db)
    return engine.get_score_history(msme_id, months)

@router.get("/benchmark/{industry}")
async def get_industry_benchmark(
    industry: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    engine = FinancialHealthScoringEngine(db)
    return engine.get_industry_benchmark(industry)
```

---

## 5.4 UI DESIGN

```
┌─────────────────────────────────────────────────────────────────┐
│  FINANCIAL HEALTH SCORE                                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  OVERALL SCORE                                           │    │
│  │                                                          │    │
│  │              ┌─────────────┐                            │    │
│  │              │             │                            │    │
│  │              │     78      │                            │    │
│  │              │     /100    │                            │    │
│  │              │             │                            │    │
│  │              │   Grade: B+ │                            │    │
│  │              └─────────────┘                            │    │
│  │                                                          │    │
│  │  Risk: Moderate  |  Confidence: 85%  |  Data: 78%      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  RADAR CHART                                             │    │
│  │                                                          │    │
│  │                    Cash Flow (72)                       │    │
│  │                       ╱╲                                │    │
│  │                      ╱  ╲                               │    │
│  │  Working Capital   ╱    ╲   Revenue (85)               │    │
│  │     (65)         ╱      ╲                              │    │
│  │                 ╱   ★    ╲                             │    │
│  │                ╱    78    ╲                            │    │
│  │  Digital     ╱──────────╲     Compliance (90)          │    │
│  │  (45)       ╲            ╱                             │    │
│  │              ╲          ╱                              │    │
│  │  Employee    ╲        ╱     Payment (88)               │    │
│  │  (82)         ╲      ╱                                 │    │
│  │                ╲    ╱                                  │    │
│  │  Business      ╲  ╱     Liquidity (65)                 │    │
│  │  (78)           ╲╱                                     │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  SCORE BREAKDOWN                                         │    │
│  │                                                          │    │
│  │  Cash Flow:           72/100  ████████████████░░░░      │    │
│  │  Revenue:             85/100  ████████████████████░░    │    │
│  │  Compliance:          90/100  █████████████████████░    │    │
│  │  Liquidity:           65/100  ██████████████░░░░░░░     │    │
│  │  Payment Discipline:  88/100  ████████████████████░░    │    │
│  │  Employee Stability:  82/100  ██████████████████░░░░    │    │
│  │  Business Stability:  78/100  ████████████████░░░░░     │    │
│  │  Digital Transaction: 45/100  ██████████░░░░░░░░░░░     │    │
│  │  Working Capital:     65/100  ██████████████░░░░░░░     │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  INDUSTRY COMPARISON                                     │    │
│  │                                                          │    │
│  │  Your Score: 78  |  Industry Average: 72               │    │
│  │  You are in top 35% of your industry                    │    │
│  │                                                          │    │
│  │  [View Detailed Benchmark]                              │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5.5 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| Database Schema | 1 day |
| Scoring Engine | 4 days |
| Dimension Scorers | 3 days |
| Dynamic Weights | 2 days |
| Industry Benchmarks | 2 days |
| API Endpoints | 1 day |
| Frontend | 3 days |
| Testing | 2 days |
| **Total** | **18 days** |

---

## 5.6 HACKATHON PRIORITY

**CRITICAL** - Core differentiator of the platform
