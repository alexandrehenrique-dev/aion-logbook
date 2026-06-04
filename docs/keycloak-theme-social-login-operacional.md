# Keycloak — Tema Customizado + Login Social: Guia Operacional

> Aion Logbook · Keycloak 26.0.7 · Docker Compose
> Última revisão: 2026-06-04

---

## 1. Visão Geral

A tela de login do Aion Logbook é servida pelo Keycloak com um tema customizado (`aion-logbook`) que reflete a identidade visual da plataforma. O frontend React inicia o fluxo OAuth2/OIDC (Authorization Code + PKCE) e redireciona o usuário para a tela do Keycloak, que autentica e devolve o token.

**Fluxo resumido:**
```
Usuário → Frontend (Vite/React)
       → Keycloak (tela de login customizada)
       → [Login local | Google | Microsoft | Apple]
       → Token → Frontend → API Spring Boot
```

---

## 2. O Que Foi Automatizado

| Item | Automatizado via |
|---|---|
| Tema visual `aion-logbook` | Volume no compose (dev) / Dockerfile (prod) |
| Realm `aion-logbook` | `--import-realm` no compose |
| `loginTheme: aion-logbook` no realm | realm JSON |
| Client `aion-logbook-web` com PKCE S256 | realm JSON |
| Redirect URIs local + produção | realm JSON |
| Identity providers criados (desabilitados) | realm JSON |
| Ativação com credenciais reais | Script `configure-identity-providers.sh` |
| Placeholders de env | `.env.example` |

---

## 3. O Que Não Pode Ser 100% Automatizado

**O Docker Compose não cria aplicações nos provedores externos.**

Para cada provedor social, é necessário um cadastro manual no painel externo **uma única vez**. Depois disso, a configuração do Keycloak é replicável via script.

| Provedor | Painel externo | Automatizável após cadastro |
|---|---|---|
| Google | Google Cloud Console | Sim (script kcadm) |
| Microsoft | Azure Portal / Entra ID | Sim (script kcadm) |
| Apple | Apple Developer | Sim, mas com ressalvas¹ |

