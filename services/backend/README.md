<!-- AI_DISCLAIMER v1.0 -->
# Things to know to happily work on this project

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

## Getting started

The backend is a Spring Boot application. From this directory, start it with:

```bash
mvn -s settings.xml spring-boot:run
```

The application listens on `http://localhost:8080/`. Make sure the required local
services and application configuration are available before starting it.

For the complete local setup, including running the frontend with a real or mocked
backend and starting the required services, see the [frontend README](../frontend/README.md)
and the [Docker deployment guide](../../DOCKER-DEPLOYMENT.md).

### Software requirements

- Java 21
- Maven 3.9 or later
- PostgreSQL (the default configuration points to `localhost:5432`)

The project uses Maven dependencies from a configured package repository. Use
`settings.xml` when resolving dependencies or running Maven commands locally.

### Build

```bash
mvn -s settings.xml clean package
```

### Tests

Run the unit and integration test suite with:

```bash
mvn -s settings.xml verify
```

Test reports are generated under `target/surefire-reports/`; JaCoCo coverage
reports are generated under `target/site/jacoco/`.

### PostgreSQL integration tests

Tests tagged `postgres-integration` are excluded from the default build. They
require a local PostgreSQL container running and configured in
`src/test/resources/application-integration-test.yml`.

From this directory, run them with the `integration-test` Maven profile:

```bash
mvn -P integration-test test
```

To run a single test class, for example:

```bash
mvn -P integration-test -Dtest=PhysicalEquipmentPostgresMigrationTest -Dsurefire.failIfNoSpecifiedTests=false test
```

These tests create isolated `g4it48_t_*` schemas and drop them afterwards.

### G4IT-48 live-stack check

With the local application and its dependencies running, execute from this directory:

```bash
bash e2e/g4it48-live-e2e.sh
```

The script loads `G4IT_E2E_USER` and `G4IT_E2E_PASSWORD` from
`workspace/docker/.env` at the repository root, independently of the current
working directory. Existing non-empty environment values take precedence.
`G4IT_E2E_ENV_FILE` can select another account configuration file.

Account values may be unquoted or enclosed in single or double quotes. Blank
lines, comments, an optional `export` prefix and CRLF line endings are supported.
Values are literal: shell expansion, escape processing and multiline values are
not supported. Only the two account keys are loaded; the file is never executed
as shell code, and account values are not printed.

Test this configuration loading with synthetic data and no network calls:

```bash
python3 -m unittest discover -s e2e -p test_g4it48_config.py
```

#### Synthetic Smartphone factors in tests

The default unit tests use H2 or mocked referential data and do not require a
local reference-data fixture. The PostgreSQL integration tests apply
[`g4it48-local-reference.sql`](src/test/resources/sql/g4it48-local-reference.sql)
automatically in the fixture-specific test cases, after creating an isolated
schema and applying the required referential Liquibase changelogs. The fixture
is test-only and does not modify the running application's database.

With the local PostgreSQL test service configured and running, execute the
fixture tests from this directory:

```bash
mvn -P integration-test -Dtest=PhysicalEquipmentPostgresMigrationTest test
```

The tests create and drop their own `g4it48_t_*` schemas. No separate `psql`
fixture-loading or cleanup step is needed. The factors are synthetic, explicitly
labelled and unsuitable for environmental reporting; the fixture is not part of
the production Liquibase changelog.

The live-stack script above uses the already-running application and is separate
from these isolated tests: it does not apply the fixture to that application's
database. A completed evaluation task alone does not prove that numerical impact
rows were calculated successfully.
