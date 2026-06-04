import Keycloak from 'keycloak-js';
import { env } from '../../config/env';

let _keycloak: Keycloak | null = null;
let _initPromise: Promise<boolean> | null = null;

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
// onLoad: 'check-sso' restaura a sessão do Keycloak no reload da página.
// silentCheckSsoRedirectUri usa um iframe invisível para checar a sessão sem redirecionar o browser.
export function initKeycloak(): Promise<boolean> {
  const kc = getInstance();
  if (!_initPromise) {
    _initPromise = kc.init({
      pkceMethod: 'S256',
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
    });
  }
  return _initPromise;
}

export function keycloakLogin(): Promise<void> {
  return getInstance().login();
}

export function keycloakLogout(redirectUri: string): Promise<void> {
  return getInstance().logout({ redirectUri });
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
