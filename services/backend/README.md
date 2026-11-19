# Things to know to happily work on this project

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
