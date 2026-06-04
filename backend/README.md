# Aion Logbook Backend

Backend do Aion Logbook, responsavel pela API HTTP privada da aplicacao.
Ele concentra autenticacao via JWT, ownership dos recursos por usuario,
persistencia em PostgreSQL, migrations versionadas e automacoes de status dos
planos.

## Stack Principal

- Java 21
- Spring Boot 3.4.5
- Spring Security
- OAuth2 Resource Server / JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven

## Profiles Disponiveis

- `default`: profile base para ambientes reais. Usa variaveis de ambiente para
  datasource, issuer JWT, CORS e demais configuracoes operacionais.
- `local`: profile de desenvolvimento local. Deve ser criado a partir de
  `src/main/resources/application-local.example.yml`, que usa valores locais nao
  sensiveis para banco, Keycloak, Swagger e scheduler.
- `test`: usado pela suite automatizada via `src/test/resources/application-test.yml`.

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

## Configuracao

O `application.yml` versionado e o profile default do backend. Ele nao contem
credenciais reais nem URLs privadas; ambientes reais devem fornecer os valores
por variaveis de ambiente.

Variaveis principais:

| Variavel | Descricao |
|---|---|
| `DB_URL` | URL JDBC do PostgreSQL |
| `DB_USERNAME` | Usuario do banco |
| `DB_PASSWORD` | Senha do banco |
| `KEYCLOAK_ISSUER_URI` | Issuer URI do Keycloak/provedor JWT |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas para CORS |
| `SERVER_PORT` | Porta HTTP da aplicacao; padrao `8080` |
| `SERVER_CONTEXT_PATH` | Context path da aplicacao; padrao `/` |
| `FLYWAY_ENABLED` | Habilita/desabilita migrations Flyway; padrao `true` |
| `SWAGGER_ENABLED` | Habilita/desabilita Swagger/OpenAPI; padrao `false` |
| `AION_SECURITY_ENABLED` | Habilita/desabilita seguranca da API; padrao `true` |
| `AION_SCHEDULER_DUE_FIXED_DELAY` | Intervalo em ms do job DUE; padrao `60000` |
| `AION_SCHEDULER_MISSED_FIXED_DELAY` | Intervalo em ms do job MISSED; padrao `300000` |
| `ACTUATOR_HEALTH_SHOW_DETAILS` | Exibicao de detalhes no healthcheck; padrao `never` |

Para desenvolvimento local, crie um arquivo nao versionado a partir do exemplo:

```bash
cp src/main/resources/application-local.example.yml src/main/resources/application-local.yml
```

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

Se o Maven Wrapper for adicionado ao checkout em uma etapa futura, o comando
equivalente sera `./mvnw test`.

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

Em ambientes reais, configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e mantenha
`FLYWAY_ENABLED=true` para aplicar as migrations versionadas na subida.

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

## Scheduler DUE/MISSED

O backend possui jobs agendados responsaveis por atualizar automaticamente o
estado dos planos:

- DUE: planos `SCHEDULED` ou `PENDING` viram `DUE` quando `plannedStartAt <= now`.
- MISSED: planos `DUE` viram `MISSED` quando `plannedEndAt < now`.
- Planos `IN_PROGRESS` nunca viram `MISSED` automaticamente.
- Eventos `DUE` e `MISSED` sao criados de forma idempotente.
- Os jobs usam `Clock` injetavel e executam dentro de transacao.

Os intervalos sao configurados por `AION_SCHEDULER_DUE_FIXED_DELAY` e
`AION_SCHEDULER_MISSED_FIXED_DELAY`. A migracao
`V009__scheduler_plan_events_unique.sql` adiciona uma restricao unica parcial
para evitar eventos duplicados de scheduler por plano. O projeto ainda nao usa
ShedLock; a idempotencia fica concentrada no banco e na camada de aplicacao.

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

## Observacoes de Seguranca

- Nao versionar `application-local.yml`, `.env`, tokens, senhas ou URLs privadas.
- O profile default deve receber configuracoes por variaveis de ambiente.
- Endpoints privados dependem de JWT Bearer valido quando `AION_SECURITY_ENABLED=true`.
- `CORS_ALLOWED_ORIGINS` deve listar apenas origens confiaveis em ambientes reais.
- Swagger deve permanecer desabilitado por padrao fora de ambientes controlados.
