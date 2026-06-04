# Aion Logbook Backend

Backend Spring Boot do Aion Log Book. Ele centraliza autenticacao JWT via Keycloak, ownership dos recursos do usuario autenticado, persistencia PostgreSQL, migrations Flyway, dashboard, analytics, direcoes, planos, sessoes, logbook, onboarding, settings e bug reports.

## Stack

- Java 21
- Spring Boot 3.4.5
- Spring Web, Validation e Actuator
- Spring Security com OAuth2 Resource Server / JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Springdoc OpenAPI
- Maven
- JUnit 5, Mockito e Testcontainers

## Pre-requisitos

- JDK 21.
- Maven 3.9+ instalado localmente. Este checkout nao versiona Maven Wrapper.
- Docker e Docker Compose para PostgreSQL e Keycloak locais.
- Portas padrao livres: API `8080`, PostgreSQL `5433`, Keycloak `8181`.

## Dependencias Locais

Na raiz do repositorio:

```bash
cp .env.example .env
docker compose up -d postgres keycloak
```

Servicos esperados:

- PostgreSQL: `localhost:5433`, database `aion_logbook`.
- Keycloak: `http://localhost:8181`.
- Realm: `aion-logbook`.
- Client SPA: `aion-logbook-web`.

O realm local e importado de `docker/keycloak/import/aion-logbook-realm.json`.

## Configuracao

O arquivo `src/main/resources/application.yml` usa variaveis de ambiente e nao deve receber credenciais reais. Para desenvolvimento local, crie um arquivo nao versionado:

```bash
cp src/main/resources/application-local.example.yml src/main/resources/application-local.yml
```

Variaveis principais:

| Variavel | Descricao |
|---|---|
| `DB_URL` | URL JDBC do PostgreSQL |
| `DB_USERNAME` | Usuario do banco |
| `DB_PASSWORD` | Senha do banco |
| `KEYCLOAK_ISSUER_URI` | Issuer URI do provedor JWT |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas para CORS |
| `SERVER_PORT` | Porta HTTP da aplicacao; padrao `8080` |
| `SERVER_CONTEXT_PATH` | Context path opcional; no Compose atual usa `/logbook` |
| `FLYWAY_ENABLED` | Habilita migrations Flyway; padrao `true` |
| `SWAGGER_ENABLED` | Habilita Swagger/OpenAPI; padrao `false` |
| `AION_SECURITY_ENABLED` | Habilita seguranca da API; padrao `true` |
| `TELEGRAM_ENABLED` | Habilita notificacoes de BugReport no Telegram; padrao `false` |
| `TELEGRAM_BOT_TOKEN` | Token do bot Telegram usado para BugReport; nao versionar |
| `TELEGRAM_CHAT_ID` | Chat, grupo ou canal que recebe os BugReports |
| `AION_SCHEDULER_DUE_FIXED_DELAY` | Intervalo do job DUE em ms |
| `AION_SCHEDULER_MISSED_FIXED_DELAY` | Intervalo do job MISSED em ms |

Secrets, tokens, dumps, logs, `application-local.yml` e arquivos `.env*` locais devem ficar fora do Git.

## Rodar Localmente

A partir de `backend/`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Healthcheck:

```bash
curl -i http://localhost:8080/actuator/health
```

Swagger local, quando `SWAGGER_ENABLED=true` ou profile `local`:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

## Testes e Build

A partir de `backend/`:

```bash
mvn test
mvn clean package
```

Use `mvn clean verify` quando quiser rodar o ciclo completo Maven.

## Migrations

As migrations Flyway ficam em:

```txt
src/main/resources/db/migration
```

Com `spring.flyway.enabled=true`, o Flyway executa automaticamente na subida da aplicacao. Para aplicar migrations localmente, suba o PostgreSQL via Docker Compose e inicie a API com o profile `local`.

## Keycloak e Seguranca

Todos os endpoints privados sob `/api/v1/**` exigem Bearer JWT valido quando `AION_SECURITY_ENABLED=true`. O issuer e configurado por `KEYCLOAK_ISSUER_URI`; no ambiente local esperado, ele aponta para:

```txt
http://localhost:8181/realms/aion-logbook
```

O backend le claims como `sub`, `email`, `preferred_username` e `name` para resolver ou criar o `UserProfile`. O identificador interno usado nos dominios vem sempre do JWT/contexto autenticado via `CurrentUserService`; endpoints privados nao devem aceitar `userId` no payload.

## Estrutura

Base package:

```txt
br.com.byop.aionlogbook
```

Pacotes principais:

