# Aion Logbook — Local Infra

Infra local com PostgreSQL e Keycloak.

## Subir

```bash
cp .env.example .env
docker compose up -d
```

## Validar

```bash
docker compose ps
docker compose logs -f keycloak
docker compose logs -f postgres
```

Keycloak:
- http://localhost:8181
- Admin: admin/admin
- Realm: aion-logbook
- Client: aion-logbook-web

PostgreSQL:

```bash
docker exec -it aion-logbook-postgres psql -U aion_user -d aion_logbook -c "select current_database(), current_user;"
```
