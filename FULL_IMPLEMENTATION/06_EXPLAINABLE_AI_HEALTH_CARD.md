# MODULE 6: EXPLAINABLE AI & FINANCIAL HEALTH CARD
## Complete Implementation Guide

---

## 6.1 DATABASE SCHEMA

```sql
-- Score Explanations
CREATE TABLE score_explanations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    score_id UUID NOT NULL REFERENCES financial_health_scores(id),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    
    -- SHAP Values
    shap_values JSONB NOT NULL,
    base_value DECIMAL(10,4),
    
    -- Factor Analysis
    positive_factors JSONB NOT NULL,
    negative_factors JSONB NOT NULL,
    
    -- Plain Language Explanation
    explanation_text TEXT NOT NULL,
    
    -- Improvement Suggestions
    improvement_suggestions JSONB,
    expected_improvements JSONB,
    
    -- Counterfactual
    counterfactual JSONB,
    
    -- Confidence
    explanation_confidence DECIMAL(3,2),
    
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Financial Health Card
CREATE TABLE financial_health_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    msme_id UUID NOT NULL REFERENCES msme_profiles(id),
    score_id UUID NOT NULL REFERENCES financial_health_scores(id),
    explanation_id UUID REFERENCES score_explanations(id),
    
    -- Card Data
    card_data JSONB NOT NULL,
    
    -- Strengths & Weaknesses
    strengths JSONB NOT NULL,
    weaknesses JSONB NOT NULL,
    
    -- Risk Assessment
    risk_category VARCHAR(20) NOT NULL,
    risk_factors JSONB,
    
    -- Loan Readiness
    loan_readiness_score DECIMAL(5,2),
    loan_readiness_status VARCHAR(20),
    recommended_loan_amount DECIMAL(15,2),
    eligible_products JSONB,
    approval_probability DECIMAL(5,2),
    
    -- Recommendations
    recommendations JSONB,
    
    -- Sharing
    share_token VARCHAR(100) UNIQUE,
    share_expires_at TIMESTAMPTZ,
    
    -- PDF Generation
    pdf_generated BOOLEAN DEFAULT FALSE,
    pdf_url VARCHAR(500),
    
    -- Validity
    valid_from TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ NOT NULL,
    
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Explanation Templates
CREATE TABLE explanation_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dimension VARCHAR(50) NOT NULL,
    score_range_min DECIMAL(5,2),
    score_range_max DECIMAL(5,2),
    template_text TEXT NOT NULL,
    positive_template TEXT,
    negative_template TEXT,
    improvement_template TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Counterfactual Explanations
CREATE TABLE counterfactual_explanations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    score_id UUID NOT NULL REFERENCES financial_health_scores(id),
    current_features JSONB NOT NULL,
    counterfactual_features JSONB NOT NULL,
    feature_changes JSONB NOT NULL,
    expected_score_change DECIMAL(5,2),
    feasibility_score DECIMAL(3,2),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Actionable Recommendations
CREATE TABLE actionable_recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id UUID NOT NULL REFERENCES financial_health_cards(id),
    recommendation_type VARCHAR(50) NOT NULL,
    priority VARCHAR(10) NOT NULL,
    dimension VARCHAR(50),
    current_value DECIMAL(15,4),
    target_value DECIMAL(15,4),
    action_description TEXT NOT NULL,
    expected_impact DECIMAL(5,2),
    impact_timeline VARCHAR(50),
    specific_actions JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

## 6.2 EXPLAINABLE AI SERVICE

```python
# app/services/explainable_ai.py
from typing import Dict, List, Optional
import numpy as np
import shap
from sqlalchemy.orm import Session
from app.models import ScoreExplanation, FinancialHealthScore

