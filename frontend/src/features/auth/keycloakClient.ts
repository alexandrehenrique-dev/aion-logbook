import Keycloak from 'keycloak-js';
import { env } from '../../config/env';

let _keycloak: Keycloak | null = null;
let _initPromise: Promise<boolean> | null = null;

const logbookBaseUrl = `${window.location.origin}/logbook/`;

function getInstance(): Keycloak {
  if (!_keycloak) {
    _keycloak = new Keycloak({
      url: env.keycloakUrl!,
      realm: env.keycloakRealm!,
      clientId: env.keycloakClientId!,
    });
  }
  return _keycloak;
}

// Promise singleton: seguro chamar múltiplas vezes (React StrictMode).
export function initKeycloak(): Promise<boolean> {
  const kc = getInstance();
  console.info('[AION_KEYCLOAK_CONFIG]', {
    pkceEnabled: env.keycloakPkceEnabled,
    secureContext: window.isSecureContext,
    hasCrypto: typeof window.crypto !== 'undefined',
    hasSubtle: typeof window.crypto?.subtle !== 'undefined',
  });
  if (!_initPromise) {
    _initPromise = kc.init({
      onLoad: 'check-sso',
      checkLoginIframe: false,
      pkceMethod: env.keycloakPkceEnabled ? 'S256' : false,
    });
  }

  return _initPromise;
}

export function keycloakLogin(): Promise<void> {
  return getInstance().login({
    redirectUri: `${window.location.origin}/logbook/`,
  });
}

export function keycloakLogout(): Promise<void> {
  return getInstance().logout({
    redirectUri: logbookBaseUrl,
  });
}

export function getKeycloakToken(): string | undefined {
  return _keycloak?.token;
}

export function mapKeycloakUser(): { id: string; name: string; email: string } {
  const kc = getInstance();

  return {
    id: kc.subject ?? 'keycloak-user',
    name:
      (kc.tokenParsed?.['name'] as string) ??
      (kc.tokenParsed?.['preferred_username'] as string) ??
      'Usuário',
    email: (kc.tokenParsed?.['email'] as string) ?? '',
  };
}