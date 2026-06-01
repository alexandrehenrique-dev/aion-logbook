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
  components/
    feedback/           — GlobalLoading, Skeleton, EmptyState, ErrorState
  config/
    env.ts              — leitura centralizada de import.meta.env
  features/
    auth/               — AuthContext, useAuth (mock login/logout)
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
| `VITE_AUTH_MODE` | `mock` | Login aceita qualquer credencial, retorna usuário dev |
| `VITE_AUTH_MODE` | `keycloak` | Preparado para integração Keycloak/OIDC (TODO) |

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

```bash
pnpm install
pnpm dev
```

O app estará disponível em `http://localhost:5173`.

## Build

```bash
pnpm build
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
GET/POST       /api/directions
GET/PUT/DELETE /api/directions/:id

GET/POST       /api/plans
GET/PUT/DELETE /api/plans/:id
POST           /api/plans/:id/start|complete|partial|postpone|ignore|cancel|modify

GET            /api/dashboard/today
GET            /api/dashboard/summary

GET/POST       /api/sessions
GET            /api/sessions/:id

GET/POST       /api/logs
GET/PUT/DELETE /api/logs/:id

GET            /api/analytics/overview
GET            /api/analytics/plans-by-day
GET            /api/analytics/status-distribution
GET            /api/analytics/time-by-direction
GET            /api/analytics/planned-vs-executed
```

## Integração futura com backend

O backend será Spring Boot + Keycloak. Em produção, o build estático do frontend será servido pelo próprio Spring Boot.

Para ativar o backend real:
```env
VITE_API_MODE=real
VITE_API_BASE_URL=http://localhost:8080/api
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

- [ ] Integração com backend Spring Boot
- [ ] Autenticação real via Keycloak
- [ ] Docker Compose (frontend + backend + Keycloak + PostgreSQL)
- [ ] Deploy no genesis-lab
- [ ] TanStack Query para cache de dados
- [ ] Testes (Vitest + Testing Library)