class ExplainableAIService:
    def __init__(self, db: Session):
        self.db = db
        self.explainer = None  # SHAP explainer initialized on demand
    
    async def generate_explanation(
        self,
        score_id: str,
        features: Dict,
        dimension_scores: Dict
    ) -> Dict:
        """Generate comprehensive explanation for the score"""
        
        # 1. Compute SHAP values
        shap_values = self._compute_shap_values(features)
        
        # 2. Identify positive and negative factors
        positive_factors = self._identify_positive_factors(dimension_scores, features)
        negative_factors = self._identify_negative_factors(dimension_scores, features)
        
        # 3. Generate plain language explanation
        explanation_text = self._generate_plain_language_explanation(
            dimension_scores, positive_factors, negative_factors
        )
        
        # 4. Generate improvement suggestions
        improvements = self._generate_improvement_suggestions(
            dimension_scores, features
        )
        
        # 5. Generate counterfactual
        counterfactual = self._generate_counterfactual(features, dimension_scores)
        
        # 6. Save explanation
        explanation = ScoreExplanation(
            score_id=score_id,
            msme_id=features.get('msme_id'),
            shap_values=shap_values,
            base_value=shap_values.get('base_value', 0),
            positive_factors=positive_factors,
            negative_factors=negative_factors,
            explanation_text=explanation_text,
            improvement_suggestions=improvements,
            counterfactual=counterfactual,
            explanation_confidence=0.85
        )
        
        self.db.add(explanation)
        self.db.commit()
        
        return {
            'shap_values': shap_values,
            'positive_factors': positive_factors,
            'negative_factors': negative_factors,
            'explanation_text': explanation_text,
            'improvement_suggestions': improvements,
            'counterfactual': counterfactual,
            'confidence': 0.85
        }
    
    def _compute_shap_values(self, features: Dict) -> Dict:
        """Compute SHAP values for feature importance"""
        
        # In production, use trained model
        # Simulated SHAP values
        
        feature_importance = {
            'monthly_revenue': {'impact': 15.2, 'direction': 'positive'},
            'net_cash_flow': {'impact': 12.5, 'direction': 'positive'},
            'digital_adoption_score': {'impact': -8.3, 'direction': 'negative'},
            'employee_growth_rate': {'impact': 6.7, 'direction': 'positive'},
            'gst_compliance_ratio': {'impact': 9.1, 'direction': 'positive'},
            'bounced_checks': {'impact': -5.4, 'direction': 'negative'},
            'working_capital_months': {'impact': 7.8, 'direction': 'positive'},
            'utility_payment_score': {'impact': 4.2, 'direction': 'positive'}
        }
        
        return {
            'base_value': 50.0,
            'feature_importance': feature_importance,
            'top_positive': sorted(
                [(k, v) for k, v in feature_importance.items() if v['direction'] == 'positive'],
                key=lambda x: x[1]['impact'],
                reverse=True
            )[:5],
            'top_negative': sorted(
                [(k, v) for k, v in feature_importance.items() if v['direction'] == 'negative'],
                key=lambda x: x[1]['impact']
            )[:5]
        }
    
    def _identify_positive_factors(
        self, 
        dimension_scores: Dict, 
        features: Dict
    ) -> List[Dict]:
        """Identify factors contributing positively to the score"""
        
        factors = []
        
        # High scoring dimensions
        for dimension, score in dimension_scores.items():
            if score >= 70:
                factors.append({
                    'dimension': dimension,
                    'score': score,
                    'impact': score - 50,  # Impact above baseline
                    'description': self._get_dimension_description(dimension, score),
                    'emoji': '✅' if score >= 80 else '📈'
                })
        
        # Specific positive features
        if features.get('revenue_growth_rate', 0) > 10:
            factors.append({
                'dimension': 'revenue_growth',
                'value': features['revenue_growth_rate'],
                'impact': 10,
                'description': f"Strong revenue growth of {features['revenue_growth_rate']:.1f}%",
                'emoji': '📈'
            })
        
        if features.get('digital_adoption_score', 0) > 60:
            factors.append({
                'dimension': 'digital_adoption',
                'value': features['digital_adoption_score'],
                'impact': 8,
                'description': f"Good digital payment adoption ({features['digital_adoption_score']:.0f}%)",
                'emoji': '💡'
            })
        
        return sorted(factors, key=lambda x: x['impact'], reverse=True)[:5]
    
    def _identify_negative_factors(
        self, 
        dimension_scores: Dict, 
        features: Dict
    ) -> List[Dict]:
        """Identify factors contributing negatively to the score"""
        
        factors = []
        
        # Low scoring dimensions
        for dimension, score in dimension_scores.items():
            if score < 50:
                factors.append({
                    'dimension': dimension,
                    'score': score,
                    'impact': -(50 - score),  # Impact below baseline
                    'description': self._get_dimension_description(dimension, score),
                    'emoji': '⚠️' if score >= 30 else '🔴'
                })
        
        # Specific negative features
        if features.get('bounced_checks', 0) > 2:
            factors.append({
                'dimension': 'bounced_checks',
                'value': features['bounced_checks'],
                'impact': -12,
                'description': f"{features['bounced_checks']} bounced checks detected",
                'emoji': '⚠️'
            })
        
        if features.get('employee_turnover_rate', 0) > 20:
            factors.append({
                'dimension': 'employee_turnover',
                'value': features['employee_turnover_rate'],
                'impact': -8,
                'description': f"High employee turnover ({features['employee_turnover_rate']:.1f}%)",
                'emoji': '⚠️'
            })
        
        return sorted(factors, key=lambda x: x['impact'])[:5]
    
    def _generate_plain_language_explanation(
        self,
        dimension_scores: Dict,
        positive_factors: List[Dict],
        negative_factors: List[Dict]
    ) -> str:
        """Generate human-readable explanation"""
        
        overall_score = sum(dimension_scores.values()) / len(dimension_scores)
        
        # Overall assessment
        if overall_score >= 80:
            assessment = "Your business demonstrates strong financial health"
        elif overall_score >= 60:
            assessment = "Your business shows good financial health with some areas for improvement"
        elif overall_score >= 40:
            assessment = "Your business has moderate financial health and several areas need attention"
        else:
            assessment = "Your business requires significant improvement in multiple financial areas"
        
        # Strengths
        strengths_text = ""
        if positive_factors:
            strengths_list = [f['description'] for f in positive_factors[:3]]
            strengths_text = f" Key strengths include: {', '.join(strengths_list)}."
        
        # Areas for improvement
        improvements_text = ""
        if negative_factors:
            improvements_list = [f['description'] for f in negative_factors[:2]]
            improvements_text = f" Areas needing attention: {', '.join(improvements_list)}."
        
        return f"{assessment}.{strengths_text}{improvements_text}"
    
    def _generate_improvement_suggestions(
        self,
        dimension_scores: Dict,
        features: Dict
    ) -> List[Dict]:
        """Generate actionable improvement suggestions"""
        
        suggestions = []
        
        # Sort dimensions by score (lowest first)
        sorted_dims = sorted(dimension_scores.items(), key=lambda x: x[1])
        
        for dimension, score in sorted_dims:
            if score < 70:
                suggestion = self._get_improvement_suggestion(dimension, score, features)
                if suggestion:
                    suggestions.append(suggestion)
        
        return suggestions[:5]  # Top 5 suggestions
    
    def _get_improvement_suggestion(
        self, 
        dimension: str, 
        score: float, 
        features: Dict
    ) -> Optional[Dict]:
        """Get specific improvement suggestion for a dimension"""
        
        suggestions_map = {
            'cash_flow': {
                'action': 'Improve cash flow management',
                'specific_actions': [
                    'Negotiate 30-day payment terms with suppliers',
                    'Implement invoice factoring for faster collections',
                    'Maintain 2-month cash reserve'
                ],
                'expected_impact': 15,
                'timeline': '3-6 months'
            },
            'revenue': {
                'action': 'Diversify and grow revenue streams',
                'specific_actions': [
                    'Expand to new customer segments',
                    'Introduce new products/services',
                    'Focus on high-margin offerings'
                ],
                'expected_impact': 12,
                'timeline': '6-12 months'
            },
            'compliance': {
                'action': 'Improve compliance practices',
                'specific_actions': [
                    'File GST returns 5 days before deadline',
                    'Set up automated payment reminders',
                    'Maintain proper documentation'
                ],
                'expected_impact': 10,
                'timeline': '1-3 months'
            },
            'liquidity': {
                'action': 'Improve liquidity position',
                'specific_actions': [
                    'Maintain higher average bank balance',
                    'Reduce unnecessary expenses',
                    'Negotiate better credit terms'
                ],
                'expected_impact': 12,
                'timeline': '3-6 months'
            },
            'payment_discipline': {
                'action': 'Enhance payment discipline',
                'specific_actions': [
                    'Pay all bills before due date',
                    'Set up auto-pay for recurring bills',
                    'Avoid bounced checks'
                ],
                'expected_impact': 10,
                'timeline': '1-3 months'
            },
            'employee_stability': {
                'action': 'Improve employee retention',
                'specific_actions': [
                    'Offer competitive salaries',
                    'Invest in employee training',
                    'Create positive work environment'
                ],
                'expected_impact': 8,
                'timeline': '6-12 months'
            },
            'business_stability': {
                'action': 'Enhance business stability',
                'specific_actions': [
                    'Maintain consistent operations',
                    'Build long-term customer relationships',
                    'Invest in business infrastructure'
                ],
                'expected_impact': 8,
                'timeline': '12-24 months'
            },
            'digital_transaction': {
                'action': 'Increase digital payment adoption',
                'specific_actions': [
                    'Accept UPI payments from all customers',
                    'Use digital invoicing',
                    'Implement online banking'
                ],
                'expected_impact': 15,
                'timeline': '1-3 months'
            },
            'working_capital': {
                'action': 'Optimize working capital',
                'specific_actions': [
                    'Reduce inventory holding period',
                    'Speed up receivables collection',
                    'Negotiate extended payables'
                ],
                'expected_impact': 10,
                'timeline': '3-6 months'
            }
        }
        
        suggestion = suggestions_map.get(dimension)
        if suggestion:
            suggestion['dimension'] = dimension
            suggestion['current_score'] = score
            suggestion['target_score'] = min(100, score + 20)
        
        return suggestion
    
    def _generate_counterfactual(
        self, 
        features: Dict, 
        dimension_scores: Dict
    ) -> Dict:
        """Generate counterfactual explanation - what would need to change"""
        
        counterfactual = {}
        
        # For each dimension, show what would improve the score
        for dimension, score in dimension_scores.items():
            if score < 70:
                target_score = min(100, score + 15)
                required_change = target_score - score
                
                counterfactual[dimension] = {
                    'current_score': score,
                    'target_score': target_score,
                    'required_improvement': required_change,
                    'suggested_changes': self._get_counterfactual_changes(dimension, features)
                }
        
        return counterfactual
    
    def _get_counterfactual_changes(self, dimension: str, features: Dict) -> List[str]:
        """Get specific changes needed for counterfactual"""
        
        changes_map = {
            'cash_flow': [
                'Increase monthly cash inflow by 20%',
                'Reduce unnecessary expenses by 15%',
                'Maintain minimum 2-month cash reserve'
            ],
            'revenue': [
                'Achieve 15% revenue growth',
                'Diversify customer base by 30%',
                'Increase average order value by 10%'
            ],
            'digital_transaction': [
                'Increase digital payment adoption to 70%',
                'Process 80% transactions digitally',
                'Maintain consistent digital transaction volume'
            ],
            'employee_stability': [
                'Reduce employee turnover to below 10%',
                'Maintain positive employee growth',
                'Keep salary payments consistent'
            ]
        }
        
        return changes_map.get(dimension, ['Improve performance in this area'])
    
    def _get_dimension_description(self, dimension: str, score: float) -> str:
        """Get description for dimension score"""
        
        descriptions = {
            'cash_flow': {
                'high': 'Strong cash flow management',
                'medium': 'Adequate cash flow',
                'low': 'Cash flow needs improvement'
            },
            'revenue': {
                'high': 'Strong revenue growth',
                'medium': 'Stable revenue',
                'low': 'Revenue concerns'
            },
            'compliance': {
                'high': 'Excellent compliance record',
                'medium': 'Good compliance',
                'low': 'Compliance needs attention'
            },
            'liquidity': {
                'high': 'Strong liquidity position',
                'medium': 'Adequate liquidity',
                'low': 'Liquidity concerns'
            },
            'payment_discipline': {
                'high': 'Excellent payment discipline',
                'medium': 'Good payment practices',
                'low': 'Payment discipline needs improvement'
            },
            'employee_stability': {
                'high': 'Stable workforce',
                'medium': 'Moderate stability',
                'low': 'Employee retention issues'
            },
            'business_stability': {
                'high': 'Strong business stability',
                'medium': 'Stable operations',
                'low': 'Business stability concerns'
            },
            'digital_transaction': {
                'high': 'Strong digital adoption',
                'medium': 'Moderate digital usage',
                'low': 'Digital adoption needs improvement'
            },
            'working_capital': {
                'high': 'Healthy working capital',
                'medium': 'Adequate working capital',
                'low': 'Working capital constraints'
            }
        }
        
        level = 'high' if score >= 70 else 'medium' if score >= 50 else 'low'
        return descriptions.get(dimension, {}).get(level, 'Score calculated')
