import { isMockMode } from '../../config/env';
import { onboardingService } from '../../services/onboardingService';
import type { AuthUser } from '../../types';

/**
 * Decisão centralizada de rota pós-login.
 *
 * Modos:
 * - VITE_API_MODE=mock  → onboarding status vem do localStorage, chaveado por user.id
 * - VITE_API_MODE=real  → onboarding status vem do backend via GET /users/me
 *                         (userService.getProfile ainda não implementado — trocar o stub abaixo)
 */
export async function resolvePostLoginRoute(user: AuthUser): Promise<string> {
  if (isMockMode) {
    return onboardingService.isCompleted(user.id) ? '/dashboard' : '/onboarding';
  }

  // Integração futura com Spring Security:
  //   const { userService } = await import('../../services/userService');
  //   const profile = await userService.getProfile();
  //   return profile.onboardingCompleted ? '/dashboard' : '/onboarding';
  //
  // Enquanto o backend não existe, retorna /dashboard como fallback seguro.
  return '/dashboard';
}
