import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { motion } from 'framer-motion';
import { Lock, Mail, AlertTriangle, ShieldCheck } from 'lucide-react';

const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export const Login = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register: registerField,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  });

  const from = location.state?.from?.pathname || '/';

  const onSubmit = async (data: LoginFormValues) => {
    setIsLoading(true);
    setErrorMsg(null);
    try {
      const user = await login(data.email, data.password);
      
      // Route based on role
      if (user.role === 'ROLE_MSME') {
        navigate('/msme');
      } else if (user.role === 'ROLE_LOAN_OFFICER' || user.role === 'ROLE_CREDIT_MANAGER') {
        navigate('/lender');
      } else if (user.role === 'ROLE_ADMIN') {
        navigate('/admin');
      } else {
        navigate(from, { replace: true });
      }
    } catch (err: any) {
      setErrorMsg(err);
    } finally {
      setIsLoading(false);
    }
  };

  // Quick fill helper for hackathon demo convenience
  const quickFill = (role: 'msme' | 'lender' | 'admin') => {
    if (role === 'msme') {
      setValue('email', 'owner@saraswatifabrics.in');
      setValue('password', 'password123');
    } else if (role === 'lender') {
      setValue('email', 'credit.mgr@idbi.com');
      setValue('password', 'password123');
    } else if (role === 'admin') {
      setValue('email', 'sys.admin@idbi.com');
      setValue('password', 'password123');
    }
  };

  return (
    <div className="flex min-h-screen bg-slate-50">
      {/* Left Branding Side (Hidden on Mobile) */}
      <div className="hidden lg:flex w-1/2 bg-primary-900 text-white flex-col justify-between p-12 relative overflow-hidden">
        {/* Background Decorative Circles */}
        <div className="absolute top-0 right-0 w-96 h-96 bg-primary-800 rounded-full blur-3xl opacity-30 -mr-20 -mt-20"></div>
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-accent rounded-full blur-3xl opacity-10 -ml-20 -mb-20"></div>

        {/* Top Header Logo */}
        <div className="flex items-center space-x-3 z-10">
          <div className="bg-accent h-10 w-10 rounded-lg flex items-center justify-center shadow-md">
            <span className="font-extrabold text-primary text-xl">IDBI</span>
          </div>
          <div>
            <h1 className="text-lg font-bold tracking-tight text-white m-0 leading-none">MSME</h1>
            <p className="text-[10px] text-accent font-semibold tracking-wider uppercase m-0 leading-none">Financial Intelligence</p>
          </div>
        </div>

        {/* Content Body */}
        <div className="my-auto z-10 max-w-lg">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
          >
            <span className="text-xs uppercase font-bold text-accent tracking-widest bg-primary-800/60 px-3 py-1.5 rounded-full">
              National Hackathon Entry
            </span>
            <h2 className="text-4xl font-extrabold tracking-tight mt-6 text-white leading-tight">
              Evaluate MSME Credit Readiness with Alternate Data
            </h2>
            <p className="mt-4 text-slate-300 text-sm leading-relaxed">
              Bridging credit boundaries for credit-invisible businesses by integrating GST, UPI networks, Account Aggregator consent, and custom scoring engines.
            </p>
          </motion.div>

          {/* Features Grid */}
          <div className="grid grid-cols-2 gap-6 mt-12">
            <div className="flex space-x-3">
              <div className="bg-primary-800/80 p-2 rounded-lg h-10 w-10 flex items-center justify-center text-accent">
                <ShieldCheck size={20} />
              </div>
              <div>
                <h4 className="text-sm font-semibold text-white">Secure Ingest</h4>
                <p className="text-xs text-slate-400 mt-1">AA consent & RBI ULI architecture compatibility.</p>
              </div>
            </div>
            <div className="flex space-x-3">
              <div className="bg-primary-800/80 p-2 rounded-lg h-10 w-10 flex items-center justify-center text-accent">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
              <div>
                <h4 className="text-sm font-semibold text-white">Dynamic Scoring</h4>
                <p className="text-xs text-slate-400 mt-1">XGBoost prediction & explainable SHAP metrics.</p>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="z-10">
          <p className="text-xs text-slate-400">© 2026 IDBI Bank Ltd. Powered by RBI Digital Public Infrastructure.</p>
        </div>
      </div>

      {/* Right Login Form Side */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-12 bg-white">
        <div className="w-full max-w-md">
          {/* Header Mobile Logo */}
          <div className="flex items-center space-x-2 lg:hidden mb-8">
            <div className="bg-primary h-8 w-8 rounded-lg flex items-center justify-center">
              <span className="font-extrabold text-accent text-sm">IDBI</span>
            </div>
            <span className="font-bold text-slate-900 tracking-tight">MSME Platform</span>
          </div>

          <h3 className="text-3xl font-extrabold text-slate-900 tracking-tight">Sign In</h3>
          <p className="text-slate-500 text-sm mt-2">Access your MSME account or Lender terminal securely.</p>

          {/* Failed validation message */}
          {errorMsg && (
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              className="mt-6 p-4 rounded-xl bg-red-50 border border-red-100 flex items-start space-x-3 text-red-700 text-sm"
            >
              <AlertTriangle className="h-5 w-5 shrink-0 text-red-500 mt-0.5" />
              <div>
                <span className="font-semibold block">Authentication Failure</span>
                <span>{errorMsg}</span>
              </div>
            </motion.div>
          )}

          {/* Form */}
          <form className="mt-8 space-y-5" onSubmit={handleSubmit(onSubmit)}>
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-2">Corporate Email</label>
              <div className="relative rounded-lg shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Mail size={18} />
                </div>
                <input
                  type="email"
                  placeholder="name@company.com"
                  {...registerField('email')}
                  className="block w-full pl-10 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition-all"
                />
              </div>
              {errors.email && (
                <p className="mt-1 text-xs text-red-600 font-medium">{errors.email.message}</p>
              )}
            </div>

            <div>
              <div className="flex justify-between items-center mb-2">
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-700">Security Password</label>
                <a href="#forgot" className="text-xs font-semibold text-secondary hover:underline">Forgot password?</a>
              </div>
              <div className="relative rounded-lg shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Lock size={18} />
                </div>
                <input
                  type="password"
                  placeholder="••••••••"
                  {...registerField('password')}
                  className="block w-full pl-10 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition-all"
                />
              </div>
              {errors.password && (
                <p className="mt-1 text-xs text-red-600 font-medium">{errors.password.message}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-md text-sm font-bold text-white bg-primary hover:bg-primary-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
            >
              {isLoading ? (
                <div className="h-5 w-5 animate-spin rounded-full border-2 border-solid border-white border-t-transparent"></div>
              ) : (
                'Access Portal'
              )}
            </button>
          </form>

          {/* Quick-Fill Demo Helpers */}
          <div className="mt-8 p-4 bg-slate-50 rounded-xl border border-slate-100">
            <h4 className="text-xs font-bold text-slate-600 uppercase tracking-wider mb-3">Sandbox sandbox quick-fill</h4>
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                onClick={() => quickFill('msme')}
                className="px-3 py-1.5 bg-white border border-slate-200 rounded-md text-xs font-semibold text-slate-700 hover:bg-slate-50 transition"
              >
                MSME Owner
              </button>
              <button
                type="button"
                onClick={() => quickFill('lender')}
                className="px-3 py-1.5 bg-white border border-slate-200 rounded-md text-xs font-semibold text-slate-700 hover:bg-slate-50 transition"
              >
                Lender Officer
              </button>
              <button
                type="button"
                onClick={() => quickFill('admin')}
                className="px-3 py-1.5 bg-white border border-slate-200 rounded-md text-xs font-semibold text-slate-700 hover:bg-slate-50 transition"
              >
                Platform Admin
              </button>
            </div>
          </div>

          <p className="mt-8 text-center text-sm text-slate-500">
            New corporate client?{' '}
            <Link to="/register" className="font-semibold text-secondary hover:underline">
              Register business profile
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};
export default Login;