- `config`: CORS, OpenAPI e configuracoes de suporte.
- `security`: Resource Server JWT, usuario autenticado e resolucao do `userId`.
- `identity`: perfil interno associado ao subject do Keycloak.
- `direction`: direcoes do usuario.
- `plan`: planos, eventos, transicoes e scheduler.
- `session`: sessoes realizadas.
- `dashboard`: leituras agregadas do dia e resumo.
- `analytics`: leituras agregadas de planos, sessoes e direcoes.
- `logbook`: entradas pessoais de logbook.
- `onboarding`: status e criacao inicial de direcoes.
- `settings`: preferencias minimas do usuario.
- `bugreport`: reports opcionais com notificacao Telegram.
- `shared`: erros, paginacao, logging, tempo e wrappers comuns.

## Endpoints Uteis

| Metodo | Endpoint | Descricao |
|---|---|---|
| `GET` | `/actuator/health` | Healthcheck da aplicacao |
| `GET` | `/api/v1/me` | Retorna/cria o perfil interno do usuario autenticado |
| `GET/POST` | `/api/v1/directions` | Lista ou cria direcoes |
| `GET/PUT/DELETE` | `/api/v1/directions/{id}` | Busca, atualiza ou arquiva uma direcao |
| `GET/POST` | `/api/v1/plans` | Lista ou cria planos |
| `GET/PUT` | `/api/v1/plans/{id}` | Busca ou atualiza um plano |
| `POST` | `/api/v1/plans/{id}/start` | Inicia um plano permitido |
| `POST` | `/api/v1/plans/{id}/complete` | Conclui um plano em andamento |
| `POST` | `/api/v1/plans/{id}/partial` | Finaliza parcialmente um plano |
| `POST` | `/api/v1/plans/{id}/postpone` | Posterga um plano permitido |
| `POST` | `/api/v1/plans/{id}/ignore` | Ignora um plano permitido |
| `POST` | `/api/v1/plans/{id}/cancel` | Cancela um plano nao terminal |
| `POST` | `/api/v1/plans/{id}/modify` | Modifica campos operacionais de um plano nao terminal |
| `GET/POST` | `/api/v1/sessions` | Lista ou cria sessoes |
| `GET/PUT` | `/api/v1/sessions/{id}` | Busca ou atualiza uma sessao |
| `GET` | `/api/v1/dashboard/today` | Visao diaria agregada |
| `GET` | `/api/v1/dashboard/summary` | Resumo geral agregado |
| `GET` | `/api/v1/analytics/overview` | Totais agregados |
| `GET` | `/api/v1/analytics/plans-by-day` | Planos por dia |
| `GET` | `/api/v1/analytics/status-distribution` | Distribuicao de status |
| `GET` | `/api/v1/analytics/time-by-direction` | Tempo por direcao |
| `GET` | `/api/v1/analytics/planned-vs-executed` | Planejado versus executado |
| `GET/POST` | `/api/v1/logs` | Lista ou cria entradas de logbook |
| `GET/PUT/DELETE` | `/api/v1/logs/{id}` | Busca, atualiza ou remove uma entrada |
| `GET` | `/api/v1/onboarding/status` | Status do onboarding e sugestoes |
| `POST` | `/api/v1/onboarding/complete` | Marca onboarding como concluido |
| `POST` | `/api/v1/onboarding/directions` | Cria direcoes em lote |
| `GET/PUT` | `/api/v1/settings` | Le ou atualiza settings minimos |
| `POST` | `/api/v1/bug-reports` | Registra bug report |

## Frontend Servido Pelo Backend

O frontend pode gerar assets diretamente em `backend/src/main/resources/static`:

```bash
cd ../frontend
npm run build:backend
cd ../backend
mvn clean package
```

O backend encaminha rotas SPA conhecidas para `index.html`.

## Troubleshooting

- `401 Unauthorized`: confira token Bearer, `VITE_AUTH_MODE=keycloak` no frontend e `KEYCLOAK_ISSUER_URI` no backend.
- Erro de CORS: ajuste `CORS_ALLOWED_ORIGINS` e os `webOrigins` do client Keycloak.
- Flyway falha: confira se o PostgreSQL local esta ativo e se a sequencia de migrations nao foi alterada em banco ja inicializado.
- Testcontainers falha: confirme que Docker esta ativo.
- Swagger nao abre: habilite `SWAGGER_ENABLED=true` ou use o profile `local`.

## LGPD

O Logbook armazena dados pessoais e conteudo livre informado pelo usuario. Logs tecnicos nao devem conter o conteudo integral de `LogEntry.content`; quando necessario para troubleshooting, registre apenas identificadores, metadados nao sensiveis e mensagens sanitizadas.
