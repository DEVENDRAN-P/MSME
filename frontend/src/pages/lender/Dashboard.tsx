import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../components/Toast';
import { requestConsent } from '../../services/consentService';
import { getBusinessHealthCard } from '../../services/healthCardService';
import { getBusinessFeatures } from '../../services/featureService';
import { approveLoan } from '../../services/loanService';
import { useFirestoreCollection } from '../../hooks/useFirestoreQuery';
import { where } from 'firebase/firestore';
import {
  Search,
  Building,
  LogOut,
  Lock,
  Unlock,
  CheckCircle
} from 'lucide-react';

interface BusinessItem {
  id: string;
  legalName: string;
  tradeName: string;
  gstin: string;
  industrySector: string;
  city: string;
  state: string;
}

export const LenderDashboard = () => {
  const { user, logout } = useAuth();
  const { addToast } = useToast();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedBusiness, setSelectedBusiness] = useState<BusinessItem | null>(null);
  const [consentType, setConsentType] = useState<'GST' | 'UPI' | 'AA' | 'ALL'>('ALL');

  // Real-time Firestore listener for business directory
  const { data: realTimeBusinesses, loading: isRealTimeLoading } = useFirestoreCollection<BusinessItem>(
    'businesses'
  );

  // Real-time consent status for selected business
  const { data: realTimeConsents } = useFirestoreCollection<any>(
    'consents',
    selectedBusiness ? [where('businessId', '==', selectedBusiness.id)] : []
  );

  const hasConsent = realTimeConsents?.some(
    (c: any) => c.status === 'APPROVED' && new Date(c.validUntil) > new Date()
  ) ?? false;

  // Query for features (only when consent active)
  const { data: features } = useQuery({
    queryKey: ['business-features', selectedBusiness?.id],
    queryFn: () => getBusinessFeatures(selectedBusiness!.id),
    enabled: !!selectedBusiness && hasConsent,
  });

  // Query for health card (only when consent active)
  const { data: card } = useQuery({
    queryKey: ['business-health-card', selectedBusiness?.id],
    queryFn: () => getBusinessHealthCard(selectedBusiness!.id),
    enabled: !!selectedBusiness && hasConsent,
  });

  // Request Consent Mutation
  const requestMutation = useMutation({
    mutationFn: () => requestConsent({ businessId: selectedBusiness!.id, consentType }),
    onSuccess: () => {
      addToast('Consent request dispatched to MSME Dashboard.', 'success');
      queryClient.invalidateQueries({ queryKey: ['check-consent', selectedBusiness?.id] });
    },
    onError: () => {
      addToast('Failed to send consent request.', 'error');
    },
  });

  // Loan Approval Mutation
  const approveMutation = useMutation({
    mutationFn: (payload: { amount: number; rate: number; tenure: number }) =>
      approveLoan({
        businessId: selectedBusiness!.id,
        amount: payload.amount,
        interestRate: payload.rate,
        tenureMonths: payload.tenure,
      }),
    onSuccess: (data) => {
      addToast(`Loan approved and disbursed! Reference ID: ${data.id}`, 'success');
      queryClient.invalidateQueries({ queryKey: ['lender-businesses'] });
    },
    onError: () => {
      addToast('Failed to approve loan.', 'error');
    },
  });

  const handleRequestConsent = () => {
    requestMutation.mutate();
  };

  const handleApproveLoan = () => {
    const loanAmt = score >= 750 ? 1500000 : score >= 680 ? 800000 : 500000;
    approveMutation.mutate({ amount: loanAmt, rate: 11.5, tenure: 12 });
  };

  const businesses = realTimeBusinesses || [];
  const filteredBusinesses = businesses.filter((b) =>
    b.legalName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    b.gstin.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const score = card?.unified_score || 300;
  const percentage = Math.min(Math.max((score - 300) / 600.0, 0), 1.0);
  const arcLength = 251.2;
  const strokeOffset = arcLength - (arcLength * percentage);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col h-screen overflow-hidden">
      {/* Navbar */}
      <nav className="bg-primary-900 text-white px-6 py-4 shadow-lg flex justify-between items-center shrink-0">
        <div className="flex items-center space-x-3">
          <div className="bg-accent h-8 w-8 rounded-lg flex items-center justify-center">
            <span className="font-extrabold text-primary text-sm">IDBI</span>
          </div>
          <span className="font-bold tracking-tight text-md">Lender Underwriting Workspace</span>
        </div>
        <div className="flex items-center space-x-4">
          <div className="text-right hidden sm:block">
            <p className="text-xs text-slate-350 font-semibold">{user?.fullName}</p>
            <p className="text-[10px] text-accent font-semibold">{user?.role.replace('ROLE_', ' ').replace('_', ' ')}</p>
          </div>
          <button
            onClick={logout}
            className="flex items-center space-x-1.5 px-3 py-1.5 bg-primary-800 hover:bg-red-950/40 text-xs font-bold rounded-lg border border-primary-700/50 hover:border-red-900/50 hover:text-red-200 transition"
          >
            <LogOut size={14} />
            <span>Sign Out</span>
          </button>
        </div>
      </nav>

      {/* Workspace */}
      <div className="flex-grow flex overflow-hidden">
        {/* Left Side: Search panel */}
        <aside className="w-80 border-r border-slate-200 bg-white flex flex-col shrink-0">
          <div className="p-4 border-b border-slate-100">
            <div className="relative">
              <Search className="absolute left-3 top-3.5 text-slate-400" size={16} />
              <input
                type="text"
                placeholder="Search by legal name or GSTIN..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-9 pr-4 py-2.5 bg-slate-50 border border-slate-200 text-xs rounded-xl focus:outline-none focus:border-primary-400 focus:bg-white transition"
              />
            </div>
          </div>

          <div className="flex-grow overflow-y-auto divide-y divide-slate-50 p-2 space-y-1">
            {isRealTimeLoading ? (
              <div className="flex justify-center items-center py-10">
                <div className="animate-spin rounded-full h-8 w-8 border-2 border-solid border-primary border-t-transparent"></div>
              </div>
            ) : filteredBusinesses.length === 0 ? (
              <p className="text-center text-xs text-slate-400 py-10 font-medium">No applicants found.</p>
            ) : (
              filteredBusinesses.map((b) => (
                <button
                  key={b.id}
                  onClick={() => setSelectedBusiness(b)}
                  className={`w-full text-left p-3.5 rounded-xl transition flex flex-col ${
                    selectedBusiness?.id === b.id
                      ? 'bg-primary-50 border border-primary-200/50 text-primary-950'
                      : 'hover:bg-slate-50 border border-transparent text-slate-700'
                  }`}
                >
                  <span className="text-xs font-bold truncate">{b.legalName}</span>
                  <span className="text-[10px] text-slate-400 mt-1">Sector: {b.industrySector}</span>
                  <span className="text-[9px] font-bold text-slate-400 mt-2 font-mono">{b.gstin}</span>
                </button>
              ))
            )}
          </div>
        </aside>

        {/* Right Side: Working workspace */}
        <main className="flex-grow overflow-y-auto bg-slate-50 p-6 md:p-8 flex flex-col">
          {!selectedBusiness ? (
            <div className="flex-grow flex flex-col items-center justify-center text-center p-8 max-w-md mx-auto">
              <div className="h-16 w-16 bg-slate-100 rounded-full flex items-center justify-center text-slate-400 mb-6 border border-slate-200">
                <Building size={28} />
              </div>
              <h3 className="text-lg font-bold text-slate-800">Select an Applicant</h3>
              <p className="text-xs text-slate-500 mt-2 leading-relaxed">
                Click on any applicant corporate profile from the directory on the left to inspect details, request data-sharing registries, or process underwriting.
              </p>
            </div>
          ) : !hasConsent ? (
            <div className="bg-white rounded-3xl border border-slate-200 p-8 shadow-sm max-w-xl mx-auto my-auto text-center w-full">
              <div className="h-14 w-14 bg-amber-50 text-amber-600 rounded-full flex items-center justify-center mx-auto mb-6">
                <Lock size={26} />
              </div>
              <h3 className="text-lg font-bold text-slate-800">Approved Consent Required</h3>
              <p className="text-xs text-slate-500 mt-3 leading-relaxed max-w-sm mx-auto">
                Under DEPA privacy frameworks, you must obtain explicit, time-bound consent from <strong>{selectedBusiness.legalName}</strong> to view alternate tax and banking summaries.
              </p>

              <div className="mt-8 border-t border-slate-100 pt-6 max-w-xs mx-auto">
                <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block text-left mb-2">Select Data Scope</label>
                <select
                  value={consentType}
                  onChange={(e: any) => setConsentType(e.target.value)}
                  className="w-full bg-slate-50 border border-slate-250 py-2.5 px-3 text-xs rounded-xl focus:outline-none focus:bg-white mb-6"
                >
                  <option value="ALL">ALL (GST, UPI, Account Aggregator)</option>
                  <option value="GST">GST Ledger trails only</option>
                  <option value="UPI">UPI collections QR logs only</option>
                  <option value="AA">AA Bank ledgers only</option>
                </select>

                <button
                  onClick={handleRequestConsent}
                  disabled={requestMutation.isPending}
                  className="w-full py-3 bg-primary hover:bg-primary-850 text-white font-bold text-xs rounded-xl shadow-md transition flex items-center justify-center space-x-2 disabled:opacity-50"
                >
                  <Unlock size={14} />
                  <span>Request Data Access</span>
                </button>
              </div>
            </div>
          ) : (
            <div className="space-y-8 max-w-6xl mx-auto w-full">
              {/* Profile Card */}
              <div className="bg-white rounded-2xl border border-slate-150 p-6 shadow-sm flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
                <div>
                  <span className="text-[9px] uppercase font-bold tracking-widest text-primary-650 bg-primary-50 px-2 py-1 rounded-md">
                    {selectedBusiness.industrySector}
                  </span>
                  <h2 className="text-2xl font-black text-slate-900 mt-3 mb-1 tracking-tight">{selectedBusiness.legalName}</h2>
                  <div className="flex flex-wrap gap-4 text-xs text-slate-400 mt-2 font-mono">
                    <span>GSTIN: <strong className="text-slate-655">{selectedBusiness.gstin}</strong></span>
                    <span>City: <strong className="text-slate-655">{selectedBusiness.city}, {selectedBusiness.state}</strong></span>
                  </div>
                </div>
                <div className="bg-emerald-50 text-emerald-800 border border-emerald-100 p-3 rounded-xl flex items-center space-x-2 text-xs font-bold">
                  <CheckCircle size={16} />
                  <span>DEPA Data Sharing Consent Approved</span>
                </div>
              </div>

              {/* Scorecard Layout */}
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="bg-white rounded-3xl p-6 border border-slate-150 shadow-sm flex flex-col justify-between items-center text-center">
                  <div className="w-full">
                    <span className="text-[10px] uppercase font-bold tracking-widest text-slate-400">Scorecard prediction</span>

                    <div className="relative mt-8 mb-4 flex justify-center">
                      <svg viewBox="0 0 200 120" className="w-56 h-36">
                        <path d="M 20,100 A 80,80 0 0,1 180,100" fill="none" stroke="#f1f5f9" strokeWidth="12" strokeLinecap="round" />
                        <path
                          d="M 20,100 A 80,80 0 0,1 180,100"
                          fill="none"
                          stroke={score >= 750 ? '#10b981' : score >= 680 ? '#0c8fe3' : score >= 600 ? '#f59e0b' : '#ef4444'}
                          strokeWidth="12"
                          strokeLinecap="round"
                          strokeDasharray={arcLength}
                          strokeDashoffset={strokeOffset}
                        />
                      </svg>
                      <div className="absolute inset-0 flex flex-col items-center justify-end pb-3">
                        <span className="text-4xl font-black text-slate-900 tracking-tighter leading-none">{score}</span>
                        <span className="text-[9px] text-slate-400 font-bold uppercase tracking-wider mt-1.5">Unified Rating</span>
                      </div>
                    </div>

                    <span className={`inline-block px-3 py-1 rounded-md text-[10px] font-black border uppercase tracking-wider ${
                      card?.grade === 'PRIME_PLUS' ? 'bg-emerald-50 text-emerald-700 border-emerald-100' :
                      card?.grade === 'PRIME' ? 'bg-blue-50 text-blue-700 border-blue-100' :
                      card?.grade === 'NEAR_PRIME' ? 'bg-amber-50 text-amber-700 border-amber-100' :
                      'bg-rose-50 text-rose-700 border-rose-100'
                    }`}>
                      {card?.grade?.replace('_', ' ') || 'SUB_PRIME'}
                    </span>
                  </div>

                  <p className="text-slate-400 text-[10px] mt-6 leading-normal italic px-2">"{card?.description}"</p>
                </div>

                <div className="lg:col-span-2 bg-white rounded-3xl p-6 border border-slate-150 shadow-sm space-y-5">
                  <h3 className="text-xs font-black uppercase tracking-wider text-slate-400">Scorecard sub-components</h3>
                  {card && (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <DimensionBar title="Revenue Health" score={card.dimension_scores.revenue_health} color="bg-emerald-500" />
                      <DimensionBar title="Compliance Health" score={card.dimension_scores.compliance_health} color="bg-blue-500" />
                      <DimensionBar title="Liquidity Health" score={card.dimension_scores.liquidity_health} color="bg-indigo-500" />
                      <DimensionBar title="Workforce & Digital Quotient" score={card.dimension_scores.workforce_health} color="bg-orange-500" />
                    </div>
                  )}

                  {features && (
                    <div className="border-t border-slate-100 pt-4 mt-4 grid grid-cols-2 md:grid-cols-4 gap-4 text-xs text-slate-500">
                      <div>
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Turnover MoM</p>
                        <p className="font-extrabold text-slate-800 mt-1">{(features.gst_turnover_growth_rate * 100).toFixed(1)}%</p>
                      </div>
                      <div>
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Revenue CV</p>
                        <p className="font-extrabold text-slate-800 mt-1">{features.gst_turnover_volatility.toFixed(2)}</p>
                      </div>
                      <div>
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Cash Coverage</p>
                        <p className="font-extrabold text-slate-800 mt-1">{features.bank_cash_coverage_ratio.toFixed(2)}x</p>
                      </div>
                      <div>
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Utility Arrears</p>
                        <p className="font-extrabold text-slate-800 mt-1">{(features.utility_late_payment_ratio * 100).toFixed(0)}%</p>
                      </div>
                    </div>
                  )}
                </div>
              </div>

              {/* Disbursement Controls */}
              <div className="bg-white rounded-3xl p-6 border border-slate-150 shadow-sm flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
                <div>
                  <h3 className="text-sm font-black uppercase tracking-wider text-slate-400">Loan underwriting disbursement decision</h3>
                  <p className="text-xs text-slate-500 mt-1.5 max-w-xl leading-normal">
                    The score yields a pre-approval loan capacity. Choose loan amount and interest rates below.
                  </p>
                </div>
                <button
                  onClick={handleApproveLoan}
                  disabled={approveMutation.isPending}
                  className="px-6 py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl shadow-md transition self-stretch md:self-auto text-center disabled:opacity-50"
                >
                  {approveMutation.isPending ? 'Disbursing...' : 'Approve & Disburse Loan'}
                </button>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

const DimensionBar = ({ title, score, color }: { title: string; score: number; color: string }) => {
  return (
    <div>
      <div className="flex justify-between text-xs font-semibold text-slate-700 mb-1">
        <span>{title}</span>
        <span>{score}/100</span>
      </div>
      <div className="h-2 w-full bg-slate-100 rounded-full overflow-hidden">
        <div className={`h-full rounded-full ${color}`} style={{ width: `${score}%` }} />
      </div>
    </div>
  );
};

export default LenderDashboard;