```

---

## 6.3 FINANCIAL HEALTH CARD SERVICE

```python
# app/services/health_card_service.py
from typing import Dict, List
from datetime import datetime, timedelta
import secrets
from sqlalchemy.orm import Session
from app.models import FinancialHealthCard, FinancialHealthScore, ScoreExplanation

class FinancialHealthCardService:
    def __init__(self, db: Session):
        self.db = db
    
    async def generate_health_card(
        self,
        msme_id: str,
        score_data: Dict,
        explanation_data: Dict
    ) -> Dict:
        """Generate comprehensive financial health card"""
        
        # 1. Identify strengths and weaknesses
        strengths = self._identify_strengths(score_data['dimension_scores'])
        weaknesses = self._identify_weaknesses(score_data['dimension_scores'])
        
        # 2. Generate loan readiness assessment
        loan_readiness = self._assess_loan_readiness(score_data)
        
        # 3. Generate recommendations
        recommendations = self._generate_recommendations(
            score_data, explanation_data
        )
        
        # 4. Create card data
        card_data = {
            'overall_score': score_data['overall_score'],
            'grade': score_data['grade'],
            'dimension_scores': score_data['dimension_scores'],
            'risk_category': score_data['risk_category'],
            'risk_score': score_data['risk_score'],
            'confidence': score_data['confidence'],
            'data_completeness': score_data['data_completeness'],
            'strengths': strengths,
            'weaknesses': weaknesses,
            'loan_readiness': loan_readiness,
            'recommendations': recommendations,
            'explanation': explanation_data.get('explanation_text', ''),
            'generated_at': datetime.utcnow().isoformat()
        }
        
        # 5. Generate share token
        share_token = secrets.token_urlsafe(32)
        
        # 6. Save health card
        health_card = FinancialHealthCard(
            msme_id=msme_id,
            score_id=score_data.get('score_id'),
            card_data=card_data,
            strengths=strengths,
            weaknesses=weaknesses,
            risk_category=score_data['risk_category'],
            risk_factors=weaknesses,
            loan_readiness_score=loan_readiness['score'],
            loan_readiness_status=loan_readiness['status'],
            recommended_loan_amount=loan_readiness['recommended_amount'],
            eligible_products=loan_readiness['eligible_products'],
            approval_probability=loan_readiness['approval_probability'],
            recommendations=recommendations,
            share_token=share_token,
            share_expires_at=datetime.utcnow() + timedelta(days=30),
            valid_from=datetime.utcnow(),
            valid_until=datetime.utcnow() + timedelta(days=30)
        )
        
        self.db.add(health_card)
        self.db.commit()
        
        return {
            'card_id': str(health_card.id),
            'card_data': card_data,
            'share_token': share_token,
            'valid_until': health_card.valid_until.isoformat()
        }
    
    def _identify_strengths(self, dimension_scores: Dict) -> List[Dict]:
        """Identify business strengths"""
        
        strengths = []
        
        for dimension, score in dimension_scores.items():
            if score >= 70:
                strength_level = 'Excellent' if score >= 85 else 'Good'
                strengths.append({
                    'dimension': dimension,
                    'score': score,
                    'level': strength_level,
                    'description': self._get_strength_description(dimension, score)
                })
        
        return sorted(strengths, key=lambda x: x['score'], reverse=True)[:5]
    
    def _identify_weaknesses(self, dimension_scores: Dict) -> List[Dict]:
        """Identify areas for improvement"""
        
        weaknesses = []
        
        for dimension, score in dimension_scores.items():
            if score < 60:
                weakness_level = 'Critical' if score < 40 else 'Needs Improvement'
                weaknesses.append({
                    'dimension': dimension,
                    'score': score,
                    'level': weakness_level,
                    'description': self._get_weakness_description(dimension, score)
                })
        
        return sorted(weaknesses, key=lambda x: x['score'])[:5]
    
    def _assess_loan_readiness(self, score_data: Dict) -> Dict:
        """Assess loan readiness"""
        
        overall_score = score_data['overall_score']
        risk_category = score_data['risk_category']
        
        # Calculate loan readiness score
        loan_score = min(100, overall_score * 1.1)
        
        # Determine status
        if loan_score >= 80:
            status = 'Highly Ready'
            approval_probability = 85
        elif loan_score >= 60:
            status = 'Ready'
            approval_probability = 70
        elif loan_score >= 40:
            status = 'Partially Ready'
            approval_probability = 50
        else:
            status = 'Not Ready'
            approval_probability = 25
        
        # Recommended loan amount (based on turnover estimate)
        recommended_amount = self._calculate_recommended_amount(score_data)
        
        # Eligible products
        eligible_products = self._determine_eligible_products(loan_score)
        
        return {
            'score': round(loan_score, 2),
            'status': status,
            'approval_probability': approval_probability,
            'recommended_amount': recommended_amount,
            'eligible_products': eligible_products
        }
    
    def _calculate_recommended_amount(self, score_data: Dict) -> float:
        """Calculate recommended loan amount"""
        
        # Base amount based on score
        base_amount = score_data['overall_score'] * 10000
        
        # Adjust for risk
        risk_multiplier = 1 - (score_data['risk_score'] / 200)
        
        recommended = base_amount * risk_multiplier
        
        # Round to nearest lakh
        return round(recommended / 100000) * 100000
    
    def _determine_eligible_products(self, loan_score: float) -> List[Dict]:
        """Determine eligible loan products"""
        
        products = []
        
        if loan_score >= 60:
            products.append({
                'name': 'Working Capital Loan',
                'amount_range': '₹5-25 Lakhs',
                'interest_range': '9.5-11.5%',
                'tenure': 'Up to 12 months'
            })
        
        if loan_score >= 70:
            products.append({
                'name': 'Term Loan',
                'amount_range': '₹10-50 Lakhs',
                'interest_range': '10.5-12.5%',
                'tenure': '12-60 months'
            })
        
        if loan_score >= 75:
            products.append({
                'name': 'Equipment Finance',
                'amount_range': '₹5-30 Lakhs',
                'interest_range': '11-13%',
                'tenure': '24-60 months'
            })
        
        if loan_score >= 80:
            products.append({
                'name': 'Business Expansion Loan',
                'amount_range': '₹25-100 Lakhs',
                'interest_range': '10-12%',
                'tenure': '36-84 months'
            })
        
        return products
    
    def _generate_recommendations(
        self, 
        score_data: Dict, 
        explanation_data: Dict
    ) -> List[Dict]:
        """Generate actionable recommendations"""
        
        recommendations = []
        
        # From explanation
        if 'improvement_suggestions' in explanation_data:
            for suggestion in explanation_data['improvement_suggestions'][:5]:
                recommendations.append({
                    'type': 'improvement',
                    'priority': 'high' if suggestion.get('expected_impact', 0) > 10 else 'medium',
                    'action': suggestion['action'],
                    'specific_actions': suggestion.get('specific_actions', []),
                    'expected_impact': suggestion.get('expected_impact', 0),
                    'timeline': suggestion.get('timeline', '3-6 months')
                })
        
        # Add general recommendations
        overall_score = score_data['overall_score']
        
        if overall_score < 60:
            recommendations.append({
                'type': 'urgent',
                'priority': 'high',
                'action': 'Focus on improving multiple financial dimensions',
                'specific_actions': [
                    'Connect all data sources for better assessment',
                    'Improve payment discipline',
                    'Increase digital transaction adoption'
                ],
                'expected_impact': 20,
                'timeline': '3-6 months'
            })
        
        return recommendations
    
    def _get_strength_description(self, dimension: str, score: float) -> str:
        """Get description for strength"""
        
        descriptions = {
            'cash_flow': 'Strong cash flow management with positive trends',
            'revenue': 'Consistent revenue growth and diversification',
            'compliance': 'Excellent compliance with regulatory requirements',
            'liquidity': 'Healthy liquidity position with adequate reserves',
            'payment_discipline': 'Timely payments to vendors and suppliers',
            'employee_stability': 'Stable workforce with low turnover',
            'business_stability': 'Consistent operations and growth',
            'digital_transaction': 'Good adoption of digital payment methods',
            'working_capital': 'Optimal working capital management'
        }
        
        return descriptions.get(dimension, 'Strong performance in this area')
    
    def _get_weakness_description(self, dimension: str, score: float) -> str:
        """Get description for weakness"""
        
        descriptions = {
            'cash_flow': 'Cash flow needs improvement - consider better receivables management',
            'revenue': 'Revenue growth is slow - explore new markets or products',
            'compliance': 'Compliance gaps detected - ensure timely filings',
            'liquidity': 'Liquidity position is weak - maintain higher reserves',
            'payment_discipline': 'Payment delays detected - improve payment practices',
            'employee_stability': 'High employee turnover - review retention strategies',
            'business_stability': 'Business operations need stabilization',
            'digital_transaction': 'Low digital adoption - increase UPI/digital payments',
            'working_capital': 'Working capital constraints - optimize inventory and receivables'
        }
        
        return descriptions.get(dimension, 'Area needs attention')
    
    def get_health_card(self, msme_id: str) -> Dict:
        """Get latest health card for MSME"""
        
        card = self.db.query(FinancialHealthCard).filter(
            FinancialHealthCard.msme_id == msme_id
        ).order_by(FinancialHealthCard.created_at.desc()).first()
        
        if not card:
            return None
        
        return {
            'card_id': str(card.id),
            'card_data': card.card_data,
            'valid_until': card.valid_until.isoformat(),
            'share_token': card.share_token
        }
    
    def get_shared_card(self, share_token: str) -> Dict:
        """Get health card by share token"""
        
        card = self.db.query(FinancialHealthCard).filter(
            FinancialHealthCard.share_token == share_token,
            FinancialHealthCard.share_expires_at > datetime.utcnow()
        ).first()
        
        if not card:
            return None
        
        return {
            'card_data': card.card_data,
            'valid_until': card.valid_until.isoformat()
        }
