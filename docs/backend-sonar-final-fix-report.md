# Relatório de Correção — SonarQube Backend
**Data:** 2026-06-04
**Build:** `mvn test` → 208 testes, 0 falhas, 0 erros — **BUILD SUCCESS**

---

## Issues Corrigidas (21/21)

### 1. `AnalyticsRepositoryTest.java` — S5853
**Abordagem:** Agrupamento de múltiplos `assertThat(resultado.campo())` sobre o mesmo objeto usando `.satisfies()` e encadeamento direto de `.anyMatch()`.
- `shouldReturnZeroOverviewWhenDatabaseIsEmptyForUser` → `.satisfies(r -> {...})`
- `shouldCalculateOverviewOnlyForOwner` → `.satisfies(r -> {...})`
- `shouldFilterOverviewByDateRange` → `.satisfies(r -> {...})`
- `shouldReturnPlansByDay` → `.satisfies(first -> {...})` e `.satisfies(second -> {...})`
- `shouldReturnStatusDistribution` → `.anyMatch(...).anyMatch(...)` encadeados
- `shouldReturnTimeByDirection` → `.satisfies(first -> {...})`
- `shouldReturnPlannedVsExecuted` → `.satisfies(first -> {...})`
- `shouldReturnExecutedEvenWithoutPlans` → `.satisfies(first -> {...})`

---

### 2. `BugReportMapperTest.java` — S5853
**Abordagem:** Agrupamento com `.satisfies()` e encadeamento de asserções no mesmo sujeito.
- `shouldCreateDomainWithAuthenticatedUserId` → `.satisfies(b -> {...})`
- `shouldSanitizeSensitiveMetadata` → `.containsEntry(...).doesNotContainKeys(...)` encadeados
- `shouldSanitizeNestedSensitiveMetadata` → `.containsEntry(...).doesNotContainKey(...)` encadeados
- `shouldCreateSuccessResponse` → `.satisfies(r -> {...})`

---

### 3. `CreatePlanUseCase.java` — S6829
**Abordagem:** Adicionado `@Autowired` ao construtor público (o que Spring usa para injeção). O construtor package-private permanece para testes com `Clock` customizado.

---

### 4. `DashboardServiceTest.java` — `eq()` inútil
**Abordagem:** Removidos os wrappers `eq()` das duas chamadas a `sumDurationMinutesByUserIdBetween` onde todos os argumentos usavam `eq()`. Valores passados diretamente.

---

### 5. `DirectionService.java` — S6809 (self-invocation transacional)
**Abordagem:** Extraído `findByIdInternal(UUID id)` como método privado sem anotação `@Transactional`. O método público `findById` delega para ele, e `update`/`archive` também chamam `findByIdInternal` diretamente — evitando a invocação via proxy `this`.

---

### 6. `DirectionServiceTest.java` — S1488
**Abordagem:** Eliminada variável temporária `userProfile` no método auxiliar; retorno direto de `mock(UserProfile.class)`.

---

### 7. `LogEntryRepository.java` — S107 (9 parâmetros)
**Abordagem:** Removido método `findByFilters` com 9 parâmetros via `@Query` nativa. A interface agora estende `LogEntryRepositoryCustom`, que declara `findByFilters(UUID, LogEntrySearchCriteria, Pageable)` — 3 parâmetros.
**Arquivos criados:**
- `LogEntrySearchCriteria.java` — record com os 7 critérios de busca
- `LogEntryRepositoryCustom.java` — interface do fragmento customizado
- `LogEntryRepositoryImpl.java` — implementação com `EntityManager` + SQL dinâmico (sem parâmetros nulos problemáticos)

---

### 8. `LogEntryService.java` — S107 (9 parâmetros)
**Abordagem:** Método `list` refatorado de 9 para 3 parâmetros (`UUID userId`, `LogEntrySearchCriteria criteria`, `Pageable pageable`). Removida dependência de `ObjectMapper` (movida para `LogEntryRepositoryImpl`).
**Impacto:** `LogEntryController` atualizado para construir `LogEntrySearchCriteria` antes de chamar `service.list`.

---

### 9. `RequestIdFilterTest.java` — S5853
**Abordagem:** Encadeamento direto em `shouldGenerateRequestIdWhenHeaderIsBlank`: `assertThat(responseRequestId).isNotBlank().isNotEqualTo("   ")`.

---

