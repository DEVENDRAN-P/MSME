import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../components/Toast';
import { motion } from 'framer-motion';
import { Lock, Mail, User, Phone, Briefcase, AlertTriangle } from 'lucide-react';

const registerSchema = z.object({
  fullName: z.string().min(1, 'Full name is required').max(100, 'Name is too long'),
  email: z.string().min(1, 'Email is required').email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  phone: z.string().optional(),
  role: z.enum(['ROLE_MSME', 'ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER'] as const),
});

type RegisterFormValues = z.infer<typeof registerSchema>;

export const Register = () => {
  const { register, loginWithGoogle } = useAuth();
  const navigate = useNavigate();
  const { addToast } = useToast();
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register: registerField,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      role: 'ROLE_MSME',
    }
  });

  const onSubmit = async (data: RegisterFormValues) => {
    setIsLoading(true);
    setErrorMsg(null);
    setSuccessMsg(null);
    try {
      await register(data.email, data.password, data.fullName, data.role, data.phone);
      setSuccessMsg('Account registered successfully! Redirecting to secure login...');
      addToast('Account created successfully!', 'success');
      setTimeout(() => {
        navigate('/login');
      }, 2500);
    } catch (err: any) {
      const msg = err.response?.data?.message || (typeof err.response?.data === 'string' ? err.response.data : null) || err.message || String(err);
      setErrorMsg(msg);
      addToast(msg || 'Registration failed', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen bg-slate-50">
      {/* Left Branding Side */}
      <div className="hidden lg:flex w-1/2 bg-primary-900 text-white flex-col justify-between p-12 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-96 h-96 bg-primary-800 rounded-full blur-3xl opacity-30 -mr-20 -mt-20"></div>
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-accent rounded-full blur-3xl opacity-10 -ml-20 -mb-20"></div>

        <div className="flex items-center space-x-3 z-10">
          <div className="bg-accent h-10 w-10 rounded-lg flex items-center justify-center shadow-md">
            <span className="font-extrabold text-primary text-xl">IDBI</span>
          </div>
          <div>
            <h1 className="text-lg font-bold tracking-tight text-white m-0 leading-none">MSME</h1>
            <p className="text-[10px] text-accent font-semibold tracking-wider uppercase m-0 leading-none">Financial Intelligence</p>
          </div>
        </div>

        <div className="my-auto z-10 max-w-lg">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
          >
            <h2 className="text-4xl font-extrabold tracking-tight text-white leading-tight">
              Create Your Digital Banking Identity
            </h2>
            <p className="mt-4 text-slate-300 text-sm leading-relaxed">
              Register business profile nodes to participate in alternate scoring models, scenario testing, and secure document aggregation networks.
            </p>
          </motion.div>
        </div>

        <div className="z-10">
          <p className="text-xs text-slate-400">&copy; 2026 IDBI Bank Ltd. Powered by RBI Digital Public Infrastructure.</p>
        </div>
      </div>

      {/* Right Register Form Side */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-12 bg-white">
        <div className="w-full max-w-md">
          <div className="flex items-center space-x-2 lg:hidden mb-8">
            <div className="bg-primary h-8 w-8 rounded-lg flex items-center justify-center">
              <span className="font-extrabold text-accent text-sm">IDBI</span>
            </div>
            <span className="font-bold text-slate-900 tracking-tight">MSME Platform</span>
          </div>

          <h3 className="text-3xl font-extrabold text-slate-900 tracking-tight">Register</h3>
          <p className="text-slate-500 text-sm mt-2">Initialize your credentials for the platform.</p>

          {errorMsg && (
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              className="mt-6 p-4 rounded-xl bg-red-50 border border-red-100 flex items-start space-x-3 text-red-700 text-sm"
            >
              <AlertTriangle className="h-5 w-5 shrink-0 text-red-500 mt-0.5" />
              <div>
                <span className="font-semibold block">Registration Error</span>
                <span>{errorMsg}</span>
              </div>
            </motion.div>
          )}

          {successMsg && (
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              className="mt-6 p-4 rounded-xl bg-emerald-50 border border-emerald-100 flex items-start space-x-3 text-emerald-800 text-sm"
            >
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="h-5 w-5 shrink-0 text-emerald-600 mt-0.5">
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 01-1.043 3.296 3.745 3.745 0 01-3.296 1.043A3.745 3.745 0 0112 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 01-3.296-1.043A3.745 3.745 0 013 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 011.043-3.296 3.746 3.746 0 013.296-1.043A3.746 3.746 0 0112 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 013.296 1.043 3.746 3.746 0 011.043 3.296A3.745 3.745 0 0121 12z" />
              </svg>
              <div>
                <span className="font-semibold block">Success</span>
                <span>{successMsg}</span>
              </div>
            </motion.div>
          )}

          <form className="mt-8 space-y-4" onSubmit={handleSubmit(onSubmit)}>
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">Full Name</label>
              <div className="relative rounded-lg shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <User size={18} />
                </div>
                <input
                  type="text"
                  placeholder="John Doe"
                  {...registerField('fullName')}
                  className="block w-full pl-10 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition-all"
                />
              </div>
              {errors.fullName && (
                <p className="mt-1 text-xs text-red-600 font-medium">{errors.fullName.message}</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">Corporate Email</label>
              <div className="relative rounded-lg shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Mail size={18} />
                </div>
                <input
                  type="email"
                  placeholder="name@company.com"
                  {...registerField('email')}
                  className="block w-full pl-10 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition-all"
                />
              </div>
              {errors.email && (
                <p className="mt-1 text-xs text-red-600 font-medium">{errors.email.message}</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">Security Password</label>
              <div className="relative rounded-lg shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Lock size={18} />
                </div>
                <input
                  type="password"
                  placeholder="&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;"
                  {...registerField('password')}
                  className="block w-full pl-10 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition-all"
                />
              </div>
              {errors.password && (
                <p className="mt-1 text-xs text-red-600 font-medium">{errors.password.message}</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">Mobile Contact (Optional)</label>
              <div className="relative rounded-lg shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Phone size={18} />
                </div>
                <input
                  type="text"
                  placeholder="9876543210"
                  {...registerField('phone')}
                  className="block w-full pl-10 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition-all"
                />
              </div>
              {errors.phone && (
                <p className="mt-1 text-xs text-red-600 font-medium">{errors.phone.message}</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">Platform Role</label>
              <div className="relative rounded-lg shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Briefcase size={18} />
                </div>
                <select
                  {...registerField('role')}
                  className="block w-full pl-10 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-secondary/20 focus:border-secondary transition-all appearance-none"
                >
                  <option value="ROLE_MSME">MSME Business Owner</option>
                  <option value="ROLE_LOAN_OFFICER">IDBI Loan Officer</option>
                  <option value="ROLE_CREDIT_MANAGER">IDBI Credit Manager</option>
                </select>
                <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-slate-400">
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </div>
              </div>
              {errors.role && (
                <p className="mt-1 text-xs text-red-600 font-medium">{errors.role.message}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full flex justify-center py-2.5 px-4 border border-transparent rounded-lg shadow-md text-sm font-bold text-white bg-primary hover:bg-primary-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 mt-2"
            >
              {isLoading ? (
                <div className="h-5 w-5 animate-spin rounded-full border-2 border-solid border-white border-t-transparent"></div>
              ) : (
                'Register Profile'
              )}
            </button>
          </form>

          {/* Divider */}
          <div className="mt-6 relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-200"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="bg-white px-4 text-slate-500">or continue with</span>
            </div>
          </div>

          <button
            type="button"
            onClick={async () => {
              setIsLoading(true);
              setErrorMsg(null);
              try {
                const user = await loginWithGoogle();
                addToast('Account created successfully!', 'success');
                if (user.role === 'ROLE_MSME') navigate('/msme');
                else if (user.role === 'ROLE_LOAN_OFFICER' || user.role === 'ROLE_CREDIT_MANAGER') navigate('/lender');
                else if (user.role === 'ROLE_ADMIN') navigate('/admin');
                else navigate('/login');
              } catch (err: any) {
                const msg = err.response?.data?.message || (typeof err.response?.data === 'string' ? err.response.data : null) || err.message || String(err);
                setErrorMsg(msg);
                addToast(msg || 'Google sign-in failed', 'error');
              } finally {
                setIsLoading(false);
              }
            }}
            disabled={isLoading}
            className="mt-4 w-full flex justify-center items-center gap-3 py-3 px-4 border border-slate-200 rounded-lg shadow-sm text-sm font-semibold text-slate-700 bg-white hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-slate-400 disabled:opacity-50 transition-all"
          >
            <svg className="h-5 w-5" viewBox="0 0 24 24">
              <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
              <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
              <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
              <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
            </svg>
            Sign up with Google
          </button>

          <p className="mt-6 text-center text-sm text-slate-500">
            Already have a profile?{' '}
            <Link to="/login" className="font-semibold text-secondary hover:underline">
              Access secure portal
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
