# Deploy e Validacao Genesis Lab

Este guia cobre a validacao inicial do Aion Log Book no ambiente `genesis-lab`.

## Premissas

- PostgreSQL provisionado e acessivel pelo backend.
- Keycloak 26 com realm `aion-logbook`.
- Backend Spring Boot com Java 21.
- Frontend React gerado por Vite e, preferencialmente neste ciclo, servido pelo backend.

## Variaveis Obrigatorias

Backend:

```env
DB_URL=jdbc:postgresql://<host>:5432/aion_logbook
DB_USERNAME=<usuario>
DB_PASSWORD=<senha>
KEYCLOAK_ISSUER_URI=https://auth.genesis-lab.dev/realms/aion-logbook
CORS_ALLOWED_ORIGINS=https://aion.genesis-lab.dev
AION_SECURITY_ENABLED=true
FLYWAY_ENABLED=true
SWAGGER_ENABLED=false
```

Frontend para build de validacao:

```env
VITE_API_MODE=real
VITE_API_BASE_URL=/api/v1
VITE_AUTH_MODE=keycloak
VITE_KEYCLOAK_ENABLED=true
VITE_KEYCLOAK_URL=https://auth.genesis-lab.dev
VITE_KEYCLOAK_REALM=aion-logbook
VITE_KEYCLOAK_CLIENT_ID=aion-logbook-web
```

Keycloak:

- Redirect URI: `https://aion.genesis-lab.dev/*`
- Web origin: `https://aion.genesis-lab.dev`
- Post logout redirect URI: `https://aion.genesis-lab.dev/*`
- Login theme: `aion-logbook`
- PKCE: `S256`

## Build

```bash
cd frontend
npm install
npm run lint
npm run build
npm run build:backend

cd ../backend
mvn clean test
mvn clean package
```

## Smoke Tests

1. Abrir `https://aion.genesis-lab.dev/login`.
2. Confirmar redirecionamento para Keycloak e retorno para a SPA.
3. Acessar `/api/v1/me` autenticado.
4. Criar uma direcao.
5. Criar um plano vinculado a uma direcao.
6. Iniciar e concluir um plano.
7. Confirmar criacao automatica de sessao quando aplicavel.
8. Criar uma entrada de logbook e testar filtros.
9. Abrir analytics e validar agregacoes sem erro em banco com poucos dados.
10. Enviar bug report com Telegram desabilitado e confirmar `201 Created`.

## Pontos de Atencao

- O realm versionado contem providers Google e Microsoft com secrets vazios. Aplique secrets reais por ambiente usando `docker/keycloak/scripts/configure-identity-providers.sh` ou mecanismo equivalente.
- Apple exige HTTPS e dominio valido; nao validar Apple em localhost.
- O Compose local monta tema e scripts por volume. Para Genesis Lab, prefira imagem Keycloak construida com `docker/keycloak/Dockerfile`.
- `AION_SECURITY_ENABLED=false` deve ser usado apenas em testes controlados, nunca em validacao real.
- `SWAGGER_ENABLED=false` e recomendado fora de ambientes internos.
- O build servido pelo backend deve ser refeito sempre que `frontend/src` ou variaveis `VITE_*` mudarem.

## Rollback

- Backend: manter pacote anterior disponivel e reverter para ele se migrations nao destrutivas permitirem.
- Keycloak: exportar realm antes de alteracoes manuais em providers, temas ou redirects.
- Banco: manter backup antes da primeira validacao com dados reais.
