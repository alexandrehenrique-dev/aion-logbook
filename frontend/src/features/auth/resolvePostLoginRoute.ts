import { isMockMode } from '../../config/env';
import { onboardingService } from '../../services/onboardingService';

export async function resolvePostLoginRoute(): Promise<string> {
  try {
    if (isMockMode) {
      const status = await onboardingService.getStatus();
      return status.completed ? '/dashboard' : '/onboarding';
    }

    // Real mode: ask backend via GET /me or /onboarding/status
    const { userService } = await import('../../services/userService');
    const profile = await userService.getProfile();
    return (profile as { onboardingCompleted?: boolean }).onboardingCompleted ? '/dashboard' : '/onboarding';
  } catch {
    return '/dashboard';
  }
}
