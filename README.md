# Aion Log Book

Aion Log Book e uma aplicacao full stack para direcao pessoal, planejamento, execucao, sessoes, analytics e registros de logbook. O frontend React pode rodar em modo mock para desenvolvimento rapido ou integrado ao backend Spring Boot com autenticacao Keycloak. Em validacao/deploy, o build estatico do frontend pode ser servido pelo proprio backend.

## Stack

- Backend: Java 21, Spring Boot 3.4, Spring Security, OAuth2 Resource Server, JPA, PostgreSQL, Flyway, Springdoc, Maven, Testcontainers.
- Frontend: React 18, TypeScript, Vite 6, Tailwind CSS 4, React Router 7, MSW, Keycloak JS, Recharts, Sonner.
- Infra local: Docker Compose, PostgreSQL 16, Keycloak 26 com realm importado e tema customizado.

## Estrutura

```txt
backend/                 API Spring Boot e migrations Flyway
frontend/                SPA React/Vite
docker-compose.yml       PostgreSQL + Keycloak locais
docker/keycloak/import   Realm local do Aion Log Book
docker/keycloak/themes   Tema customizado do login Keycloak
docker/keycloak/scripts  Scripts kcadm para identity providers
docs/                    Documentacao tecnica auxiliar
```

## Infraestrutura Local

Copie o exemplo de ambiente e preencha apenas o que for necessario:

```bash
cp .env.example .env
docker compose up -d
docker compose ps
```

Servicos padrao:

- PostgreSQL: `localhost:5433`, database `aion_logbook`, usuario `aion_user`.
- Keycloak: `http://localhost:8181`, admin local `admin/admin` se `.env` nao sobrescrever.
- Realm: `aion-logbook`.
- Client SPA: `aion-logbook-web`.

Validacao rapida do banco:

```bash
docker exec -it aion-logbook-postgres psql -U aion_user -d aion_logbook -c "select current_database(), current_user;"
```

## Keycloak

O Compose importa `docker/keycloak/import/aion-logbook-realm.json`, habilita o tema `aion-logbook` e monta scripts em `/opt/keycloak/scripts`.

Para login social, nao coloque secrets no realm JSON. Configure `.env` com os IDs/secrets reais e rode, depois que o Keycloak estiver no ar:

```bash
docker exec aion-logbook-keycloak bash /opt/keycloak/scripts/configure-identity-providers.sh
```

Redirect URIs locais esperadas:

- Frontend Vite: `http://localhost:5173/*`
- Frontend servido pelo backend: `http://localhost:8080/*`

Detalhes operacionais: [docs/keycloak-theme-social-login-operacional.md](docs/keycloak-theme-social-login-operacional.md).

## Backend

Crie o profile local nao versionado:

```bash
cp backend/src/main/resources/application-local.example.yml backend/src/main/resources/application-local.yml
```

Rode a API:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Healthcheck:

```bash
curl -i http://localhost:8080/actuator/health
```

Swagger local, quando `SWAGGER_ENABLED=true` ou profile `local`:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

Mais detalhes: [backend/README.md](backend/README.md).

## Frontend

Use npm neste checkout, pois ha `package-lock.json`. Se preferir pnpm e ele estiver instalado, os scripts tambem funcionam.

```bash
cd frontend
npm install
npm run dev
```

O frontend local abre em `http://localhost:5173`.

Copie o exemplo de ambiente do frontend quando precisar sobrescrever modos:

```bash
cp frontend/.env.example frontend/.env.local
```

Modos principais:

- `VITE_API_MODE=mock`: MSW intercepta chamadas e permite desenvolver sem backend.
- `VITE_API_MODE=real`: chamadas vao para `VITE_API_BASE_URL`.
- `VITE_AUTH_MODE=mock`: login local de desenvolvimento.
- `VITE_AUTH_MODE=keycloak`: Authorization Code Flow + PKCE via Keycloak.

Para backend real local:

```env
VITE_API_MODE=real
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_AUTH_MODE=keycloak
VITE_KEYCLOAK_ENABLED=true
VITE_KEYCLOAK_URL=http://localhost:8181
VITE_KEYCLOAK_REALM=aion-logbook
VITE_KEYCLOAK_CLIENT_ID=aion-logbook-web
```

## Build do Frontend Servido Pelo Backend

Gere o build estatico dentro de `backend/src/main/resources/static`:

```bash
cd frontend
npm run build:backend
```

Depois rode ou empacote o backend:

