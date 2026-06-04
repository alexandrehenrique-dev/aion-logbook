# Aion Logbook Backend

Backend do Aion Logbook, responsavel pela API HTTP privada da aplicacao.

## Stack Principal

- Java 21
- Spring Boot 3.4.5
- Spring Security
- OAuth2 Resource Server / JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven

## Estrutura

Base package:

```txt
br.com.byop.aionlogbook
```

Modulos principais atuais:

```txt
br.com.byop.aionlogbook
├── config
├── direction
│   ├── api
│   ├── application
│   ├── domain
│   ├── dto
│   ├── infrastructure
│   └── mapper
├── identity
├── plan
├── security
├── session
└── shared
```

A API versionada fica sob `/api/v1/**`.

## Configuracao Local

O perfil conhecido para desenvolvimento local e `local`.

Crie o arquivo de configuracao local a partir do exemplo:

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
| `FLYWAY_ENABLED` | Habilita/desabilita migrations Flyway |
| `SWAGGER_ENABLED` | Habilita/desabilita Swagger/OpenAPI |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas para CORS |

## Rodar Localmente

A partir de `backend/`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Healthcheck:

```bash
curl -i http://localhost:8080/actuator/health
```

## Testes

Execute a suite automatizada a partir de `backend/`:

```bash
mvn test
```

Se o Maven Wrapper estiver disponivel no checkout:

```bash
./mvnw test
```

## Migrations

As migrations ficam em:

```txt
src/main/resources/db/migration
```

O Flyway executa automaticamente na subida da aplicacao quando `spring.flyway.enabled=true`.

Para aplicar migrations localmente, suba o banco configurado e inicie a aplicacao com o perfil `local`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Endpoints Principais

Todos os endpoints abaixo ficam sob a API privada versionada:

| Metodo | Endpoint | Descricao |
|---|---|---|
| `GET` | `/api/v1/me` | Retorna/cria o perfil interno do usuario autenticado |
| `GET` | `/api/v1/directions` | Lista direcoes do usuario autenticado; por padrao retorna `ACTIVE` |
| `POST` | `/api/v1/directions` | Cria uma direcao com status `ACTIVE` |
| `GET` | `/api/v1/directions/{id}` | Busca uma direcao do usuario autenticado |
| `PUT` | `/api/v1/directions/{id}` | Atualiza uma direcao do usuario autenticado |
| `DELETE` | `/api/v1/directions/{id}` | Arquiva logicamente uma direcao do usuario autenticado |
| `GET` | `/api/v1/plans` | Lista planos do usuario autenticado com filtros opcionais |
| `POST` | `/api/v1/plans` | Cria um plano e registra evento de criacao |
| `GET` | `/api/v1/plans/{id}` | Busca um plano do usuario autenticado |
| `PUT` | `/api/v1/plans/{id}` | Atualiza um plano e registra evento de atualizacao |
| `GET` | `/api/v1/plans/{id}/events` | Lista eventos do plano do usuario autenticado |
| `POST` | `/api/v1/plans/{id}/start` | Inicia um plano permitido |
| `POST` | `/api/v1/plans/{id}/complete` | Conclui um plano em andamento |
| `POST` | `/api/v1/plans/{id}/partial` | Finaliza parcialmente um plano |
| `POST` | `/api/v1/plans/{id}/postpone` | Posterga um plano permitido |
| `POST` | `/api/v1/plans/{id}/ignore` | Ignora um plano permitido |
| `POST` | `/api/v1/plans/{id}/cancel` | Cancela um plano nao terminal |
| `POST` | `/api/v1/plans/{id}/modify` | Modifica campos operacionais de um plano nao terminal |
| `GET` | `/api/v1/sessions` | Lista sessoes do usuario autenticado com filtros opcionais |
| `POST` | `/api/v1/sessions` | Cria uma sessao manual |
| `GET` | `/api/v1/sessions/{id}` | Busca uma sessao do usuario autenticado |
| `PUT` | `/api/v1/sessions/{id}` | Atualiza uma sessao do usuario autenticado |

## Seguranca

- Endpoints `/api/v1/**` exigem JWT Bearer valido.
- O backend atua como OAuth2 Resource Server e valida tokens JWT.
- O usuario autenticado e resolvido a partir do JWT pelo fluxo de `AuthenticatedUserProvider` e `UserProfileService`.
- `userId` nao deve ser enviado no request.
- Ownership e sempre derivado do usuario autenticado.
- Recursos de outro usuario devem ser tratados como inexistentes e retornar `404`.

## Dominio Direction

`Direction` representa uma direcao de vida/trabalho cadastrada pelo usuario autenticado.

Status atuais:

- `ACTIVE`: direcao ativa, usada na listagem padrao.
- `ARCHIVED`: direcao arquivada por delete logico.

Regras principais:

- A criacao sempre usa `status = ACTIVE`.
- Buscas e alteracoes usam sempre `id + userProfileId`.
- `DELETE` nao remove fisicamente o registro.
- O arquivamento define `status = ARCHIVED`, `archivedAt = now` e atualiza `updatedAt`.

## Dominio Plan

