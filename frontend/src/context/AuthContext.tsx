import { createContext, useContext, useState, useEffect, useRef } from 'react';
import type { ReactNode } from 'react';
import {
  onIdTokenChanged,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signInWithPopup,
  signOut as firebaseSignOut,
  type User as FirebaseUser
} from 'firebase/auth';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { auth, googleProvider, db } from '../config/firebase';
import type { User, AuthState, UserRole } from '../types/auth';

interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<User>;
  register: (email: string, password: string, fullName: string, role: UserRole, phone?: string) => Promise<void>;
  loginWithGoogle: () => Promise<User>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<string | null>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const TOKEN_REFRESH_INTERVAL = 55 * 60 * 1000; // 55 minutes

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [state, setState] = useState<AuthState>({
    user: null,
    isAuthenticated: false,
    isLoading: true,
  });
  const refreshTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const fetchUserProfile = async (firebaseUser: FirebaseUser): Promise<User | null> => {
    try {
      const userDoc = await getDoc(doc(db, 'users', firebaseUser.uid));
      if (userDoc.exists()) {
        return { id: firebaseUser.uid, ...userDoc.data() } as User;
      }
      return null;
    } catch (error) {
      console.error('Error fetching user profile from Firestore:', error);
      return null;
    }
  };

  const setupTokenRefresh = (firebaseUser: FirebaseUser) => {
    if (refreshTimerRef.current) {
      clearInterval(refreshTimerRef.current);
    }

    refreshTimerRef.current = setInterval(async () => {
      try {
        const token = await firebaseUser.getIdToken(true);
        localStorage.setItem('idbi_firebase_token', token);
      } catch (error) {
        console.error('Token refresh failed:', error);
      }
    }, TOKEN_REFRESH_INTERVAL);
  };

  useEffect(() => {
    const unsubscribe = onIdTokenChanged(auth, async (firebaseUser) => {
      if (firebaseUser) {
        const token = await firebaseUser.getIdToken();
        localStorage.setItem('idbi_firebase_token', token);

        const userProfile = await fetchUserProfile(firebaseUser);
        if (userProfile) {
          setState({ user: userProfile, isAuthenticated: true, isLoading: false });
          setupTokenRefresh(firebaseUser);
        } else {
          setState({ user: null, isAuthenticated: false, isLoading: false });
        }
      } else {
        localStorage.removeItem('idbi_firebase_token');
        setState({ user: null, isAuthenticated: false, isLoading: false });
        if (refreshTimerRef.current) {
          clearInterval(refreshTimerRef.current);
          refreshTimerRef.current = null;
        }
      }
    });

    return () => {
      unsubscribe();
      if (refreshTimerRef.current) {
        clearInterval(refreshTimerRef.current);
      }
    };
  }, []);

  const login = async (email: string, password: string): Promise<User> => {
    setState(prev => ({ ...prev, isLoading: true }));
    try {
      const credential = await signInWithEmailAndPassword(auth, email, password);
      const token = await credential.user.getIdToken();
      localStorage.setItem('idbi_firebase_token', token);

      const userProfile = await fetchUserProfile(credential.user);
      if (!userProfile) throw new Error('User profile not found in Firestore');

      setState({ user: userProfile, isAuthenticated: true, isLoading: false });
      setupTokenRefresh(credential.user);
      return userProfile;
    } catch (error: any) {
      setState(prev => ({ ...prev, isLoading: false }));
      throw error.message || 'Login failed';
    }
  };

  const register = async (email: string, password: string, fullName: string, role: UserRole, phone?: string): Promise<void> => {
    setState(prev => ({ ...prev, isLoading: true }));
    try {
      const credential = await createUserWithEmailAndPassword(auth, email, password);

      await setDoc(doc(db, 'users', credential.user.uid), {
        email,
        fullName,
        role,
        phone: phone || null,
        status: 'ACTIVE',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      });

      setState(prev => ({ ...prev, isLoading: false }));
    } catch (error: any) {
      setState(prev => ({ ...prev, isLoading: false }));
      throw error.message || 'Registration failed';
    }
  };

  const loginWithGoogle = async (): Promise<User> => {
    setState(prev => ({ ...prev, isLoading: true }));
    try {
      const credential = await signInWithPopup(auth, googleProvider);
      const token = await credential.user.getIdToken();
      localStorage.setItem('idbi_firebase_token', token);

      let userProfile = await fetchUserProfile(credential.user);

      if (!userProfile) {
        const newProfile: User = {
          id: credential.user.uid,
          email: credential.user.email || '',
          fullName: credential.user.displayName || 'Google User',
          role: 'ROLE_MSME',
          status: 'ACTIVE',
        };
        await setDoc(doc(db, 'users', credential.user.uid), {
          ...newProfile,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        });
        userProfile = newProfile;
      }

      setState({ user: userProfile, isAuthenticated: true, isLoading: false });
      setupTokenRefresh(credential.user);
      return userProfile;
    } catch (error: any) {
      setState(prev => ({ ...prev, isLoading: false }));
      throw error.message || 'Google sign-in failed';
    }
  };

  const logout = async () => {
    await firebaseSignOut(auth);
    localStorage.removeItem('idbi_firebase_token');
    setState({ user: null, isAuthenticated: false, isLoading: false });
    if (refreshTimerRef.current) {
      clearInterval(refreshTimerRef.current);
      refreshTimerRef.current = null;
    }
  };

  const refreshToken = async (): Promise<string | null> => {
    try {
      if (auth.currentUser) {
        const token = await auth.currentUser.getIdToken(true);
        localStorage.setItem('idbi_firebase_token', token);
        return token;
      }
      return null;
    } catch (error) {
      console.error('Manual token refresh failed:', error);
      return null;
    }
  };

  return (
    <AuthContext.Provider value={{ ...state, login, register, loginWithGoogle, logout, refreshToken }}>
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
