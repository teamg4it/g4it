---
workflow: feature-implementation
trigger: user
date: '2026-11-20'
status: validated
trace_id: e6fa33d4-27cf-4d12-84a2-3c70d47d88da
station: constitution
agent: spec-orchestrator (substitute executor, user-approved)
skill: spec-constitution
inputDocuments:
  - imputs/feature-g4it-48.txt
  - services/backend/pom.xml
  - services/frontend/package.json
  - .github/workflows/backend-build-test-analyze.yml
  - .github/workflows/frontend-build-test-analyze.yml
changeHistory:
  - date: '2026-11-20'
    description: 'Initial brownfield constitution for G4IT (context: feature G4IT-48)'
    changes:
      - 'Derived stack, architecture and conventions from codebase evidence'
      - 'Filled all minimum coverage sections of the constitution template'
  - date: '2026-11-20'
    description: 'Applied user decisions from constitution checkpoint'
    changes:
      - 'DAST before release: not mandatory'
      - 'Distributed tracing: not required (logs and metrics suffice)'
      - 'Confirmed WCAG 2.2 AA, API p95 < 500ms, coverage 80% new code / 100% impact paths'
holisticQualityRating: not-rated
overallStatus: approved
---
<!-- AI_DISCLAIMER v1.0 -->
# Project Constitution: G4IT (Green for IT)

> This file has been created (totally or partially) with the assistance of artificial intelligence tools.
> All content has been generated under the direct supervision of a named individual,
> and under the AI.Backbone Orchestrator Compliance framework

**Created**: 2026-11-20
**Status**: Validated
**Version**: 1.0.0
**Scope**: App-wide (brownfield). First consumer: feature G4IT-48 — Inventories: BYOD/COPE equipment usage allocation.

Value source legend: `code` = detected in repository, `asked` = to confirm with the team, `confirmed` = confirmed by the team.

---

## 1. Architectural Preferences

### Technology Stack

- **Backend**: Java 21, Spring Boot 4.0.x (parent POM), Maven, Lombok, MapStruct (`code`)
- **Frontend**: Angular 21, TypeScript, Karma/Jasmine unit tests, Cypress E2E (`code`)
- **Database**: PostgreSQL, schema managed by Liquibase changelogs under `services/backend/src/main/resources/db` (`code`)
- **Identity**: Keycloak (dedicated service `services/keycloak`) (`code`)
- **Infrastructure**: Docker images per service (backend, frontend, documentation, keycloak); Azure profile (`application-azure.yml`) (`code`)
- **Documentation site**: Hugo (`services/documentation`) (`code`)

### Architecture Style

- [ ] Monolith
- [x] Modular monolith — single Spring Boot backend split into `api*` functional modules (e.g. `apiinventory`, `apievaluating`, `apiindicator`, `apireferential`) plus `common`, `config`, `exception`, `external`
- [ ] Microservices
- [ ] Serverless
- [x] Hybrid — separate SPA frontend, IdP and docs containers

### Design Principles

- Single Responsibility per `api*` module; cross-cutting code only in `common`/`config`/`exception`
- API-first: REST contracts defined in OpenAPI (`src/main/resources/swagger`) and generated with `openapi-generator-maven-plugin`; controllers implement generated interfaces
- Layering: generated API → controller/delegate → business service → repository (JPA); DTO ↔ entity mapping via MapStruct mappers
- Schema changes ONLY through new, append-only Liquibase changesets — never edit applied changesets
- Environmental impact computations follow published methodologies (e.g. RCP SI); allocation rules (such as BYOD/COPE professional-use fractions) are explicit, documented, and default-driven with optional override
- Changes extend existing modules (e.g. inventory/evaluating) rather than creating parallel structures

---

## 2. Quality Expectations

### Code Quality Standards

- Follow existing package and naming conventions of the touched module
- Self-documenting code; Javadoc/TSDoc for public APIs and non-trivial calculation logic
- SonarQube analysis in CI (`sonar-project.properties`, `*-analyze-main.yml`); no new blocker/critical issues
- Cyclomatic complexity SHOULD NOT exceed 10 per method
- i18n: user-facing backend messages in `messages.properties` / `messages_fr.properties`; frontend labels translated (EN/FR)

