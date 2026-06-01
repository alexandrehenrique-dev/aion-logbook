import { isMockMode } from '../config/env';

export async function initMocks() {
  if (!isMockMode) return;

  if (typeof window !== 'undefined') {
    const { worker } = await import('./browser');
    await worker.start({
      onUnhandledRequest: 'bypass',
      quiet: true,
    });
  }
}
