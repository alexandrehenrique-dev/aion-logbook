# Aion Logbook Backend

Backend do Aion Logbook, responsavel pela API HTTP privada da aplicacao. O servico centraliza autenticacao JWT, ownership dos recursos por usuario, persistencia em PostgreSQL, migrations Flyway, automacoes de status de planos, registro de sessoes e agregacoes do dashboard.

## Stack Principal

- Java 21
- Spring Boot 3.4.5
- Spring Security
- OAuth2 Resource Server / JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Springdoc OpenAPI
- Maven
- Testcontainers

## Requisitos Locais

- JDK 21
- Maven 3.9+ ou Maven Wrapper, quando disponivel no checkout
- Docker e Docker Compose para PostgreSQL e Keycloak locais
- Porta `8080` livre para a API
- Portas `5433` e `8181` livres para os servicos locais padrao

## Dependencias Locais

O Docker Compose fica na raiz versionada do projeto, um nivel acima deste diretorio:

```bash
cd ..
docker compose up -d
```

Servicos locais:

- PostgreSQL: `localhost:5433`, database `aion_logbook`
- Keycloak: `http://localhost:8181`

O realm local do Keycloak e importado de `docker/keycloak/import/aion-logbook-realm.json`.

## Configuracao

O `src/main/resources/application.yml` usa variaveis de ambiente e nao deve receber credenciais reais. Para desenvolvimento local, crie um arquivo nao versionado a partir do exemplo:

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
| `FLYWAY_ENABLED` | Habilita migrations Flyway; padrao `true` |
| `SWAGGER_ENABLED` | Habilita Swagger/OpenAPI; padrao `false` |
| `AION_SECURITY_ENABLED` | Habilita seguranca da API; padrao `true` |
| `AION_SCHEDULER_DUE_FIXED_DELAY` | Intervalo do job DUE em ms |
| `AION_SCHEDULER_MISSED_FIXED_DELAY` | Intervalo do job MISSED em ms |

## Rodar a Aplicacao

A partir de `backend/`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Se o Maven Wrapper estiver disponivel:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Healthcheck:

```bash
curl -i http://localhost:8080/actuator/health
```

## Testes

A partir de `backend/`:

```bash
mvn test
```

Build completo recomendado antes de publicar alteracoes:

```bash
mvn clean verify
```

Quando o Maven Wrapper estiver disponivel, use `./mvnw test` e `./mvnw clean verify`.

## Migrations

As migrations Flyway ficam em:

```txt
src/main/resources/db/migration
```

Com `spring.flyway.enabled=true`, o Flyway executa automaticamente na subida da aplicacao. Para aplicar migrations localmente, suba o PostgreSQL via Docker Compose e inicie a API com o profile `local`.

## Swagger / OpenAPI

O Swagger fica desabilitado por padrao fora de ambientes controlados. No profile local, com `SWAGGER_ENABLED=true`, acesse:

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Estrutura

Base package:

```txt
br.com.byop.aionlogbook
```

Camadas e pacotes principais:

- `config`: CORS, OpenAPI e configuracoes de suporte.
- `security`: Resource Server JWT, usuario autenticado e resolucao do `userId`.
- `identity`: `UserProfile` interno associado ao subject do Keycloak.
- `direction`: controller, service, DTOs, mapper, repository e entidade `Direction`.
- `plan`: controllers, use cases, DTOs, mapper, repositories, entidades e politicas de transicao.
- `session`: controller, service, DTOs, mapper, repository e entidade `SessionLog`.
- `dashboard`: controller, service e DTOs/projections de agregacao.
- `scheduler`: jobs de atualizacao automatica de status dos planos.
- `shared`: erros, paginacao, logging, tempo e wrappers comuns.

A API versionada fica sob `/api/v1/**`.

## Dominios

### Identity / UserProfile

Mantem o perfil interno do usuario autenticado. O `userId` usado nos demais dominios e sempre derivado do JWT/contexto autenticado; ele nao deve ser aceito no payload das APIs privadas.

### Directions

`Direction` representa uma direcao do usuario. A entidade pertence a `UserProfile` por `user_profile_id`, nao por uma coluna direta `userId`. Queries por usuario devem usar `userProfile.id` ou derived queries com `UserProfileId`.

Status atuais:

- `ACTIVE`
- `ARCHIVED`

### Plans

`Plan` representa uma unidade planejada de trabalho, opcionalmente vinculada a uma direcao ativa do mesmo usuario. Criacao, listagem, busca, atualizacao, eventos e transicoes filtram por `userId`.

Status atuais: `DRAFT`, `SCHEDULED`, `PENDING`, `DUE`, `IN_PROGRESS`, `COMPLETED`, `PARTIAL`, `POSTPONED`, `IGNORED`, `CANCELED`, `MISSED`.

### Sessions

`SessionLog` registra sessoes realizadas pelo usuario autenticado, manualmente ou a partir de transicoes de planos. `planId` e `directionId`, quando informados, precisam pertencer ao usuario. Nao ha endpoint `DELETE` para sessoes no MVP.