```bash
cd ../backend
mvn clean package
java -jar target/aion-logbook-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

Rotas SPA como `/dashboard`, `/plans/**`, `/directions/**`, `/analytics/**`, `/settings/**`, `/logbook/**`, `/onboarding` e `/login` sao encaminhadas para `index.html` pelo backend.

## Variaveis de Ambiente

Raiz `.env` para Compose:

- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`
- `KEYCLOAK_ADMIN`, `KEYCLOAK_ADMIN_PASSWORD`, `KEYCLOAK_PORT`, `KEYCLOAK_REALM`
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `MICROSOFT_CLIENT_ID`, `MICROSOFT_CLIENT_SECRET`, `MICROSOFT_TENANT_ID`
- `APPLE_CLIENT_ID`, `APPLE_TEAM_ID`, `APPLE_KEY_ID`, `APPLE_CLIENT_SECRET`

Backend:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `KEYCLOAK_ISSUER_URI`
- `CORS_ALLOWED_ORIGINS`
- `SERVER_PORT`, `SERVER_CONTEXT_PATH`
- `FLYWAY_ENABLED`, `SWAGGER_ENABLED`, `AION_SECURITY_ENABLED`
- `TELEGRAM_ENABLED`, `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHAT_ID`
- `AION_SCHEDULER_DUE_FIXED_DELAY`, `AION_SCHEDULER_MISSED_FIXED_DELAY`

Frontend:

- `VITE_API_MODE`, `VITE_API_BASE_URL`
- `VITE_AUTH_MODE`, `VITE_KEYCLOAK_ENABLED`
- `VITE_KEYCLOAK_URL`, `VITE_KEYCLOAK_REALM`, `VITE_KEYCLOAK_CLIENT_ID`

## Endpoints Principais

- `GET /actuator/health`
- `GET /api/v1/me`
- `/api/v1/directions`
- `/api/v1/plans`
- `/api/v1/sessions`
- `/api/v1/logs`
- `/api/v1/dashboard/today`
- `/api/v1/dashboard/summary`
- `/api/v1/analytics/overview`
- `/api/v1/analytics/plans-by-day`
- `/api/v1/analytics/status-distribution`
- `/api/v1/analytics/time-by-direction`
- `/api/v1/analytics/planned-vs-executed`
- `POST /api/v1/bug-reports`

Com `AION_SECURITY_ENABLED=true`, endpoints sob `/api/v1/**` exigem Bearer JWT emitido pelo realm `aion-logbook`.

## Validacao Antes de Commit/Deploy

```bash
cd backend
mvn clean test
mvn clean package

cd ../frontend
npm run lint
npm run build
npm run build:backend
```

## Genesis Lab

Pontos para validar no ambiente `genesis-lab`:

- Definir dominios reais de app e auth, por exemplo `https://aion.genesis-lab.dev` e `https://auth.genesis-lab.dev`.
- Atualizar redirect URIs e web origins do client Keycloak se os dominios reais forem diferentes do realm versionado.
- Usar secrets reais apenas no ambiente, nunca no Git.
- Configurar `KEYCLOAK_ISSUER_URI` com o issuer publico acessivel pelo backend.
- Configurar `CORS_ALLOWED_ORIGINS` com a origem publica do frontend quando frontend e backend estiverem separados.
- Em deploy com frontend servido pelo backend, gerar `npm run build:backend` antes do pacote Java.
- Preferir imagem Keycloak com tema embutido usando `docker/keycloak/Dockerfile`; o volume de tema do Compose e voltado ao desenvolvimento local.

Checklist detalhado: [docs/deploy-genesis-lab.md](docs/deploy-genesis-lab.md).

## Troubleshooting

- `401 Unauthorized` em `/api/v1/**`: confira `VITE_AUTH_MODE=keycloak`, token no header `Authorization` e `KEYCLOAK_ISSUER_URI`.
- Erro de CORS: ajuste `CORS_ALLOWED_ORIGINS` no backend e `webOrigins` do client no Keycloak.
- Keycloak sem tema: reinicie o container e confirme o volume `docker/keycloak/themes:/opt/keycloak/themes`.
- Login social sem aparecer: confira se o provider esta habilitado e se os secrets foram aplicados via script kcadm.
- Frontend build para backend falha: rode `npm install` em `frontend/` e tente `npm run build` antes de `npm run build:backend`.
- Testcontainers falha no backend: confirme que Docker esta ativo.

## Seguranca de Arquivos

Arquivos `.env`, `.env.*`, `application-local.yml`, secrets, dumps, logs, volumes Docker e builds gerados estao ignorados. Os arquivos versionados de exemplo devem manter valores vazios ou placeholders.
