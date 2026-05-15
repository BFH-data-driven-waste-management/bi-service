# BI Service

Backend service exposing analytics endpoints over the `analytics` and `analytics_derived` schemas produced by the [Data Foundation](https://github.com/BFH-data-driven-waste-management/data-foundation).

---
## Repository Structure

- `/http` - HTTP request collections for manual endpoint testing.
- `/src/main/java/ch/bfh/ddwm/biservice` - application code.
    - One package per feature: `bindetails`, `binlist`, `binmap`, `dashboard`, `tourdetails`, `toursoverview`. Each follows the same internal layout (`controller`, `service`, `repository`, `model`, `dto`).
    - `common` - shared modules and cross-cutting components (e.g. `api`, `config`, `dto`, `model`, `repository`, KPI calculation, date providers).
    - `BiServiceApplication` - Spring Boot entry point.
- `/src/main/resources`
    - `/db/migration` - Flyway migrations.
        - `/common` - DDL and schema definitions.
        - `/dev` - synthetic data migrations, generated from Data Foundation exports (see Prerequisites).
    - `application.yml`, `application-dev.yml` - Spring Boot configuration (base and `dev` profile).
- `docker-compose.yml` - Docker Compose definition for the database.
- `pom.xml` - Maven project definition (dependencies, plugins, build configuration).
- `mvnw`, `mvnw.cmd` - Maven wrappers.

---
## Execution model

Local execution is based on a Docker Compose service that initialises and starts a PostgreSQL database.
Further development execution is based on locally installed software.
Only the database is containerised.

---
## Prerequisites

### Operating System
The implementation runs on Ubuntu and macOS.
The following versions are tested:
- Ubuntu 22.04.3 LTS (WSL2)
- macOS 26.4.1

### Software
- Docker (engine version >=20)
- Docker Compose (version >=2)
- Java 21
- Maven 3.9+ (or use the bundled wrappers `mvnw` / `mvnw.cmd`)

### Data
The `common` migrations under `src/main/resources/db/migration/common` are version-controlled in this repository and define the schema.
The `dev` migrations under `src/main/resources/db/migration/dev` are not provided and contain synthetic data exported from the Data Foundation.
See the following section for instructions.

---
## Integrate synthetic data from the Data Foundation

1. Set up and run the [Data Foundation](https://github.com/BFH-data-driven-waste-management/data-foundation) as instructed in its README.md and USAGE.md.
2. Export each table of the `analytics` and `analytics_derived` schemas as a separate SQL file (multi-row `INSERT` statements) and place the files in `src/main/resources/db/migration/dev`.
3. Convert the exported files into Flyway-versioned migrations. On first run, make the script executable:
```bash
   chmod +x ./src/main/resources/db/migration/dev/convert
   ./src/main/resources/db/migration/dev/convert
```
The script renames each file to the `V{version}__{name}.sql` pattern expected by Flyway, assigning versions consistent with foreign-key dependencies between tables.

---
## Environment setup

### 1. Start the database container:
```bash
docker compose up -d
```
Expected result:
- The `bi-postgres` container is running (verify with `docker ps`), with status `Up`.

### 2. Apply the `common` (schema) and `dev` (synthetic data) migrations:
```bash
mvn flyway:migrate
```
Expected result:
- The `analytics` and `analytics_derived` schemas are populated with the tables and rows defined by the migration scripts under `src/main/resources/db/migration`. Verify by inspecting the database with any PostgreSQL client (e.g. IntelliJ's database tool). Connection URL: `jdbc:postgresql://localhost:5434/postgres`, with user and password both `postgres` (as defined in `docker-compose.yml`).

### 3. Generate jOOQ sources from the migrated schema:
```bash
mvn generate-sources
```
This step must be run after migrations so that generated sources reflect the current schema.

Expected result:
- `target/generated-sources/jooq/ch/bfh/ddwm/biservice/jooq/generated` contains the `analytics` and `analytics_derived` packages, each with the generated Java classes.

All Maven steps can also be run using the IntelliJ Maven plugin.

---
## Usage

Start the application with the `dev` profile active.
The most convenient option is an IDE run configuration; reload the Maven project beforehand if jOOQ sources were just regenerated.

Expected result:
- The application responds on a representative endpoint (e.g. `GET http://localhost:8080/api/bins/binmap`) with status `200 OK` and a corresponding JSON body. See `/http` for ready-to-use request collections, executable with any compatible HTTP client (e.g. IntelliJ).

---
## Additional information

- To reset the database and re-run the setup, remove the volume first:
```bash
docker compose down -v
docker compose up -d
mvn flyway:migrate
mvn generate-sources
```

---
## Authors

- Affolter Marco, [marco.affolter2@students.bfh.ch](mailto:marco.affolter.2@students.bfh.ch)
- Scherer Janic, [janic.scherer@students.bfh.ch](mailto:janic.scherer@students.bfh.ch)
- Scherer Luca, [luca.scherer@students.bfh.ch](mailto:luca.scherer@students.bfh.ch)

---
## License

Copyright (c) 2026 Affolter Marco, Scherer Janic, Scherer Luca. All rights reserved.

This repository is made available for academic, educational, and research purposes only. Commercial use, redistribution, sublicensing, hosted use, or use in production systems requires prior written permission from the copyright holders. See the [LICENSE](./LICENSE) file for details.