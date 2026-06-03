import { http } from '../lib/http/httpClient';

export type OnboardingStatus = {
  completed: boolean;
};

export type OnboardingDirectionInput = {
  name: string;
  description?: string;
  color?: string;
  icon?: string;
  identityPhrase?: string;
};

export const onboardingService = {
  getStatus: () => http.get<OnboardingStatus>('/onboarding/status'),
  createDirections: (directions: OnboardingDirectionInput[]) =>
    http.post<void>('/onboarding/directions', { directions }),
  complete: () => http.post<void>('/onboarding/complete'),
};
