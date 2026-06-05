import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import { authService } from '../../services/authService';
import { isMockAuthMode, isKeycloakAuthMode } from '../../config/env';
import type { AuthUser } from '../../types';
import {
  getValidToken,
  initKeycloak,
  keycloakLogin,
  keycloakLogout,
  mapKeycloakUser,
} from './keycloakClient';
import { registerTokenGetter } from '../../lib/http/httpClient';

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
  login: () => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({ status: 'loading' });
  const kcInitRef = useRef(false);

  useEffect(() => {
    if (isKeycloakAuthMode) {
      // Guard against React StrictMode double-invocation
      if (kcInitRef.current) return;
      kcInitRef.current = true;

      initKeycloak()
        .then((authenticated) => {
          if (authenticated) {
            registerTokenGetter(getValidToken);
            setState({ status: 'authenticated', user: mapKeycloakUser() });
          } else {
            setState({ status: 'unauthenticated' });
          }
        })
        .catch((error) => {
          console.error('[AION_KEYCLOAK_INIT_ERROR]', error);
          setState({ status: 'unauthenticated' });
        });
      return;
    }

    // mock mode: restore from sessionStorage
    const stored = sessionStorage.getItem(STORAGE_KEY);
    if (stored) {
      try {
        setState({ status: 'authenticated', user: JSON.parse(stored) as AuthUser });
      } catch {
        setState({ status: 'unauthenticated' });
      }
    } else {
      setState({ status: 'unauthenticated' });
    }
  }, []);

  const login = useCallback(async () => {
    if (isKeycloakAuthMode) {
      // Redirects browser to Keycloak — never returns
      await keycloakLogin();
      return;
    }

    // mock mode (isMockAuthMode) — aceita qualquer coisa, retorna usuário dev
    void isMockAuthMode;
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(MOCK_DEV_USER));
    setState({ status: 'authenticated', user: MOCK_DEV_USER });
  }, []);

  const logout = useCallback(async () => {
    if (isKeycloakAuthMode) {
      sessionStorage.removeItem(STORAGE_KEY);
      await keycloakLogout();
      return;
    }

    try {
      await authService.logout();
    } catch {
      // ignore
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
