import { http } from '../lib/http/httpClient';
import { isMockMode } from '../config/env';
import type { UserProfile } from '../types';
import { adaptMe, type MeResponse } from '../lib/api-adapters';

export const authService = {
  // Chamado apenas em modo mock para encerrar sessão via MSW.
  // Em modo keycloak, o logout é feito diretamente pelo keycloakClient.
  logout: () => {
    if (!isMockMode) return Promise.resolve({ success: true });
    return http.post<{ success: boolean }>('/auth/logout');
  },

  // Perfil do usuário autenticado. Em modo real, adapta MeResponse → UserProfile.
  me: async (): Promise<UserProfile> => {
    if (isMockMode) {
      return http.get<UserProfile>('/me');
    }
    const raw = await http.get<MeResponse>('/me');
    return adaptMe(raw);
  },
};
