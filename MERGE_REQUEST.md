# Contexto

Entrega do modulo backend de planos do Aion Logbook, adicionando persistencia, API privada e trilha basica de eventos por plano.

# O que foi desenvolvido

- Modulo `plan` com dominio, DTOs, mapper, repositories, casos de uso e controller REST.
- Endpoints privados em `/api/v1/plans`.
- Migrations para `plans` e `plan_events`.
- Resolucao do usuario atual via `CurrentUserService`.
- Cobertura de testes unitarios, WebMvc e repositories com PostgreSQL/Testcontainers.

# Alteracoes tecnicas

- Inclusao de Lombok e configuracao explicita do annotation processor para JDKs recentes.
- Criacao das migrations `V003__create_plans.sql` e `V004__create_plan_events.sql`.
- Validacao de ownership de `directionId` na criacao/atualizacao de planos.
- Registro de eventos `CREATED` e `UPDATED`.
- Listagem de planos com combinacao livre de filtros opcionais.
- Tratamento de recursos inexistentes via `ResourceNotFoundException`.
- Ajuste dos testes de seguranca para mockar os novos use cases do modulo `plan`.

# Testes executados

- `mvn clean test`

Resultado:

- 85 testes executados.
- 0 falhas.
- 0 erros.
- 0 ignorados.

# Impactos

- Novo contrato HTTP para planos no backend.
- Novas tabelas e indices no PostgreSQL.
- Dependencia de annotation processing do Lombok no build Maven.
- A API passa a exigir que direcoes vinculadas a planos estejam ativas e pertençam ao usuario autenticado.

# Riscos

- O modulo usa `api`/`dto`/`mapper`, enquanto o documento de padrao descreve `web`; isso segue o padrao real ja usado por `direction`, mas diverge da nomenclatura documental.
- `UpdatePlanRequest` preserva campos nulos, entao nao ha mecanismo explicito para limpar campos opcionais via update.
- O relogio dos casos de uso usa `America/Sao_Paulo` internamente para regras de data, enquanto o bean global existente e UTC.
- Warnings de JDK recente permanecem relacionados a Lombok/Byte Buddy usando APIs `sun.misc.Unsafe`.

# Evidencias

- `git diff develop...HEAD` foi executado e retornou vazio porque a branch atual esta em `develop`; as alteracoes revisadas estavam no worktree.
- `git status --ignored --short` confirmou artefatos ignorados locais: `.DS_Store`, `frontend/node_modules`, `backend/target`, `.env` e outros.
- `mvn clean test` passou apos os ajustes.

# Checklist de validacao

- [x] Diff contra `develop` executado.
- [x] Alteracoes do worktree revisadas.
- [x] Bugs potenciais analisados.
- [x] Regressões avaliadas.
- [x] Arquitetura e padroes avaliados.
- [x] Documentacao atualizada.
- [x] Imports/warnings avaliados via compilacao.
- [x] Build Maven ajustado.
- [x] Testes executados com sucesso.
- [x] Arquivos temporarios identificados como ignorados.
- [x] Merge/rebase/reset/push force nao executados.
