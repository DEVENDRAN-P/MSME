# MODULE 14: BONUS FEATURES
## Digital Twin, Early Warning, Portfolio Intelligence

---

## 14.1 DIGITAL TWIN SERVICE (JAVA)

```java
// service/DigitalTwinService.java
package com.msme.forecast.service;

import com.msme.forecast.client.AIServiceClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DigitalTwinService {
    
    private final AIServiceClient aiServiceClient;
    
    public DigitalTwinService(AIServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }
    
    public Map<String, Object> createDigitalTwin(UUID msmeId) {
        // Fetch real data and create digital twin
        Map<String, Object> twin = new HashMap<>();
        
        twin.put("msme_id", msmeId.toString());
        twin.put("created_at", new Date().toString());
        twin.put("status", "active");
        
        // Current state
        Map<String, Object> currentState = new HashMap<>();
        currentState.put("health_score", 68);
        currentState.put("cash_balance", 450000);
        currentState.put("monthly_revenue", 850000);
        currentState.put("monthly_expenses", 720000);
        currentState.put("outstanding_loans", 2500000);
        
        twin.put("current_state", currentState);
        
        return twin;
    }
    
    public Map<String, Object> simulateWhatIf(
            UUID msmeId,
            Map<String, Object> currentState,
            Map<String, Object> changes) {
        
        // Simulate what-if scenario
        Map<String, Object> result = new HashMap<>();
        
        double revenueChange = changes.getOrDefault("revenue_change", 0.0) instanceof Number ?
            ((Number) changes.get("revenue_change")).doubleValue() : 0.0;
        double expenseChange = changes.getOrDefault("expense_change", 0.0) instanceof Number ?
            ((Number) changes.get("expense_change")).doubleValue() : 0.0;
        
        double currentRevenue = currentState.getOrDefault("monthly_revenue", 0.0) instanceof Number ?
            ((Number) currentState.get("monthly_revenue")).doubleValue() : 850000;
        double currentExpenses = currentState.getOrDefault("monthly_expenses", 0.0) instanceof Number ?
            ((Number) currentState.get("monthly_expenses")).doubleValue() : 720000;
        
        double newRevenue = currentRevenue * (1 + revenueChange / 100);
        double newExpenses = currentExpenses * (1 + expenseChange / 100);
        double newCashFlow = newRevenue - newExpenses;
        
        result.put("original_revenue", currentRevenue);
        result.put("new_revenue", newRevenue);
        result.put("original_expenses", currentExpenses);
        result.put("new_expenses", newExpenses);
        result.put("original_cash_flow", currentRevenue - currentExpenses);
        result.put("new_cash_flow", newCashFlow);
        result.put("impact", newCashFlow - (currentRevenue - currentExpenses));
        
        return result;
    }
    
    public List<Map<String, Object>> runMonteCarloSimulation(
            UUID msmeId,
            int simulations,
            int horizonMonths) {
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (int i = 0; i < simulations; i++) {
            Map<String, Object> simulation = new HashMap<>();
            simulation.put("simulation_id", i + 1);
            simulation.put("final_score", 50 + Math.random() * 40);
            simulation.put("final_cash", 300000 + Math.random() * 400000);
            results.add(simulation);
        }
        
        return results;
    }
}
```

---

## 14.2 PORTFOLIO INTELLIGENCE SERVICE (JAVA)

