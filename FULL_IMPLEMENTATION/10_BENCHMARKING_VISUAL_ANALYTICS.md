# MODULE 10: INDUSTRY BENCHMARKING & VISUAL ANALYTICS
## Production-Grade Implementation

---

## 10.1 MICROSERVICES STRUCTURE

```
ai-services/benchmarking/
├── app/
│   ├── main.py
│   ├── benchmark/
│   │   ├── industry_benchmark.py
│   │   ├── percentile_calculator.py
│   │   └── comparison_engine.py
│   ├── data/
│   │   └── benchmark_data.py
│   └── api/
│       └── routes.py
├── data/
│   ├── industry_benchmarks.json
│   └── top_performers.json
├── requirements.txt
└── Dockerfile
```

---

## 10.2 AI BENCHMARKING SERVICE (PYTHON)

```python
# ai-services/benchmarking/app/main.py
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
import json
from pathlib import Path

app = FastAPI(title="AI Industry Benchmarking Service", version="1.0.0")

class BenchmarkRequest(BaseModel):
    msme_id: str
    industry: str
    msme_category: Optional[str] = None  # Micro, Small, Medium
    scores: Dict[str, float]

class ComparisonRequest(BaseModel):
    msme_id: str
    scores: Dict[str, float]
    industry: str

class IndustryBenchmark:
    """Industry benchmark data and calculations"""
    
    def __init__(self):
        self.benchmarks = self._load_benchmarks()
    
    def _load_benchmarks(self) -> Dict[str, Dict]:
        """Load industry benchmark data"""
        
        # Default benchmarks - in production, load from database
        return {
            "manufacturing": {
                "overall": {"avg": 68, "p25": 52, "p75": 82, "top10": 88},
                "dimensions": {
                    "cash_flow": {"avg": 62, "p25": 45, "p75": 78},
                    "revenue": {"avg": 70, "p25": 55, "p75": 85},
                    "compliance": {"avg": 72, "p25": 58, "p75": 88},
                    "liquidity": {"avg": 58, "p25": 40, "p75": 75},
                    "payment_discipline": {"avg": 65, "p25": 48, "p75": 82},
                    "employee_stability": {"avg": 60, "p25": 42, "p75": 78},
                    "business_stability": {"avg": 66, "p25": 50, "p75": 80},
                    "digital_transaction": {"avg": 48, "p25": 30, "p75": 65},
                    "working_capital": {"avg": 55, "p25": 38, "p75": 72}
                },
                "sample_size": 1250
            },
            "services": {
                "overall": {"avg": 72, "p25": 58, "p75": 85, "top10": 92},
                "dimensions": {
                    "cash_flow": {"avg": 68, "p25": 52, "p75": 82},
                    "revenue": {"avg": 74, "p25": 60, "p75": 88},
                    "compliance": {"avg": 75, "p25": 62, "p75": 90},
                    "liquidity": {"avg": 62, "p25": 45, "p75": 78},
                    "payment_discipline": {"avg": 70, "p25": 55, "p75": 85},
                    "employee_stability": {"avg": 65, "p25": 48, "p75": 82},
                    "business_stability": {"avg": 70, "p25": 55, "p75": 85},
                    "digital_transaction": {"avg": 55, "p25": 35, "p75": 72},
                    "working_capital": {"avg": 60, "p25": 42, "p75": 76}
                },
                "sample_size": 980
            },
            "trading": {
                "overall": {"avg": 65, "p25": 48, "p75": 78, "top10": 85},
                "dimensions": {
                    "cash_flow": {"avg": 58, "p25": 40, "p75": 72},
                    "revenue": {"avg": 68, "p25": 52, "p75": 82},
                    "compliance": {"avg": 70, "p25": 55, "p75": 85},
                    "liquidity": {"avg": 55, "p25": 38, "p75": 70},
                    "payment_discipline": {"avg": 62, "p25": 45, "p75": 78},
                    "employee_stability": {"avg": 58, "p25": 40, "p75": 75},
                    "business_stability": {"avg": 64, "p25": 48, "p75": 78},
                    "digital_transaction": {"avg": 50, "p25": 32, "p75": 68},
                    "working_capital": {"avg": 52, "p25": 35, "p75": 68}
                },
                "sample_size": 820
            },
            "technology": {
                "overall": {"avg": 78, "p25": 65, "p75": 90, "top10": 95},
                "dimensions": {
                    "cash_flow": {"avg": 75, "p25": 60, "p75": 88},
                    "revenue": {"avg": 80, "p25": 68, "p75": 92},
                    "compliance": {"avg": 82, "p25": 70, "p75": 95},
                    "liquidity": {"avg": 72, "p25": 58, "p75": 85},
                    "payment_discipline": {"avg": 78, "p25": 65, "p75": 90},
                    "employee_stability": {"avg": 70, "p25": 55, "p75": 85},
                    "business_stability": {"avg": 75, "p25": 62, "p75": 88},
                    "digital_transaction": {"avg": 72, "p25": 58, "p75": 88},
                    "working_capital": {"avg": 68, "p25": 52, "p75": 82}
                },
                "sample_size": 650
            },
            "healthcare": {
                "overall": {"avg": 74, "p25": 60, "p75": 88, "top10": 93},
                "dimensions": {
                    "cash_flow": {"avg": 70, "p25": 55, "p75": 85},
                    "revenue": {"avg": 76, "p25": 62, "p75": 90},
                    "compliance": {"avg": 85, "p25": 72, "p75": 95},
                    "liquidity": {"avg": 65, "p25": 48, "p75": 80},
                    "payment_discipline": {"avg": 72, "p25": 58, "p75": 88},
                    "employee_stability": {"avg": 68, "p25": 52, "p75": 82},
                    "business_stability": {"avg": 72, "p25": 58, "p75": 85},
                    "digital_transaction": {"avg": 58, "p25": 40, "p75": 75},
                    "working_capital": {"avg": 62, "p25": 45, "p75": 78}
                },
                "sample_size": 520
            }
        }
    
    def get_benchmark(self, industry: str, category: Optional[str] = None) -> Dict:
        """Get industry benchmark data"""
        
        industry = industry.lower()
        
        if industry not in self.benchmarks:
            # Return default benchmark
            industry = "manufacturing"
        
        benchmark = self.benchmarks[industry].copy()
        benchmark["industry"] = industry
        
        return benchmark
    
    def compare_with_industry(self, scores: Dict[str, float], industry: str) -> Dict:
        """Compare MSME scores with industry benchmarks"""
        
        benchmark = self.get_benchmark(industry)
        
        comparisons = {}
        overall_diff = 0
        
        for dimension, score in scores.items():
            if dimension in benchmark.get("dimensions", {}):
                bench = benchmark["dimensions"][dimension]
                diff = score - bench["avg"]
                
                # Calculate percentile
                percentile = self._calculate_percentile(score, bench)
                
                comparisons[dimension] = {
                    "score": score,
                    "benchmark_avg": bench["avg"],
                    "difference": round(diff, 2),
                    "percentile": percentile,
                    "status": "above" if diff > 5 else "below" if diff < -5 else "at"
                }
                overall_diff += diff
        
        # Overall comparison
        overall_bench = benchmark["overall"]
        msme_overall = sum(scores.values()) / len(scores) if scores else 0
        
        return {
            "msme_score": round(msme_overall, 2),
            "industry_average": overall_bench["avg"],
            "difference": round(msme_overall - overall_bench["avg"], 2),
            "percentile": self._calculate_percentile(msme_overall, overall_bench),
            "rank": self._estimate_rank(msme_overall, overall_bench),
            "dimension_comparisons": comparisons,
            "insights": self._generate_insights(comparisons),
            "industry": industry,
            "sample_size": benchmark["sample_size"]
        }
    
    def _calculate_percentile(self, score: float, benchmark: Dict) -> int:
        """Calculate percentile based on benchmark"""
        
        avg = benchmark.get("avg", 50)
        p25 = benchmark.get("p25", 35)
        p75 = benchmark.get("p75", 65)
        
        if score >= p75 + 10:
            return 90
        elif score >= p75:
            return 75
        elif score >= avg:
            return 50
        elif score >= p25:
            return 35
        else:
            return 20
    
    def _estimate_rank(self, score: float, benchmark: Dict) -> str:
        """Estimate rank in industry"""
        
        top10 = benchmark.get("top10", 85)
        p75 = benchmark.get("p75", 75)
        avg = benchmark.get("avg", 50)
        
        if score >= top10:
            return "Top 10%"
        elif score >= p75:
            return "Top 25%"
        elif score >= avg:
            return "Top 50%"
        else:
            return "Bottom 50%"
    
    def _generate_insights(self, comparisons: Dict) -> List[str]:
        """Generate insights from comparisons"""
        
        insights = []
        
        strengths = [d for d, c in comparisons.items() if c["status"] == "above"]
        weaknesses = [d for d, c in comparisons.items() if c["status"] == "below"]
        
        if strengths:
            insights.append(f"Strengths: {', '.join(s.replace('_', ' ').title() for s in strengths)}")
        
        if weaknesses:
            insights.append(f"Areas to improve: {', '.join(s.replace('_', ' ').title() for s in weaknesses)}")
        
        # Highlight significant differences
        for dim, comp in comparisons.items():
            if abs(comp["difference"]) > 15:
                direction = "exceeds" if comp["difference"] > 0 else "lags behind"
                insights.append(f"{dim.replace('_', ' ').title()} {direction} industry average by {abs(comp['difference']):.1f} points")
        
        return insights

class TopPerformerAnalyzer:
    """Analyze top performing MSMEs"""
    
    def __init__(self):
        self.top_performers = self._load_top_performers()
    
    def _load_top_performers(self) -> Dict[str, List]:
        """Load top performer data"""
        
        # Simulated top performers
        return {
            "manufacturing": [
                {"rank": 1, "score": 92, "industry_avg": 68},
                {"rank": 2, "score": 90, "industry_avg": 68},
                {"rank": 3, "score": 88, "industry_avg": 68}
            ],
            "services": [
                {"rank": 1, "score": 95, "industry_avg": 72},
                {"rank": 2, "score": 93, "industry_avg": 72},
                {"rank": 3, "score": 91, "industry_avg": 72}
            ]
        }
    
    def get_top_performers(self, industry: str, limit: int = 10) -> List[Dict]:
        """Get top performing MSMEs"""
        
        performers = self.top_performers.get(industry.lower(), [])
        return performers[:limit]
    
    def get_improvement_areas(self, scores: Dict[str, float], industry: str) -> List[Dict]:
        """Identify areas for improvement to reach top performer level"""
        
        benchmark = IndustryBenchmark()
        industry_data = benchmark.get_benchmark(industry)
        
        improvements = []
        
        for dim, score in scores.items():
            if dim in industry_data.get("dimensions", {}):
                target = industry_data["dimensions"][dim]["p75"]
                if score < target:
                    gap = target - score
                    improvements.append({
                        "dimension": dim,
                        "current_score": score,
                        "target_score": target,
                        "gap": round(gap, 2),
                        "priority": "high" if gap > 20 else "medium" if gap > 10 else "low"
                    })
        
        return sorted(improvements, key=lambda x: x["gap"], reverse=True)

# Initialize services
benchmark_service = IndustryBenchmark()
top_performer_service = TopPerformerAnalyzer()

@app.post("/api/v1/benchmarking/industry")
async def get_industry_benchmark(industry: str, category: str = None):
    """Get industry benchmark data"""
    return benchmark_service.get_benchmark(industry, category)

@app.post("/api/v1/benchmarking/compare")
async def compare_with_industry(request: ComparisonRequest):
    """Compare MSME with industry benchmarks"""
    return benchmark_service.compare_with_industry(request.scores, request.industry)

@app.get("/api/v1/benchmarking/top-performers/{industry}")
async def get_top_performers(industry: str, limit: int = 10):
    """Get top performing MSMEs"""
    return top_performer_service.get_top_performers(industry, limit)

@app.post("/api/v1/benchmarking/improvements")
async def get_improvement_areas(request: ComparisonRequest):
    """Get areas for improvement"""
    return top_performer_service.get_improvement_areas(request.scores, request.industry)

@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "ai-benchmarking"}
```

