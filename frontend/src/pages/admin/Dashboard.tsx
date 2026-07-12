import { useAuth } from '../../context/AuthContext';
import { LogOut, ShieldCheck, Users, Settings, Database } from 'lucide-react';

export const AdminDashboard = () => {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      {/* Navbar */}
      <nav className="bg-primary-900 text-white px-6 py-4 shadow-lg flex justify-between items-center">
        <div className="flex items-center space-x-3">
          <div className="bg-accent h-8 w-8 rounded-lg flex items-center justify-center">
            <span className="font-extrabold text-primary text-sm">IDBI</span>
          </div>
          <span className="font-bold tracking-tight text-md">Platform Administrative Node</span>
        </div>
        <div className="flex items-center space-x-4">
          <div className="text-right hidden sm:block">
            <p className="text-xs text-slate-300 font-semibold">{user?.fullName}</p>
            <p className="text-[10px] text-accent font-semibold">Super Administrator</p>
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
          <h2 className="text-3xl font-extrabold text-slate-900 m-0">Administrator Terminal</h2>
          <p className="text-slate-500 text-sm mt-1">Platform operations status: Stable</p>
        </div>

        {/* Dashboard Skeleton Card */}
        <div className="bg-white rounded-2xl border border-slate-100 p-8 shadow-sm">
          <div className="flex items-center space-x-4 mb-6">
            <div className="bg-primary-100 text-primary p-3 rounded-xl">
              <ShieldCheck size={24} />
            </div>
            <div>
              <h3 className="text-lg font-bold text-slate-900">Module 1 Completed: Administrative Authentication Verified</h3>
              <p className="text-xs text-slate-500">Method-level pre-authorization rules verified for role ROLE_ADMIN.</p>
            </div>
          </div>

          <p className="text-sm text-slate-600 leading-relaxed max-w-3xl">
            You have logged in successfully as an **Administrator**. The dashboard allows monitoring and system management. In future modules, you will configure user registration networks, audit logging, model parameter fine-tuning, database migrations, and health checks.
          </p>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-8">
            <div className="bg-slate-50 rounded-xl p-5 border border-slate-100">
              <div className="text-accent mb-3"><Users size={24} /></div>
              <h4 className="text-sm font-bold text-slate-900">User Management</h4>
              <p className="text-xs text-slate-500 mt-1">Audit, register, and assign permissions for banking operators.</p>
            </div>
            <div className="bg-slate-50 rounded-xl p-5 border border-slate-100">
              <div className="text-secondary mb-3"><Settings size={24} /></div>
              <h4 className="text-sm font-bold text-slate-900">AI Model Monitor</h4>
              <p className="text-xs text-slate-500 mt-1">Watch XGBoost drift metrics and prediction latency dashboards.</p>
            </div>
            <div className="bg-slate-50 rounded-xl p-5 border border-slate-100">
              <div className="text-rose-600 mb-3"><Database size={24} /></div>
              <h4 className="text-sm font-bold text-slate-900">System Logs & DB</h4>
              <p className="text-xs text-slate-500 mt-1">Track transactional schemas, active cache connections, and audit trails.</p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};
export default AdminDashboard;