```java
// service/PortfolioIntelligenceService.java
package com.msme.creditmanager.service;

import com.msme.creditmanager.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PortfolioIntelligenceService {
    
    private final MsmeProfileRepository msmeProfileRepository;
    private final HealthScoreRepository healthScoreRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    
    public PortfolioIntelligenceService(
            MsmeProfileRepository msmeProfileRepository,
            HealthScoreRepository healthScoreRepository,
            LoanApplicationRepository loanApplicationRepository) {
        this.msmeProfileRepository = msmeProfileRepository;
        this.healthScoreRepository = healthScoreRepository;
        this.loanApplicationRepository = loanApplicationRepository;
    }
    
    public Map<String, Object> getPortfolioOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // Total MSMEs
        long totalMsme = msmeProfileRepository.count();
        overview.put("total_msme", totalMsme);
        
        // Average health score
        double avgScore = healthScoreRepository.findAverageScore();
        overview.put("average_health_score", avgScore);
        
        // Score distribution
        Map<String, Long> distribution = healthScoreRepository.getScoreDistribution();
        overview.put("score_distribution", distribution);
        
        // Active loans
        long activeLoans = loanApplicationRepository.countByStatus("approved");
        overview.put("active_loans", activeLoans);
        
        // Total portfolio value
        double totalValue = loanApplicationRepository.sumApprovedAmount();
        overview.put("total_portfolio_value", totalValue);
        
        return overview;
    }
    
    public Map<String, Object> getRiskAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        
        // Risk distribution
        Map<String, Long> riskDistribution = new HashMap<>();
        riskDistribution.put("low", msmeProfileRepository.countByRiskLevel("low"));
        riskDistribution.put("medium", msmeProfileRepository.countByRiskLevel("medium"));
        riskDistribution.put("high", msmeProfileRepository.countByRiskLevel("high"));
        riskDistribution.put("critical", msmeProfileRepository.countByRiskLevel("critical"));
        
        analysis.put("risk_distribution", riskDistribution);
        
        // NPA rate
        double npaRate = loanApplicationRepository.calculateNPARate();
        analysis.put("npa_rate", npaRate);
        
        // Default probability
        analysis.put("default_probability", calculateDefaultProbability());
        
        return analysis;
    }
    
    public List<Map<String, Object>> getHighRiskMSMEs() {
        List<Map<String, Object>> highRiskMsme = new ArrayList<>();
        
        // Fetch MSMEs with low health scores
        var msmeList = msmeProfileRepository.findByRiskLevel("high");
        
        for (var msme : msmeList) {
            Map<String, Object> item = new HashMap<>();
            item.put("msme_id", msme.getId());
            item.put("business_name", msme.getBusinessName());
            item.put("health_score", msme.getHealthScore());
            item.put("risk_level", "high");
            highRiskMsme.add(item);
        }
        
        return highRiskMsme;
    }
    
    public List<Map<String, Object>> getHighGrowthMSMEs() {
        List<Map<String, Object>> highGrowthMsme = new ArrayList<>();
        
        var msmeList = msmeProfileRepository.findByGrowthRateGreaterThan(20);
        
        for (var msme : msmeList) {
            Map<String, Object> item = new HashMap<>();
            item.put("msme_id", msme.getId());
            item.put("business_name", msme.getBusinessName());
            item.put("growth_rate", msme.getGrowthRate());
            item.put("health_score", msme.getHealthScore());
            highGrowthMsme.add(item);
        }
        
        return highGrowthMsme;
    }
    
    public Map<String, Object> getIndustryAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        
        var industryData = msmeProfileRepository.getIndustryDistribution();
        analysis.put("industry_distribution", industryData);
        
        var industryScores = healthScoreRepository.getAverageScoresByIndustry();
        analysis.put("industry_scores", industryScores);
        
        return analysis;
    }
    
    private double calculateDefaultProbability() {
        // Simplified calculation
        double npaRate = loanApplicationRepository.calculateNPARate();
        return npaRate * 1.2; // Adjusted probability
    }
}
```

```java
// controller/PortfolioController.java
package com.msme.creditmanager.controller;

import com.msme.creditmanager.service.PortfolioIntelligenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/portfolio")
@Tag(name = "Portfolio Intelligence", description = "Portfolio analytics for credit managers")
public class PortfolioController {
    
    private final PortfolioIntelligenceService portfolioService;
    
    public PortfolioController(PortfolioIntelligenceService portfolioService) {
        this.portfolioService = portfolioService;
    }
    
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Get portfolio overview")
    public ResponseEntity<?> getOverview() {
        return ResponseEntity.ok(portfolioService.getPortfolioOverview());
    }
    
    @GetMapping("/risk-analysis")
    @PreAuthorize("hasAnyRole('CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Get risk analysis")
    public ResponseEntity<?> getRiskAnalysis() {
        return ResponseEntity.ok(portfolioService.getRiskAnalysis());
    }
    
    @GetMapping("/high-risk")
    @PreAuthorize("hasAnyRole('CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Get high risk MSMEs")
    public ResponseEntity<?> getHighRiskMSMEs() {
        return ResponseEntity.ok(portfolioService.getHighRiskMSMEs());
    }
    
    @GetMapping("/high-growth")
    @PreAuthorize("hasAnyRole('CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Get high growth MSMEs")
    public ResponseEntity<?> getHighGrowthMSMEs() {
        return ResponseEntity.ok(portfolioService.getHighGrowthMSMEs());
    }
    
    @GetMapping("/industry-analysis")
    @PreAuthorize("hasAnyRole('CREDIT_MANAGER', 'ADMIN')")
    @Operation(summary = "Get industry analysis")
    public ResponseEntity<?> getIndustryAnalysis() {
        return ResponseEntity.ok(portfolioService.getIndustryAnalysis());
    }
}
```

