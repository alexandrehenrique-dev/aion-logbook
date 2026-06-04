# Aion Logbook Frontend

Frontend React/Vite do Aion Log Book. A SPA cobre dashboard, direcoes, planos, sessoes, logbook, analytics, onboarding, settings e login, podendo rodar com dados mockados via MSW ou integrada ao backend Spring Boot com Keycloak.

## Stack

- React 18
- TypeScript
- Vite 6
- Tailwind CSS 4
- React Router 7
- MSW 2
- Keycloak JS
- Recharts
- Sonner
- Radix UI / shadcn
- npm com `package-lock.json`

## Pre-requisitos

- Node.js 20+.
- npm. Este checkout possui `package-lock.json`; use npm como gerenciador padrao.
- Backend Spring Boot e Keycloak apenas quando `VITE_API_MODE=real` ou `VITE_AUTH_MODE=keycloak`.

## Instalacao

A partir de `frontend/`:

```bash
npm install
```

## Execucao Local

```bash
npm run dev
```

O app abre em:

```txt
http://localhost:5173
```

Para sobrescrever variaveis locais, copie o exemplo:

```bash
cp .env.example .env.local
```

Arquivos `.env`, `.env.local`, `.env.backend` e outros `.env*` locais nao devem ser versionados.

## Scripts

```bash
npm run dev
npm run lint
npm run typecheck
npm run build
npm run build:backend
npm run preview
```

- `dev`: inicia Vite.
- `lint`: roda ESLint.
- `typecheck`: roda `tsc -b`.
- `build`: gera `dist/`.
- `build:backend`: gera o build estatico em `../backend/src/main/resources/static`.
- `preview`: serve o build local do Vite.

## Variaveis de Ambiente

Principais variaveis:

| Variavel | Valores | Descricao |
|---|---|---|
| `VITE_API_MODE` | `mock`, `real` | Define se as chamadas usam MSW ou backend real |
| `VITE_API_BASE_URL` | URL ou path | Base URL da API, por exemplo `http://localhost:8080/api/v1` |
| `VITE_AUTH_MODE` | `mock`, `keycloak` | Define login local ou Keycloak |
| `VITE_KEYCLOAK_ENABLED` | `true`, `false` | Liga integracao Keycloak |
| `VITE_KEYCLOAK_URL` | URL | URL publica do Keycloak |
| `VITE_KEYCLOAK_REALM` | texto | Realm Keycloak |
| `VITE_KEYCLOAK_CLIENT_ID` | texto | Client SPA publico |
| `VITE_ENABLE_NOTIFICATIONS` | `true`, `false` | Feature flag de notificacoes |

Desenvolvimento sem backend:

```env
VITE_API_MODE=mock
VITE_AUTH_MODE=mock
VITE_KEYCLOAK_ENABLED=false
```

Backend e Keycloak locais:

```env
VITE_API_MODE=real
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_AUTH_MODE=keycloak
VITE_KEYCLOAK_ENABLED=true
VITE_KEYCLOAK_URL=http://localhost:8181
VITE_KEYCLOAK_REALM=aion-logbook
VITE_KEYCLOAK_CLIENT_ID=aion-logbook-web
```

Frontend servido pelo Spring Boot:

```env
VITE_API_MODE=real
VITE_API_BASE_URL=/api/v1
VITE_AUTH_MODE=keycloak
VITE_KEYCLOAK_ENABLED=true
```

## Mock, API Real e MSW

Com `VITE_API_MODE=mock`, o MSW e inicializado por `src/mocks/init.ts` e intercepta chamadas REST no navegador. Os handlers ficam em `src/mocks/handlers.ts`.

Com `VITE_API_MODE=real`, os services chamam `VITE_API_BASE_URL` via `src/lib/http/httpClient.ts`. Componentes devem consumir services, nao `fetch` direto.

O worker publico do MSW fica em:

```txt
public/mockServiceWorker.js
```

## Autenticacao

- `VITE_AUTH_MODE=mock`: fluxo local de desenvolvimento.
- `VITE_AUTH_MODE=keycloak`: Authorization Code Flow + PKCE via `keycloak-js`.

Arquivos principais:

```txt
src/features/auth/AuthContext.tsx
src/features/auth/keycloakClient.ts
src/features/auth/resolvePostLoginRoute.ts
public/silent-check-sso.html
```

O client esperado no ambiente local e `aion-logbook-web` no realm `aion-logbook`.

## Integracao com Backend

API local esperada:

```txt
http://localhost:8080/api/v1
```

Endpoints consumidos pelos services incluem:

- `/api/v1/me`
- `/api/v1/directions`
- `/api/v1/plans`
- `/api/v1/sessions`
- `/api/v1/logs`
- `/api/v1/dashboard/today`
- `/api/v1/dashboard/summary`
- `/api/v1/analytics/*`
- `/api/v1/onboarding/*`
- `/api/v1/settings`
- `/api/v1/bug-reports`

## Build para Backend

`npm run build:backend` roda typecheck e Vite em modo `backend`, limpando e recriando:

```txt
../backend/src/main/resources/static
```

Se precisar de valores especificos para esse modo, use variaveis de ambiente do shell/CI ou um arquivo local ignorado pelo Git, como `.env.backend`.

## Estrutura

```txt
src/
  app/                 rotas, layouts, paginas e componentes
  config/env.ts        leitura centralizada de import.meta.env
  features/auth/       AuthContext, useAuth e Keycloak client
  features/bug-report/ modal de bug report
  lib/http/            cliente HTTP
  lib/api-adapters/    adaptadores de contratos da API
  mocks/               MSW e handlers mockados
  services/            services por dominio
  styles/              CSS global, tema e Tailwind
  types/               contratos TypeScript
  utils/               helpers compartilhados
```

## Troubleshooting

- MSW nao intercepta: confirme `VITE_API_MODE=mock` e a existencia de `public/mockServiceWorker.js`.
- `401 Unauthorized`: confira `VITE_AUTH_MODE=keycloak`, `VITE_KEYCLOAK_ENABLED=true`, realm/client e token no header.
- Erro de CORS: confira `CORS_ALLOWED_ORIGINS` no backend e `webOrigins` do client Keycloak.
- Build falha por tipos: rode `npm run typecheck` para isolar o erro.
- Build para backend nao reflete mudancas: rode `npm run build:backend` novamente e confira `backend/src/main/resources/static`.
