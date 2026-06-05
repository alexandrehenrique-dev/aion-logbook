export const env = {
  apiMode: import.meta.env.VITE_API_MODE ?? 'mock',
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  authMode: import.meta.env.VITE_AUTH_MODE ?? 'mock',
  keycloakEnabled: import.meta.env.VITE_KEYCLOAK_ENABLED === 'true',
  keycloakUrl: import.meta.env.VITE_KEYCLOAK_URL as string | undefined,
  keycloakRealm: import.meta.env.VITE_KEYCLOAK_REALM as string | undefined,
  keycloakClientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID as string | undefined,
  // Módulo de notificações ainda não implementado no backend.
  // Manter false até GET /api/v1/notifications existir.
  enableNotifications: import.meta.env.VITE_ENABLE_NOTIFICATIONS === 'true',
  keycloakPkceEnabled: import.meta.env.VITE_KEYCLOAK_PKCE_ENABLED !== 'false',
} as const;

export const isMockMode = env.apiMode === 'mock';
export const isRealApiMode = env.apiMode === 'real';
export const isMockAuthMode = env.authMode === 'mock';
export const isKeycloakAuthMode = env.authMode === 'keycloak';
export const isNotificationsEnabled = env.enableNotifications;
