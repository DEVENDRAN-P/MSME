import { useQuery } from '@tanstack/react-query';
import { getMyHealthCard } from '../../services/healthCardService';
import { motion } from 'framer-motion';
import {
  TrendingUp,
  FileCheck,
  Coins,
  Users,
  Lightbulb,
  Download,
  Award,
  AlertTriangle,
  ArrowLeft
} from 'lucide-react';

import { useNavigate } from 'react-router-dom';

export const FinancialHealth = () => {
  const navigate = useNavigate();

  const { data: card, isLoading, error } = useQuery({
    queryKey: ['my-health-card'],
    queryFn: getMyHealthCard,
    retry: false
  });

  if (isLoading) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center bg-primary-900 text-white">
        <div className="relative flex h-20 w-20 items-center justify-center">
          <div className="absolute h-16 w-16 animate-spin rounded-full border-4 border-solid border-accent border-t-transparent"></div>
          <span className="font-semibold text-accent text-sm">IDBI</span>
        </div>
        <p className="mt-4 text-slate-300 animate-pulse text-sm font-medium tracking-wide">
          Running scoring neural network ensemble in Python...
        </p>
      </div>
    );
  }

  const isEngineOffline = error || !card;

  // Math for SVG Semi-Circle Gauge Arc
  // Score range: 300 to 900 (span of 600)
  const score = card?.unified_score || 300;
  const percentage = Math.min(Math.max((score - 300) / 600.0, 0), 1.0);
  const arcLength = 251.2; // Circumference of semicircle (pi * r where r = 80)
  const strokeOffset = arcLength - (arcLength * percentage);

  const getScoreColor = (scoreVal: number) => {
    if (scoreVal >= 750) return '#10b981'; // Saturated green
    if (scoreVal >= 680) return '#0c8fe3'; // Saturated blue
    if (scoreVal >= 600) return '#f59e0b'; // Saturated yellow/amber
    return '#ef4444'; // Red
  };

  const getGradeBadge = (gradeStr: string) => {
    switch (gradeStr) {
      case 'PRIME_PLUS':
        return 'bg-emerald-50 text-emerald-700 border-emerald-100';
      case 'PRIME':
        return 'bg-blue-50 text-blue-700 border-blue-100';
      case 'NEAR_PRIME':
        return 'bg-amber-50 text-amber-700 border-amber-100';
      case 'SUB_PRIME':
      default:
        return 'bg-rose-50 text-rose-700 border-rose-100';
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 p-6 md:p-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-8 gap-4">
          <div className="flex items-center space-x-4">
            <button
              onClick={() => navigate('/msme')}
              className="p-2 bg-white hover:bg-slate-100 rounded-xl border border-slate-200 transition text-slate-600"
            >
              <ArrowLeft size={18} />
            </button>
            <div>
              <h1 className="text-3xl font-black text-slate-900 tracking-tight m-0">Financial Health Card</h1>
              <p className="text-slate-500 text-sm mt-1">Alternate Credit Score card compiled via DPI registries integration.</p>
            </div>
          </div>

          {!isEngineOffline && (
            <button
              onClick={() => alert('Official PDF Scorecard generation is enabled for staging.')}
              className="flex items-center space-x-2 px-5 py-2.5 bg-white text-slate-700 border border-slate-250 rounded-xl font-bold text-xs hover:bg-slate-50 transition shadow-sm"
            >
              <Download size={14} />
              <span>Download credit report</span>
            </button>
          )}
        </div>

        {isEngineOffline ? (
          /* Error State */
          <div className="bg-white rounded-2xl border border-slate-150 p-8 text-center shadow-sm max-w-xl mx-auto mt-12">
            <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-amber-50 text-amber-600 mb-6">
              <AlertTriangle size={28} />
            </div>
            <h3 className="text-xl font-bold text-slate-800">Scoring Engine Unreachable</h3>
            <p className="text-slate-500 text-sm mt-3 leading-relaxed">
              We could not compile your credit health score. Please ensure you have ingested alternate transaction data and that the Python FastAPI AI service is active on port 8000.
            </p>
            <div className="mt-8 flex justify-center gap-4">
              <button
                onClick={() => navigate('/msme/data-ingest')}
                className="px-5 py-2.5 bg-primary text-white font-bold text-sm rounded-xl hover:bg-primary-800 transition"
              >
                Go Ingest Data
              </button>
              <button
                onClick={() => window.location.reload()}
                className="px-5 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-sm rounded-xl transition"
              >
                Retry Scoring
              </button>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Left: Gauge Scorecard card */}
            <div className="bg-white rounded-3xl p-8 border border-slate-150 shadow-sm flex flex-col justify-between items-center text-center">
              <div className="w-full">
                <span className="text-[10px] uppercase font-bold tracking-widest text-slate-400">Alternate Credit Rating</span>
                
                {/* SVG Gauge Semicircle */}
                <div className="relative mt-8 mb-4 flex justify-center">
                  <svg viewBox="0 0 200 120" className="w-64 h-40">
                    <path
                      d="M 20,100 A 80,80 0 0,1 180,100"
                      fill="none"
                      stroke="#f1f5f9"
                      strokeWidth="14"
                      strokeLinecap="round"
                    />
                    <motion.path
                      d="M 20,100 A 80,80 0 0,1 180,100"
                      fill="none"
                      stroke={getScoreColor(score)}
                      strokeWidth="14"
                      strokeLinecap="round"
                      strokeDasharray={arcLength}
                      initial={{ strokeDashoffset: arcLength }}
                      animate={{ strokeDashoffset: strokeOffset }}
                      transition={{ duration: 1.2, ease: 'easeOut' }}
                    />
                  </svg>
                  
                  {/* Floating Unified Score Display */}
                  <div className="absolute inset-0 flex flex-col items-center justify-end pb-3">
                    <span className="text-5xl font-black text-slate-900 tracking-tighter leading-none">{score}</span>
                    <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider mt-2">Score Range: 300-900</span>
                  </div>
                </div>

                <div className="flex justify-center mt-2">
                  <span className={`px-3 py-1 rounded-md text-xs font-black border uppercase tracking-wider ${getGradeBadge(card.grade)}`}>
                    Grade: {card.grade.replace('_', ' ')}
                  </span>
                </div>
              </div>

              <div className="mt-8 border-t border-slate-100 pt-6 w-full text-slate-600 text-sm leading-relaxed">
                <p className="font-semibold text-slate-800">Scoring analysis</p>
                <p className="text-slate-500 mt-2 text-xs">{card.description}</p>
              </div>
            </div>

            {/* Right: Sub-dimension scores & Credit Eligibility */}
            <div className="lg:col-span-2 space-y-8">
              {/* Credit Offerings Banner based on grade */}
              <div className="bg-gradient-to-r from-primary-900 to-primary-850 text-white rounded-3xl p-6 shadow-md relative overflow-hidden flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
                <div className="absolute top-0 right-0 w-64 h-64 bg-accent rounded-full blur-3xl opacity-10 -mr-20 -mt-20"></div>
                <div>
                  <div className="flex items-center space-x-2 text-accent">
                    <Award size={18} />
                    <span className="text-[10px] uppercase font-bold tracking-wider">Credit Pre-Approval Node</span>
                  </div>
                  {score >= 750 ? (
                    <>
                      <h3 className="text-xl font-bold mt-3 mb-1">Pre-Approved for up to ₹15,00,000</h3>
                      <p className="text-slate-300 text-xs">Collateral-free overdraft limits active based on consistent GSTR filings.</p>
                    </>
                  ) : score >= 680 ? (
                    <>
                      <h3 className="text-xl font-bold mt-3 mb-1">Pre-Eligible for up to ₹8,00,000</h3>
                      <p className="text-slate-300 text-xs">Collateral-free cash credit limit standing. Final bank officer approval required.</p>
                    </>
                  ) : score >= 600 ? (
                    <>
                      <h3 className="text-xl font-bold mt-3 mb-1">Eligible for Credit lines</h3>
                      <p className="text-slate-300 text-xs">Secured loans available. Underwriters may ask for invoice factoring links or a guarantor.</p>
                    </>
                  ) : (
                    <>
                      <h3 className="text-xl font-bold mt-3 mb-1">Credit Limits Restricted</h3>
                      <p className="text-slate-300 text-xs">Maintain punctual utility bill payments and lower turnover volatility to boost score.</p>
                    </>
                  )}
                </div>
                {score >= 680 && (
                  <button
                    onClick={() => alert('Launching pre-approved credit lines checkout...')}
                    className="px-5 py-2.5 bg-accent hover:bg-accent-600 text-primary-950 font-black text-xs rounded-xl shadow-md transition shrink-0 self-stretch md:self-auto text-center"
                  >
                    Apply Now
                  </button>
                )}
              </div>

              {/* Sub-Dimension Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <DimensionCard
                  icon={<TrendingUp className="text-emerald-600" />}
                  title="Revenue Health"
                  score={card.dimension_scores.revenue_health}
                  desc="Calculated from growth rate indices and month-on-month turnover consistency in GSTR filings."
                />
                <DimensionCard
                  icon={<FileCheck className="text-blue-600" />}
                  title="Compliance Health"
                  score={card.dimension_scores.compliance_health}
                  desc="Measures punctuality of GSTR-1 filings and electrical/telecom utility billing payments."
                />
                <DimensionCard
                  icon={<Coins className="text-indigo-650" />}
                  title="Liquidity Health"
                  score={card.dimension_scores.liquidity_health}
                  desc="Evaluates average bank ledger balances (MAB) and monthly cash inflows vs outflows coverage ratios."
                />
                <DimensionCard
                  icon={<Users className="text-orange-650" />}
                  title="Workforce & Digital Quotient"
                  score={card.dimension_scores.workforce_health}
                  desc="Incorporates EPFO employee payroll stability alongside QR merchant UPI payment penetration."
                />
              </div>

              {/* Explainable AI & Improvement Suggestions Section */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-8">
                {/* Left: Explainable AI Factors */}
                <div className="bg-white rounded-2xl border border-slate-150 p-6 shadow-sm flex flex-col justify-between">
                  <div>
                    <div className="flex justify-between items-center mb-4">
                      <h4 className="font-bold text-slate-800 text-sm">Credit Score Explainability (SHAP Contributions)</h4>
                      <span className="text-[10px] font-bold text-slate-500 bg-slate-100 px-2.5 py-1 rounded-lg">
                        Confidence: {Math.round((card.confidence || 0.95) * 100)}%
                      </span>
                    </div>
                    
                    {/* Positive Contributors */}
                    <div className="mb-4">
                      <span className="text-[10px] uppercase font-bold text-emerald-600 tracking-wider">Positive Contributors</span>
                      {card.positive_contributors && card.positive_contributors.length > 0 ? (
                        <ul className="space-y-2 mt-2">
                          {card.positive_contributors.map((c: string, idx: number) => (
                            <li key={idx} className="flex items-start text-xs text-slate-600">
                              <span className="text-emerald-500 mr-2 font-bold">✓</span>
                              <span>{c}</span>
                            </li>
                          ))}
                        </ul>
                      ) : (
                        <p className="text-xs text-slate-400 mt-2 italic">No significant positive boosters identified.</p>
                      )}
                    </div>

                    {/* Negative Contributors */}
                    <div>
                      <span className="text-[10px] uppercase font-bold text-rose-600 tracking-wider">Negative Contributors</span>
                      {card.negative_contributors && card.negative_contributors.length > 0 ? (
                        <ul className="space-y-2 mt-2">
                          {card.negative_contributors.map((c: string, idx: number) => (
                            <li key={idx} className="flex items-start text-xs text-slate-600">
                              <span className="text-rose-500 mr-2 font-bold">⚠</span>
                              <span>{c}</span>
                            </li>
                          ))}
                        </ul>
                      ) : (
                        <p className="text-xs text-slate-400 mt-2 italic">No significant negative drags detected.</p>
                      )}
                    </div>
                  </div>
                </div>

                {/* Right: Score Improvement Roadmap */}
                <div className="bg-white rounded-2xl border border-slate-150 p-6 shadow-sm flex flex-col justify-between">
                  <div>
                    <h4 className="font-bold text-slate-800 text-sm mb-4">Actionable Rating Improvement Suggestions</h4>
                    <div className="space-y-4">
                      {card.improvement_suggestions && card.improvement_suggestions.length > 0 ? (
                        card.improvement_suggestions.map((s: any, idx: number) => (
                          <div key={idx} className="flex justify-between items-start border-b border-slate-50 pb-3 last:border-b-0 last:pb-0">
                            <div className="flex items-start pr-4">
                              <span className="text-amber-500 mr-2 font-bold mt-0.5">•</span>
                              <span className="text-xs text-slate-600">{s.suggestion}</span>
                            </div>
                            <span className="text-xs font-extrabold text-amber-700 bg-amber-50 border border-amber-100 px-2 py-0.5 rounded shrink-0">
                              +{s.expected_improvement} points
                            </span>
                          </div>
                        ))
                      ) : (
                        <p className="text-xs text-slate-400 italic">No recommendations required. Maintain your current status.</p>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

// Sub-component card helper
interface DimensionCardProps {
  icon: React.ReactNode;
  title: string;
  score: number;
  desc: string;
}

const DimensionCard = ({ icon, title, score, desc }: DimensionCardProps) => {
  const getProgressColor = (val: number) => {
    if (val >= 80) return 'bg-emerald-500';
    if (val >= 60) return 'bg-blue-500';
    if (val >= 40) return 'bg-amber-500';
    return 'bg-rose-500';
  };

  return (
    <div className="bg-white rounded-2xl border border-slate-150 p-6 shadow-sm flex flex-col justify-between">
      <div>
        <div className="flex justify-between items-center mb-4">
          <div className="flex items-center space-x-2">
            <div className="bg-slate-50 p-2 rounded-xl border border-slate-100">{icon}</div>
            <h4 className="font-bold text-slate-800 text-sm">{title}</h4>
          </div>
          <span className="text-xl font-black text-slate-900">{score}/100</span>
        </div>
        
        {/* Progress indicator */}
        <div className="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden mb-4">
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: `${score}%` }}
            transition={{ duration: 0.8 }}
            className={`h-full rounded-full ${getProgressColor(score)}`}
          />
        </div>
      </div>

      <div className="pt-2 border-t border-slate-100 flex items-start space-x-1.5 text-xs text-slate-500 leading-normal">
        <Lightbulb size={15} className="text-accent shrink-0 mt-0.5" />
        <p>{desc}</p>
      </div>
    </div>
  );
};

export default FinancialHealth;
