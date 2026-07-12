import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getMyLoans, simulateForecast } from '../../services/loanService';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend
} from 'recharts';
import {
  TrendingUp,
  Coins,
  ShieldCheck,
  ArrowLeft,
  AlertTriangle,
  Lightbulb
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const ForecastSimulator = () => {
  const navigate = useNavigate();

  // 1. Fetch active loan ledger
  const { data: loans, isLoading: isLoansLoading } = useQuery({
    queryKey: ['my-loans'],
    queryFn: getMyLoans,
  });

  const activeLoan = loans && loans.length > 0 ? loans[0] : null;

  // Sliders State (defaults)
  const [amount, setAmount] = useState(500000); // 5L default
  const [rate, setRate] = useState(12.0); // 12% default
  const [tenure, setTenure] = useState(12); // 12 months default

  // Update slider variables if active loan exists to lock it to active values
  useEffect(() => {
    if (activeLoan) {
      setAmount(activeLoan.amount);
      setRate(activeLoan.interestRate);
      setTenure(activeLoan.tenureMonths);
    }
  }, [activeLoan]);

  // 2. Query: Simulate forecast based on slider inputs (auto-triggers on slider drag!)
  const { data: forecast, isLoading: isForecastLoading, error: forecastError } = useQuery({
    queryKey: ['forecast-sim', amount, rate, tenure],
    queryFn: () => simulateForecast(amount, rate, tenure),
    placeholderData: (prev) => prev, // keeps old chart rendered during drags!
  });

  // Format currency
  const formatCurrency = (value: number) => {
    if (value >= 100000) {
      return `₹${(value / 100000).toFixed(1)}L`;
    }
    return `₹${(value / 1000).toFixed(0)}k`;
  };

  const isEngineOffline = forecastError || !forecast;

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
            <h1 className="text-3xl font-black text-slate-900 tracking-tight m-0">Cash Flow Forecasting</h1>
            <p className="text-slate-500 text-sm mt-1">Simulate corporate cash reserves under different loan configurations.</p>
          </div>
        </div>

        {isLoansLoading ? (
          <div className="flex justify-center items-center py-20">
            <div className="animate-spin rounded-full h-10 w-10 border-4 border-solid border-primary border-t-transparent"></div>
          </div>
        ) : isEngineOffline ? (
          /* Error State */
          <div className="bg-white rounded-2xl border border-slate-150 p-8 text-center shadow-sm max-w-xl mx-auto mt-12">
            <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-amber-50 text-amber-600 mb-6">
              <AlertTriangle size={28} />
            </div>
            <h3 className="text-xl font-bold text-slate-800">Forecasting Model Offline</h3>
            <p className="text-slate-500 text-sm mt-3 leading-relaxed">
              We could not generate cash flow projections. Please ensure your Python FastAPI AI service is active on port 8000.
            </p>
            <button
              onClick={() => window.location.reload()}
              className="mt-6 px-6 py-2.5 bg-primary text-white font-bold text-sm rounded-xl hover:bg-primary-800 transition"
            >
              Retry Calculation
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Left Column: Sliders Sandbox OR Active Loan Card */}
            <div className="space-y-6">
              {activeLoan ? (
                /* Locked Card representing active disbursed loan */
                <div className="bg-gradient-to-br from-primary-900 to-primary-850 text-white rounded-3xl p-6 shadow-md relative overflow-hidden">
                  <div className="absolute top-0 right-0 w-48 h-48 bg-accent rounded-full blur-3xl opacity-10 -mr-16 -mt-16"></div>
                  <div className="flex items-center space-x-2 text-accent">
                    <ShieldCheck size={18} />
                    <span className="text-[10px] uppercase font-bold tracking-wider">Active Disbursement</span>
                  </div>
                  <h3 className="text-3xl font-black text-white mt-4 tracking-tight">₹{activeLoan.amount.toLocaleString('en-IN')}</h3>
                  <p className="text-xs text-slate-350 mt-1">Disbursed on: {new Date(activeLoan.disbursedAt).toLocaleDateString()}</p>
                  
                  <div className="border-t border-primary-700/60 pt-4 mt-6 grid grid-cols-2 gap-4 text-xs">
                    <div>
                      <p className="text-slate-400 font-bold uppercase tracking-wider text-[10px]">Interest Rate</p>
                      <p className="font-extrabold text-white mt-0.5">{activeLoan.interestRate}% P.A.</p>
                    </div>
                    <div>
                      <p className="text-slate-400 font-bold uppercase tracking-wider text-[10px]">Tenure</p>
                      <p className="font-extrabold text-white mt-0.5">{activeLoan.tenureMonths} Months</p>
                    </div>
                  </div>
                </div>
              ) : (
                /* Interactive Sliders Sandbox */
                <div className="bg-white rounded-3xl p-6 border border-slate-150 shadow-sm space-y-6">
                  <div>
                    <h3 className="text-sm font-black uppercase tracking-wider text-slate-700">Loan Sandbox Calculator</h3>
                    <p className="text-[10px] text-slate-400 mt-1">Drag sliders to adjust loan configurations.</p>
                  </div>

                  {/* Slider 1: Amount */}
                  <div>
                    <div className="flex justify-between text-xs font-bold text-slate-650 mb-2">
                      <span>Loan Amount</span>
                      <span className="text-primary-900">₹{(amount / 100000).toFixed(1)} Lakhs</span>
                    </div>
                    <input
                      type="range"
                      min={100000}
                      max={1500000}
                      step={50000}
                      value={amount}
                      onChange={(e) => setAmount(Number(e.target.value))}
                      className="w-full h-1 bg-slate-100 rounded-lg appearance-none cursor-pointer accent-primary"
                    />
                    <div className="flex justify-between text-[9px] text-slate-400 font-bold mt-1.5">
                      <span>₹1L</span>
                      <span>₹15L (Max)</span>
                    </div>
                  </div>

                  {/* Slider 2: Tenure */}
                  <div>
                    <div className="flex justify-between text-xs font-bold text-slate-650 mb-2">
                      <span>Tenure Months</span>
                      <span className="text-primary-900">{tenure} Months</span>
                    </div>
                    <input
                      type="range"
                      min={6}
                      max={24}
                      step={3}
                      value={tenure}
                      onChange={(e) => setTenure(Number(e.target.value))}
                      className="w-full h-1 bg-slate-100 rounded-lg appearance-none cursor-pointer accent-primary"
                    />
                    <div className="flex justify-between text-[9px] text-slate-400 font-bold mt-1.5">
                      <span>6m</span>
                      <span>24m</span>
                    </div>
                  </div>

                  {/* Slider 3: Interest Rate */}
                  <div>
                    <div className="flex justify-between text-xs font-bold text-slate-650 mb-2">
                      <span>Interest Rate (P.A.)</span>
                      <span className="text-primary-900">{rate.toFixed(1)}%</span>
                    </div>
                    <input
                      type="range"
                      min={10.0}
                      max={16.0}
                      step={0.5}
                      value={rate}
                      onChange={(e) => setRate(Number(e.target.value))}
                      className="w-full h-1 bg-slate-100 rounded-lg appearance-none cursor-pointer accent-primary"
                    />
                    <div className="flex justify-between text-[9px] text-slate-400 font-bold mt-1.5">
                      <span>10.0%</span>
                      <span>16.0%</span>
                    </div>
                  </div>
                </div>
              )}

              {/* Calculated EMI indicator */}
              {forecast && forecast.length > 0 && (
                <div className="bg-white rounded-3xl p-6 border border-slate-150 shadow-sm text-center">
                  <span className="text-[10px] uppercase font-bold tracking-widest text-slate-400">Estimated Monthly EMI</span>
                  <h3 className="text-3xl font-black text-slate-900 mt-3 tracking-tight">
                    ₹{forecast[0].emi.toLocaleString('en-IN', { maximumFractionDigits: 0 })}
                  </h3>
                  <div className="mt-4 pt-4 border-t border-slate-100 flex items-start space-x-1.5 text-xs text-slate-500 text-left leading-normal">
                    <Lightbulb size={16} className="text-accent shrink-0 mt-0.5" />
                    <p>
                      EMI values are calculated via standard bank amortization formulas. Net surplus reflects remaining cash after base costs and EMI repayments.
                    </p>
                  </div>
                </div>
              )}
            </div>

            {/* Right Column: Recharts Chart & Bullet Schedules */}
            <div className="lg:col-span-2 space-y-8">
              {/* Cash Flow Forecast Area Chart */}
              <div className="bg-white rounded-3xl p-6 border border-slate-150 shadow-sm flex flex-col">
                <div className="flex items-center justify-between mb-6">
                  <div className="flex items-center space-x-2">
                    <TrendingUp className="text-primary" size={20} />
                    <h3 className="text-sm font-bold uppercase tracking-wider text-slate-700">6-Month Cash Flow Forecast Simulation</h3>
                  </div>
                  {isForecastLoading && (
                    <div className="animate-spin rounded-full h-4 w-4 border-2 border-solid border-primary border-t-transparent"></div>
                  )}
                </div>

                <div className="h-80 w-full text-xs">
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={forecast}>
                      <defs>
                        <linearGradient id="colorSales" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#0c8fe3" stopOpacity={0.2}/>
                          <stop offset="95%" stopColor="#0c8fe3" stopOpacity={0}/>
                        </linearGradient>
                        <linearGradient id="colorSurplus" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#10b981" stopOpacity={0.2}/>
                          <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                      <XAxis dataKey="month" stroke="#94a3b8" />
                      <YAxis tickFormatter={formatCurrency} stroke="#94a3b8" />
                      <Tooltip formatter={(value: any) => formatCurrency(value)} />
                      <Legend />
                      <Area type="monotone" name="Projected Sales Inflow" dataKey="projectedSales" stroke="#0c8fe3" strokeWidth={2} fillOpacity={1} fill="url(#colorSales)" />
                      <Area type="monotone" name="Projected Net Surplus" dataKey="netSurplus" stroke="#10b981" strokeWidth={2} fillOpacity={1} fill="url(#colorSurplus)" />
                      <Area type="monotone" name="EMI Obligation" dataKey="emi" stroke="#ef4444" strokeWidth={1.5} fill="none" strokeDasharray="5 5" />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              </div>

              {/* Table/List: Forecast monthly list values */}
              <div className="bg-white rounded-3xl p-6 border border-slate-150 shadow-sm flex flex-col">
                <div className="flex items-center space-x-2 mb-4">
                  <Coins className="text-indigo-650" size={20} />
                  <h3 className="text-sm font-bold uppercase tracking-wider text-slate-700">Projected Amortization Timeline</h3>
                </div>
                <div className="overflow-x-auto">
                  <table className="min-w-full text-left text-sm text-slate-500">
                    <thead className="bg-slate-50 text-slate-700 text-xs font-bold uppercase tracking-wider">
                      <tr>
                        <th className="px-4 py-3">Month</th>
                        <th className="px-4 py-3">Projected Sales</th>
                        <th className="px-4 py-3">EMI Repayment</th>
                        <th className="px-4 py-3">Net Cash Surplus</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {forecast?.map((row, idx) => (
                        <tr key={idx} className="hover:bg-slate-50/50">
                          <td className="px-4 py-3 font-semibold text-slate-900">{row.month}</td>
                          <td className="px-4 py-3 text-slate-800">₹{row.projectedSales.toLocaleString('en-IN')}</td>
                          <td className="px-4 py-3 text-rose-600 font-medium">₹{row.emi.toLocaleString('en-IN')}</td>
                          <td className="px-4 py-3 text-emerald-700 font-bold">₹{row.netSurplus.toLocaleString('en-IN')}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ForecastSimulator;
