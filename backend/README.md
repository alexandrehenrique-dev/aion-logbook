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
├── security
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
- O dominio `Plan` ainda nao foi implementado.
