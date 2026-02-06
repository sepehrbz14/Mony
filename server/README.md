# Mony Ktor Server

This module provides a simple Ktor backend with Postgres, Exposed, and Flyway.

## Run locally

1. Start Postgres:
   ```bash
   docker compose -f server/docker-compose.yml up -d
   ```

2. Run the server:
   ```bash
   ./gradlew :server:run
   ```

## Environment variables

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/mony` | JDBC URL |
| `DB_USER` | `mony` | DB username |
| `DB_PASSWORD` | `mony` | DB password |

## Endpoints

* `GET /health`
* `GET /expenses`
* `POST /expenses`
