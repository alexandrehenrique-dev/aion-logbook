-- Cria banco dedicado para o Keycloak no mesmo Postgres da aplicação.
-- Executado automaticamente na primeira subida do container postgres.
SELECT 'CREATE DATABASE keycloak_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak_db')\gexec