---

## 10.3 VISUAL ANALYTICS COMPONENTS (REACT)

```tsx
// frontend/src/components/charts/VisualAnalytics.tsx
import React from 'react';

// 1. Radar Chart Component
interface RadarChartProps {
  scores: Record<string, number>;
  benchmarks?: Record<string, number>;
  size?: number;
}

export const RadarChart: React.FC<RadarChartProps> = ({ 
  scores, 
  benchmarks, 
  size = 300 
}) => {
  const dimensions = Object.keys(scores);
  const center = size / 2;
  const radius = size / 2 - 40;
  const angleStep = (2 * Math.PI) / dimensions.length;
  
  const getPoint = (index: number, value: number) => {
    const angle = index * angleStep - Math.PI / 2;
    const r = (value / 100) * radius;
    return {
      x: center + r * Math.cos(angle),
      y: center + r * Math.sin(angle)
    };
  };
  
  const dataPoints = dimensions.map((dim, i) => getPoint(i, scores[dim]));
  const dataPath = dataPoints.map((p, i) => 
    (i === 0 ? 'M' : 'L') + `${p.x},${p.y}`
  ).join(' ') + 'Z';
  
  const benchmarkPoints = benchmarks 
    ? dimensions.map((dim, i) => getPoint(i, benchmarks[dim] || 50))
    : [];
  const benchmarkPath = benchmarkPoints.length > 0
    ? benchmarkPoints.map((p, i) => (i === 0 ? 'M' : 'L') + `${p.x},${p.y}`).join(' ') + 'Z'
    : '';
  
  return (
    <svg viewBox={`0 0 ${size} ${size}`} className="w-full h-auto">
      {/* Grid circles */}
      {[20, 40, 60, 80, 100].map(level => (
        <circle 
          key={level} 
          cx={center} 
          cy={center} 
          r={(level / 100) * radius} 
          fill="none" 
          stroke="#e5e7eb" 
          strokeWidth="1"
        />
      ))}
      
      {/* Axis lines */}
      {dimensions.map((_, i) => {
        const angle = i * angleStep - Math.PI / 2;
        return (
          <line
            key={i}
            x1={center}
            y1={center}
            x2={center + radius * Math.cos(angle)}
            y2={center + radius * Math.sin(angle)}
            stroke="#e5e7eb"
            strokeWidth="1"
          />
        );
      })}
      
      {/* Benchmark polygon */}
      {benchmarkPath && (
        <path 
          d={benchmarkPath} 
          fill="rgba(156, 163, 175, 0.2)" 
          stroke="#9ca3af" 
          strokeWidth="2"
          strokeDasharray="4"
        />
      )}
      
      {/* Data polygon */}
      <path 
        d={dataPath} 
        fill="rgba(59, 130, 246, 0.3)" 
        stroke="#3b82f6" 
        strokeWidth="2"
      />
      
      {/* Data points */}
      {dataPoints.map((p, i) => (
        <circle 
          key={i} 
          cx={p.x} 
          cy={p.y} 
          r="4" 
          fill="#3b82f6" 
        />
      ))}
      
      {/* Labels */}
      {dimensions.map((dim, i) => {
        const angle = i * angleStep - Math.PI / 2;
        const labelRadius = radius + 25;
        const x = center + labelRadius * Math.cos(angle);
        const y = center + labelRadius * Math.sin(angle);
        return (
          <text 
            key={dim} 
            x={x} 
            y={y} 
            textAnchor="middle" 
            dominantBaseline="middle"
            className="text-xs fill-gray-600"
          >
            {dim.replace('_', ' ')}
          </text>
        );
      })}
    </svg>
  );
};

// 2. Health Gauge Component
interface HealthGaugeProps {
  score: number;
  grade: string;
  size?: number;
}

export const HealthGauge: React.FC<HealthGaugeProps> = ({ 
  score, 
  grade, 
  size = 200 
}) => {
  const getColor = (score: number) => {
    if (score >= 80) return '#10b981';
    if (score >= 60) return '#3b82f6';
    if (score >= 40) return '#f59e0b';
    return '#ef4444';
  };
  
  const getGrade = (score: number) => {
    if (score >= 90) return 'A+';
    if (score >= 80) return 'A';
    if (score >= 70) return 'B+';
    if (score >= 60) return 'B';
    if (score >= 50) return 'C';
    if (score >= 40) return 'D';
    return 'F';
  };
  
  const circumference = 2 * Math.PI * (size / 2 - 20);
  const strokeDasharray = `${(score / 100) * circumference} ${circumference}`;
  
  return (
    <div className="relative inline-flex items-center justify-center">
      <svg width={size} height={size}>
        {/* Background circle */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={size / 2 - 20}
          fill="none"
          stroke="#e5e7eb"
          strokeWidth="12"
        />
        
        {/* Score arc */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={size / 2 - 20}
          fill="none"
          stroke={getColor(score)}
          strokeWidth="12"
          strokeLinecap="round"
          strokeDasharray={strokeDasharray}
          transform={`rotate(-90 ${size / 2} ${size / 2})`}
        />
        
        {/* Score text */}
        <text
          x={size / 2}
          y={size / 2 - 10}
          textAnchor="middle"
          className="text-3xl font-bold fill-gray-800"
        >
          {score}
        </text>
        
        {/* Grade text */}
        <text
          x={size / 2}
          y={size / 2 + 15}
          textAnchor="middle"
          className="text-lg fill-gray-600"
        >
          Grade: {grade || getGrade(score)}
        </text>
      </svg>
    </div>
  );
};

// 3. Trend Line Component
interface TrendLineProps {
  data: { date: string; value: number }[];
  color?: string;
  height?: number;
  showArea?: boolean;
}

export const TrendLine: React.FC<TrendLineProps> = ({ 
  data, 
  color = '#3b82f6', 
  height = 100,
  showArea = true 
}) => {
  const width = 300;
  const padding = 20;
  
  if (data.length === 0) return null;
  
  const values = data.map(d => d.value);
  const max = Math.max(...values);
  const min = Math.min(...values);
  const range = max - min || 1;
  
  const points = data.map((d, i) => {
    const x = padding + (i / (data.length - 1)) * (width - 2 * padding);
    const y = padding + (1 - (d.value - min) / range) * (height - 2 * padding);
    return { x, y };
  });
  
  const linePath = points.map((p, i) => 
    (i === 0 ? 'M' : 'L') + `${p.x},${p.y}`
  ).join(' ');
  
  const areaPath = linePath + 
    ` L${points[points.length - 1].x},${height - padding}` +
    ` L${points[0].x},${height - padding} Z`;
  
  return (
    <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-auto">
      {/* Grid lines */}
      {[0, 0.25, 0.5, 0.75, 1].map((ratio, i) => (
        <line
          key={i}
          x1={padding}
          y1={padding + ratio * (height - 2 * padding)}
          x2={width - padding}
          y2={padding + ratio * (height - 2 * padding)}
          stroke="#f3f4f6"
          strokeWidth="1"
        />
      ))}
      
      {/* Area fill */}
      {showArea && (
        <path 
          d={areaPath} 
          fill={`${color}20`} 
        />
      )}
      
      {/* Line */}
      <path 
        d={linePath} 
        fill="none" 
        stroke={color} 
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      
      {/* Data points */}
      {points.map((p, i) => (
        <circle 
          key={i} 
          cx={p.x} 
          cy={p.y} 
          r="3" 
          fill={color} 
        />
      ))}
    </svg>
  );
};

// 4. Risk Heatmap Component
interface RiskHeatmapProps {
  risks: Record<string, number>;
}

export const RiskHeatmap: React.FC<RiskHeatmapProps> = ({ risks }) => {
  const getColor = (risk: number) => {
    if (risk < 25) return 'bg-green-100 text-green-800';
    if (risk < 50) return 'bg-yellow-100 text-yellow-800';
    if (risk < 75) return 'bg-orange-100 text-orange-800';
    return 'bg-red-100 text-red-800';
  };
  
  return (
    <div className="grid grid-cols-3 gap-2">
      {Object.entries(risks).map(([dimension, risk]) => (
        <div
          key={dimension}
          className={`p-3 rounded ${getColor(risk)}`}
        >
          <div className="text-xs font-medium">
            {dimension.replace('_', ' ')}
          </div>
          <div className="text-lg font-bold">{risk}%</div>
        </div>
      ))}
    </div>
  );
};

// 5. Industry Comparison Bar
interface IndustryComparisonProps {
  msmeScore: number;
  industryAvg: number;
  dimension: string;
  p25?: number;
  p75?: number;
}

export const IndustryComparison: React.FC<IndustryComparisonProps> = ({
  msmeScore,
  industryAvg,
  dimension,
  p25 = 35,
  p75 = 75
}) => {
  const getStatusColor = () => {
    if (msmeScore > industryAvg) return 'text-green-600';
    if (msmeScore < industryAvg) return 'text-red-600';
    return 'text-gray-600';
  };
  
  const getBarColor = () => {
    if (msmeScore >= p75) return 'bg-green-500';
    if (msmeScore >= industryAvg) return 'bg-blue-500';
    if (msmeScore >= p25) return 'bg-yellow-500';
    return 'bg-red-500';
  };
  
  return (
    <div className="space-y-1">
      <div className="flex justify-between text-sm">
        <span className="font-medium">{dimension.replace('_', ' ')}</span>
        <span className={getStatusColor()}>
          {msmeScore > industryAvg ? '↑' : msmeScore < industryAvg ? '↓' : '='} 
          {' '}{Math.abs(msmeScore - industryAvg).toFixed(1)}
        </span>
      </div>
      <div className="relative h-6 bg-gray-100 rounded">
        {/* Percentile range indicator */}
        <div 
          className="absolute h-6 bg-gray-200 rounded"
          style={{ 
            left: `${p25}%`, 
            width: `${p75 - p25}%` 
          }}
        />
        
        {/* MSME score bar */}
        <div 
          className={`absolute h-6 rounded ${getBarColor()}`}
          style={{ width: `${msmeScore}%` }}
        />
        
        {/* Industry average marker */}
        <div 
          className="absolute h-6 w-1 bg-gray-800"
          style={{ left: `${industryAvg}%` }}
        />
      </div>
      <div className="flex justify-between text-xs text-gray-500">
        <span>P25: {p25}</span>
        <span>Avg: {industryAvg}</span>
        <span>P75: {p75}</span>
      </div>
    </div>
  );
};

// 6. Score Timeline with Annotations
interface ScoreTimelineProps {
  timeline: { date: string; score: number; event?: string }[];
}

export const ScoreTimeline: React.FC<ScoreTimelineProps> = ({ timeline }) => {
  const getScoreColor = (score: number) => {
    if (score >= 70) return 'bg-green-500';
    if (score >= 50) return 'bg-blue-500';
    if (score >= 30) return 'bg-yellow-500';
    return 'bg-red-500';
  };
  
  return (
    <div className="space-y-3">
      {timeline.map((point, i) => (
        <div key={i} className="flex items-start gap-4">
          <div className="flex flex-col items-center">
            <div className={`w-3 h-3 rounded-full ${getScoreColor(point.score)}`} />
            {i < timeline.length - 1 && (
              <div className="w-0.5 h-8 bg-gray-200" />
            )}
          </div>
          <div className="flex-1 pb-4">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium">{point.date}</span>
              <span className="text-lg font-bold">{point.score}</span>
            </div>
            {point.event && (
              <p className="text-xs text-gray-500 mt-1">{point.event}</p>
            )}
          </div>
        </div>
      ))}
    </div>
  );
};

// 7. Loan Readiness Meter
interface LoanReadinessMeterProps {
  readiness: number;
  probability: number;
  factors: { name: string; score: number }[];
}

export const LoanReadinessMeter: React.FC<LoanReadinessMeterProps> = ({
  readiness,
  probability,
  factors
}) => {
  const getReadinessColor = (value: number) => {
    if (value >= 70) return 'bg-green-500';
    if (value >= 50) return 'bg-yellow-500';
    return 'bg-red-500';
  };
  
  return (
    <div className="space-y-6">
      {/* Main meters */}
      <div className="space-y-4">
        <div>
          <div className="flex justify-between mb-1">
            <span className="text-sm font-medium">Loan Readiness</span>
            <span className="text-sm font-bold">{readiness}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-4">
            <div 
              className={`h-4 rounded-full ${getReadinessColor(readiness)}`}
              style={{ width: `${readiness}%` }}
            />
          </div>
        </div>
        
        <div>
          <div className="flex justify-between mb-1">
            <span className="text-sm font-medium">Approval Probability</span>
            <span className="text-sm font-bold">{probability}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-4">
            <div 
              className="h-4 rounded-full bg-blue-500"
              style={{ width: `${probability}%` }}
            />
          </div>
        </div>
      </div>
      
      {/* Factors breakdown */}
      <div className="space-y-2">
        <h4 className="font-medium text-sm">Contributing Factors</h4>
        {factors.map((factor, i) => (
          <div key={i} className="flex items-center gap-2">
            <span className="text-xs w-24 truncate">{factor.name}</span>
            <div className="flex-1 bg-gray-100 rounded-full h-2">
              <div 
                className={`h-2 rounded-full ${
                  factor.score >= 70 ? 'bg-green-500' :
                  factor.score >= 50 ? 'bg-blue-500' : 'bg-yellow-500'
                }`}
                style={{ width: `${factor.score}%` }}
              />
            </div>
            <span className="text-xs font-medium w-8">{factor.score}</span>
          </div>
        ))}
      </div>
    </div>
  );
};
```

---

## 10.4 API ENDPOINTS SUMMARY

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/benchmarking/industry` | POST | Get industry benchmark |
| `/api/v1/benchmarking/compare` | POST | Compare with industry |
| `/api/v1/benchmarking/top-performers/{industry}` | GET | Get top performers |
| `/api/v1/benchmarking/improvements` | POST | Get improvement areas |

---

## 10.5 ESTIMATED DEVELOPMENT TIME

| Component | Time |
|-----------|------|
| AI Benchmarking Service | 2 days |
| Visual Analytics Components | 3 days |
| Integration | 2 days |
| Testing | 1 day |
| **Total** | **8 days** |

---

## 10.6 HACKATHON PRIORITY

**MEDIUM-HIGH** - Visual appeal and industry comparison add value
