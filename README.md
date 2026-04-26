# DSS Service

## Dev Setup

### Prerequisites

- Java 21
- Docker
- Maven 3.9+ (or use wrappers `mvnw`/`mvnw.cmd`)

### Linux / macOS / Windows

#### 1) DB setup with Docker Compose 
- See `src/main/resources/docker/docker-compose.yml`
- Ensure docker daemon is running
```bash
docker compose up -d
```
(alternatively via IntelliJ with Docker plugin)

#### 2) Fetch migration scripts for synthetic data (from OneDrive) and place them in `src/main/resources/db/migration/dev`

#### 3) Migrate both common (DDL/schema) and dev (synthetic data) migrations from `src/main/resources/db/migration`
```bash
mvn -Pdev flyway:migrate
```
(alternatively via IntelliJ with Maven plugin)

#### 4) Generate jOOQ sources from the current database schema 
- Must be run after migrations to reflect the latest schema
```bash
mvn -Pdev generate-sources
```
(alternatively via IntelliJ with Maven plugin)

#### 5) Start app
- Sync/Reload Maven if necessary (catches generated sources for jOOQ)
- Use IDE run configuration with active `dev` profile and start (most convenient)

### All-in-one
```bash
docker compose down -v &&
docker compose up -d &&
mvn -Pdev flyway:migrate &&
mvn -Pdev generate-sources
```