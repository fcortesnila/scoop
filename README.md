# Scoop

A mobile-first ice-cream truck ordering and operator app. The backend is Spring Boot 3 / Java 21 with MariaDB and Flyway; the frontend is React + Vite.

## Run with Docker Compose

1. Copy `.env.example` to `.env` and set unique MariaDB passwords, a Google OAuth client ID, and the comma-separated operator email allow-list.
2. In Google Cloud Console, create an **OAuth client ID** of type **Web application**. Add `http://localhost:5173` to **Authorized JavaScript origins**. Put its client ID in `GOOGLE_CLIENT_ID`.
3. Start the stack from the repository root:

   ```sh
   docker compose up --build
   ```

4. Open [http://localhost:5173](http://localhost:5173). Flyway creates the schema and loads starter flavors, ingredients, stops, and historical orders on the first database start.

Google ID tokens are validated by the API against Google's issuer and the configured client ID. Users in `OPERATOR_EMAILS` receive operator access; other verified Google accounts are customers. The Claude-powered insights are optional; set `IA_API_KEY` to enable recommendations. Numeric inventory alerts and historical route suggestions work without it.

## Local development

Start MariaDB with `docker compose -f scoop-api/docker-compose.yml up -d`, then run the API from `scoop-api` with `mvn spring-boot:run` (Java 21 and Maven required). Set `GOOGLE_CLIENT_ID`, `OPERATOR_EMAILS`, and optionally `IA_API_KEY` in the environment. The API listens on port 8080.

Run the web app with Node 20 or newer:

```sh
cd scoop-web
npm ci
npm run dev
```

Set `VITE_GOOGLE_CLIENT_ID` in the frontend build environment for Google sign-in. Vite serves the UI on port 5173 and proxies `/api` requests to `http://localhost:8080`.

The demo operator button is available only in the Vite development build. To enable the API demo login, activate the Spring `demo` profile; do not enable that profile in a public deployment.

## Configuration

| Variable | Purpose |
| --- | --- |
| `MARIADB_DATABASE`, `MARIADB_USER`, `MARIADB_PASSWORD`, `MARIADB_ROOT_PASSWORD` | MariaDB database and credentials |
| `GOOGLE_CLIENT_ID` | Google OAuth web client ID; also used as the ID-token audience |
| `OPERATOR_EMAILS` | Comma-separated verified Google email addresses granted operator access |
| `IA_API_KEY` | Optional Claude API key for restock and route recommendations |
| `CLAUDE_MODEL` | Claude model used for AI text features |
| `SERVICE_END_HOUR` | Local service-day end hour (0–23) for stockout alerts |
| `CORS_ORIGIN` | Allowed browser origin for the API |

## Tests

Backend unit and MariaDB Testcontainers integration tests live under `scoop-api/src/test`. Run them with `mvn test` from `scoop-api`; Docker must be running for Testcontainers. Build-check the UI with `npm ci && npm run build` from `scoop-web`.