`Plan` representa um plano operacional do usuario autenticado, opcionalmente vinculado a uma direcao ativa.

Status atuais:

- `DRAFT`
- `SCHEDULED`
- `PENDING`
- `DUE`
- `IN_PROGRESS`
- `COMPLETED`
- `PARTIAL`
- `POSTPONED`
- `IGNORED`
- `CANCELED`
- `MISSED`

Regras principais:

- A criacao sempre deriva o ownership do usuario autenticado.
- `directionId`, quando informado, precisa pertencer ao usuario e estar ativo.
- A listagem permite filtros opcionais por `status`, `directionId` e `plannedDate`.
- Criacoes e atualizacoes registram eventos em `plan_events`.

## Dominio SessionLog

`SessionLog` registra sessoes realizadas pelo usuario autenticado, criadas manualmente ou de forma automatica a partir de transicoes de planos.

Endpoints disponiveis:

| Metodo | Endpoint | Descricao |
|---|---|---|
| `GET` | `/api/v1/sessions` | Lista sessoes do usuario autenticado |
| `POST` | `/api/v1/sessions` | Cria uma sessao manual |
| `GET` | `/api/v1/sessions/{id}` | Busca uma sessao por ID |
| `PUT` | `/api/v1/sessions/{id}` | Atualiza uma sessao |

Filtros suportados na listagem:

- `directionId`
- `planId`
- `dateFrom`
- `dateTo`
- `page`
- `size`
- `sort`

Regras principais:

- `userId` vem sempre do JWT/contexto autenticado.
- `planId` e `directionId`, quando informados, devem pertencer ao usuario.
- Recurso inexistente ou pertencente a outro usuario retorna `404`.
- Nao ha endpoint `DELETE` para sessoes no MVP.
- A transicao `complete` de um plano `IN_PROGRESS` cria uma sessao automatica.
- A transicao `partial` cria uma sessao automatica quando houver duracao.
- `durationMinutes` usa `actualMinutes` quando informado; caso contrario, calcula a diferenca entre `startedAt` e `finishedAt`.

Exemplo de payload para criacao manual:

```json
{
  "planId": "00000000-0000-0000-0000-000000000000",
  "directionId": "00000000-0000-0000-0000-000000000000",
  "startedAt": "2026-06-03T20:00:00-03:00",
  "finishedAt": "2026-06-03T20:45:00-03:00",
  "actualMinutes": 45,
  "result": "Sessão concluída",
  "notes": "Notas livres da sessão"
}
```

## ETAPA 7 - Transicoes de Plan

A Etapa 7 implementa as transicoes operacionais de `Plan` no backend. Cada transicao busca o recurso por `id + userId`, persiste o novo estado no plano e registra um evento em `plan_events` via `PlanEventRepository`.

Endpoints de transicao:

| Metodo | Endpoint | Evento |
|---|---|---|
| `POST` | `/api/v1/plans/{id}/start` | `STARTED` |
| `POST` | `/api/v1/plans/{id}/complete` | `COMPLETED` |
| `POST` | `/api/v1/plans/{id}/partial` | `PARTIAL_COMPLETED` |
| `POST` | `/api/v1/plans/{id}/postpone` | `POSTPONED` |
| `POST` | `/api/v1/plans/{id}/ignore` | `IGNORED` |
| `POST` | `/api/v1/plans/{id}/cancel` | `CANCELED` |
| `POST` | `/api/v1/plans/{id}/modify` | `MODIFIED` |

Transicoes permitidas:

| Acao | Origem permitida | Destino |
|---|---|---|
| `start` | `SCHEDULED`, `PENDING`, `DUE`, `MISSED`, `POSTPONED` | `IN_PROGRESS` |
| `complete` | `IN_PROGRESS` | `COMPLETED` |
| `partial` | `IN_PROGRESS`, `DUE`, `PENDING`, `MISSED` | `PARTIAL` |
| `postpone` | `SCHEDULED`, `PENDING`, `DUE`, `MISSED`, `POSTPONED` | `POSTPONED` |
| `ignore` | `SCHEDULED`, `PENDING`, `DUE`, `MISSED` | `IGNORED` |
| `cancel` | Qualquer estado nao terminal | `CANCELED` |
| `modify` | Qualquer estado nao terminal | Mantem o estado atual |

Estados terminais nao transicionam: `COMPLETED`, `PARTIAL`, `IGNORED` e `CANCELED`.

Regras e erros esperados:

- Apenas um plano pode ficar `IN_PROGRESS` por usuario; iniciar outro plano em andamento retorna `409 CONFLICT`.
- Plano inexistente ou pertencente a outro usuario retorna `404 RESOURCE_NOT_FOUND`.
- Transicao invalida retorna `400 INVALID_TRANSITION`.
- `start` atualiza `startedAt`; `complete` e `partial` atualizam `finishedAt`; `actualMinutes` e atualizado quando aplicavel.
- `complete` cria `SessionLog` automatico; `partial` cria `SessionLog` automatico quando houver duracao.
- `lastStatusChangedAt` e atualizado em toda transicao desta etapa.

Scheduler e dashboard ainda nao fazem parte desta etapa.
