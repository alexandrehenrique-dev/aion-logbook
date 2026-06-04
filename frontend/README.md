# Aion Logbook — Frontend

> Não é uma agenda. É um mapa para não se perder de si mesmo.

## Visão

Aion Logbook é uma plataforma pessoal de direção, planejamento, execução e memória de jornada.

Não é uma lista de tarefas punitiva. É um sistema de continuidade existencial — para registrar intenção, execução, desvios e aprendizado ao longo do tempo.

## Stack

| Tecnologia | Versão | Papel |
|---|---|---|
| React | 18 | UI framework |
| TypeScript | — | Tipagem estática |
| Vite | 6 | Build tool |
| Tailwind CSS | 4 | Estilização |
| react-router | 7 | Roteamento |
| MSW | 2 | Mock Service Worker |
| next-themes | 0.4 | Tema system/light/dark |
| recharts | 2 | Gráficos |
| motion | 12 | Animações |
| Radix UI / shadcn | — | Componentes primitivos |

## Arquitetura

```
src/
  app/
    App.tsx             — raiz, providers, rotas
    layouts/            — MainLayout (sidebar, header, notif, perfil)
    pages/              — Dashboard, Plans, Directions, Analytics, etc.
    components/         — Button, Card, ThemeToggle, ui/* (shadcn)
    providers/          — ThemeProvider
  config/
    env.ts              — leitura centralizada de import.meta.env
  features/
    auth/               — AuthContext, useAuth, Keycloak client
    bug-report/         — BugReportModal
  lib/
    http/
      httpClient.ts     — fetch encapsulado (get/post/put/patch/delete)
  mocks/
    browser.ts          — setupWorker MSW
    handlers.ts         — todos os handlers REST mockados
    init.ts             — inicialização condicional por VITE_API_MODE
  services/
    authService.ts
    dashboardService.ts
    planService.ts
    directionService.ts
    sessionService.ts
    logbookService.ts
    analyticsService.ts
    browserNotificationService.ts
    searchService.ts
  types/
    index.ts            — contratos de domínio alinhados com backend
  utils/
    cn.ts
```

## Estratégia Mock/API

O sistema suporta dois modos controlados por variável de ambiente:

| Variável | Valor | Comportamento |
|---|---|---|
| `VITE_API_MODE` | `mock` | MSW intercepta todas as chamadas HTTP |
| `VITE_API_MODE` | `real` | Chamadas vão para `VITE_API_BASE_URL` |

Componentes consomem serviços. Nunca chamam `fetch` diretamente. Os contratos de dados são idênticos nos dois modos.

## Autenticação

| Variável | Valor | Comportamento |
|---|---|---|
| `VITE_AUTH_MODE` | `mock` | Login local de desenvolvimento |
| `VITE_AUTH_MODE` | `keycloak` | Authorization Code Flow + PKCE via Keycloak |

## Variáveis de ambiente

Copie `.env.example` para `.env.local`:

```bash
cp .env.example .env.local
```

```env
VITE_API_MODE=mock
VITE_API_BASE_URL=http://localhost:8080/api

VITE_AUTH_MODE=mock
VITE_KEYCLOAK_ENABLED=false
VITE_KEYCLOAK_URL=http://localhost:8181
VITE_KEYCLOAK_REALM=aion-logbook
VITE_KEYCLOAK_CLIENT_ID=aion-logbook-web
```

## Rodando localmente

Este checkout possui `package-lock.json`; use `npm` por padrão:

```bash
npm install
npm run dev
```

Se `pnpm` estiver instalado e você preferir usá-lo:

```bash
pnpm install
pnpm dev
```

O app estará disponível em `http://localhost:5173`.

## Build

```bash
npm run build
```

Build para ser servido pelo backend Spring Boot:

```bash
npm run build:backend
```

## Domínios e contratos

Os tipos TypeScript em `src/types/index.ts` espelham os contratos do backend Spring Boot futuro.

**PlanStatus (UPPERCASE):**
```
DRAFT | SCHEDULED | PENDING | DUE | IN_PROGRESS | COMPLETED
PARTIAL | POSTPONED | IGNORED | CANCELED | MISSED
```

**Labels humanizados (não punitivos):**
- `MISSED` → "Ficou para trás"
- `IGNORED` → "Ignorado por escolha"
- `PARTIAL` → "Feito parcialmente"
- `DUE` → "Chegou a hora"

## Endpoints mockados (espelham backend futuro)

```
GET/POST       /api/v1/directions
GET/PUT/DELETE /api/v1/directions/:id

GET/POST       /api/v1/plans
GET/PUT/DELETE /api/v1/plans/:id
POST           /api/v1/plans/:id/start|complete|partial|postpone|ignore|cancel|modify

GET            /api/v1/dashboard/today
GET            /api/v1/dashboard/summary

GET/POST       /api/v1/sessions
GET            /api/v1/sessions/:id

GET/POST       /api/v1/logs
GET/PUT/DELETE /api/v1/logs/:id

GET            /api/v1/analytics/overview
GET            /api/v1/analytics/plans-by-day
GET            /api/v1/analytics/status-distribution
GET            /api/v1/analytics/time-by-direction
GET            /api/v1/analytics/planned-vs-executed
```

## Integração com backend

O backend é Spring Boot + Keycloak. Em validação/deploy, o build estático do frontend pode ser servido pelo próprio Spring Boot.

Para ativar o backend real:
```env
VITE_API_MODE=real
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Para ativar Keycloak:
```env
VITE_AUTH_MODE=keycloak
VITE_KEYCLOAK_ENABLED=true
```

## Convenções

- Não usar `fetch` em componentes — sempre via services
- Não hardcodar dados mock em páginas
- Preservar contratos de tipo ao modificar handlers
- Linguagem não punitiva em toda a UI
- Responsividade obrigatória (mobile-first)

## Próximos passos

- [ ] Docker Compose (frontend + backend + Keycloak + PostgreSQL)
- [ ] Deploy no genesis-lab
- [ ] TanStack Query para cache de dados
- [ ] Testes (Vitest + Testing Library)