---

## 14.3 AI DIGITAL TWIN (PYTHON)

```python
# ai-services/forecasting/app/digital_twin.py
from typing import Dict, List, Any
import random
import math

class DigitalTwinEngine:
    """Digital Twin simulation engine"""
    
    def __init__(self):
        self.version = "1.0.0"
    
    def simulate_scenario(
        self,
        current_state: Dict[str, Any],
        scenario_params: Dict[str, Any],
        months: int
    ) -> Dict[str, Any]:
        """Run scenario simulation"""
        
        # Initialize simulation
        state = current_state.copy()
        history = []
        alerts = []
        
        for month in range(months):
            # Apply scenario factors
            revenue_change = scenario_params.get("revenue_change", 0) / 100
            expense_change = scenario_params.get("expense_change", 0) / 100
            interest_rate_change = scenario_params.get("interest_rate_change", 0) / 100
            
            # Update financials
            new_revenue = state.get("monthly_revenue", 0) * (1 + revenue_change / 12)
            new_expenses = state.get("monthly_expenses", 0) * (1 + expense_change / 12)
            
            # Calculate cash flow
            cash_flow = new_revenue - new_expenses
            new_cash = state.get("cash_balance", 0) + cash_flow
            
            # EMI calculation
            outstanding_loans = state.get("outstanding_loans", 0)
            loan_emi = outstanding_loans * 0.01 * (1 + interest_rate_change)
            new_cash -= loan_emi
            
            # Update health score
            health_score = self._calculate_health_score(
                state.get("health_score", 50),
                new_revenue,
                new_expenses,
                new_cash
            )
            
            # Check for alerts
            if new_cash < 0:
                alerts.append({
                    "type": "negative_balance",
                    "month": month + 1,
                    "severity": "critical",
                    "message": "Cash balance turned negative"
                })
            
            if health_score < 40:
                alerts.append({
                    "type": "low_score",
                    "month": month + 1,
                    "severity": "high",
                    "message": f"Health score dropped to {health_score:.1f}"
                })
            
            # Update state
            state = {
                "health_score": health_score,
                "cash_balance": new_cash,
                "monthly_revenue": new_revenue,
                "monthly_expenses": new_expenses,
                "outstanding_loans": outstanding_loans
            }
            
            history.append({
                "month": month + 1,
                "state": state.copy()
            })
        
        # Analyze results
        scores = [h["state"]["health_score"] for h in history]
        cash_balances = [h["state"]["cash_balance"] for h in history]
        
        analysis = {
            "score_trend": {
                "start": scores[0] if scores else 50,
                "end": scores[-1] if scores else 50,
                "min": min(scores) if scores else 0,
                "max": max(scores) if scores else 100,
                "average": sum(scores) / len(scores) if scores else 50
            },
            "cash_flow_analysis": {
                "lowest_balance": min(cash_balances) if cash_balances else 0,
                "highest_balance": max(cash_balances) if cash_balances else 0,
                "end_balance": cash_balances[-1] if cash_balances else 0
            },
            "risk_assessment": self._assess_risk(scores, cash_balances),
            "recommendations": self._generate_recommendations(history, scenario_params)
        }
        
        return {
            "initial_state": current_state,
            "final_state": state,
            "monthly_history": history,
            "alerts": alerts,
            "analysis": analysis,
            "risk_level": analysis["risk_assessment"]
        }
    
    def _calculate_health_score(self, current, revenue, expenses, cash):
        """Calculate health score based on financial metrics"""
        
        score = current
        
        # Revenue vs Expenses ratio
        if expenses > 0:
            ratio = revenue / expenses
            if ratio > 1.2:
                score += 2
            elif ratio < 0.8:
                score -= 3
        
        # Cash balance impact
        if cash < 0:
            score -= 10
        elif cash < expenses:
            score -= 3
        else:
            score += 1
        
        return max(0, min(100, score))
    
    def _assess_risk(self, scores, cash_balances):
        """Assess overall risk"""
        
        if not scores:
            return "unknown"
        
        final_score = scores[-1]
        min_score = min(scores)
        negative_months = sum(1 for c in cash_balances if c < 0)
        
        if final_score >= 70 and min_score >= 50 and negative_months == 0:
            return "low"
        elif final_score >= 50 and negative_months <= 2:
            return "medium"
        elif final_score >= 30:
            return "high"
        return "critical"
    
    def _generate_recommendations(self, history, params):
        """Generate recommendations based on simulation"""
        
        recommendations = []
        
        if params.get("revenue_change", 0) < -10:
            recommendations.append("Diversify revenue streams to reduce dependency")
        
        if params.get("expense_change", 0) > 10:
            recommendations.append("Implement cost optimization measures")
        
        negative_months = [
            h["month"] for h in history if h["state"]["cash_balance"] < 0
        ]
        if negative_months:
            recommendations.append(
                f"Cash shortfall expected in months {negative_months}. Maintain buffer."
            )
        
        final_score = history[-1]["state"]["health_score"] if history else 50
        if final_score < 50:
            recommendations.append("Consider debt restructuring options")
        
        return recommendations

# Initialize engine
digital_twin_engine = DigitalTwinEngine()
```

