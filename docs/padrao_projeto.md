# Aion Logbook — Padrão de Projeto Backend

Este documento define o padrão técnico adotado no backend do **Aion Logbook**.

O objetivo é manter o projeto consistente desde o início: arquitetura modular, segurança centralizada, domínio protegido por usuário autenticado via Keycloak, rotas versionadas, testes previsíveis e baixo acoplamento entre camadas.

Este documento deve ser usado como referência antes de criar qualquer novo endpoint, domínio, entidade, serviço, migration, controller ou teste.

---

## 1. Base package oficial

Todo código Java do backend deve estar abaixo de:

```txt
br.com.byop.aionlogbook
```

Estrutura atual esperada:

```txt
src/main/java/br/com/byop/aionlogbook/
├── Application.java
├── config/
├── identity/
├── security/
└── shared/
```

Regra:

- Não criar pacotes fora de `br.com.byop.aionlogbook`.
- Não usar packages antigos, genéricos ou temporários.
- Não misturar domínio de negócio dentro de `shared`, `config` ou `security`.

---

## 2. Arquitetura adotada

O backend segue uma arquitetura **modular por domínio**, com separação interna por responsabilidades.

A estrutura padrão de um domínio é:

```txt
<domain>/
├── application/
├── domain/
├── infrastructure/
└── web/
```

Exemplo real:

```txt
identity/
├── application/
│   └── UserProfileService.java
├── domain/
│   └── UserProfile.java
├── infrastructure/
│   └── UserProfileRepository.java
└── web/
    ├── MeController.java
    └── MeResponse.java
```

### Responsabilidade de cada camada

#### `domain`

Contém as regras centrais do domínio.

Pode conter:

- entidades JPA quando a entidade é também o modelo persistido;
- enums de domínio;
- objetos de valor;
- métodos comportamentais da entidade;
- invariantes do domínio.

Não deve conter:

- controller;
- DTO HTTP;
- chamadas para outros sistemas;
- lógica de autenticação;
- lógica de infraestrutura.

#### `application`

Contém os casos de uso da aplicação.

Pode conter:

- services;
- orquestração de regras;
- transações;
- chamadas a repositories;
- validações de permissão do usuário autenticado;
- regras de create-on-first-access;
- regras de autorização contextual.

Não deve conter:

- detalhes HTTP;
- anotações de controller;
- SQL manual espalhado;
- dependência direta de servlet request, salvo exceção justificada.

#### `infrastructure`

Contém integração com mecanismos externos ou persistência.

Pode conter:

- Spring Data repositories;
- adapters;
- clients externos;
- implementações técnicas;
- integrações futuras.

Não deve conter regra de negócio sensível.

#### `web`

Contém a borda HTTP do domínio.

Pode conter:

- controllers;
- request DTOs;
- response DTOs;
- mappers simples de entrada/saída;
- anotações de validação.

Não deve conter:

- regra de negócio;
- acesso direto a repository;
- extração manual de usuário do token;
- criação de entidade complexa sem passar pelo service.

---

## 3. Módulos globais

### `security`

Responsável pela segurança da aplicação.

Contém:

```txt
security/
├── AuthenticatedUser.java
├── AuthenticatedUserProvider.java
├── SecurityConfig.java
└── SecurityProperties.java
```

Responsabilidades:

- configurar Spring Security;
- configurar Resource Server JWT;
- configurar rotas públicas e privadas;
- extrair o usuário autenticado do JWT;
- centralizar regras técnicas de autenticação.

### `config`

Responsável por configurações gerais.

Exemplos:

```txt
config/
├── AppCorsProperties.java
└── OpenApiConfig.java
```

### `shared`

Responsável por componentes reutilizáveis e independentes de domínio.

Estrutura atual:

```txt
shared/
├── error/
├── logging/
├── pagination/
├── time/
└── web/
```

Regra importante:

- `shared` não deve conhecer domínios específicos.
- Domínios podem usar `shared`.
- `shared` não deve depender de `identity`, `routines`, `tasks`, `logs`, etc.

---

## 4. Segurança e Keycloak

### Modelo adotado

O backend é um **OAuth2 Resource Server**.

