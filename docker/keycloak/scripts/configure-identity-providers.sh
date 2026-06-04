#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# configure-identity-providers.sh
#
# Configura identity providers (Google, Microsoft, Apple) no Keycloak via
# kcadm.sh, lendo credenciais de variáveis de ambiente.
#
# Uso:
#   docker exec aion-logbook-keycloak \
#     bash /opt/keycloak/scripts/configure-identity-providers.sh
#
# Ou externamente (ajuste KEYCLOAK_URL para o endereço acessível):
#   KEYCLOAK_URL=http://localhost:8181 \
#   KEYCLOAK_ADMIN=admin \
#   KEYCLOAK_ADMIN_PASSWORD=admin \
#   GOOGLE_CLIENT_ID=xxx \
#   GOOGLE_CLIENT_SECRET=yyy \
#   bash configure-identity-providers.sh
#
# Pré-requisito: realm 'aion-logbook' já importado (compose --import-realm).
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

# ── Variáveis com defaults ──────────────────────────────────────────────────
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8181}"
KEYCLOAK_ADMIN="${KEYCLOAK_ADMIN:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-aion-logbook}"
KCADM="/opt/keycloak/bin/kcadm.sh"

# ── Funções helpers ─────────────────────────────────────────────────────────
log()  { echo "[aion-kc] $*"; }
warn() { echo "[aion-kc] WARN: $*" >&2; }

wait_keycloak() {
  log "Aguardando Keycloak ficar disponível em $KEYCLOAK_URL ..."
  until curl -sf "$KEYCLOAK_URL/health/ready" > /dev/null 2>&1; do
    sleep 3
  done
  log "Keycloak disponível."
}

auth_kcadm() {
  log "Autenticando no Keycloak admin ..."
  "$KCADM" config credentials \
    --server "$KEYCLOAK_URL" \
    --realm master \
    --user "$KEYCLOAK_ADMIN" \
    --password "$KEYCLOAK_ADMIN_PASSWORD"
  log "Autenticado."
}

provider_exists() {
  local alias="$1"
  "$KCADM" get identity-provider/instances/"$alias" \
    -r "$KEYCLOAK_REALM" > /dev/null 2>&1
}

# ── Início ──────────────────────────────────────────────────────────────────
wait_keycloak
auth_kcadm

# ── Google ──────────────────────────────────────────────────────────────────
if [ -n "${GOOGLE_CLIENT_ID:-}" ] && [ -n "${GOOGLE_CLIENT_SECRET:-}" ]; then
  log "Configurando Google ..."
  if provider_exists "google"; then
    "$KCADM" update identity-provider/instances/google \
      -r "$KEYCLOAK_REALM" \
      -s enabled=true \
      -s 'config.clientId='"$GOOGLE_CLIENT_ID" \
      -s 'config.clientSecret='"$GOOGLE_CLIENT_SECRET"
    log "Google atualizado."
  else
    "$KCADM" create identity-provider/instances \
      -r "$KEYCLOAK_REALM" \
      -s alias=google \
      -s displayName=Google \
      -s providerId=google \
      -s enabled=true \
      -s trustEmail=true \
      -s firstBrokerLoginFlowAlias="first broker login" \
      -s 'config={"clientId":"'"$GOOGLE_CLIENT_ID"'","clientSecret":"'"$GOOGLE_CLIENT_SECRET"'","defaultScope":"openid profile email","useJwksUrl":"true","syncMode":"IMPORT"}'
    log "Google criado."
  fi
else
  warn "GOOGLE_CLIENT_ID ou GOOGLE_CLIENT_SECRET não definidos — Google ignorado."
fi

# ── Microsoft ───────────────────────────────────────────────────────────────
if [ -n "${MICROSOFT_CLIENT_ID:-}" ] && [ -n "${MICROSOFT_CLIENT_SECRET:-}" ]; then
  MICROSOFT_TENANT="${MICROSOFT_TENANT_ID:-common}"
  log "Configurando Microsoft (tenant: $MICROSOFT_TENANT) ..."
  if provider_exists "microsoft"; then
    "$KCADM" update identity-provider/instances/microsoft \
      -r "$KEYCLOAK_REALM" \
      -s enabled=true \
      -s 'config.clientId='"$MICROSOFT_CLIENT_ID" \
      -s 'config.clientSecret='"$MICROSOFT_CLIENT_SECRET" \
      -s 'config.tenantId='"$MICROSOFT_TENANT"
    log "Microsoft atualizado."
  else
    "$KCADM" create identity-provider/instances \
      -r "$KEYCLOAK_REALM" \
      -s alias=microsoft \
      -s displayName=Microsoft \
      -s providerId=microsoft \
      -s enabled=true \
      -s trustEmail=false \
      -s firstBrokerLoginFlowAlias="first broker login" \
      -s 'config={"clientId":"'"$MICROSOFT_CLIENT_ID"'","clientSecret":"'"$MICROSOFT_CLIENT_SECRET"'","tenantId":"'"$MICROSOFT_TENANT"'","defaultScope":"openid profile email","useJwksUrl":"true","syncMode":"IMPORT"}'
    log "Microsoft criado."
  fi
else
  warn "MICROSOFT_CLIENT_ID ou MICROSOFT_CLIENT_SECRET não definidos — Microsoft ignorado."
fi

# ── Apple ───────────────────────────────────────────────────────────────────
# ATENÇÃO: Apple exige:
# - Service ID (clientId)
# - Team ID
# - Key ID
# - Chave privada .p8 (conteúdo completo, incluindo header/footer PEM)
# - O client_secret JWT é gerado pelo Keycloak a partir dos campos acima
#
# Limitação: Apple só aceita redirect URIs HTTPS com domínio válido.
# Não funciona com http://localhost — use apenas em produção.

if [ -n "${APPLE_CLIENT_ID:-}" ] && [ -n "${APPLE_TEAM_ID:-}" ] && \
   [ -n "${APPLE_KEY_ID:-}" ]   && [ -n "${APPLE_CLIENT_SECRET:-}" ]; then
  log "Configurando Apple ..."
  if provider_exists "apple"; then
    "$KCADM" update identity-provider/instances/apple \
      -r "$KEYCLOAK_REALM" \
      -s enabled=true \
      -s 'config.clientId='"$APPLE_CLIENT_ID" \
      -s 'config.teamId='"$APPLE_TEAM_ID" \
      -s 'config.keyId='"$APPLE_KEY_ID" \
      -s 'config.privateKey='"$APPLE_CLIENT_SECRET"
    log "Apple atualizado."
  else
    "$KCADM" create identity-provider/instances \
      -r "$KEYCLOAK_REALM" \
      -s alias=apple \
      -s displayName=Apple \
      -s providerId=apple \
      -s enabled=true \
      -s trustEmail=true \
      -s firstBrokerLoginFlowAlias="first broker login" \
      -s 'config={"clientId":"'"$APPLE_CLIENT_ID"'","teamId":"'"$APPLE_TEAM_ID"'","keyId":"'"$APPLE_KEY_ID"'","privateKey":"'"$APPLE_CLIENT_SECRET"'","defaultScope":"openid name email","syncMode":"IMPORT"}'
    log "Apple criado."
  fi
else
  warn "APPLE_CLIENT_ID, APPLE_TEAM_ID, APPLE_KEY_ID ou APPLE_CLIENT_SECRET não definidos — Apple ignorado."
fi

log "Configuração de identity providers concluída."