¹ Apple exige chave privada `.p8`, HTTPS com domínio válido — não funciona em `localhost`.
² O provider `apple` **não está embutido** no Keycloak 26 community edition. Requer extensão de terceiros (ex: [keycloak-apple-social-identity-provider](https://github.com/BenjaminFavre/keycloak-apple-social-identity-provider)) ou uso de provider genérico OIDC. Por isso o Apple **não é incluído no realm import** — configure manualmente via Admin UI após instalar a extensão.

---

## 4. Estrutura de Arquivos Criada

```
repo/
├── .env.example                                ← variáveis com placeholders
├── docker-compose.yml                          ← volume do tema adicionado
└── docker/
    └── keycloak/
        ├── Dockerfile                          ← imagem customizada (produção)
        ├── import/
        │   └── aion-logbook-realm.json         ← realm com loginTheme e providers
        ├── scripts/
        │   └── configure-identity-providers.sh ← ativa providers via kcadm
        └── themes/
            └── aion-logbook/
                └── login/
                    ├── theme.properties
                    ├── login.ftl
                    └── resources/
                        ├── css/
                        │   └── aion-login.css
                        └── img/
                            ├── aion-logo.svg
                            └── aion-symbol.svg
```

---

## 5. Como o Tema Customizado Funciona

O tema `aion-logbook` estende o tema clássico `keycloak` (estável em Keycloak 26):

- **`theme.properties`**: declara o pai, importa módulos comuns e carrega o CSS customizado em adição ao CSS base.
- **`login.ftl`**: template Freemarker que substitui a tela de login padrão. Mantém todos os elementos obrigatórios do Keycloak (ação de formulário, CSRF, campos `username`/`password`, botões sociais) e adiciona o logo e o tagline do Aion.
- **`aion-login.css`**: aplica a identidade visual — fundo escuro, card central, paleta teal/azul-névoa, responsividade e acessibilidade básica.
- **`aion-logo.svg`**: logo horizontal vetorial (símbolo bússola + wordmark AION LOGBOOK).

**Paleta:**

| Token | Hex | Uso |
|---|---|---|
| `--aion-bg` | `#0d1f26` | Fundo da página |
| `--aion-bg-card` | `#132830` | Card de login |
| `--aion-teal` | `#2c5f6f` | Botão primário, símbolo |
| `--aion-accent` | `#a8d5df` | Labels, destaques |
| `--aion-white` | `#e8f4f7` | Texto principal |
| `--aion-muted` | `#7a9eaa` | Texto secundário |

---

## 6. Como Ativar o Tema no Realm

O realm JSON já define `"loginTheme": "aion-logbook"`. Ao importar o realm, o tema é aplicado automaticamente.

Para verificar ou alterar via UI:
1. Keycloak Admin → realm `aion-logbook` → **Realm Settings** → aba **Themes**
2. Campo **Login Theme** → selecionar `aion-logbook`
3. Salvar

Via kcadm:
```bash
/opt/keycloak/bin/kcadm.sh update realms/aion-logbook \
  -s loginTheme=aion-logbook \
  --server http://localhost:8181 \
  --realm master \
  --user admin \
  --password <senha>
```

---

## 7. Como o Docker Compose Monta o Tema

**Desenvolvimento (padrão atual):**

```yaml
volumes:
  - ./docker/keycloak/themes:/opt/keycloak/themes:ro
```

O volume monta toda a pasta `themes/` no container. Qualquer alteração nos arquivos do tema fica disponível sem reiniciar o container — basta recarregar a página do Keycloak.

**Produção (recomendado):**

Substitua `image:` por `build:` no compose:

```yaml
keycloak:
  build:
    context: ./docker/keycloak
  # remova o volume do tema
```

O `Dockerfile` em `docker/keycloak/Dockerfile` copia o tema para dentro da imagem, garantindo imutabilidade e portabilidade.

---

## 8. Como Importar o Realm

O realm é importado automaticamente pelo Keycloak na inicialização quando `--import-realm` está presente no `command:` do compose.

O Keycloak verifica se o realm já existe antes de importar. Se o realm já existir, o import é ignorado (não sobrescreve).

**Para reimportar (quando necessário):**
```bash
# 1. Apagar o realm existente via UI ou API
# 2. Recriar o container
docker compose down keycloak
docker compose up -d keycloak
```

**Importar manualmente via kcadm:**
```bash
/opt/keycloak/bin/kcadm.sh create realms \
  -f /opt/keycloak/data/import/aion-logbook-realm.json \
  --server http://localhost:8181 --realm master \
  --user admin --password <senha>
```

---

## 9. Como Configurar Google

### Passo A — Google Cloud Console (manual, uma vez por ambiente)

1. Acesse [console.cloud.google.com](https://console.cloud.google.com)
2. Crie ou selecione um projeto
3. **APIs & Services** → **OAuth consent screen**
   - User type: External
   - Preencha app name, email, domínio
   - Adicione scopes: `email`, `profile`, `openid`
4. **APIs & Services** → **Credentials** → **Create Credentials** → OAuth Client ID
   - Application type: **Web application**
   - Authorized redirect URIs:
     ```
     http://localhost:8181/realms/aion-logbook/broker/google/endpoint
     https://auth.genesis-lab.dev/realms/aion-logbook/broker/google/endpoint
     ```
5. Copie **Client ID** e **Client Secret**
6. Guarde em `.env` (nunca comite)

### Passo B — Keycloak (automatizável)

```bash
KEYCLOAK_URL=http://localhost:8181 \
KEYCLOAK_ADMIN=admin \
KEYCLOAK_ADMIN_PASSWORD=<senha> \
GOOGLE_CLIENT_ID=<seu-client-id> \
GOOGLE_CLIENT_SECRET=<seu-secret> \
bash docker/keycloak/scripts/configure-identity-providers.sh
```

Ou via kcadm direto no container:
```bash
docker exec aion-logbook-keycloak \
  /opt/keycloak/bin/kcadm.sh update identity-provider/instances/google \
  -r aion-logbook \
  -s enabled=true \
  -s 'config.clientId=SEU_CLIENT_ID' \
  -s 'config.clientSecret=SEU_SECRET' \
  --server http://localhost:8080 \
  --realm master --user admin --password admin
```

---

## 10. Como Configurar Microsoft / Outlook

### Passo A — Azure Portal (manual, uma vez por ambiente)

1. Acesse [portal.azure.com](https://portal.azure.com) → **Azure Active Directory** → **App registrations**
2. **New registration**
   - Name: `Aion Logbook`
   - Supported account types: **Accounts in any organizational directory and personal Microsoft accounts** (para Outlook/hotmail)
   - Redirect URI: Web →
     ```
     http://localhost:8181/realms/aion-logbook/broker/microsoft/endpoint
     https://auth.genesis-lab.dev/realms/aion-logbook/broker/microsoft/endpoint
     ```
3. Após criar: copie o **Application (client) ID**
4. **Certificates & secrets** → **New client secret**
   - Copie o valor do secret (visível apenas uma vez)
5. Guarde em `.env`

### Passo B — Keycloak (automatizável)

```bash
KEYCLOAK_URL=http://localhost:8181 \
MICROSOFT_CLIENT_ID=<app-id> \
MICROSOFT_CLIENT_SECRET=<secret> \
MICROSOFT_TENANT_ID=common \
bash docker/keycloak/scripts/configure-identity-providers.sh
```

**Nota sobre `TENANT_ID`:**
- `common` → qualquer conta Microsoft (pessoal + organizacional)
- `organizations` → apenas contas organizacionais
- `<tenant-id-específico>` → apenas usuários do seu tenant Azure

---

## 11. Como Configurar Apple

### Limitação importante

> **Apple exige HTTPS com domínio válido.** Não é possível testar login com Apple em `localhost`. Configure apenas para o ambiente de produção (`auth.genesis-lab.dev`).

### Passo A — Apple Developer (manual, uma vez)

1. Acesse [developer.apple.com](https://developer.apple.com) → **Account** → **Certificates, IDs & Profiles**
2. **Identifiers** → **+** → **Services IDs**
   - Descrição: `Aion Logbook`
   - Identifier: `dev.genesis-lab.aion` (ou similar — será o `clientId` no Keycloak)
   - Habilite **Sign In with Apple**
   - Configure redirect URL:
     ```
     https://auth.genesis-lab.dev/realms/aion-logbook/broker/apple/endpoint
     ```
3. **Keys** → **+** → habilite **Sign In with Apple**
   - Baixe a chave `.p8` (guardada com segurança — não pode ser baixada novamente)
   - Anote: **Key ID** e **Team ID** (visível no canto superior direito)
4. Guarde em `.env`:
   - `APPLE_CLIENT_ID` = o identifier do Services ID
   - `APPLE_TEAM_ID` = seu Team ID
   - `APPLE_KEY_ID` = o Key ID da chave criada
   - `APPLE_CLIENT_SECRET` = conteúdo da chave `.p8` (incluindo `-----BEGIN PRIVATE KEY-----`)

### Passo B — Keycloak (automatizável em produção)

```bash
KEYCLOAK_URL=https://auth.genesis-lab.dev \
APPLE_CLIENT_ID=dev.genesis-lab.aion \
APPLE_TEAM_ID=ABCDE12345 \
APPLE_KEY_ID=XYZKEY1234 \
APPLE_CLIENT_SECRET="$(cat /caminho/para/key.p8)" \
bash docker/keycloak/scripts/configure-identity-providers.sh
```

O Keycloak 26 tem suporte nativo ao provider `apple` — ele gera o JWT de client secret automaticamente a partir da chave privada.

---

## 12. Como Usar `.env` por Ambiente

Nunca comite `.env`. Copie `.env.example` e preencha:

```bash
cp .env.example .env
# edite .env com os valores reais do ambiente
```

**Local dev (sem social login):**
```env
POSTGRES_PASSWORD=aion_password
KEYCLOAK_ADMIN_PASSWORD=admin
# deixe GOOGLE_*, MICROSOFT_*, APPLE_* vazios
```

**Genesis-lab (com social login):**
```env
POSTGRES_PASSWORD=<senha-segura>
KEYCLOAK_ADMIN_PASSWORD=<senha-segura>
GOOGLE_CLIENT_ID=<id>
GOOGLE_CLIENT_SECRET=<secret>
MICROSOFT_CLIENT_ID=<id>
MICROSOFT_CLIENT_SECRET=<secret>
MICROSOFT_TENANT_ID=common
APPLE_CLIENT_ID=<service-id>
APPLE_TEAM_ID=<team>
APPLE_KEY_ID=<key-id>
APPLE_CLIENT_SECRET=<conteúdo-p8>
```

Para secrets sensíveis em produção, prefira Docker Secrets ou um secret manager (Vault, AWS Secrets Manager) em vez de variáveis de ambiente diretas no compose.

---

## 13. Como Replicar em Outro Ambiente

1. **Clonar o repo** — o tema já está versionado em `docker/keycloak/themes/`
2. **Criar `.env`** a partir de `.env.example` com as credenciais do ambiente
3. **Cadastrar redirect URIs** nos provedores externos (Google Console, Azure, Apple Developer) para o novo domínio
4. **Subir compose**: `docker compose up -d`
5. **Executar o script** de configuração de providers:
   ```bash
   docker exec aion-logbook-keycloak bash /opt/keycloak/scripts/configure-identity-providers.sh
   ```
   Ou externamente com as variáveis de ambiente setadas
6. **Validar** (ver seção 14)

---

## 14. Como Validar Localmente

```bash
# 1. Subir infraestrutura
docker compose up -d

# 2. Acompanhar logs do Keycloak
docker compose logs -f keycloak

# 3. Aguardar a mensagem "Keycloak X.Y.Z on JVM..." nos logs

# 4. Acessar admin
# URL: http://localhost:8181
# Usuário: admin / senha: admin (ou do seu .env)

# 5. Verificar realm
# Admin → Realm selector → aion-logbook → deve existir

# 6. Verificar tema
# Realm Settings → Themes → Login Theme = aion-logbook

# 7. Abrir tela de login
# http://localhost:8181/realms/aion-logbook/protocol/openid-connect/auth?client_id=aion-logbook-web&response_type=code&scope=openid&redirect_uri=http://localhost:5173/

# 8. Criar usuário de teste
# Admin → Users → Add user → definir senha em Credentials

# 9. Testar login com usuário/senha criado

# 10. Se providers sociais configurados: botões devem aparecer na tela de login
```

---

## 15. Como Validar em Produção (Genesis-lab)

```bash
# 1. Ajustar .env com domínios de produção e secrets reais
# 2. Subir compose (ou pipeline CI)
docker compose -f docker-compose.yml up -d

# 3. Verificar https://auth.genesis-lab.dev/health/ready

# 4. Acessar admin: https://auth.genesis-lab.dev

# 5. Executar script de providers
KEYCLOAK_URL=https://auth.genesis-lab.dev \
KEYCLOAK_ADMIN_PASSWORD=<senha> \
GOOGLE_CLIENT_ID=<id> GOOGLE_CLIENT_SECRET=<secret> \
MICROSOFT_CLIENT_ID=<id> MICROSOFT_CLIENT_SECRET=<secret> \
bash docker/keycloak/scripts/configure-identity-providers.sh

# 6. Testar fluxo completo pelo frontend:
# https://aion.genesis-lab.dev → clicar em Login → redireciona para Keycloak
# → tela customizada Aion Logbook aparece → login social funciona
```

---

## 16. Troubleshooting

### Tema não aparece na tela de login

**Causa:** Keycloak não encontrou a pasta do tema.

```bash
# Verificar se o volume foi montado corretamente
docker exec aion-logbook-keycloak ls /opt/keycloak/themes/aion-logbook/

# Deve listar: login/

# Reiniciar container após montar volume pela primeira vez
docker compose restart keycloak
```

### Tela de login abre mas sem estilos Aion

**Causa:** O realm não tem `loginTheme=aion-logbook` aplicado, ou o cache do browser.

```bash
# Verificar configuração do realm
docker exec aion-logbook-keycloak \
  /opt/keycloak/bin/kcadm.sh get realms/aion-logbook \
  --server http://localhost:8080 --realm master \
  --user admin --password admin \
  | grep loginTheme

# Se vazio, aplicar:
docker exec aion-logbook-keycloak \
  /opt/keycloak/bin/kcadm.sh update realms/aion-logbook \
  -s loginTheme=aion-logbook \
  --server http://localhost:8080 --realm master \
  --user admin --password admin
```

Limpe o cache do browser ou use aba anônima.

### Botões sociais não aparecem

**Causa:** Providers desabilitados no realm (comportamento esperado sem credenciais).

Execute o script `configure-identity-providers.sh` com as variáveis de ambiente preenchidas. Providers com credenciais em branco ficam `enabled=false` pelo design.

### Erro "Invalid redirect_uri"

**Causa:** A URI de redirecionamento do frontend não está na lista do client.

Verifique o client `aion-logbook-web` no realm:
- Admin → Clients → aion-logbook-web → Valid Redirect URIs
- Deve conter a URI que o frontend está usando

### Apple: "Invalid redirect_uri" ou "invalid_request"

**Causa:** Apple não aceita `http://localhost`. Só funciona com HTTPS e domínio válido registrado no Apple Developer.

Use Google ou Microsoft para testes locais.

### Import do realm ignorado na segunda vez

**Comportamento esperado:** Keycloak não reimporta realms existentes. Para aplicar mudanças do JSON em um realm já existente:
1. Delete o realm via Admin UI ou API
2. Reinicie o container: `docker compose restart keycloak`

Ou aplique as mudanças via kcadm sem reimportar.

### Erro de CORS no frontend

**Causa:** Web Origins não configurado para a origem do frontend.

Verifique o client `aion-logbook-web` → Web Origins. Deve conter `http://localhost:5173`.

---

## 17. Como Não Vazar Secrets

**Regras:**

1. **Nunca comite `.env`** — está no `.gitignore`
2. **Nunca coloque secrets no `docker-compose.yml`** diretamente
3. **Nunca coloque secrets no realm JSON** — use valores vazios no JSON e o script kcadm
4. **Use `.env.example`** apenas com placeholders
5. **Em produção:** prefira Docker Secrets, Vault, ou variáveis de ambiente do CI/CD
6. **Rotacione** client secrets periodicamente nos painéis dos provedores
7. **A chave `.p8` da Apple** nunca pode ser baixada novamente — guarde em um secret manager

**Verificar antes de um commit:**
```bash
git diff --cached | grep -i 'secret\|password\|private_key\|client_secret'
```

---

## 18. Checklist por Ambiente

### Local dev

- [ ] `docker compose up -d` sobe sem erro
- [ ] Keycloak acessível em `http://localhost:8181`
- [ ] Realm `aion-logbook` existe no Admin
- [ ] Login Theme = `aion-logbook` nas configurações do realm
- [ ] Tela de login em `http://localhost:8181/realms/aion-logbook/account/` carrega com tema Aion
- [ ] Usuário local criado e login usuário/senha funciona
- [ ] Frontend em `http://localhost:5173` redireciona para Keycloak ao clicar em Login
- [ ] Após login, token recebido pelo frontend

### Genesis-lab / Produção

- [ ] Variáveis de ambiente configuradas (sem commitar)
- [ ] Keycloak acessível em `https://auth.genesis-lab.dev/health/ready`
- [ ] Redirect URIs de produção configuradas nos provedores externos
- [ ] Script `configure-identity-providers.sh` executado com sucesso
- [ ] Botões Google/Microsoft aparecem na tela de login
- [ ] Fluxo de login social completo testado
- [ ] Apple configurado (opcional, apenas se domínio HTTPS válido)
- [ ] Secrets não commitados no Git

### Novo ambiente futuro

- [ ] Definir domínio público do Keycloak
- [ ] Atualizar redirect URIs nos provedores (Google Console, Azure, Apple Developer)
- [ ] Preencher `.env` com credenciais do ambiente
- [ ] Subir compose
- [ ] Executar script de providers
- [ ] Validar tela de login e fluxo completo

---

## Checklist Aion Logbook — Keycloak

- [x] Tema customizado criado
- [x] Tema montado no Docker Compose
- [x] Realm import atualizado
- [x] Login theme definido como `aion-logbook`
- [x] Client `aion-logbook-web` configurado
- [x] Redirect URIs locais configuradas
- [x] Redirect URIs produção documentadas
- [x] Google documentado
- [x] Microsoft/Outlook documentado
- [x] Apple documentado
- [x] Secrets fora do Git
- [x] `.env.example` atualizado
- [x] Processo replicável por ambiente documentado
- [x] Troubleshooting documentado

---

## Resposta à Pergunta Central

> **Dá para deixar Google/Microsoft/Apple no compose para não configurar manualmente em outros ambientes?**

**Não completamente — mas quase.**

| Etapa | Automatizável |
|---|---|
| Subir Keycloak | Sim — `docker compose up` |
| Montar tema | Sim — volume no compose |
| Importar realm com providers (desabilitados) | Sim — `--import-realm` |
| Ativar providers com credenciais reais | Sim — `configure-identity-providers.sh` com env vars |
| Criar a aplicação no Google Cloud Console | **Não** — exige UI do Google |
| Criar o App Registration no Azure | **Não** — exige UI do Azure |
| Criar Service ID e chave no Apple Developer | **Não** — exige UI da Apple |

**Depois que as credenciais existem:** basta preencher o `.env` e rodar o script. A replicação entre ambientes é automatizada a partir daí.
