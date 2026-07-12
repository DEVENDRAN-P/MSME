import { useQuery } from '@tanstack/react-query';
import { getMyFeatures } from '../../services/featureService';
import { motion } from 'framer-motion';
import {
  FileSpreadsheet,
  Coins,
  AlertTriangle,
  Lightbulb,
  Compass,
  ArrowLeft
} from 'lucide-react';

import { useNavigate } from 'react-router-dom';

export const FeatureIntelligence = () => {
  const navigate = useNavigate();

  const { data: features, isLoading, error } = useQuery({
    queryKey: ['my-features'],
    queryFn: getMyFeatures,
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
          Computing multi-dimensional features in Python...
        </p>
      </div>
    );
  }

  // Handle missing data / AI engine offline error state
  const isEngineOffline = error || !features;

  return (
    <div className="min-h-screen bg-slate-50 p-6 md:p-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex items-center space-x-4 mb-8">
          <button
            onClick={() => navigate('/msme')}
            className="p-2 bg-white hover:bg-slate-100 rounded-xl border border-slate-200 transition text-slate-600"
          >
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="text-3xl font-black text-slate-900 tracking-tight m-0">Credit Feature Intelligence</h1>
            <p className="text-slate-500 text-sm mt-1">Multi-dimensional credit vectors computed by the XGBoost feature engine.</p>
          </div>
        </div>

        {isEngineOffline ? (
          /* Error / Sync Required State */
          <div className="bg-white rounded-2xl border border-slate-150 p-8 text-center shadow-sm max-w-xl mx-auto mt-12">
            <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-amber-50 text-amber-600 mb-6">
              <AlertTriangle size={28} />
            </div>
            <h3 className="text-xl font-bold text-slate-800">AI Calculation Engine Offline</h3>
            <p className="text-slate-500 text-sm mt-3 leading-relaxed">
              We could not compute features. This may happen if:
            </p>
            <ul className="text-left text-xs text-slate-600 space-y-2 mt-4 max-w-sm mx-auto list-disc pl-5">
              <li>The Python FastAPI AI service on port 8000 is not running.</li>
              <li>You have not connected and synced your Alternate Data streams.</li>
            </ul>
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
                Retry Calculation
              </button>
            </div>
          </div>
        ) : (
          /* Feature Metrics Grid */
          <div className="space-y-8">
            {/* Section 1: GST & Compliance */}
            <div>
              <div className="flex items-center space-x-2 pb-2 mb-6 border-b border-slate-200 text-primary-900">
                <FileSpreadsheet size={20} />
                <h3 className="text-sm font-black uppercase tracking-wider">GST Compliance & Revenue Growth</h3>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <MetricCard
                  title="Turnover Growth Rate"
                  value={`${(features.gst_turnover_growth_rate * 100).toFixed(1)}%`}
                  label="MoM Average Growth"
                  rating={features.gst_turnover_growth_rate > 0.05 ? 'EXCELLENT' : features.gst_turnover_growth_rate > 0 ? 'GOOD' : 'STAGNANT'}
                  progress={Math.min(Math.max((features.gst_turnover_growth_rate + 0.1) * 333, 0), 100)} // scale growth (-10% to +20%) to 0-100
                  desc="Measures month-on-month sales traction. Sustained growth demonstrates market expansion and demand scalability."
                />
                <MetricCard
                  title="Revenue Volatility (CV)"
                  value={features.gst_turnover_volatility.toFixed(2)}
                  label="Coefficient of Variation"
                  rating={features.gst_turnover_volatility < 0.15 ? 'EXCELLENT' : features.gst_turnover_volatility < 0.30 ? 'MODERATE' : 'VOLATILE'}
                  progress={Math.max(100 - (features.gst_turnover_volatility * 200), 0)} // lower volatility is better (high progress)
                  desc="Measures cash flow consistency. Lower variance implies a predictable receipt pattern, which reduces the risk of default."
                />
                <MetricCard
                  title="Filing Compliance Rate"
                  value={`${(features.gst_filing_discipline_ratio * 100).toFixed(0)}%`}
                  label="On-Time Tax Filings"
                  rating={features.gst_filing_discipline_ratio > 0.95 ? 'EXCELLENT' : features.gst_filing_discipline_ratio > 0.85 ? 'GOOD' : 'RISK'}
                  progress={features.gst_filing_discipline_ratio * 100}
                  desc="Tax filings consistency. Punctuality in GSTR G2B records signals solid corporate governance and operation compliance."
                />
              </div>
            </div>

            {/* Section 2: Cash Flow and Liquidity */}
            <div>
              <div className="flex items-center space-x-2 pb-2 mb-6 border-b border-slate-200 text-primary-900">
                <Coins size={20} />
                <h3 className="text-sm font-black uppercase tracking-wider">Banking & Cash Flow Liquidity</h3>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <MetricCard
                  title="Cash Coverage Ratio"
                  value={`${features.bank_cash_coverage_ratio.toFixed(2)}x`}
                  label="Inward vs Outward Remittances"
                  rating={features.bank_cash_coverage_ratio > 1.05 ? 'EXCELLENT' : features.bank_cash_coverage_ratio >= 1.0 ? 'HEALTHY' : 'DEFICIT'}
                  progress={Math.min(features.bank_cash_coverage_ratio * 70, 100)}
                  desc="Total deposits divided by withdrawals. A value above 1.0x indicates the enterprise generates surplus cash flow to fund operational costs and pay debt interest."
                />
                <MetricCard
                  title="MAB to Turnover Ratio"
                  value={`${(features.bank_mab_to_turnover_ratio * 100).toFixed(1)}%`}
                  label="Monthly Average Balance / Revenue"
                  rating={features.bank_mab_to_turnover_ratio > 0.15 ? 'EXCELLENT' : features.bank_mab_to_turnover_ratio > 0.08 ? 'GOOD' : 'LOW'}
                  progress={Math.min(features.bank_mab_to_turnover_ratio * 400, 100)} // 25% average MAB is 100% progress
                  desc="Monthly Average Balance relative to revenue. Reflects liquidity reserves. A high balance ratio provides insulation against temporary sales contractions."
                />
              </div>
            </div>

            {/* Section 3: Digital footprint, payroll & payment compliance */}
            <div>
              <div className="flex items-center space-x-2 pb-2 mb-6 border-b border-slate-200 text-primary-900">
                <Compass size={20} />
                <h3 className="text-sm font-black uppercase tracking-wider">Payroll, Digital Adoption & Utility Discipline</h3>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <MetricCard
                  title="UPI Adoption Quotient"
                  value={`${(features.upi_penetration_ratio * 100).toFixed(0)}%`}
                  label="UPI Receipts Share of Revenue"
                  rating={features.upi_penetration_ratio > 0.5 ? 'HIGH ADOPTION' : features.upi_penetration_ratio > 0.2 ? 'MODERATE' : 'TRADITIONAL'}
                  progress={features.upi_penetration_ratio * 100}
                  desc="Portion of overall revenue transacted via UPI. High digital footprint signals modern retail integration and transparent receipts."
                />
                <MetricCard
                  title="Utility Default Ratio"
                  value={`${(features.utility_late_payment_ratio * 100).toFixed(0)}%`}
                  label="Utility Arrears & Delayed Bills"
                  rating={features.utility_late_payment_ratio === 0 ? 'EXCELLENT' : features.utility_late_payment_ratio < 0.15 ? 'GOOD' : 'RISK'}
                  progress={Math.max(100 - (features.utility_late_payment_ratio * 100), 0)} // lower default is better (high progress)
                  desc="Frequency of telecom, power, or water bills paid late. Punctuality here is a strong proxy for operational creditworthiness."
                />
                <MetricCard
                  title="EPFO Workforce Growth"
                  value={`${(features.epfo_employee_growth_rate * 100).toFixed(1)}%`}
                  label="Employee Count Change"
                  rating={features.epfo_employee_growth_rate > 0.1 ? 'EXPANDING' : features.epfo_employee_growth_rate >= 0 ? 'STABLE' : 'CONTRACTING'}
                  progress={Math.min(Math.max((features.epfo_employee_growth_rate + 0.15) * 333, 0), 100)} // scale -15% to +15%
                  desc="12-month change in employee count. An expanding workforce signifies growth stability and operational scaling."
                />
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

// Helper component for metric card rendering
interface MetricCardProps {
  title: string;
  value: string;
  label: string;
  rating: string;
  progress: number;
  desc: string;
}

const MetricCard = ({ title, value, label, rating, progress, desc }: MetricCardProps) => {
  const getRatingColor = (rate: string) => {
    if (rate.includes('EXCELLENT') || rate.includes('HEALTHY') || rate.includes('ADOPTION') || rate.includes('EXPANDING')) {
      return 'bg-emerald-50 text-emerald-700 border-emerald-100';
    }
    if (rate.includes('GOOD') || rate.includes('STABLE') || rate.includes('MODERATE')) {
      return 'bg-blue-50 text-blue-700 border-blue-100';
    }
    return 'bg-rose-50 text-rose-700 border-rose-100';
  };

  const getBarColor = (rate: string) => {
    if (rate.includes('EXCELLENT') || rate.includes('HEALTHY') || rate.includes('ADOPTION') || rate.includes('EXPANDING')) {
      return 'bg-emerald-500';
    }
    if (rate.includes('GOOD') || rate.includes('STABLE') || rate.includes('MODERATE')) {
      return 'bg-blue-500';
    }
    return 'bg-rose-500';
  };

  return (
    <div className="bg-white rounded-2xl border border-slate-150 p-6 shadow-sm flex flex-col justify-between h-full">
      <div>
        <div className="flex justify-between items-start">
          <div>
            <h4 className="text-xs font-black text-slate-500 uppercase tracking-wider">{title}</h4>
            <p className="text-3xl font-black text-slate-900 mt-2 tracking-tight">{value}</p>
            <p className="text-[10px] text-slate-400 font-semibold mt-1 uppercase tracking-wide">{label}</p>
          </div>
          <span className={`px-2.5 py-1 text-[10px] font-black rounded-md border ${getRatingColor(rating)}`}>
            {rating}
          </span>
        </div>

        {/* Progress bar */}
        <div className="mt-5 h-2 w-full bg-slate-100 rounded-full overflow-hidden">
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: `${progress}%` }}
            transition={{ duration: 0.8, ease: 'easeOut' }}
            className={`h-full rounded-full ${getBarColor(rating)}`}
          />
        </div>
      </div>

      <div className="mt-6 pt-4 border-t border-slate-100 flex items-start space-x-2 text-xs text-slate-500 leading-normal">
        <Lightbulb size={16} className="text-accent shrink-0 mt-0.5" />
        <p>{desc}</p>
      </div>
    </div>
  );
};

export default FeatureIntelligence;