O login real acontece no frontend via Keycloak usando fluxo adequado para SPA, preferencialmente Authorization Code + PKCE.

O backend:

- não implementa login;
- não recebe senha;
- não cria sessão HTTP;
- não gera JWT;
- não aceita `userId` do frontend;
- apenas valida o JWT emitido pelo Keycloak.

### Configuração local

Keycloak local:

```txt
http://localhost:8181
```

Realm:

```txt
aion-logbook
```

Issuer URI:

```txt
http://localhost:8181/realms/aion-logbook
```

Exemplo em `application-local.yml`:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8181/realms/aion-logbook
```

---

## 5. Regras obrigatórias de autenticação

### Regra principal

Nunca aceitar identificador de usuário enviado pelo frontend.

Proibido:

```java
@GetMapping("/api/v1/logs/{userId}")
public List<LogResponse> findByUserId(@PathVariable UUID userId) {
    ...
}
```

Proibido:

```java
@PostMapping("/api/v1/logs")
public LogResponse create(@RequestBody CreateLogRequest request) {
    UUID userId = request.userId();
    ...
}
```

Correto:

```java
AuthenticatedUser user = authenticatedUserProvider.getCurrentUser();
String keycloakSubject = user.keycloakSubject();
```

Ou, preferencialmente, usar um serviço de perfil:

```java
UserProfile profile = userProfileService.getOrCreateCurrentUserProfile();
UUID userProfileId = profile.getId();
```

### Fonte oficial de identidade

A identidade do usuário autenticado vem de:

```java
jwt.getSubject()
```

O `sub` do Keycloak é o vínculo técnico entre Keycloak e banco local.

---

## 6. Keycloak vs `user_profiles`

O Keycloak gerencia identidade:

- login;
- senha;
- sessão;
- MFA;
- e-mail;
- username;
- roles;
- emissão de token.

O banco do Aion Logbook gerencia perfil interno da aplicação.

Tabela:

```txt
user_profiles
```

Finalidade:

- vincular dados internos ao `sub` do Keycloak;
- permitir preferências futuras;
- armazenar timezone;
- armazenar onboarding;
- relacionar rotinas, logs, tarefas e eventos ao usuário;
- permitir auditoria interna;
- desacoplar o domínio da aplicação do provedor de identidade.

Regra:

```txt
Keycloak = identidade/autenticação
Aion DB = perfil/domínio da aplicação
```

---

## 7. Rotas públicas e privadas

### Rotas privadas

Toda rota versionada deve exigir JWT:

```txt
/api/v1/**
```

Critério:

- sem token: `401 Unauthorized`;
- token inválido: `401 Unauthorized`;
- token válido sem permissão futura: `403 Forbidden`;
- token válido com permissão: resposta normal.

### Rotas públicas

Devem permanecer públicas:

```txt
/
/index.html
/assets/**
/favicon.ico
/*.js
/*.css
/actuator/health
```

Swagger deve ficar público apenas em ambiente local/dev:

```txt
/swagger-ui/**
/swagger-ui.html
/v3/api-docs/**
```

---

## 8. Padrão de endpoint

Toda nova rota deve seguir estes princípios:

- usar `/api/v1`;
- controller fino;
- service com regra de negócio;
- repository somente em `infrastructure`;
- request/response DTOs em `web`;
- não retornar entidade diretamente quando houver risco de vazar campos internos;
- validar entrada com Bean Validation;
- obter usuário autenticado pelo backend;
- nunca confiar em `userId`, `profileId` ou `keycloakSubject` vindo do frontend;
- filtrar dados pelo usuário autenticado;
- testar cenário com JWT e sem JWT quando for rota privada.

---

## 9. Checklist para criar nova funcionalidade

Antes de implementar um novo endpoint, responder:

- Qual é o domínio?
- A funcionalidade pertence a um domínio existente ou exige novo módulo?
- A rota é pública ou privada?
- A rota precisa de usuário autenticado?
- A entidade pertence ao usuário?
- Como a consulta será protegida contra acesso cruzado?
- Precisa de migration?
- Precisa de índice?
- Precisa de paginação?
- Precisa de ordenação?
- Precisa de auditoria?
- Precisa de logs?
- Precisa de tratamento de erro específico?
- Quais testes unitários são necessários?
- Quais testes de controller são necessários?
- Quais testes de segurança são necessários?

---

## 10. Convenção de nomes

### Controller

```txt
<Feature>Controller
```

Exemplos:

```txt
MeController
RoutineController
LogEntryController
```

### Service

```txt
<Feature>Service
```

Exemplos:

```txt
UserProfileService
RoutineService
LogEntryService
```

### Repository

```txt
<Entity>Repository
```

Exemplos:

```txt
UserProfileRepository
RoutineRepository
LogEntryRepository
```

### Request DTO

```txt
<Create|Update|Search><Feature>Request
```

Exemplos:

```txt
CreateRoutineRequest
UpdateRoutineRequest
SearchLogEntriesRequest
```

### Response DTO

```txt
<Feature>Response
```

Exemplos:

```txt
RoutineResponse
LogEntryResponse
MeResponse
```

### Migration

```txt
VNNN__descricao_em_snake_case.sql
```

Exemplos:

```txt
V003__create_user_profiles.sql
V004__create_routines.sql
V005__create_log_entries.sql
```

---

## 11. Snippet base — estrutura de novo domínio

Exemplo para um domínio chamado `routines`:

```txt
routines/
├── application/
│   └── RoutineService.java
├── domain/
│   └── Routine.java
├── infrastructure/
│   └── RoutineRepository.java
└── web/
    ├── CreateRoutineRequest.java
    ├── RoutineController.java
    └── RoutineResponse.java
```

Testes:

```txt
src/test/java/br/com/byop/aionlogbook/routines/
├── application/
│   └── RoutineServiceTest.java
└── web/
    └── RoutineControllerTest.java
```

---

## 12. Snippet base — entidade pertencente ao usuário

```java
package br.com.byop.aionlogbook.routines.domain;

import br.com.byop.aionlogbook.identity.domain.UserProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "routines")
public class Routine {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Routine() {
    }

    public Routine(UserProfile userProfile, String title, String description, OffsetDateTime now) {
        this.id = UUID.randomUUID();
        this.userProfile = userProfile;
        this.title = title;
        this.description = description;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String title, String description, OffsetDateTime now) {
        this.title = title;
        this.description = description;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
```

Pontos importantes:

- entidade tem `userProfile` obrigatório;
- criação recebe `UserProfile` vindo do backend;
- não existe `userProfileId` no request;
- datas devem ser geradas pelo backend;
- preferir `TimeProvider` em services para facilitar teste.

---

## 13. Snippet base — migration

```sql
CREATE TABLE routines (
    id UUID PRIMARY KEY,
    user_profile_id UUID NOT NULL,
    title VARCHAR(160) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_routines_user_profile
        FOREIGN KEY (user_profile_id)
        REFERENCES user_profiles (id)
);

CREATE INDEX idx_routines_user_profile_id
    ON routines (user_profile_id);

CREATE INDEX idx_routines_user_profile_created_at
    ON routines (user_profile_id, created_at DESC);
```

Regras:

- toda tabela pertencente ao usuário deve ter FK para `user_profiles`;
- criar índice por `user_profile_id`;
- criar índices compostos conforme padrão de consulta;
- não criar colunas `keycloak_subject` repetidas em todo domínio sem necessidade;
- o vínculo preferencial é por `user_profile_id`.

---

## 14. Snippet base — repository protegido por usuário

```java
package br.com.byop.aionlogbook.routines.infrastructure;

import br.com.byop.aionlogbook.routines.domain.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutineRepository extends JpaRepository<Routine, UUID> {

    List<Routine> findAllByUserProfileIdOrderByCreatedAtDesc(UUID userProfileId);

    Optional<Routine> findByIdAndUserProfileId(UUID id, UUID userProfileId);

    boolean existsByIdAndUserProfileId(UUID id, UUID userProfileId);
}
```

Regra obrigatória:

Nunca buscar entidade de usuário por `id` puro quando ela pertence a um usuário.

Evitar:

```java
repository.findById(id);
```

Preferir:

```java
repository.findByIdAndUserProfileId(id, userProfileId);
```

Isso evita que o usuário autenticado consulte dados de outro usuário.

---

## 15. Snippet base — request DTO

```java
package br.com.byop.aionlogbook.routines.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoutineRequest(
        @NotBlank
        @Size(max = 160)
        String title,

        @Size(max = 4000)
        String description
) {
}
```

Regras:

- request DTO fica em `web`;
- aplicar Bean Validation;
- não colocar `userId`;
- não colocar `userProfileId`;
- não colocar `keycloakSubject`;
- não aceitar campos controlados pelo backend, como `createdAt`, `updatedAt`, `ownerId`.

---

## 16. Snippet base — response DTO

```java
package br.com.byop.aionlogbook.routines.web;

import br.com.byop.aionlogbook.routines.domain.Routine;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RoutineResponse(
        UUID id,
        String title,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static RoutineResponse from(Routine routine) {
        return new RoutineResponse(
                routine.getId(),
                routine.getTitle(),
                routine.getDescription(),
                routine.getCreatedAt(),
                routine.getUpdatedAt()
        );
    }
}
```

Regras:

- response DTO não precisa expor `userProfileId` por padrão;
- nunca expor dados sensíveis;
- não expor detalhes internos do Keycloak;
- se precisar expor permissões/roles, criar DTO específico.

---

## 17. Snippet base — service

```java
package br.com.byop.aionlogbook.routines.application;

import br.com.byop.aionlogbook.identity.application.UserProfileService;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.routines.domain.Routine;
import br.com.byop.aionlogbook.routines.infrastructure.RoutineRepository;
import br.com.byop.aionlogbook.routines.web.CreateRoutineRequest;
import br.com.byop.aionlogbook.shared.time.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoutineService {

    private final UserProfileService userProfileService;
    private final RoutineRepository repository;
    private final TimeProvider timeProvider;

    public RoutineService(
            UserProfileService userProfileService,
            RoutineRepository repository,
            TimeProvider timeProvider
    ) {
        this.userProfileService = userProfileService;
        this.repository = repository;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public Routine create(CreateRoutineRequest request) {
        UserProfile userProfile = userProfileService.getOrCreateCurrentUserProfile();

        Routine routine = new Routine(
                userProfile,
                request.title(),
                request.description(),
                timeProvider.now()
        );

        return repository.save(routine);
    }

    @Transactional(readOnly = true)
    public List<Routine> findAllFromCurrentUser() {
        UserProfile userProfile = userProfileService.getOrCreateCurrentUserProfile();

        return repository.findAllByUserProfileIdOrderByCreatedAtDesc(userProfile.getId());
    }

    @Transactional(readOnly = true)
    public Routine findByIdFromCurrentUser(UUID id) {
        UserProfile userProfile = userProfileService.getOrCreateCurrentUserProfile();

        return repository.findByIdAndUserProfileId(id, userProfile.getId())
                .orElseThrow(() -> new IllegalArgumentException("Rotina não encontrada."));
    }
}
```

Regras:

- service busca o usuário atual;
- service aplica escopo por usuário;
- repository recebe `userProfileId` nas consultas sensíveis;
- controller não acessa repository;
- `@Transactional` fica no service;
- `TimeProvider` evita uso direto de `OffsetDateTime.now()` em services.

---

## 18. Snippet base — controller

```java
package br.com.byop.aionlogbook.routines.web;

import br.com.byop.aionlogbook.routines.application.RoutineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/routines")
public class RoutineController {

    private final RoutineService service;

    public RoutineController(RoutineService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoutineResponse create(@Valid @RequestBody CreateRoutineRequest request) {
        return RoutineResponse.from(service.create(request));
    }

    @GetMapping
    public List<RoutineResponse> findAll() {
        return service.findAllFromCurrentUser()
                .stream()
                .map(RoutineResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public RoutineResponse findById(@PathVariable UUID id) {
        return RoutineResponse.from(service.findByIdFromCurrentUser(id));
    }
}
```

Regras:

- controller usa `/api/v1`;
- controller não recebe `userId`;
- controller não extrai JWT;
- controller não chama repository;
- controller converte domínio para response;
- request body deve usar `@Valid`.

---

## 19. Padrão de resposta HTTP

O projeto possui `shared.web.ApiResponse`.

Para endpoints simples, há duas opções aceitáveis:

### Opção A — retornar DTO direto

Exemplo:

```java
@GetMapping("/api/v1/me")
public MeResponse me() {
    return MeResponse.from(userProfileService.getOrCreateCurrentUserProfile());
}
```

Boa para endpoints internos simples, como `/me`.

### Opção B — envelopar com `ApiResponse`

Exemplo:

```java
@GetMapping
public ApiResponse<List<RoutineResponse>> findAll() {
    List<RoutineResponse> data = service.findAllFromCurrentUser()
            .stream()
            .map(RoutineResponse::from)
            .toList();

    return ApiResponse.success(data);
}
```

Regra de decisão:

- usar DTO direto enquanto o endpoint for simples;
- usar `ApiResponse` quando houver padrão de resposta global, mensagens, metadados ou paginação;
- usar `PageResponse` para listas paginadas.

---

## 20. Paginação

Para endpoints de listagem potencialmente grandes, usar componentes de `shared.pagination`.

Estrutura existente:

```txt
shared/pagination/
├── PageRequestParams.java
└── PageResponse.java
```

Princípios:

- listas grandes devem ser paginadas;
- definir limite máximo de `size`;
- ordenar por campos indexados;
- sempre filtrar por usuário autenticado antes de paginar;
- nunca retornar dados de outro usuário.

Exemplo conceitual:

```java
@GetMapping
public PageResponse<RoutineResponse> findAll(PageRequestParams pageRequest) {
    return service.findAllFromCurrentUser(pageRequest)
            .map(RoutineResponse::from);
}
```

Adaptar conforme implementação real de `PageResponse`.

---

## 21. Tratamento de erros

O projeto possui:

```txt
shared/error/
├── ApiErrorResponse.java
├── FieldErrorResponse.java
└── GlobalExceptionHandler.java
```

Regras:

- erros devem ser padronizados no `GlobalExceptionHandler`;
- validação Bean Validation deve retornar erro de campo;
- erro de entidade inexistente deve retornar `404`;
- erro de autenticação deve retornar `401` pelo Spring Security;
- erro de autorização deve retornar `403`;
- erro inesperado deve retornar `500`, com log interno e resposta segura.

Evitar lançar `IllegalArgumentException` em regra definitiva de negócio quando houver exceção mais específica.

Preferir criar exceções como:

```txt
RoutineNotFoundException
ForbiddenResourceAccessException
BusinessRuleViolationException
```

Mas não criar hierarquia grande antes da necessidade real.

---

## 22. Snippet base — exceção de recurso não encontrado

```java
package br.com.byop.aionlogbook.routines.application;

public class RoutineNotFoundException extends RuntimeException {

    public RoutineNotFoundException() {
        super("Rotina não encontrada.");
    }
}
```

No service:

```java
return repository.findByIdAndUserProfileId(id, userProfile.getId())
        .orElseThrow(RoutineNotFoundException::new);
```

No `GlobalExceptionHandler`, mapear para `404` quando a exceção existir.

---

## 23. Logs e correlation id

O projeto possui:

```txt
shared/logging/CorrelationIdFilter.java
```

Regras:

- logs de entrada/saída devem preservar correlation id;
- services podem logar eventos relevantes;
- não logar token JWT;
- não logar senha;
- não logar dados sensíveis;
- logs devem ajudar auditoria e debug.

Exemplo:

```java
private static final Logger log = LoggerFactory.getLogger(RoutineService.class);

log.info("Criando rotina para userProfileId={}", userProfile.getId());
```

Evitar:

```java
log.info("Authorization header: {}", authorization);
```

---

## 24. Testes obrigatórios por nova rota

Toda nova funcionalidade deve ter, no mínimo:

```txt
<Service>Test
<Controller>Test
```

Quando envolver segurança HTTP:

```txt
SecurityConfigTest
```

Quando envolver repository customizado:

```txt
<Repository>Test
```

Quando envolver mapper complexo:

```txt
<Mapper>Test
```

---

## 25. Snippet base — teste de service

```java
package br.com.byop.aionlogbook.routines.application;

import br.com.byop.aionlogbook.identity.application.UserProfileService;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.routines.domain.Routine;
import br.com.byop.aionlogbook.routines.infrastructure.RoutineRepository;
import br.com.byop.aionlogbook.routines.web.CreateRoutineRequest;
import br.com.byop.aionlogbook.shared.time.TimeProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoutineServiceTest {

    private final UserProfileService userProfileService = mock(UserProfileService.class);
    private final RoutineRepository repository = mock(RoutineRepository.class);
    private final TimeProvider timeProvider = mock(TimeProvider.class);

    private final RoutineService service = new RoutineService(
            userProfileService,
            repository,
            timeProvider
    );

    @Nested
    class Create {

        @Test
        void shouldCreateRoutineForCurrentUser() {
            OffsetDateTime now = OffsetDateTime.parse("2026-06-02T12:00:00Z");
            UserProfile userProfile = new UserProfile("sub-123", "loki@byop.com", "loki", "Loki");
            CreateRoutineRequest request = new CreateRoutineRequest("Estudar", "Estudar Spring Security");

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(userProfile);
            when(timeProvider.now()).thenReturn(now);
            when(repository.save(any(Routine.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Routine result = service.create(request);

            assertThat(result.getId()).isNotNull();
            assertThat(result.getUserProfile()).isEqualTo(userProfile);
            assertThat(result.getTitle()).isEqualTo("Estudar");
            assertThat(result.getDescription()).isEqualTo("Estudar Spring Security");
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);

            ArgumentCaptor<Routine> captor = ArgumentCaptor.forClass(Routine.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getUserProfile()).isEqualTo(userProfile);
        }
    }

    @Nested
    class FindByIdFromCurrentUser {

        @Test
        void shouldReturnRoutineWhenItBelongsToCurrentUser() {
            UUID routineId = UUID.randomUUID();
            UserProfile userProfile = new UserProfile("sub-123", "loki@byop.com", "loki", "Loki");
            Routine routine = new Routine(userProfile, "Estudar", "Spring", OffsetDateTime.parse("2026-06-02T12:00:00Z"));

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(userProfile);
            when(repository.findByIdAndUserProfileId(routineId, userProfile.getId())).thenReturn(Optional.of(routine));

            Routine result = service.findByIdFromCurrentUser(routineId);

            assertThat(result).isEqualTo(routine);
        }

        @Test
        void shouldThrowWhenRoutineDoesNotBelongToCurrentUserOrDoesNotExist() {
            UUID routineId = UUID.randomUUID();
            UserProfile userProfile = new UserProfile("sub-123", "loki@byop.com", "loki", "Loki");

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(userProfile);
            when(repository.findByIdAndUserProfileId(routineId, userProfile.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findByIdFromCurrentUser(routineId))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
```

Pontos testados:

- usuário vem do backend;
- entidade é criada vinculada ao usuário atual;
- repository recebe `userProfileId`;
- busca por ID não usa `findById(id)` puro;
- tentativa de acesso cruzado vira não encontrado ou proibido.

---

## 26. Snippet base — teste de controller isolado

```java
package br.com.byop.aionlogbook.routines.web;

import br.com.byop.aionlogbook.routines.application.RoutineService;
import br.com.byop.aionlogbook.routines.domain.Routine;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoutineControllerTest {

    private final RoutineService service = mock(RoutineService.class);
    private final RoutineController controller = new RoutineController(service);

    @Nested
    class FindAll {

        @Test
        void shouldReturnCurrentUserRoutines() {
            UserProfile userProfile = new UserProfile("sub-123", "loki@byop.com", "loki", "Loki");
            Routine routine = new Routine(userProfile, "Estudar", "Spring", OffsetDateTime.parse("2026-06-02T12:00:00Z"));

            when(service.findAllFromCurrentUser()).thenReturn(List.of(routine));

            List<RoutineResponse> response = controller.findAll();

            assertThat(response).hasSize(1);
            assertThat(response.getFirst().id()).isEqualTo(routine.getId());
            assertThat(response.getFirst().title()).isEqualTo("Estudar");

            verify(service).findAllFromCurrentUser();
        }
    }
}
```

Observação:

- teste isolado de controller não valida Spring Security;
- serve para validar delegação e conversão de response;
- segurança deve ser validada com `MockMvc`.

---

## 27. Snippet base — teste HTTP com segurança

Para validar segurança real, preferir:

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
```

Evitar usar apenas `@WebMvcTest` para validar `SecurityConfig`, porque slice test pode não carregar todos os beans necessários ou pode carregar comportamento diferente do contexto real.

Exemplo:

```java
package br.com.byop.aionlogbook.security;

import br.com.byop.aionlogbook.identity.application.UserProfileService;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.shared.time.TimeProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private TimeProvider timeProvider;

    @Nested
    class ApiV1 {

        @Test
        void shouldReturnUnauthorizedWhenCallingPrivateRouteWithoutToken() throws Exception {
            when(timeProvider.now()).thenReturn(OffsetDateTime.parse("2026-06-02T12:00:00Z"));

            mockMvc.perform(get("/api/v1/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldAllowPrivateRouteWithJwt() throws Exception {
            UserProfile profile = new UserProfile(
                    "sub-123",
                    "loki@byop.com",
                    "loki",
                    "Loki"
            );

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(profile);

            mockMvc.perform(get("/api/v1/me")
                            .with(jwt().jwt(token -> token
                                    .subject("sub-123")
                                    .claim("email", "loki@byop.com")
                                    .claim("preferred_username", "loki")
                                    .claim("name", "Loki")
                            )))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.keycloakSubject", is("sub-123")))
                    .andExpect(jsonPath("$.email", is("loki@byop.com")))
                    .andExpect(jsonPath("$.username", is("loki")))
                    .andExpect(jsonPath("$.fullName", is("Loki")));
        }
    }

    @Nested
    class PublicRoutes {

        @Test
        void shouldNotRequireAuthenticationForHealth() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void shouldNotRequireAuthenticationForStaticIndex() throws Exception {
            mockMvc.perform(get("/index.html"))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }

        @Test
        void shouldNotRequireAuthenticationForSwaggerInLocalProfile() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
        }
    }
}
```

Regra de assert para rotas públicas em teste de segurança:

```java
assertThat(status).isNotIn(401, 403);
```

Motivo:

- em contexto de teste, a rota pode não existir e retornar `404`;
- se o recurso existir, pode retornar `200`;
- o que a segurança precisa garantir é que não seja bloqueado por autenticação/autorização.

---

## 28. Snippet base — teste de controller com MockMvc e JWT

Para testar um controller específico com MockMvc:

```java
package br.com.byop.aionlogbook.routines.web;

import br.com.byop.aionlogbook.routines.application.RoutineService;
import br.com.byop.aionlogbook.routines.domain.Routine;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.shared.time.TimeProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoutineController.class)
class RoutineControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoutineService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private TimeProvider timeProvider;

    @Test
    void shouldReturnRoutinesWithJwt() throws Exception {
        UserProfile userProfile = new UserProfile("sub-123", "loki@byop.com", "loki", "Loki");
        Routine routine = new Routine(userProfile, "Estudar", "Spring", OffsetDateTime.parse("2026-06-02T12:00:00Z"));

        when(service.findAllFromCurrentUser()).thenReturn(List.of(routine));

        mockMvc.perform(get("/api/v1/routines")
                        .with(jwt().jwt(token -> token.subject("sub-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Estudar")));
    }
}
```

Observações:

- se o `GlobalExceptionHandler` depender de `TimeProvider`, mockar `TimeProvider`;
- se o slice não carregar `SecurityConfig`, validar a segurança real em `SecurityConfigTest` com `@SpringBootTest`;
- usar `@MockitoBean` no Spring Boot 3.4+;
- se o projeto estiver em versão anterior, usar `@MockBean`.

---

## 29. CORS

Origem local do frontend:

```txt
http://localhost:5173
```

Regras:

- CORS deve ser configurado centralmente;
- não liberar `*` com credentials;
- ambiente local pode ser permissivo apenas para portas conhecidas;
- produção deve usar domínio real;
- headers mínimos: `Authorization`, `Content-Type`, `Accept`;
- métodos conforme necessidade: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.

Exemplo conceitual:

```java
config.setAllowedOrigins(List.of("http://localhost:5173"));
config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
config.setAllowCredentials(true);
```

---

## 30. CSRF e sessão

Como o backend é API stateless protegida por Bearer Token:

```txt
CSRF disabled
SessionCreationPolicy.STATELESS
```

Justificativa:

- frontend usa token Bearer;
- backend não mantém sessão HTTP;
- autenticação vem do Keycloak;
- cada request carrega sua autorização.

---

## 31. Teste manual com Postman

### 1. Sem token

```http
GET http://localhost:8080/api/v1/me
```

Esperado:

```http
401 Unauthorized
```

### 2. Obter token local no Keycloak

Para teste local, pode-se usar Direct Access Grants, desde que seja apenas em ambiente de desenvolvimento.

Request:

```http
POST http://localhost:8181/realms/aion-logbook/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded
```

Body:

```txt
grant_type=password
client_id=aion-logbook-spa
username=<usuario>
password=<senha>
```

Resposta esperada:

```json
{
  "access_token": "eyJ..."
}
```

### 3. Chamar rota autenticada

```http
GET http://localhost:8080/api/v1/me
Authorization: Bearer <access_token>
```

Esperado:

```http
200 OK
```

Com payload do perfil interno:

```json
{
  "id": "...",
  "keycloakSubject": "...",
  "email": "...",
  "username": "...",
  "fullName": "...",
  "createdAt": "...",
  "updatedAt": "..."
}
```

---

## 32. Teste manual pelo browser

Browser puro não é ideal para testar Bearer Token manualmente.

Fluxo correto:

1. frontend SPA redireciona para Keycloak;
2. usuário loga;
3. Keycloak devolve token para a SPA;
4. SPA chama backend com:

```http
Authorization: Bearer <access_token>
```

Para debug rápido, usar Postman, Insomnia ou curl.

---

## 33. Curl útil

Sem token:

```bash
curl -i http://localhost:8080/api/v1/me
```

Com token:

```bash
curl -i \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/me
```

Obter token local:

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

---

## 34. Checklist final para Pull Request

Antes de abrir PR/MR:

- [ ] Package está abaixo de `br.com.byop.aionlogbook`.
- [ ] Novo domínio segue `application/domain/infrastructure/web`.
- [ ] Rotas usam `/api/v1`.
- [ ] Nenhum endpoint privado aceita `userId` do frontend.
- [ ] Dados de usuário são filtrados por `UserProfile` atual.
- [ ] JWT é validado pelo Resource Server.
- [ ] `jwt.getSubject()` é a origem do vínculo com Keycloak.
- [ ] Controller não acessa repository.
- [ ] Service concentra regra de negócio.
- [ ] Repository usa consultas com `userProfileId` quando necessário.
- [ ] Migration criada quando houver nova tabela.
- [ ] Índices criados conforme consultas.
- [ ] DTOs de request têm validação.
- [ ] DTOs de response não vazam campos internos.
- [ ] Testes unitários de service criados.
- [ ] Testes de controller criados.
- [ ] Testes de segurança criados ou atualizados.
- [ ] Rotas públicas continuam sem `401/403`.
- [ ] `/api/v1/**` continua exigindo token.
- [ ] Logs não expõem token ou dados sensíveis.
- [ ] Erros seguem `GlobalExceptionHandler`.
- [ ] Rodou `mvn test`.

---

## 35. Regras de ouro

1. Backend não faz login.
2. Backend não aceita senha.
3. Backend não aceita `userId` do frontend.
4. Backend confia no JWT validado, não no payload enviado pelo usuário.
5. `jwt.getSubject()` é o vínculo com Keycloak.
6. `user_profiles` é perfil interno, não cadastro paralelo de autenticação.
7. Toda consulta de recurso do usuário deve ser escopada pelo usuário autenticado.
8. Controller é fino.
9. Service governa regra de negócio.
10. Repository não decide permissão.
11. Teste de segurança real usa `@SpringBootTest` + `@AutoConfigureMockMvc`.
12. Rotas públicas não podem retornar `401` ou `403`.
13. Rotas privadas sem token devem retornar `401`.
14. Rotas privadas com token válido devem funcionar.
15. Código novo deve parecer que sempre pertenceu ao projeto.
