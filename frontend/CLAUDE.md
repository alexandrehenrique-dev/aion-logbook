# Aion Logbook Frontend — Claude Instructions

## Propósito

Frontend web do Aion Logbook, plataforma pessoal de direção, planejamento, execução e memória de jornada.

Frase central: "Não é uma agenda. É um mapa para não se perder de si mesmo."

## Escopo deste diretório

Este diretório contém exclusivamente o frontend React/TypeScript.

Não criar backend aqui. Não criar Spring Boot. Não criar Docker Compose.

## Arquitetura

```
src/
  app/          — App.tsx, páginas, layouts, providers, componentes ui
  components/   — componentes reutilizáveis (feedback/, etc.)
  config/       — env.ts (leitura centralizada de variáveis de ambiente)
  features/     — auth/, bug-report/ (features por domínio)
  lib/          — http/httpClient.ts (fetch encapsulado)
  mocks/        — browser.ts, handlers.ts, init.ts (MSW)
  services/     — um arquivo por domínio, espelham endpoints futuros
  types/        — contratos TypeScript alinhados com o backend
  utils/        — utilitários gerais
```

## Regras

- Componentes NÃO chamam `fetch` diretamente. Usam `services` ou hooks.
- Mocks centralizados em `src/mocks/`. Não hardcodar dados em páginas.
- Contratos de domínio em `src/types/index.ts` — status UPPERCASE.
- Troca mock/API real via env: `VITE_API_MODE=mock|real`.
- Autenticação via env: `VITE_AUTH_MODE=mock|keycloak`.
- Linguagem da UI nunca punitiva (MISSED="Ficou para trás", não "Falhou").
- Responsividade obrigatória: mobile, tablet, desktop.
- Tema: `system/light/dark` com `storageKey="aion-theme"` via next-themes.

## Ambiente

```env
VITE_API_MODE=mock | real
VITE_API_BASE_URL=http://localhost:8080/api
VITE_AUTH_MODE=mock | keycloak
VITE_KEYCLOAK_ENABLED=false | true
VITE_KEYCLOAK_URL=http://localhost:8181
VITE_KEYCLOAK_REALM=aion-logbook
VITE_KEYCLOAK_CLIENT_ID=aion-logbook-web
```

Copie `.env.example` para `.env.local` para desenvolvimento.

## Comandos

```bash
pnpm install
pnpm dev
pnpm build
```

## Stack

React 18 + TypeScript + Vite 6 + Tailwind CSS v4 + react-router v7 + MSW 2 + next-themes + recharts + motion + Radix UI/shadcn

## Integração futura

Backend: Spring Boot + Keycloak. Quando `VITE_API_MODE=real`, o httpClient chamará `VITE_API_BASE_URL`. Keycloak será ativado por `VITE_AUTH_MODE=keycloak`.

## Não fazer

- Não criar backend
- Não alterar regras de negócio
- Não acoplar componentes diretamente ao backend
- Não hardcodar dados de mock em páginas/componentes
- Não usar `window.location.href` — usar `navigate()` do react-router