```

---

## 6.4 API ENDPOINTS

```python
# app/api/v1/explainability.py
from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import HTMLResponse
from app.core.deps import get_current_user, get_db
from app.services.explainable_ai import ExplainableAIService
from app.services.health_card_service import FinancialHealthCardService

router = APIRouter(prefix="/explainability", tags=["Explainable AI"])

@router.post("/explain/{score_id}")
async def generate_explanation(
    score_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = ExplainableAIService(db)
    
    # Get score data
    from app.models import FinancialHealthScore
    score = db.query(FinancialHealthScore).filter(
        FinancialHealthScore.id == score_id
    ).first()
    
    if not score:
        raise HTTPException(status_code=404, detail="Score not found")
    
    # Get features
    from app.models import FeatureStore
    features = {
        f.feature_name: f.feature_value 
        for f in db.query(FeatureStore).filter(
            FeatureStore.msme_id == score.msme_id
        ).all()
    }
    
    dimension_scores = {
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
    
    return await service.generate_explanation(score_id, features, dimension_scores)

@router.post("/health-card/{msme_id}")
async def generate_health_card(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    card_service = FinancialHealthCardService(db)
    explain_service = ExplainableAIService(db)
    
    # Get latest score
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
    
    dimension_scores = {
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
    
    # Generate explanation
    explanation = await explain_service.generate_explanation(
        str(score.id), features, dimension_scores
    )
    
    # Generate health card
    score_data = {
        'score_id': str(score.id),
        'overall_score': float(score.overall_score),
        'grade': score.overall_grade,
        'dimension_scores': dimension_scores,
        'risk_category': score.risk_category,
        'risk_score': float(score.risk_score),
        'confidence': float(score.confidence_score),
        'data_completeness': float(score.data_completeness)
    }
    
    return await card_service.generate_health_card(msme_id, score_data, explanation)

@router.get("/health-card/{msme_id}")
async def get_health_card(
    msme_id: str,
    current_user = Depends(get_current_user),
    db = Depends(get_db)
):
    service = FinancialHealthCardService(db)
    card = service.get_health_card(msme_id)
    
    if not card:
        raise HTTPException(status_code=404, detail="No health card found")
    
    return card

@router.get("/share/{share_token}", response_class=HTMLResponse)
async def get_shared_health_card(
    share_token: str,
    db = Depends(get_db)
):
    service = FinancialHealthCardService(db)
    card = service.get_shared_card(share_token)
    
    if not card:
        return "<h1>Card not found or expired</h1>"
    
    # Return HTML view of the card
    return f"""
    <html>
        <head><title>Financial Health Card</title></head>
        <body>
            <h1>Financial Health Card</h1>
            <pre>{card['card_data']}</pre>
        </body>
    </html>
    """
```

---

## 6.5 UI DESIGN

```
┌─────────────────────────────────────────────────────────────────┐
│  FINANCIAL HEALTH CARD                                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │  IDBI BANK                                       │    │    │
│  │  │  MSME Financial Intelligence Platform            │    │    │
│  │  │                                                   │    │    │
│  │  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │    │    │
│  │  │                                                   │    │    │
│  │  │  FINANCIAL HEALTH CARD                           │    │    │
│  │  │                                                   │    │    │
│  │  │  Rahul Textiles Pvt Ltd                          │    │    │
│  │  │  GSTIN: 27ABCDE1234F1Z5                          │    │    │
│  │  │                                                   │    │    │
│  │  │  ┌─────────────┐                                 │    │    │
│  │  │  │             │  Overall Score: 78/100          │    │    │
│  │  │  │     78      │  Grade: B+                      │    │    │
│  │  │  │    ★★★★     │  Risk: Moderate                 │    │    │
│  │  │  │             │  Valid until: Dec 31, 2024       │    │    │
│  │  │  └─────────────┘                                 │    │    │
│  │  │                                                   │    │    │
│  │  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │    │    │
│  │  │                                                   │    │    │
│  │  │  SCORE BREAKDOWN                                  │    │    │
│  │  │                                                   │    │    │
│  │  │  Cash Flow:           72  ████████████████░░░░    │    │    │
│  │  │  Revenue:             85  ████████████████████░░  │    │    │
│  │  │  Compliance:          90  █████████████████████░  │    │    │
│  │  │  Liquidity:           65  ██████████████░░░░░░░   │    │    │
│  │  │  Payment Discipline:  88  ████████████████████░░  │    │    │
│  │  │  Employee Stability:  82  ██████████████████░░░░  │    │    │
│  │  │  Business Stability:  78  ████████████████░░░░░   │    │    │
│  │  │  Digital Transaction: 45  ██████████░░░░░░░░░░░   │    │    │
│  │  │  Working Capital:     65  ██████████████░░░░░░░   │    │    │
│  │  │                                                   │    │    │
│  │  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │    │    │
│  │  │                                                   │    │    │
│  │  │  STRENGTHS                                        │    │    │
│  │  │  ✅ Excellent compliance record (90)             │    │    │
│  │  │  ✅ Strong payment discipline (88)               │    │    │
│  │  │  📈 Good revenue growth (85)                      │    │    │
│  │  │                                                   │    │    │
│  │  │  AREAS FOR IMPROVEMENT                            │    │    │
│  │  │  ⚠️  Digital adoption needs work (45)             │    │    │
│  │  │  ⚠️  Liquidity can be improved (65)              │    │    │
│  │  │                                                   │    │    │
│  │  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │    │    │
│  │  │                                                   │    │    │
│  │  │  LOAN READINESS                                   │    │    │
│  │  │  Score: 85%  |  Status: Highly Ready             │    │    │
│  │  │  Recommended Amount: ₹25,00,000                  │    │    │
│  │  │  Approval Probability: 78%                        │    │    │
│  │  │                                                   │    │    │
│  │  │  ELIGIBLE PRODUCTS                                │    │    │
│  │  │  • Working Capital Loan: ₹5-25 Lakhs             │    │    │
│  │  │  • Term Loan: ₹10-50 Lakhs                       │    │    │
│  │  │  • Equipment Finance: ₹5-30 Lakhs                │    │    │
│  │  │                                                   │    │    │
│  │  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │    │    │
│  │  │                                                   │    │    │
│  │  │  AI EXPLANATION                                   │    │    │
│  │  │  Your business demonstrates good financial health │    │    │
│  │  │  with strong compliance and payment practices.    │    │    │
│  │  │  Key strengths: Excellent compliance record,      │    │    │
│  │  │  strong payment discipline. Areas needing         │    │    │
│  │  │  attention: Digital adoption, Liquidity.          │    │    │
│  │  │                                                   │    │    │
│  │  │  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │    │    │
│  │  │                                                   │    │    │
│  │  │  [Download PDF] [Share] [View Details]            │    │    │
│  │  │                                                   │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6.6 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| Database Schema | 1 day |
| Explainable AI Service | 4 days |
| Health Card Service | 3 days |
| SHAP Integration | 2 days |
| API Endpoints | 2 days |
| Frontend Components | 4 days |
| PDF Generation | 2 days |
| Testing | 2 days |
| **Total** | **20 days** |

---

## 6.7 HACKATHON PRIORITY

**CRITICAL** - Core deliverable for the hackathon
