import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getIngestSummary, syncAlternateData } from '../../services/ingestService';
import { motion, AnimatePresence } from 'framer-motion';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  BarChart,
  Bar,
  LineChart,
  Line
} from 'recharts';
import {
  Database,
  RefreshCw,
  CheckCircle,
  XCircle,
  FileCheck,
  Smartphone,
  ShieldCheck,
  Users,
  Lightbulb,
  ShoppingBag,
  TrendingUp,
  ArrowLeft
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const SYNC_STEPS = [
  { id: 'gst', name: 'GSTN Gateway: Aggregating GSTR-1 Invoices' },
  { id: 'upi', name: 'UPI Gateway: Synced merchant QR credit/debit logs' },
  { id: 'aa', name: 'Account Aggregator: Pulling bank ledger logs' },
  { id: 'epfo', name: 'EPFO Registry: Verified payroll headcount contribution' },
  { id: 'utility', name: 'Utility Billing Hub: Fetching power & telecom records' },
  { id: 'ecomm', name: 'E-commerce Feed: Fetching Amazon/ONDC sales logs' },
];

export const DataIngestion = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [syncingStepIndex, setSyncingStepIndex] = useState(-1);
  const [isSyncModalOpen, setIsSyncModalOpen] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // Fetch ingestion status
  const { data: summary, isLoading } = useQuery({
    queryKey: ['ingest-summary'],
    queryFn: getIngestSummary,
    retry: false
  });

  // Sync Mutation
  const syncMutation = useMutation({
    mutationFn: () => syncAlternateData('ALL'),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ingest-summary'] });
    }
  });

  const handleSyncAll = async () => {
    setIsSyncModalOpen(true);
    setErrorMsg(null);

    // Simulate step-by-step sync animation for hackathon visual experience
    for (let index = 0; index < SYNC_STEPS.length; index++) {
      setSyncingStepIndex(index);
      await new Promise((resolve) => setTimeout(resolve, 800));
    }

    try {
      await syncMutation.mutateAsync();
      setSyncingStepIndex(SYNC_STEPS.length); // All complete
      setTimeout(() => {
        setIsSyncModalOpen(false);
        setSyncingStepIndex(-1);
      }, 1500);
    } catch (err: any) {
      setErrorMsg(err.toString());
      setSyncingStepIndex(-1);
      setIsSyncModalOpen(false);
    }
  };

  // Formatter for Currency (INR Lakhs/Thousands)
  const formatCurrency = (value: number) => {
    if (value >= 100000) {
      return `₹${(value / 100000).toFixed(1)}L`;
    }
    return `₹${(value / 1000).toFixed(0)}k`;
  };

  const isDataEmpty = !summary || (!summary.gstSynced && !summary.upiSynced);

  return (
    <div className="min-h-screen bg-slate-50 p-6 md:p-8">
      <div className="max-w-7xl mx-auto">
        {/* Back Button & Header */}
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-8 gap-4">
          <div className="flex items-center space-x-4">
            <button
              onClick={() => navigate('/msme')}
              className="p-2 bg-white hover:bg-slate-100 rounded-xl border border-slate-200 transition text-slate-600"
            >
              <ArrowLeft size={18} />
            </button>
            <div>
              <h1 className="text-3xl font-black text-slate-900 tracking-tight m-0">Alternate Data Ingestion</h1>
              <p className="text-slate-500 text-sm mt-1">Simulate linkages to tax, utility, and bank registries.</p>
            </div>
          </div>

          <button
            onClick={handleSyncAll}
            disabled={syncMutation.isPending || isLoading}
            className="flex items-center space-x-2 px-6 py-3 bg-primary text-white rounded-xl shadow-md font-bold text-sm hover:bg-primary-800 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            <RefreshCw size={16} className={syncMutation.isPending ? 'animate-spin' : ''} />
            <span>Sync All alternate data</span>
          </button>
        </div>

        {/* Sync Error Alert */}
        {errorMsg && (
          <div className="mb-6 p-4 rounded-xl bg-red-50 border border-red-100 flex items-start space-x-3 text-red-700 text-sm">
            <XCircle className="h-5 w-5 shrink-0 text-red-500 mt-0.5" />
            <div>
              <span className="font-semibold block">Sync Error</span>
              <span>{errorMsg}</span>
            </div>
          </div>
        )}

        {/* Integration Feeds Status Grid */}
        <div className="grid grid-cols-1 md:grid-cols-6 gap-5 mb-8">
          <StatusCard
            icon={<FileCheck className="text-blue-600" />}
            title="GSTN Invoices"
            desc="GSTR-1 & GSTR-3B filings"
            isSynced={summary?.gstSynced}
          />
          <StatusCard
            icon={<Smartphone className="text-emerald-600" />}
            title="UPI payment logs"
            desc="Merchant payment entries"
            isSynced={summary?.upiSynced}
          />
          <StatusCard
            icon={<ShieldCheck className="text-indigo-600" />}
            title="Account Aggregator"
            desc="Direct bank current ledgers"
            isSynced={summary?.aaSynced}
          />
          <StatusCard
            icon={<Users className="text-orange-600" />}
            title="EPFO Registry"
            desc="Payroll stability metrics"
            isSynced={summary?.epfoSynced}
          />
          <StatusCard
            icon={<Lightbulb className="text-amber-500" />}
            title="Utility billing"
            desc="Electricity, telecom bills"
            isSynced={summary?.utilitySynced}
          />
          <StatusCard
            icon={<ShoppingBag className="text-rose-600" />}
            title="E-commerce feeds"
            desc="Amazon & ONDC online receipts"
            isSynced={summary?.ecommSynced}
          />
        </div>

        {isDataEmpty ? (
          /* Empty State */
          <div className="bg-white rounded-2xl border border-slate-100 p-12 text-center shadow-sm max-w-xl mx-auto mt-12">
            <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-slate-100 text-slate-400 mb-6">
              <Database size={28} />
            </div>
            <h3 className="text-xl font-bold text-slate-800">Alternate Data Streams Empty</h3>
            <p className="text-slate-500 text-sm mt-3 leading-relaxed">
              No transactional data has been ingested yet. Click the sync button above to trigger sandbox simulators and generate 12 months of sector-appropriate business data.
            </p>
            <button
              onClick={handleSyncAll}
              className="mt-6 px-6 py-2.5 bg-primary text-white font-bold text-sm rounded-xl hover:bg-primary-800 transition"
            >
              Sync Alternate Data Now
            </button>
          </div>
        ) : (
          /* Ingested Charts & Timelines */
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Chart 1: Revenue vs UPI Credit Receipts */}
            <div className="bg-white rounded-2xl p-6 border border-slate-150 shadow-sm flex flex-col">
              <div className="flex items-center space-x-2 mb-6">
                <TrendingUp className="text-primary-600" size={20} />
                <h3 className="text-sm font-bold uppercase tracking-wider text-slate-700">GST Turnover vs UPI Credit Receipts</h3>
              </div>
              <div className="h-80 w-full text-xs">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={summary.gstRecords.map((item, idx) => ({
                    month: item.month,
                    turnover: item.turnover,
                    upiCredit: summary.upiRecords[idx]?.creditVolume || 0,
                  }))}>
                    <defs>
                      <linearGradient id="colorTurnover" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#0a1e3f" stopOpacity={0.2}/>
                        <stop offset="95%" stopColor="#0a1e3f" stopOpacity={0}/>
                      </linearGradient>
                      <linearGradient id="colorUpi" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#10b981" stopOpacity={0.2}/>
                        <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                    <XAxis dataKey="month" stroke="#94a3b8" />
                    <YAxis tickFormatter={formatCurrency} stroke="#94a3b8" />
                    <Tooltip formatter={(value: any) => formatCurrency(value)} />
                    <Legend />
                    <Area type="monotone" name="GST Declared Revenue" dataKey="turnover" stroke="#0a1e3f" strokeWidth={2.5} fillOpacity={1} fill="url(#colorTurnover)" />
                    <Area type="monotone" name="UPI Credit collections" dataKey="upiCredit" stroke="#10b981" strokeWidth={2} fillOpacity={1} fill="url(#colorUpi)" />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Chart 2: Account Aggregator Bank Inflow vs Outflow */}
            <div className="bg-white rounded-2xl p-6 border border-slate-150 shadow-sm flex flex-col">
              <div className="flex items-center space-x-2 mb-6">
                <ShieldCheck className="text-indigo-600" size={20} />
                <h3 className="text-sm font-bold uppercase tracking-wider text-slate-700">Bank Inflows vs Outflows (Account Aggregator)</h3>
              </div>
              <div className="h-80 w-full text-xs">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={summary.bankRecords}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                    <XAxis dataKey="month" stroke="#94a3b8" />
                    <YAxis tickFormatter={formatCurrency} stroke="#94a3b8" />
                    <Tooltip formatter={(value: any) => formatCurrency(value)} />
                    <Legend />
                    <Bar name="Deposits (Inward Remittances)" dataKey="inflows" fill="#0c8fe3" radius={[4, 4, 0, 0]} />
                    <Bar name="Withdrawals (Outflow)" dataKey="outflows" fill="#ef4444" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Chart 3: EPFO Workforce Index */}
            {summary.epfoSynced && (
              <div className="bg-white rounded-2xl p-6 border border-slate-150 shadow-sm flex flex-col">
                <div className="flex items-center space-x-2 mb-6">
                  <Users className="text-orange-600" size={20} />
                  <h3 className="text-sm font-bold uppercase tracking-wider text-slate-700">Employee Stability Index (EPFO Payroll)</h3>
                </div>
                <div className="h-80 w-full text-xs">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={summary.epfoRecords}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                      <XAxis dataKey="month" stroke="#94a3b8" />
                      <YAxis stroke="#94a3b8" />
                      <Tooltip />
                      <Legend />
                      <Line type="monotone" name="Active Employees Count" dataKey="employeeCount" stroke="#f59e0b" strokeWidth={2.5} activeDot={{ r: 8 }} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </div>
            )}

            {/* Table: GST Filings Record Summary */}
            <div className="bg-white rounded-2xl p-6 border border-slate-150 shadow-sm flex flex-col">
              <div className="flex items-center space-x-2 mb-4">
                <FileCheck className="text-blue-600" size={20} />
                <h3 className="text-sm font-bold uppercase tracking-wider text-slate-700">GST filing record logs</h3>
              </div>
              <div className="overflow-x-auto flex-grow">
                <table className="min-w-full text-left text-sm text-slate-500">
                  <thead className="bg-slate-50 text-slate-700 text-xs font-bold uppercase tracking-wider">
                    <tr>
                      <th className="px-4 py-3">Filing Month</th>
                      <th className="px-4 py-3">Declared Turnover</th>
                      <th className="px-4 py-3">Tax Paid (GSTR-3B)</th>
                      <th className="px-4 py-3">Filing Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {summary.gstRecords.slice(0, 6).map((record, index) => (
                      <tr key={index} className="hover:bg-slate-50/50">
                        <td className="px-4 py-3 font-semibold text-slate-900">{record.month}</td>
                        <td className="px-4 py-3 text-slate-800">₹{record.turnover.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                        <td className="px-4 py-3 text-slate-800">₹{record.taxPaid.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                        <td className="px-4 py-3">
                          <span className={`px-2.5 py-1 text-xs font-bold rounded-full ${
                            record.status === 'FILED' ? 'bg-emerald-50 text-emerald-700 border border-emerald-100' : 'bg-amber-50 text-amber-700 border border-amber-100'
                          }`}>
                            {record.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Dynamic Sync Progress Modal */}
      <AnimatePresence>
        {isSyncModalOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-primary-950/40 backdrop-blur-sm">
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="bg-white rounded-3xl border border-slate-100 max-w-md w-full p-8 shadow-2xl"
            >
              <div className="flex items-center space-x-3 mb-6">
                <div className="h-10 w-10 bg-primary-100 rounded-xl flex items-center justify-center text-primary-650">
                  <Database className="animate-pulse" size={20} />
                </div>
                <div>
                  <h3 className="text-lg font-bold text-slate-900">Ingesting Alternate Data</h3>
                  <p className="text-xs text-slate-500">Connecting DPI registries sandbox...</p>
                </div>
              </div>

              {/* Progress Steps */}
              <div className="space-y-4">
                {SYNC_STEPS.map((step, idx) => {
                  const isDone = syncingStepIndex > idx;
                  const isSyncing = syncingStepIndex === idx;

                  return (
                    <div key={step.id} className="flex items-center justify-between text-sm">
                      <div className="flex items-center space-x-3">
                        {isDone ? (
                          <CheckCircle className="text-emerald-500 shrink-0" size={18} />
                        ) : isSyncing ? (
                          <RefreshCw className="text-primary animate-spin shrink-0" size={18} />
                        ) : (
                          <div className="h-4.5 w-4.5 rounded-full border-2 border-slate-200 shrink-0"></div>
                        )}
                        <span className={`font-medium ${isSyncing ? 'text-primary font-bold' : isDone ? 'text-slate-800' : 'text-slate-400'}`}>
                          {step.name}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>

              {syncingStepIndex === SYNC_STEPS.length && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="mt-8 p-4 bg-emerald-50 rounded-xl border border-emerald-100 flex items-center space-x-3 text-emerald-800 text-xs font-semibold"
                >
                  <CheckCircle size={18} />
                  <span>Sync Complete! Cash flow digital twin generated.</span>
                </motion.div>
              )}
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};

// Helper components for status cards
interface StatusCardProps {
  icon: React.ReactNode;
  title: string;
  desc: string;
  isSynced?: boolean;
}

const StatusCard = ({ icon, title, desc, isSynced }: StatusCardProps) => {
  return (
    <div className="bg-white rounded-2xl border border-slate-100 p-5 shadow-sm flex flex-col justify-between items-start min-h-[130px]">
      <div className="flex justify-between items-start w-full">
        <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-100">{icon}</div>
        {isSynced ? (
          <CheckCircle className="text-emerald-500 shrink-0" size={20} />
        ) : (
          <XCircle className="text-slate-350 shrink-0" size={20} />
        )}
      </div>
      <div className="mt-4">
        <h4 className="text-xs font-bold text-slate-800">{title}</h4>
        <p className="text-[10px] text-slate-400 mt-1 leading-normal">{desc}</p>
      </div>
    </div>
  );
};

export default DataIngestion;
