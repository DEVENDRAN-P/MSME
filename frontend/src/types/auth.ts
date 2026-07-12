export type UserRole = 'ROLE_MSME' | 'ROLE_LOAN_OFFICER' | 'ROLE_CREDIT_MANAGER' | 'ROLE_ADMIN';

export type UserStatus = 'ACTIVE' | 'INACTIVE';

export interface User {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
  phone?: string;
  status: UserStatus;
}

export interface AuthState {
  token: string | null;
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: string;
  email: string;
  fullName: string;
  role: UserRole;
}
