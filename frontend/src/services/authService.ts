import { http } from '../lib/http/httpClient';
import type { AuthUser } from '../types';

export const authService = {
  // Chamado em modo mock para encerrar sessão via MSW.
  // Em modo keycloak, o logout é feito diretamente pelo keycloakClient.
  logout: () => http.post<{ success: boolean }>('/auth/logout'),

  // Usado futuramente quando o Spring Security estiver disponível
  // para enriquecer os dados do perfil além do que está no token.
  me: () => http.get<AuthUser>('/user/me'),
};
