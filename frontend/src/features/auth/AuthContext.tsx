import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import { authService } from '../../services/authService';
import { isMockAuthMode } from '../../config/env';
import type { AuthUser } from '../../types';

const STORAGE_KEY = 'aion:auth-user';

const MOCK_DEV_USER: AuthUser = {
  id: 'user-1',
  name: 'Viajante',
  email: 'viajante@aion.app',
};

type AuthState =
  | { status: 'loading' }
  | { status: 'authenticated'; user: AuthUser }
  | { status: 'unauthenticated' };

type AuthContextValue = {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({ status: 'loading' });

  useEffect(() => {
    const stored = sessionStorage.getItem(STORAGE_KEY);
    if (stored) {
      try {
        setState({ status: 'authenticated', user: JSON.parse(stored) });
      } catch {
        setState({ status: 'unauthenticated' });
      }
    } else {
      setState({ status: 'unauthenticated' });
    }
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    if (isMockAuthMode) {
      // In mock mode, accept any credentials and return the dev user
      void email; void password;
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(MOCK_DEV_USER));
      setState({ status: 'authenticated', user: MOCK_DEV_USER });
      return;
    }

    // TODO: replace with Keycloak flow when VITE_AUTH_MODE=keycloak
    const { user } = await authService.login({ email, password });
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(user));
    setState({ status: 'authenticated', user });
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch {
      // ignore logout errors
    }
    sessionStorage.removeItem(STORAGE_KEY);
    setState({ status: 'unauthenticated' });
  }, []);

  const value: AuthContextValue = {
    user: state.status === 'authenticated' ? state.user : null,
    isAuthenticated: state.status === 'authenticated',
    isLoading: state.status === 'loading',
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