### Dashboard

O dashboard e uma camada de leitura agregada. Ele nao e entidade persistida, nao possui tabela propria e apenas consolida dados ja existentes de planos, direcoes e sessoes.

## Endpoints Principais

Todos os endpoints privados exigem JWT Bearer valido quando `AION_SECURITY_ENABLED=true`.

| Metodo | Endpoint | Descricao |
|---|---|---|
| `GET` | `/api/v1/me` | Retorna/cria o perfil interno do usuario autenticado |
| `GET` | `/api/v1/directions` | Lista direcoes do usuario autenticado |
| `POST` | `/api/v1/directions` | Cria uma direcao |
| `GET` | `/api/v1/directions/{id}` | Busca uma direcao do usuario autenticado |
| `PUT` | `/api/v1/directions/{id}` | Atualiza uma direcao do usuario autenticado |
| `DELETE` | `/api/v1/directions/{id}` | Arquiva logicamente uma direcao |
| `GET` | `/api/v1/plans` | Lista planos do usuario autenticado com filtros opcionais |
| `POST` | `/api/v1/plans` | Cria um plano |
| `GET` | `/api/v1/plans/{id}` | Busca um plano do usuario autenticado |
| `PUT` | `/api/v1/plans/{id}` | Atualiza um plano |
| `GET` | `/api/v1/plans/{id}/events` | Lista eventos do plano |
| `POST` | `/api/v1/plans/{id}/start` | Inicia um plano permitido |
| `POST` | `/api/v1/plans/{id}/complete` | Conclui um plano em andamento |
| `POST` | `/api/v1/plans/{id}/partial` | Finaliza parcialmente um plano |
| `POST` | `/api/v1/plans/{id}/postpone` | Posterga um plano permitido |
| `POST` | `/api/v1/plans/{id}/ignore` | Ignora um plano permitido |
| `POST` | `/api/v1/plans/{id}/cancel` | Cancela um plano nao terminal |
| `POST` | `/api/v1/plans/{id}/modify` | Modifica campos operacionais de um plano nao terminal |
| `GET` | `/api/v1/sessions` | Lista sessoes do usuario autenticado |
| `POST` | `/api/v1/sessions` | Cria uma sessao manual |
| `GET` | `/api/v1/sessions/{id}` | Busca uma sessao do usuario autenticado |
| `PUT` | `/api/v1/sessions/{id}` | Atualiza uma sessao do usuario autenticado |
| `GET` | `/api/v1/dashboard/today` | Retorna a visao diaria agregada do usuario autenticado |
| `GET` | `/api/v1/dashboard/summary` | Retorna o resumo geral agregado do usuario autenticado |

## ETAPA 10 - Dashboard Minimo

Endpoints implementados:

- `GET /api/v1/dashboard/today`
- `GET /api/v1/dashboard/summary`

Regras de seguranca:

- Ambos os endpoints sao protegidos por JWT.
- O `userId` vem sempre do usuario autenticado via `CurrentUserService`.
- Nenhum endpoint recebe `userId` por parametro.
- Todas as queries filtram por usuario.
- Joins de sessoes com planos e direcoes tambem restringem o ownership do usuario autenticado.
- `Direction` e filtrada por `direction.userProfile.id`, pois nao possui coluna direta `userId`.

Agregacoes de `/today`:

- Data atual no timezone padrao.
- Saudacao baseada no horario local.
- Planos em andamento.
- Planos do dia em `DUE`, `MISSED`, `COMPLETED`, `PARTIAL` e `PENDING`.
- Tempo total de sessoes do dia em minutos.
- Taxa de conclusao do dia.
- Quantidade de direcoes ativas.
- Ultimas sessoes, enriquecidas com `planTitle` e `directionName`.
- `lastLogEntries` retorna lista vazia enquanto o dominio Logbook ainda nao existir.

Agregacoes de `/summary`:

- Tempo total registrado em sessoes.
- Taxa geral de conclusao de planos.
- Total de planos criados.
- Total de planos concluidos ou parcialmente concluidos.
- Quantidade de direcoes ativas.
- Tempo registrado na semana atual.
- `streak` retorna `0` enquanto a regra dedicada ainda nao existir.
- Ultima atividade registrada em sessoes.

Timezone:

- O timezone padrao usado pelo dashboard e `America/Sao_Paulo`.
- Essa escolha e fixa enquanto `UserProfile` ainda nao possuir timezone persistido.

Persistencia:

- Dashboard nao e entidade persistida.
- Nao existe tabela, migration ou repository proprio de dashboard.
- A camada apenas agrega dados existentes de `Plan`, `Direction` e `SessionLog`.

## Seguranca

- Endpoints `/api/v1/**` exigem JWT Bearer valido.
- O backend atua como OAuth2 Resource Server.
- Ownership e sempre derivado do usuario autenticado.
- Recursos de outro usuario devem ser tratados como inexistentes e retornar `404` quando aplicavel.
- Nao versionar `application-local.yml`, `.env`, tokens, senhas, dumps locais ou volumes Docker.
