const storageKey = (userId: string) => `aion:onboarding-completed:${userId}`;

export const onboardingService = {
  isCompleted(userId: string): boolean {
    return localStorage.getItem(storageKey(userId)) === 'true';
  },

  markCompleted(userId: string): void {
    localStorage.setItem(storageKey(userId), 'true');
  },

  // Útil para testes e para resetar onboarding em desenvolvimento
  reset(userId: string): void {
    localStorage.removeItem(storageKey(userId));
  },
};
