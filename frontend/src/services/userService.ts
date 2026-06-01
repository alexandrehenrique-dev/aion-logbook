import { http } from '../lib/http/httpClient';
import type { UserProfile } from '../types';

// Usado em VITE_API_MODE=real para obter perfil do usuário autenticado.
// O backend valida o JWT via Spring Security e retorna dados da aplicação,
// incluindo onboardingCompleted — que o Keycloak não conhece.
export const userService = {
  getProfile: () => http.get<UserProfile>('/users/me'),
};
