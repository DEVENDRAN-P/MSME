import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { UserRole } from '../types/auth';

interface ProtectedRouteProps {
  children: ReactNode;
  allowedRoles?: UserRole[];
}

export const ProtectedRoute = ({ children, allowedRoles }: ProtectedRouteProps) => {
  const { isAuthenticated, user, isLoading } = useAuth();
  const location = useLocation();

  // Premium bank loader screen
  if (isLoading) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center bg-primary-900 text-white">
        <div className="relative flex h-20 w-20 items-center justify-center">
          <div className="absolute h-16 w-16 animate-spin rounded-full border-4 border-solid border-accent border-t-transparent"></div>
          <span className="font-semibold text-accent text-sm">IDBI</span>
        </div>
        <p className="mt-4 text-slate-300 animate-pulse text-sm font-medium tracking-wide">
          Verifying secure credentials...
        </p>
      </div>
    );
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Check roles authorization
  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center bg-neutral-slate px-4 text-center">
        <div className="max-w-md rounded-2xl bg-white p-8 shadow-xl border border-slate-100">
          <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-red-100 text-red-600">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-8 h-8">
              <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z" />
            </svg>
          </div>
          <h2 className="mt-6 text-2xl font-bold text-slate-900">Access Denied</h2>
          <p className="mt-3 text-slate-500 text-sm">
            Your user role <strong className="text-primary-800">{user.role}</strong> does not have authorization to view this terminal.
          </p>
          <div className="mt-8 flex justify-center gap-4">
            <button
              onClick={() => window.history.back()}
              className="px-4 py-2 text-sm font-semibold text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg transition-colors duration-200"
            >
              Go Back
            </button>
            <NavigateToDashboard role={user.role} />
          </div>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};

// Helper component to route user to their respective default dashboard
const NavigateToDashboard = ({ role }: { role: UserRole }) => {
  const getDashboardPath = (role: UserRole) => {
    switch (role) {
      case 'ROLE_MSME':
        return '/msme';
      case 'ROLE_LOAN_OFFICER':
      case 'ROLE_CREDIT_MANAGER':
        return '/lender';
      case 'ROLE_ADMIN':
        return '/admin';
      default:
        return '/login';
    }
  };

  return (
    <a
      href={getDashboardPath(role)}
      className="px-4 py-2 text-sm font-semibold text-white bg-primary hover:bg-primary-800 rounded-lg transition-colors duration-200"
    >
      My Dashboard
    </a>
  );
};

export default ProtectedRoute;