---

## 14.4 EARLY WARNING ENGINE (PYTHON)

```python
# ai-services/early-warning/app/monitor.py
from typing import Dict, List, Any
from datetime import datetime, timedelta

class EarlyWarningMonitor:
    """Real-time early warning monitoring system"""
    
    def __init__(self):
        self.thresholds = {
            "health_score_low": 40,
            "health_score_critical": 30,
            "cash_balance_low": 100000,
            "cash_balance_critical": 0,
            "revenue_decline_pct": 15,
            "expense_increase_pct": 20,
            "debt_ratio_high": 0.5,
            "debt_ratio_critical": 0.7,
            "payment_delay_days": 30
        }
    
    def check_warnings(self, msme_id: str, data: Dict[str, Any]) -> List[Dict]:
        """Check for all warning signs"""
        
        warnings = []
        
        # Health Score Warnings
        health_score = data.get("health_score", 100)
        if health_score < self.thresholds["health_score_critical"]:
            warnings.append(self._create_warning(
                "critical_health_score",
                "critical",
                f"Health score critically low: {health_score}",
                {"current_score": health_score, "threshold": self.thresholds["health_score_critical"]}
            ))
        elif health_score < self.thresholds["health_score_low"]:
            warnings.append(self._create_warning(
                "low_health_score",
                "high",
                f"Health score below threshold: {health_score}",
                {"current_score": health_score, "threshold": self.thresholds["health_score_low"]}
            ))
        
        # Cash Balance Warnings
        cash_balance = data.get("cash_balance", 0)
        if cash_balance < self.thresholds["cash_balance_critical"]:
            warnings.append(self._create_warning(
                "negative_cash_balance",
                "critical",
                f"Cash balance negative: ₹{cash_balance:,.0f}",
                {"current_balance": cash_balance}
            ))
        elif cash_balance < self.thresholds["cash_balance_low"]:
            warnings.append(self._create_warning(
                "low_cash_balance",
                "high",
                f"Cash balance critically low: ₹{cash_balance:,.0f}",
                {"current_balance": cash_balance, "threshold": self.thresholds["cash_balance_low"]}
            ))
        
        # Revenue Decline Warnings
        revenue_history = data.get("revenue_history", [])
        if len(revenue_history) >= 3:
            recent_avg = sum(revenue_history[-3:]) / 3
            older_avg = sum(revenue_history[-6:-3]) / 3 if len(revenue_history) >= 6 else recent_avg
            
            if older_avg > 0:
                decline_pct = (older_avg - recent_avg) / older_avg * 100
                if decline_pct > self.thresholds["revenue_decline_pct"]:
                    warnings.append(self._create_warning(
                        "revenue_decline",
                        "high",
                        f"Revenue declined by {decline_pct:.1f}% over last 3 months",
                        {"decline_percent": decline_pct}
                    ))
        
        # Expense Ratio Warnings
        monthly_expenses = data.get("monthly_expenses", 0)
        monthly_revenue = data.get("monthly_revenue", 1)
        if monthly_revenue > 0:
            expense_ratio = monthly_expenses / monthly_revenue
            if expense_ratio > 0.9:
                warnings.append(self._create_warning(
                    "high_expense_ratio",
                    "high",
                    f"Expenses are {expense_ratio*100:.1f}% of revenue",
                    {"expense_ratio": expense_ratio}
                ))
        
        # Debt Ratio Warnings
        outstanding_loans = data.get("outstanding_loans", 0)
        annual_revenue = data.get("annual_revenue", 1)
        if annual_revenue > 0:
            debt_ratio = outstanding_loans / annual_revenue
            if debt_ratio > self.thresholds["debt_ratio_critical"]:
                warnings.append(self._create_warning(
                    "critical_debt_ratio",
                    "critical",
                    f"Debt-to-revenue ratio critical: {debt_ratio:.2f}",
                    {"debt_ratio": debt_ratio}
                ))
            elif debt_ratio > self.thresholds["debt_ratio_high"]:
                warnings.append(self._create_warning(
                    "high_debt_ratio",
                    "high",
                    f"Debt-to-revenue ratio high: {debt_ratio:.2f}",
                    {"debt_ratio": debt_ratio}
                ))
        
        # Payment Delay Warnings
        avg_payment_delay = data.get("avg_payment_delay_days", 0)
        if avg_payment_delay > self.thresholds["payment_delay_days"]:
            warnings.append(self._create_warning(
                "payment_delays",
                "medium",
                f"Average payment delay: {avg_payment_delay} days",
                {"delay_days": avg_payment_delay}
            ))
        
        # NPA Risk Warning
        if health_score < 40 and outstanding_loans > 0:
            warnings.append(self._create_warning(
                "npa_risk",
                "critical",
                "High risk of becoming NPA",
                {"health_score": health_score, "outstanding": outstanding_loans}
            ))
        
        return warnings
    
    def _create_warning(self, warning_type, severity, description, indicators):
        """Create a warning object"""
        
        return {
            "type": warning_type,
            "severity": severity,
            "description": description,
            "indicators": indicators,
            "confidence": self._calculate_confidence(indicators),
            "detected_at": datetime.now().isoformat(),
            "recommended_action": self._get_recommended_action(warning_type)
        }
    
    def _calculate_confidence(self, indicators):
        """Calculate confidence score for warning"""
        
        # Simple confidence calculation
        base_confidence = 0.7
        
        if indicators.get("decline_percent", 0) > 20:
            base_confidence += 0.15
        if indicators.get("debt_ratio", 0) > 0.6:
            base_confidence += 0.1
        
        return min(0.95, base_confidence)
    
    def _get_recommended_action(self, warning_type):
        """Get recommended action for warning type"""
        
        actions = {
            "critical_health_score": "Urgent: Review all financial aspects and consider restructuring",
            "low_health_score": "Focus on improving key financial metrics",
            "negative_cash_balance": "Critical: Secure immediate funding or reduce expenses",
            "low_cash_balance": "Maintain cash buffer, consider short-term financing",
            "revenue_decline": "Diversify revenue sources, review pricing strategy",
            "high_expense_ratio": "Implement cost optimization measures",
            "critical_debt_ratio": "Consider debt restructuring or equity infusion",
            "high_debt_ratio": "Monitor debt levels, plan repayment strategy",
            "payment_delays": "Improve accounts receivable collection",
            "npa_risk": "Engage with lender for restructuring options"
        }
        
        return actions.get(warning_type, "Review and take appropriate action")

# Initialize monitor
early_warning_monitor = EarlyWarningMonitor()
```