### Code Review

- Minimum one peer review before merge; CI build/test/analyze must be green first

---

## 3. Security Posture

### Authentication & Authorization

- OAuth 2.0 / OIDC via Keycloak; backend secured as resource server (a `nosecurity` profile exists for local dev only — never in deployed environments)
- Organization/workspace-scoped authorization enforced on every new endpoint, consistent with existing `apiadministrator` / `apiuser` patterns
- Secrets via environment variables / platform secret store; never committed

### Data Protection

- No PII in logs; GDPR applies (EU users)
- Input validation on all API payloads (bean validation on generated DTOs)

### Security Testing

- OWASP dependency check (`backend-dep-check.yml`, frontend `dependency_check_suppressions.xml`) and licence checks (`license_finder_decisions.yml`)
- SAST through Sonar; DAST before release is not mandatory (`confirmed`)

---

## 4. Testing Expectations

### Coverage Targets

| Level | Target |
|-------|--------|
| Unit tests (backend, JUnit + JaCoCo) | ≥ 80% on new code; 100% on impact allocation/calculation paths (`confirmed`) |
| Unit tests (frontend, Karma/Jasmine) | ≥ 80% on new components/services |
| Integration tests | All new/changed REST endpoints and Liquibase migrations (PostgreSQL) |
| E2E tests (Cypress) | Core inventory journeys touched by the feature |

### Testing Discipline

- Tests written alongside/before implementation; must run in CI (`backend-build-test-analyze.yml`, `frontend-build-test-analyze.yml`)
- Deterministic, isolated tests; Arrange-Act-Assert / Given-When-Then naming
- Regression tests mandatory when defaults change (e.g. equipment without usage context keeps 100% allocation)

---

## 5. Observability Expectations

- Logging via SLF4J (Lombok `@Slf4j`); correlation-friendly, no PII
- Spring Boot Actuator health/metrics endpoints enabled
- Business audit events recorded through the existing `auditevent` module where user actions change inventory data
- Distributed tracing: not required; application logs and metrics suffice (`confirmed`)

---

## 6. Compatibility Constraints

### Browser Support

- Last 2 versions of Chrome, Firefox, Edge, Safari; WCAG 2.2 AA for new UI (`confirmed`)

### API Compatibility

- Backward-compatible changes only: new fields optional with sensible defaults; breaking changes require versioning and at least one release of deprecation notice
- CSV load/export formats (`csv-headers.yml`, `apiloadinputfiles`, `apiinout`): new columns optional; existing files must still load unchanged
- Existing evaluations/results remain reproducible; data migrations default new attributes to prior behaviour

### Integration Constraints

- Must not break external impact-calculation integrations (`external`), referential data (`apireferential`), shared links and exports

---

## 7. Documentation Discipline

- Update OpenAPI specs for any contract change (source of truth)
- Update Hugo user documentation (`services/documentation/content`) for functional changes, incl. methodology notes (RCP SI allocation)
- Record notable changes in `CHANGELOG.md`
- ADRs for significant architectural or methodology decisions; Mermaid/C4 diagrams

---

## 8. Performance Requirements

| Category | Target |
|----------|--------|
| API CRUD endpoints | p95 < 500ms (`confirmed`) |
| Inventory evaluation batch | No regression > 20% vs. baseline |
| UI Time to Interactive | < 3s |

- Allocation factors applied in-computation without extra per-equipment remote calls

---

## 9. Development Workflow

### Pre-Commit Gates

- [ ] Unit tests pass locally
- [ ] Code formatted/linted
- [ ] No new known vulnerabilities

### Pre-Merge Gates

- [ ] GitHub Actions build/test/analyze pass (backend & frontend)
- [ ] Sonar quality gate passed
- [ ] Peer review approved
- [ ] OpenAPI, i18n and docs updated where applicable

### Pre-Release Gates

- [ ] Integration and Cypress E2E pass
- [ ] Dependency/licence checks clean
- [ ] Accessibility check on changed UI
- [ ] Docker images built and published

---

## Governance

- **Amendment process**: Written proposal → team review → consensus → migration plan
- **Versioning**: MAJOR.MINOR.PATCH (semantic versioning)
- **Compliance review**: During code reviews, retrospectives, and pre-release
