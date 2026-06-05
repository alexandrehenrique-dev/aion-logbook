FROM node:22-alpine AS frontend-build

WORKDIR /app

COPY frontend/package*.json ./frontend/
COPY frontend/scripts ./frontend/scripts
COPY frontend/public ./frontend/public
COPY frontend/src ./frontend/src
COPY frontend/index.html ./frontend/
COPY frontend/tsconfig*.json ./frontend/
COPY frontend/vite.config.ts ./frontend/

COPY backend ./backend

WORKDIR /app/frontend

ARG VITE_API_MODE=real
ARG VITE_API_BASE_URL=/logbook/api/v1
ARG VITE_AUTH_MODE=keycloak
ARG VITE_KEYCLOAK_ENABLED=true
ARG VITE_KEYCLOAK_URL
ARG VITE_KEYCLOAK_REALM=aion-logbook
ARG VITE_KEYCLOAK_CLIENT_ID=aion-logbook-web
ARG VITE_ENABLE_NOTIFICATIONS=false
ARG VITE_KEYCLOAK_PKCE_ENABLED=false

ENV VITE_KEYCLOAK_PKCE_ENABLED=${VITE_KEYCLOAK_PKCE_ENABLED}
ENV VITE_API_MODE=${VITE_API_MODE}
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_AUTH_MODE=${VITE_AUTH_MODE}
ENV VITE_KEYCLOAK_ENABLED=${VITE_KEYCLOAK_ENABLED}
ENV VITE_KEYCLOAK_URL=${VITE_KEYCLOAK_URL}
ENV VITE_KEYCLOAK_REALM=${VITE_KEYCLOAK_REALM}
ENV VITE_KEYCLOAK_CLIENT_ID=${VITE_KEYCLOAK_CLIENT_ID}
ENV VITE_ENABLE_NOTIFICATIONS=${VITE_ENABLE_NOTIFICATIONS}

ARG VITE_BASE_PATH=/logbook/
ENV VITE_BASE_PATH=${VITE_BASE_PATH}

RUN npm ci
RUN npm run build:backend


FROM maven:3.9.9-eclipse-temurin-21 AS backend-build

WORKDIR /app/backend

COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

COPY backend ./

COPY --from=frontend-build /app/backend/src/main/resources/static ./src/main/resources/static

RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=backend-build /app/backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]