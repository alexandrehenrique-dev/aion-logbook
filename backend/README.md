# Aion Logbook Backend

Modulo backend do Aion Logbook, implementado em Java 21 com Spring Boot.

## Stack principal

- Java 21
- Spring Boot
- Spring Security
- OAuth2 Resource Server com JWT
- Keycloak
- Spring Data JPA
- Flyway
- PostgreSQL
- Maven
- Springdoc OpenAPI
- Testcontainers

## Arquitetura

Base package oficial:

```txt
br.com.byop.aionlogbook
```

Estrutura modular adotada:

```txt
br.com.byop.aionlogbook
├── config
├── identity
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── web
├── security
└── shared
```

A API HTTP privada e versionada fica sob `/api/v1/**`.

## Dependencias externas locais

- PostgreSQL via Docker Compose
- Keycloak local em `http://localhost:8181`
- Realm Keycloak `aion-logbook`
- Issuer URI `http://localhost:8181/realms/aion-logbook`
- Frontend local permitido por CORS em `http://localhost:5173`

Suba a infraestrutura local a partir da raiz do repositorio:

```bash
docker compose up -d postgres keycloak
```

## Configuracao local

O arquivo `application-local.yml` e local e nao deve ser versionado. Para criar uma copia local:

```bash
cp src/main/resources/application-local.example.yml src/main/resources/application-local.yml
```

Variaveis principais:

| Variavel | Default | Descricao |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5433/aion_logbook` | URL JDBC do PostgreSQL |
| `DB_USERNAME` | `aion_user` | Usuario do banco |
| `DB_PASSWORD` | `aion_password` | Senha local do banco |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8181/realms/aion-logbook` | Issuer URI do realm Keycloak |
| `FLYWAY_ENABLED` | `true` | Liga/desliga Flyway |
| `SWAGGER_ENABLED` | `false` | Liga/desliga Swagger/OpenAPI |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Origens permitidas |

## Rodar a aplicacao

Dentro de `backend/`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Healthcheck:

```bash
curl -i http://localhost:8080/actuator/health
```

Swagger em local/dev:

```bash
open http://localhost:8080/swagger-ui.html
```

## Seguranca

O backend nao implementa login, nao aceita senha, nao cria JWT e nao confia em `userId` recebido do frontend.

O login real acontece no frontend via Keycloak/PKCE. O backend atua como OAuth2 Resource Server, valida o JWT emitido pelo Keycloak e identifica o usuario autenticado por `jwt.getSubject()`.

Rotas publicas como `/actuator/health`, `/index.html`, assets da SPA e Swagger em `local`/`dev` nao exigem JWT. Rotas `/api/v1/**` exigem Bearer Token e devem retornar `401` quando chamadas sem token.

## Shared Foundation

A camada `shared` concentra infraestrutura transversal reutilizavel pelo backend.

### Recursos implementados

- erro padronizado via `ApiErrorResponse`;
- enum `ErrorCode`;
- exceptions compartilhadas;
- `GlobalExceptionHandler`;
- paginacao via `PageResponse<T>`;
- filtro `RequestIdFilter`;
- header `X-Request-Id`;
- `requestId` no MDC para rastreabilidade;
- configuracao de tempo via `Clock.systemUTC()`.

### Contrato de erro

Exemplo:

```json
{
  "timestamp": "2026-06-02T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "RESOURCE_NOT_FOUND",
  "message": "Recurso nao encontrado.",
  "path": "/api/example",
  "requestId": "req-123",
  "details": []
}
```

Erros de validacao retornam `details` por campo. Valores rejeitados de campos sensiveis, como senha, token, segredo, credencial, autorizacao, JWT ou e-mail, sao mascarados.

Mapeamentos principais:

- `MethodArgumentNotValidException`: `400 VALIDATION_ERROR`;
- `ResourceNotFoundException`: `404 RESOURCE_NOT_FOUND`;
- `InvalidTransitionException`: `400 INVALID_TRANSITION`;
- `ConflictException`: `409 CONFLICT`;
- `BusinessRuleException`: `422 BUSINESS_RULE_VIOLATION`;
- erro inesperado: `500 INTERNAL_ERROR`, sem vazar mensagem sensivel.

### Request ID

Toda requisicao deve retornar o header:

```http
X-Request-Id: <uuid-ou-id-informado>
```

Quando o cliente envia `X-Request-Id`, o backend reaproveita o valor. Quando o cliente nao envia, ou envia valor em branco, o backend gera um UUID.

O valor tambem e registrado no MDC como `requestId`. O filtro adiciona `method` e `path` ao MDC durante a requisicao e limpa esses valores ao final.

## Endpoint /api/v1/me

`GET /api/v1/me` retorna o perfil interno do usuario autenticado. O perfil fica em `user_profiles` e e criado no primeiro acesso a partir das claims do JWT.

Teste sem token:

```bash
curl -i http://localhost:8080/api/v1/me
```

Resultado esperado: `401 Unauthorized`.

## Obter token local

Para testes manuais em ambiente local, obtenha um token no Keycloak. O fluxo real da SPA deve usar Authorization Code + PKCE; o exemplo abaixo usa password grant apenas quando o client local estiver configurado para permitir esse tipo de teste.

```bash
TOKEN=$(curl -s \
  -X POST "http://localhost:8181/realms/aion-logbook/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=aion-logbook-spa" \
  -d "username=<usuario>" \
  -d "password=<senha>" \
  | jq -r '.access_token')
```

Chamada autenticada:

```bash
curl -i \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/me
```

## Testes

Os testes usam Testcontainers, entao o Docker precisa estar ativo.

```bash
mvn test
```

Se o Maven Wrapper for adicionado ao repositorio, o comando equivalente e:

```bash
./mvnw test
```

## Padrao do projeto

Antes de criar novos dominios, endpoints, migrations ou testes, consulte `../docs/padrao_projeto.md`.
