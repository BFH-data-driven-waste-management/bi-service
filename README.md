# DSS Backend

## Dev Setup

### Prerequisites

- Java 21
- Docker
- Maven 3.9+ (or just use wrapper like below)

### Linux / macOS

#### 1) DB setup with Docker Compose (`src/main/resources/docker/docker-compose.yml`)

```bash
docker compose up -d # (or via IntelliJ with Docker plugin)
```

#### 2) Migrate both common (DDL/schema) and dev (synthetic data) migrations from `src/main/resources/db/migration`.

```bash
./mvnw -Pdev flyway:migrate # (or via IntelliJ with Maven plugin)
```

#### 3) Generate jOOQ sources from the current database schema, so run this **after** Flyway.

```bash
./mvnw generate-sources # (or via IntelliJ with Maven plugin)
```

#### 4) Start app
Save IDE run configuration with active `dev` profile and start (or via CLI but less convenient).

### Windows

```powershell
docker compose up -d
```
```powershell
.\mvnw.cmd -Pdev flyway:migrate
```
```powershell
.\mvnw.cmd generate-sources
```

### DB reset

```bash
docker compose down -v
```
```bash
docker compose up -d
```
