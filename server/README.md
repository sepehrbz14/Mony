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
| `SMS_IR_API_KEY` | _(empty)_ | SMS.ir API key used by backend OTP sending |
| `SMS_IR_TEMPLATE_ID` | `567011` | SMS.ir verify template ID (expects `CODE`) |
| `SMS_IR_BASE_URL` | `https://api.sms.ir/v1/` | SMS.ir API base URL |

## Endpoints

* `GET /health`
* `GET /expenses`
* `POST /expenses`