---

## 14.5 FRONTEND - SCENARIO SIMULATOR

```tsx
// frontend/src/components/simulator/ScenarioSimulator.tsx
import React, { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Slider } from '@/components/ui/Slider';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/Tabs';
import { RadarChart, TrendLine, HealthGauge } from '@/components/charts';
import { scenarioApi } from '@/api/scenario.api';

interface ScenarioSimulatorProps {
  msmeId: string;
}

export const ScenarioSimulator: React.FC<ScenarioSimulatorProps> = ({ msmeId }) => {
  const [scenario, setScenario] = useState({
    revenue_change: 0,
    expense_change: 0,
    interest_rate_change: 0,
    months: 12
  });
  const [result, setResult] = useState<any>(null);
  
  const simulateMutation = useMutation({
    mutationFn: async () => {
      const response = await scenarioApi.simulate({
        msme_id: msmeId,
        parameters: scenario,
        months: scenario.months
      });
      return response.data;
    },
    onSuccess: (data) => {
      setResult(data);
    }
  });
  
  const presets = [
    { name: 'Economic Boom', revenue: 20, expense: 10, interest: 2 },
    { name: 'Recession', revenue: -25, expense: 5, interest: -1 },
    { name: 'Interest Rate Hike', revenue: 0, expense: 0, interest: 5 },
    { name: 'Cost Optimization', revenue: -5, expense: -20, interest: 0 },
    { name: 'Rapid Expansion', revenue: 40, expense: 50, interest: 3 }
  ];
  
  return (
    <Card>
      <CardHeader>
        <CardTitle>Scenario Simulator</CardTitle>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="custom">
          <TabsList>
            <TabsTrigger value="custom">Custom Scenario</TabsTrigger>
            <TabsTrigger value="presets">Preset Scenarios</TabsTrigger>
          </TabsList>
          
          <TabsContent value="custom" className="space-y-6">
            <div className="grid grid-cols-2 gap-6">
              <div>
                <label className="text-sm font-medium">
                  Revenue Change: {scenario.revenue_change > 0 ? '+' : ''}{scenario.revenue_change}%
                </label>
                <Slider
                  value={[scenario.revenue_change]}
                  onValueChange={(v) => setScenario({ ...scenario, revenue_change: v[0] })}
                  min={-50}
                  max={50}
                  step={5}
                />
              </div>
              
              <div>
                <label className="text-sm font-medium">
                  Expense Change: {scenario.expense_change > 0 ? '+' : ''}{scenario.expense_change}%
                </label>
                <Slider
                  value={[scenario.expense_change]}
                  onValueChange={(v) => setScenario({ ...scenario, expense_change: v[0] })}
                  min={-50}
                  max={50}
                  step={5}
                />
              </div>
              
              <div>
                <label className="text-sm font-medium">
                  Interest Rate Change: {scenario.interest_rate_change > 0 ? '+' : ''}{scenario.interest_rate_change}%
                </label>
                <Slider
                  value={[scenario.interest_rate_change]}
                  onValueChange={(v) => setScenario({ ...scenario, interest_rate_change: v[0] })}
                  min={-5}
                  max={10}
                  step={1}
                />
              </div>
              
              <div>
                <label className="text-sm font-medium">
                  Simulation Period: {scenario.months} months
                </label>
                <Slider
                  value={[scenario.months]}
                  onValueChange={(v) => setScenario({ ...scenario, months: v[0] })}
                  min={6}
                  max={36}
                  step={6}
                />
              </div>
            </div>
            
            <Button 
              onClick={() => simulateMutation.mutate()}
              disabled={simulateMutation.isPending}
            >
              {simulateMutation.isPending ? 'Running...' : 'Run Simulation'}
            </Button>
          </TabsContent>
          
          <TabsContent value="presets" className="space-y-4">
            <div className="grid grid-cols-3 gap-4">
              {presets.map((preset) => (
                <Card 
                  key={preset.name}
                  className="cursor-pointer hover:border-blue-500"
                  onClick={() => {
                    setScenario({
                      revenue_change: preset.revenue,
                      expense_change: preset.expense,
                      interest_rate_change: preset.interest,
                      months: scenario.months
                    });
                    simulateMutation.mutate();
                  }}
                >
                  <CardContent className="p-4">
                    <h4 className="font-medium">{preset.name}</h4>
                    <p className="text-sm text-gray-500">
                      Revenue: {preset.revenue > 0 ? '+' : ''}{preset.revenue}%
                    </p>
                  </CardContent>
                </Card>
              ))}
            </div>
          </TabsContent>
        </Tabs>
        
        {result && (
          <div className="mt-6 space-y-6">
            <div className="grid grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Initial State</CardTitle>
                </CardHeader>
                <CardContent>
                  <HealthGauge 
                    score={result.initial_state.health_score} 
                    grade={getGrade(result.initial_state.health_score)} 
                  />
                </CardContent>
              </Card>
              
              <Card>
                <CardHeader>
                  <CardTitle>Projected Final State</CardTitle>
                </CardHeader>
                <CardContent>
                  <HealthGauge 
                    score={result.final_state.health_score} 
                    grade={getGrade(result.final_state.health_score)} 
                  />
                </CardContent>
              </Card>
            </div>
            
            <Card>
              <CardHeader>
                <CardTitle>Health Score Projection</CardTitle>
              </CardHeader>
              <CardContent>
                <TrendLine 
                  data={result.monthly_history.map((h: any) => ({
                    date: `Month ${h.month}`,
                    value: h.state.health_score
                  }))}
                />
              </CardContent>
            </Card>
            
            {result.alerts.length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>Alerts</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {result.alerts.map((alert: any, i: number) => (
                      <div 
                        key={i} 
                        className={`p-3 rounded ${
                          alert.severity === 'critical' ? 'bg-red-100' : 'bg-yellow-100'
                        }`}
                      >
                        <p className={`font-medium ${
                          alert.severity === 'critical' ? 'text-red-800' : 'text-yellow-800'
                        }`}>
                          Month {alert.month}: {alert.message}
                        </p>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            )}
            
            {result.analysis.recommendations.length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>Recommendations</CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2">
                    {result.analysis.recommendations.map((rec: string, i: number) => (
                      <li key={i} className="flex items-start gap-2">
                        <span className="text-blue-500">•</span>
                        <span>{rec}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
};

const getGrade = (score: number) => {
  if (score >= 90) return 'A+';
  if (score >= 80) return 'A';
  if (score >= 70) return 'B+';
  if (score >= 60) return 'B';
  if (score >= 50) return 'C';
  return 'D';
};

export default ScenarioSimulator;
```

