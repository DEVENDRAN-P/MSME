import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import MSMEDashboard from './pages/msme/Dashboard';
import RegisterBusiness from './pages/msme/RegisterBusiness';
import DataIngestion from './pages/msme/DataIngestion';
import FeatureIntelligence from './pages/msme/FeatureIntelligence';
import FinancialHealth from './pages/msme/FinancialHealth';
import ConsentManager from './pages/msme/ConsentManager';
import ForecastSimulator from './pages/msme/ForecastSimulator';
import LenderDashboard from './pages/lender/Dashboard';
import AdminDashboard from './pages/admin/Dashboard';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

// Root Redirector based on user role authentication status
const RootRedirect = () => {
  const { isAuthenticated, user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center bg-primary-900 text-white">
        <div className="relative flex h-20 w-20 items-center justify-center">
          <div className="absolute h-16 w-16 animate-spin rounded-full border-4 border-solid border-accent border-t-transparent"></div>
          <span className="font-semibold text-accent text-sm">IDBI</span>
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  switch (user.role) {
    case 'ROLE_MSME':
      return <Navigate to="/msme" replace />;
    case 'ROLE_LOAN_OFFICER':
    case 'ROLE_CREDIT_MANAGER':
      return <Navigate to="/lender" replace />;
    case 'ROLE_ADMIN':
      return <Navigate to="/admin" replace />;
    default:
      return <Navigate to="/login" replace />;
  }
};

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            {/* Public Routes */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            {/* Protected Role-Based Routes */}
            <Route
              path="/msme"
              element={
                <ProtectedRoute allowedRoles={['ROLE_MSME']}>
                  <MSMEDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/msme/register-business"
              element={
                <ProtectedRoute allowedRoles={['ROLE_MSME']}>
                  <RegisterBusiness />
                </ProtectedRoute>
              }
            />
            <Route
              path="/msme/data-ingest"
              element={
                <ProtectedRoute allowedRoles={['ROLE_MSME']}>
                  <DataIngestion />
                </ProtectedRoute>
              }
            />
            <Route
              path="/msme/features"
              element={
                <ProtectedRoute allowedRoles={['ROLE_MSME']}>
                  <FeatureIntelligence />
                </ProtectedRoute>
              }
            />
            <Route
              path="/msme/health-card"
              element={
                <ProtectedRoute allowedRoles={['ROLE_MSME']}>
                  <FinancialHealth />
                </ProtectedRoute>
              }
            />
            <Route
              path="/msme/consents"
              element={
                <ProtectedRoute allowedRoles={['ROLE_MSME']}>
                  <ConsentManager />
                </ProtectedRoute>
              }
            />
            <Route
              path="/msme/forecast"
              element={
                <ProtectedRoute allowedRoles={['ROLE_MSME']}>
                  <ForecastSimulator />
                </ProtectedRoute>
              }
            />
            <Route
              path="/lender"
              element={
                <ProtectedRoute allowedRoles={['ROLE_LOAN_OFFICER', 'ROLE_CREDIT_MANAGER']}>
                  <LenderDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
                  <AdminDashboard />
                </ProtectedRoute>
              }
            />

            {/* Root & Fallback Redirects */}
            <Route path="/" element={<RootRedirect />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