### 10. `SessionLogService.java` — S107 + S6809
**S107:** Método `createAutomaticFromPlan` com 8 parâmetros substituído por `createAutomaticFromPlan(UUID userId, AutomaticSessionLogRequest request)` — 2 parâmetros.
**Arquivo criado:** `AutomaticSessionLogRequest.java` — record com os 7 campos restantes.
**S6809:** Extraído `findByIdInternal(UUID userId, UUID id)` como método privado; `update` invoca o método interno diretamente.

---

### 11. `TelegramBugReportNotifierTest.java` — Lambda com múltiplas invocações
**Abordagem:** `validBugReport()` extraído para fora das lambdas `assertThatThrownBy`/`assertThatCode` em 3 métodos de teste (`shouldReturnFalseWhenTelegramIsDisabled`, `shouldThrowWhenEnabledAndBotTokenIsMissing`, `shouldThrowWhenEnabledAndChatIdIsMissing`).

---

### 12. `TransitionPlanUseCase.java` — S6829 + S1612
**S6829:** Adicionado `@Autowired` ao construtor público (4 parâmetros).
**S1612:** Lambda `now -> plan.setStartedAt(now)` substituída por method reference `plan::setStartedAt` no método `start`.
**Colateral:** Chamadas a `sessionLogService.createAutomaticFromPlan` em `complete` e `partial` atualizadas para usar `new AutomaticSessionLogRequest(...)`.

---

### 13. `TransitionPlanUseCaseTest.java` — Lambda com múltiplas invocações
**Abordagem:** Em 4 métodos com `assertThatThrownBy`, o `new XxxRequest(...)` foi extraído para variável local antes da lambda.
**Colateral:** 4 `verify(...).createAutomaticFromPlan(...)` atualizados para `AutomaticSessionLogRequest`; 1 `verify(never())` atualizado de 8 matchers `any()` para 2.

---

## Arquivos Alterados

| Arquivo | Ação |
|---|---|
| `logbook/application/LogEntrySearchCriteria.java` | **Criado** |
| `logbook/infrastructure/LogEntryRepositoryCustom.java` | **Criado** |
| `logbook/infrastructure/LogEntryRepositoryImpl.java` | **Criado** |
| `session/application/AutomaticSessionLogRequest.java` | **Criado** |
| `logbook/infrastructure/LogEntryRepository.java` | Modificado |
| `logbook/application/LogEntryService.java` | Modificado |
| `logbook/api/LogEntryController.java` | Modificado |
| `direction/application/DirectionService.java` | Modificado |
| `session/application/SessionLogService.java` | Modificado |
| `plan/application/CreatePlanUseCase.java` | Modificado |
| `plan/application/TransitionPlanUseCase.java` | Modificado |
| `analytics/infrastructure/AnalyticsRepositoryTest.java` | Modificado |
| `bugreport/mapper/BugReportMapperTest.java` | Modificado |
| `dashboard/application/DashboardServiceTest.java` | Modificado |
| `direction/application/DirectionServiceTest.java` | Modificado |
| `shared/logging/RequestIdFilterTest.java` | Modificado |
| `bugreport/infrastructure/TelegramBugReportNotifierTest.java` | Modificado |
| `plan/application/TransitionPlanUseCaseTest.java` | Modificado |
| `logbook/infrastructure/LogEntryRepositoryTest.java` | Modificado |
| `session/application/SessionLogServiceTest.java` | Modificado |
| `logbook/application/LogEntryServiceTest.java` | Modificado |

---

## Resultado Maven

```
Tests run: 208, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## Issues Restantes

Nenhuma. Todas as 21 issues listadas foram corrigidas.

---

## Notas Técnicas

- **`LogEntryRepositoryImpl`**: usa SQL dinâmico (sem parâmetros nulos) para evitar problema de tipo em queries nativas com PostgreSQL e para compatibilidade com `@DataJpaTest` (que não disponibiliza `ObjectMapper`). O `@SuppressWarnings("unchecked")` no `getResultList()` é o padrão inevitável de type erasure de JPA nativo, não mascara problema de lógica.
- **Contratos públicos HTTP**: nenhum endpoint foi alterado. `LogEntryController` continua aceitando os mesmos query params; a agregação em `LogEntrySearchCriteria` é interna.
- **Regras de negócio**: intactas. Nenhum comportamento foi alterado, apenas estrutura e assinaturas internas.