---

## 14.6 API ENDPOINTS SUMMARY

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/digital-twin/{msmeId}` | POST | Create digital twin |
| `/api/v1/digital-twin/{msmeId}/simulate` | POST | Run what-if simulation |
| `/api/v1/digital-twin/{msmeId}/monte-carlo` | POST | Run Monte Carlo |
| `/api/v1/early-warning/{msmeId}` | GET | Check early warnings |
| `/api/v1/early-warning/{msmeId}/active` | GET | Get active warnings |
| `/api/v1/early-warning/{warningId}/acknowledge` | PUT | Acknowledge warning |
| `/api/v1/portfolio/overview` | GET | Portfolio overview |
| `/api/v1/portfolio/risk-analysis` | GET | Risk analysis |
| `/api/v1/portfolio/high-risk` | GET | High risk MSMEs |
| `/api/v1/portfolio/high-growth` | GET | High growth MSMEs |
| `/api/v1/portfolio/industry-analysis` | GET | Industry analysis |

---

## 14.7 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| Digital Twin Service | 3 days |
| AI Digital Twin (Python) | 2 days |
| Early Warning System | 3 days |
| Portfolio Intelligence | 3 days |
| Frontend Components | 3 days |
| Testing | 2 days |
| **Total** | **16 days** |

---

## 14.8 HACKATHON PRIORITY

**HIGH** - Differentiating features showcasing innovation
