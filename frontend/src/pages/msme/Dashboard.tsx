import { useAuth } from '../../context/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { Navigate, Link } from 'react-router-dom';
import { getMyBusiness } from '../../services/businessService';
import { useFirestoreDoc } from '../../hooks/useFirestoreQuery';
import { LogOut, Building, ShieldAlert, Landmark, MapPin, Calendar, Database, TrendingUp, ShieldCheck, UserCheck, Coins } from 'lucide-react';

export const MSMEDashboard = () => {
  const { user, logout } = useAuth();

  const { data: business, isLoading: isBusinessLoading, error: businessError } = useQuery({
    queryKey: ['my-business'],
    queryFn: getMyBusiness,
    retry: false,
  });

  // Real-time listener for business profile updates
  const { data: realTimeBusiness } = useFirestoreDoc<any>(
    business ? `businesses/${business.id}` : null
  );

  const displayBusiness = realTimeBusiness || business;

  if (isBusinessLoading) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center bg-primary-900 text-white">
        <div className="relative flex h-20 w-20 items-center justify-center">
          <div className="absolute h-16 w-16 animate-spin rounded-full border-4 border-solid border-accent border-t-transparent"></div>
          <span className="font-semibold text-accent text-sm">IDBI</span>
        </div>
        <p className="mt-4 text-slate-300 animate-pulse text-sm font-medium tracking-wide">
          Loading corporate business profile...
        </p>
      </div>
    );
  }

  const hasNoBusiness = businessError && (businessError as any).response?.status === 404;
  if (hasNoBusiness) {
    return <Navigate to="/msme/register-business" replace />;
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      {/* Navbar */}
      <nav className="bg-primary-900 text-white px-6 py-4 shadow-lg flex justify-between items-center">
        <div className="flex items-center space-x-3">
          <div className="bg-accent h-8 w-8 rounded-lg flex items-center justify-center">
            <span className="font-extrabold text-primary text-sm">IDBI</span>
          </div>
          <span className="font-bold tracking-tight text-md">MSME Owner Terminal</span>
        </div>
        <div className="flex items-center space-x-4">
          <div className="text-right hidden sm:block">
            <p className="text-xs text-slate-300 font-semibold">{user?.fullName}</p>
            <p className="text-[10px] text-accent font-semibold">{user?.email}</p>
          </div>
          <button
            onClick={logout}
            className="flex items-center space-x-1.5 px-3 py-1.5 bg-primary-800 hover:bg-red-950/40 text-xs font-bold rounded-lg border border-primary-700/50 hover:border-red-900/50 hover:text-red-200 transition-colors"
          >
            <LogOut size={14} />
            <span>Sign Out</span>
          </button>
        </div>
      </nav>

      {/* Main Body */}
      <main className="flex-grow p-6 md:p-8 max-w-7xl mx-auto w-full">
        <div className="mb-8">
          <h2 className="text-3xl font-extrabold text-slate-900 m-0">Welcome, {user?.fullName || 'Business Partner'}</h2>
          <p className="text-slate-500 text-sm mt-1">Platform Status: Production Active &bull; Real-time Sync Enabled</p>
        </div>

        {displayBusiness && (
          <div className="bg-gradient-to-r from-primary-900 to-primary-850 text-white rounded-2xl p-6 shadow-md mb-8 relative overflow-hidden">
            <div className="absolute top-0 right-0 w-64 h-64 bg-accent rounded-full blur-3xl opacity-10 -mr-20 -mt-20"></div>
            
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center">
              <div>
                <span className="text-[10px] uppercase font-bold text-accent tracking-widest bg-primary-800 px-2.5 py-1 rounded-md border border-primary-700">
                  Node: {displayBusiness.industrySector}
                </span>
                <h3 className="text-2xl font-black text-white mt-3 mb-1 tracking-tight">{displayBusiness.legalName}</h3>
                {displayBusiness.tradeName && <p className="text-slate-300 text-sm italic mb-4">Trading as: {displayBusiness.tradeName}</p>}
                
                <div className="flex flex-wrap gap-x-6 gap-y-2 mt-4 text-xs text-slate-300">
                  <div className="flex items-center space-x-1.5">
                    <Landmark size={14} className="text-accent" />
                    <span>GSTIN: <strong>{displayBusiness.gstin}</strong></span>
                  </div>
                  <div className="flex items-center space-x-1.5">
                    <Building size={14} className="text-accent" />
                    <span>PAN: <strong>{displayBusiness.pan}</strong></span>
                  </div>
                  <div className="flex items-center space-x-1.5">
                    <Calendar size={14} className="text-accent" />
                    <span>Incorporated: <strong>{displayBusiness.incorporationDate}</strong></span>
                  </div>
                </div>
              </div>

              <div className="mt-6 md:mt-0 bg-primary-800/60 border border-primary-700 p-4 rounded-xl text-left min-w-[240px]">
                <h4 className="text-xs font-bold text-accent uppercase tracking-wider mb-2">Registered Address</h4>
                <div className="flex items-start space-x-2 text-xs text-slate-300">
                  <MapPin size={16} className="text-slate-400 shrink-0 mt-0.5" />
                  <div>
                    <p>{displayBusiness.addressLine1}</p>
                    {displayBusiness.addressLine2 && <p>{displayBusiness.addressLine2}</p>}
                    <p>{displayBusiness.city}, {displayBusiness.state} - {displayBusiness.pincode}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        <h3 className="text-xl font-bold text-slate-800 mb-6">Core Integration Modules</h3>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm flex flex-col justify-between hover:shadow-md transition">
            <div>
              <div className="flex justify-between items-center mb-4">
                <div className="bg-primary-100 text-primary p-3 rounded-xl">
                  <Database size={20} />
                </div>
                <span className="text-[10px] font-bold text-emerald-700 bg-emerald-50 px-2 py-1 rounded-full uppercase tracking-wider">
                  Real-time
                </span>
              </div>
              <h4 className="text-md font-bold text-slate-900">1. Alternate Data Ingestion</h4>
              <p className="text-xs text-slate-500 mt-2 leading-relaxed">
                Connect and aggregate alternate data repositories (GSTIN filings, UPI collections, EPFO employee records, and Utility bill histories).
              </p>
            </div>
            <Link
              to="/msme/data-ingest"
              className="mt-6 w-full text-center py-2.5 px-4 bg-primary hover:bg-primary-800 text-xs font-bold text-accent rounded-xl shadow-sm transition"
            >
              Connect Sources
            </Link>
          </div>

          <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm flex flex-col justify-between hover:shadow-md transition">
            <div>
              <div className="flex justify-between items-center mb-4">
                <div className="bg-primary-100 text-primary p-3 rounded-xl">
                  <TrendingUp size={20} />
                </div>
                <span className="text-[10px] font-bold text-emerald-700 bg-emerald-50 px-2 py-1 rounded-full uppercase tracking-wider">
                  Processed
                </span>
              </div>
              <h4 className="text-md font-bold text-slate-900">2. AI Feature Intelligence</h4>
              <p className="text-xs text-slate-500 mt-2 leading-relaxed">
                View computed transaction indicators extracted from ingested data pipelines, including monthly cash flow averages and compliance flags.
              </p>
            </div>
            <Link
              to="/msme/features"
              className="mt-6 w-full text-center py-2.5 px-4 bg-slate-100 hover:bg-slate-200 text-xs font-bold text-slate-750 border border-slate-200 rounded-xl transition"
            >
              View Computed Features
            </Link>
          </div>

          <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm flex flex-col justify-between hover:shadow-md transition">
            <div>
              <div className="flex justify-between items-center mb-4">
                <div className="bg-primary-100 text-primary p-3 rounded-xl">
                  <ShieldCheck size={20} />
                </div>
                <span className="text-[10px] font-bold text-emerald-700 bg-emerald-50 px-2 py-1 rounded-full uppercase tracking-wider">
                  Generated
                </span>
              </div>
              <h4 className="text-md font-bold text-slate-900">3. AI Financial Health Card</h4>
              <p className="text-xs text-slate-500 mt-2 leading-relaxed">
                Explore your multidimensional credit scorecard, core business strengths, risk heatmap matrices, and explainable AI reasons.
              </p>
            </div>
            <Link
              to="/msme/health-card"
              className="mt-6 w-full text-center py-2.5 px-4 bg-slate-100 hover:bg-slate-200 text-xs font-bold text-slate-750 border border-slate-200 rounded-xl transition"
            >
              View Scorecard
            </Link>
          </div>

          <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm flex flex-col justify-between hover:shadow-md transition">
            <div>
              <div className="flex justify-between items-center mb-4">
                <div className="bg-primary-100 text-primary p-3 rounded-xl">
                  <UserCheck size={20} />
                </div>
                <span className="text-[10px] font-bold text-blue-700 bg-blue-50 px-2 py-1 rounded-full uppercase tracking-wider">
                  Consent Portal
                </span>
              </div>
              <h4 className="text-md font-bold text-slate-900">4. AA Consent Manager</h4>
              <p className="text-xs text-slate-500 mt-2 leading-relaxed">
                Manage data share consent approvals and check access request history from partnering lender and bank underwriting systems.
              </p>
            </div>
            <Link
              to="/msme/consents"
              className="mt-6 w-full text-center py-2.5 px-4 bg-slate-100 hover:bg-slate-200 text-xs font-bold text-slate-750 border border-slate-200 rounded-xl transition"
            >
              Manage Consents
            </Link>
          </div>

          <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm flex flex-col justify-between hover:shadow-md transition">
            <div>
              <div className="flex justify-between items-center mb-4">
                <div className="bg-primary-100 text-primary p-3 rounded-xl">
                  <Coins size={20} />
                </div>
                <span className="text-[10px] font-bold text-blue-700 bg-blue-50 px-2 py-1 rounded-full uppercase tracking-wider">
                  Simulation
                </span>
              </div>
              <h4 className="text-md font-bold text-slate-900">5. Cash Flow & Loan Simulator</h4>
              <p className="text-xs text-slate-500 mt-2 leading-relaxed">
                Forecast 6 months of future net surplus bank balances under simulated loan amounts, interest rates, and payback durations.
              </p>
            </div>
            <Link
              to="/msme/forecast"
              className="mt-6 w-full text-center py-2.5 px-4 bg-slate-100 hover:bg-slate-200 text-xs font-bold text-slate-750 border border-slate-200 rounded-xl transition"
            >
              Run Simulation
            </Link>
          </div>

          <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm flex flex-col justify-between hover:shadow-md transition">
            <div>
              <div className="flex justify-between items-center mb-4">
                <div className="bg-primary-100 text-primary p-3 rounded-xl">
                  <ShieldAlert size={20} />
                </div>
                <span className="text-[10px] font-bold text-amber-700 bg-amber-50 px-2 py-1 rounded-full uppercase tracking-wider">
                  Active Alerts
                </span>
              </div>
              <h4 className="text-md font-bold text-slate-900">6. Early Warning Systems</h4>
              <p className="text-xs text-slate-500 mt-2 leading-relaxed">
                Receive analytical risk warnings regarding utility arrears, late GST filing status, and unusual credit/debit transaction spikes.
              </p>
            </div>
            <div className="mt-6 w-full text-center py-2.5 px-4 bg-slate-50 text-slate-400 text-xs font-bold rounded-xl border border-slate-100 cursor-not-allowed">
              Monitoring Online
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};
export default MSMEDashboard;
