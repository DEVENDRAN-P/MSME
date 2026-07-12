import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import api from '../services/api';
import type { User, AuthState, AuthResponse, UserRole } from '../types/auth';

interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<User>;
  register: (email: string, password: String, fullName: string, role: UserRole, phone?: string) => Promise<any>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [state, setState] = useState<AuthState>({
    token: null,
    user: null,
    isAuthenticated: false,
    isLoading: true,
  });

  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = localStorage.getItem('idbi_jwt_token');
      const storedUser = localStorage.getItem('idbi_user_profile');

      if (storedToken && storedUser) {
        try {
          // Verify token validity by calling profile endpoint
          const response = await api.get('/auth/profile', {
            headers: { Authorization: `Bearer ${storedToken}` }
          });
          
          setState({
            token: storedToken,
            user: response.data,
            isAuthenticated: true,
            isLoading: false,
          });
        } catch (error) {
          console.error("Token validation failed during startup", error);
          localStorage.removeItem('idbi_jwt_token');
          localStorage.removeItem('idbi_user_profile');
          setState({
            token: null,
            user: null,
            isAuthenticated: false,
            isLoading: false,
          });
        }
      } else {
        setState(prev => ({ ...prev, isLoading: false }));
      }
    };

    initializeAuth();
  }, []);

  const login = async (email: string, password: string): Promise<User> => {
    setState(prev => ({ ...prev, isLoading: true }));
    try {
      const response = await api.post<AuthResponse>('/auth/login', { email, password });
      const { token, userId, fullName, role } = response.data;

      localStorage.setItem('idbi_jwt_token', token);
      
      const userProfile: User = {
        id: userId,
        email,
        fullName,
        role,
        status: 'ACTIVE'
      };
      
      localStorage.setItem('idbi_user_profile', JSON.stringify(userProfile));

      setState({
        token,
        user: userProfile,
        isAuthenticated: true,
        isLoading: false,
      });

      return userProfile;
    } catch (error: any) {
      setState(prev => ({ ...prev, isLoading: false }));
      throw error.response?.data?.message || 'Login failed';
    }
  };

  const register = async (
    email: string,
    password: String,
    fullName: string,
    role: UserRole,
    phone?: string
  ): Promise<any> => {
    setState(prev => ({ ...prev, isLoading: true }));
    try {
      const response = await api.post('/auth/register', {
        email,
        password,
        fullName,
        role,
        phone,
      });
      setState(prev => ({ ...prev, isLoading: false }));
      return response.data;
    } catch (error: any) {
      setState(prev => ({ ...prev, isLoading: false }));
      const errorMsg = error.response?.data?.errors 
        ? Object.values(error.response.data.errors).join(', ') 
        : error.response?.data?.message || 'Registration failed';
      throw errorMsg;
    }
  };

  const logout = () => {
    localStorage.removeItem('idbi_jwt_token');
    localStorage.removeItem('idbi_user_profile');
    setState({
      token: null,
      user: null,
      isAuthenticated: false,
      isLoading: false,
    });
  };

  return (
    <AuthContext.Provider value={{ ...state, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
