# Aion Log Book

Aion Log Book e uma aplicacao full stack para direcao pessoal, planejamento, execucao, sessoes, analytics e registros de logbook. O frontend React pode rodar com dados mockados via MSW ou integrado ao backend Spring Boot protegido por Keycloak. Para validacao e deploy, o build estatico do frontend tambem pode ser servido pelo proprio backend.

## Estrutura

```txt
backend/                 API Spring Boot, Flyway, testes e Dockerfile da aplicacao
frontend/                SPA React/Vite, MSW e build estatico
docker-compose.yml       PostgreSQL, Keycloak e app backend locais
docker/keycloak/import/  Realm local do Aion Log Book
docker/keycloak/themes/  Tema customizado do login Keycloak
docker/keycloak/scripts/ Scripts kcadm para login social
docs/                    Documentacao auxiliar e checklist de deploy
infra/                   Scripts de deploy/backup em formato .example
```

Documentacao especifica:

- [Backend](backend/README.md)
- [Frontend](frontend/README.md)
- [Deploy Genesis Lab](docs/deploy-genesis-lab.md)

## Stack

- Backend: Java 21, Spring Boot 3.4, Spring Security, OAuth2 Resource Server, JPA, PostgreSQL, Flyway, Springdoc, Maven, Testcontainers.
- Frontend: React 18, TypeScript, Vite 6, Tailwind CSS 4, React Router 7, MSW, Keycloak JS, Recharts, Sonner.
- Infra local: Docker Compose, PostgreSQL 16, Keycloak 26 com realm importado e tema customizado.

## Pre-requisitos

- JDK 21.
- Maven 3.9+ instalado localmente. Este checkout nao versiona Maven Wrapper.
- Node.js 20+ e npm. O frontend possui `package-lock.json`.
- Docker e Docker Compose para PostgreSQL, Keycloak e execucao completa em containers.

## Ambiente Local

Copie o exemplo de variaveis da raiz e preencha somente o necessario:

```bash
cp .env.example .env
docker compose up -d postgres keycloak
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

## Backend Local

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

## Frontend Local

```bash
cd frontend
npm install
npm run dev
```

O frontend abre em `http://localhost:5173`.

Copie o exemplo quando precisar sobrescrever variaveis locais:

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

## Docker

Suba tudo com Compose:

```bash
docker compose up -d --build
```

No Compose atual, o backend fica exposto em `http://localhost:8077/logbook` e usa `SERVER_CONTEXT_PATH=/logbook`. Para desenvolvimento interativo, normalmente e mais simples subir `postgres` e `keycloak` com Docker e rodar backend/frontend pelos comandos locais.

## Keycloak

O Compose importa `docker/keycloak/import/aion-logbook-realm.json`, habilita o tema `aion-logbook` e monta scripts em `/opt/keycloak/scripts`.

Para login social, nao coloque secrets no realm JSON. Configure `.env` com os IDs/secrets reais e rode, depois que o Keycloak estiver no ar:

```bash
docker exec aion-logbook-keycloak bash /opt/keycloak/scripts/configure-identity-providers.sh
```

Redirect URIs locais esperadas:

- Frontend Vite: `http://localhost:5173/*`
- Frontend servido pelo backend: `http://localhost:8080/*`

## Build e Deploy

Backend:

```bash
cd backend
mvn clean package
```

Frontend:

```bash
cd frontend
npm run build
```

Build do frontend para ser servido pelo backend:

```bash
cd frontend
npm run build:backend
cd ../backend
mvn clean package
```

Rotas SPA como `/dashboard`, `/plans/**`, `/directions/**`, `/analytics/**`, `/settings/**`, `/logbook/**`, `/onboarding` e `/login` sao encaminhadas para `index.html` pelo backend.

## Validacao Antes de Publicar

```bash
cd backend
mvn test
mvn clean package

cd ../frontend
npm run lint
npm run build
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

## Arquivos Locais e Secrets

Arquivos `.env`, `.env.*`, `application-local.yml`, secrets, dumps, logs, volumes Docker, caches, `node_modules`, `dist`, `target` e builds gerados nao devem ser versionados. Os arquivos versionados de exemplo devem manter valores vazios ou placeholders.
