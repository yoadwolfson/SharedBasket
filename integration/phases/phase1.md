# Phase 1 - Foundation, Architecture, and Shared Contracts

This phase creates the technical base for the whole project. The team sets up the Spring Boot application, layered package structure, PostgreSQL connection, base entity model, shared DTOs, exception handling, validation rules, logging, and the first version of the REST contract. No feature work should move ahead until the core structure is stable and agreed.

Tools used in this phase are Java, Spring Boot, Spring MVC, Spring Data JPA, Hibernate, Spring Security setup scaffolding, JSF project shell, HTML, CSS, JavaScript, and PostgreSQL configuration.

## Sub-stages
### 1.1 Project setup
- Create the package structure for config, controller, service, repository, entity, dto, mapper, security, exception, validation, and util.
- Configure application properties for PostgreSQL and environment separation.
- Add the basic build and dependency setup.

### 1.2 Domain skeleton
- Define the core entities and relationships from the blueprint.
- Establish IDs, timestamps, and base audit conventions.
- Prepare repository interfaces without business logic.

### 1.3 API contract foundation
- Define the initial DTOs and request/response shapes.
- Prepare a consistent error response format.
- Agree on endpoint naming conventions before feature implementation starts.

### 1.4 Shared quality base
- Add validation rules, logging strategy, and exception mapping.
- Create common mapper conventions.
- Prepare the base security configuration without completing authentication flows yet.

## Work split
- Both Developer A and Developer B work full-stack on this phase.
- Phase Lead: Developer A — Co-Lead: Developer B.
- Sub-stage ownership:
	- 1.1 Project setup: Lead Developer A, Co-Lead Developer B (frontend project shell, build alignment).
	- 1.2 Domain skeleton: Lead Developer A, Co-Lead Developer B (entity review and DTO alignment).
	- 1.3 API contract foundation: Joint design; Developer A leads endpoints, Developer B leads example UI contracts.
	- 1.4 Shared quality base: Joint implementation; Developer B leads JSF shell checks and Developer A leads security/validation plumbing.

## Control checks
- The application starts cleanly.
- PostgreSQL connection works.
- Entities map correctly.
- Base REST endpoints and error handling behave consistently.
- Frontend shell renders without depending on unfinished business features.
