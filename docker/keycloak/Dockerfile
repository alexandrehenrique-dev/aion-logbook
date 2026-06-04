FROM quay.io/keycloak/keycloak:26.0.7

# Copia o tema customizado para dentro da imagem
# Recomendado para ambientes de produção onde volumes não são usados
COPY themes/aion-logbook /opt/keycloak/themes/aion-logbook

# Copia o realm import (opcional — o compose já monta via volume)
# COPY import /opt/keycloak/data/import
