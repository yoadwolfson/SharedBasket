# Developer A Execution Plan

Developer A works as a full-stack contributor across the entire Shared Basket project. This plan is organized by the phases where Developer A is the phase lead or the main technical owner, while still covering the frontend and backend work needed in every stage.

## Operating Rule
- Developer A owns the full implementation of the features listed below.
- Each owned feature must be delivered end to end: REST API, service logic, persistence, validation, and the matching JSF screen.
- Developer B only consumes the exposed contracts for dependent work and does not co-implement these features.
- Feature ownership is exclusive so the team works like a startup with clear responsibility boundaries.
- Developer B owns group management, recommendations, the JSF shell, and real-time collaboration.

## Phase 1 - Core Platform Foundation V
- Create the Spring Boot project structure, package layout, and PostgreSQL configuration.
- Implement the domain entities, repositories, DTOs, mapper interfaces, validation rules, and shared exception handling.
- Define the REST contract shape and the backend conventions the JSF layer will consume.
- Build the base JSF integration contract so frontend pages can reuse the shared DTO and error patterns.
- Verify the application starts cleanly and the database mapping is stable.

Control Point: the application starts cleanly, PostgreSQL connects, and the base model and API contract are stable.

## Phase 2 - Authentication and Profile Management V
- Build the registration feature end to end with the REST API, service logic, persistence rules, and the JSF registration screen.
- Build the login feature end to end with authentication rules, password hashing, REST endpoints, and the JSF login screen.
- Build the profile-management feature end to end with the REST API, service logic, persistence rules, and the JSF profile screen.
- Provide backend validation, session handling, and user-state rules for these screens.
- Verify the frontend flows match the backend responses and error handling.

Control Point: registration, login, and profile updates work end to end without exposing plain-text passwords.

## Phase 4 - Categories and Shopping Items
- Build the category-management feature end to end with the REST API, service rules, persistence, and the JSF category screen.
- Build the shopping-item feature end to end with create, update, delete, quantity change, price change, purchased-state logic, and the JSF shopping-list screens.
- Add optimistic locking or equivalent concurrency protection to prevent lost updates.
- Verify the frontend receives clear conflict responses and refresh guidance.

Control Point: shopping items and categories work without silent overwrites or invalid state.

## Phase 5 - Purchase History and Reporting
- Build the purchase-history feature end to end, including persistence, REST API, service logic, and the JSF history view.
- Build the expense-reporting feature end to end, including spending totals, category totals, product totals, and budget-vs-actual calculations.
- Build the report screens that consume these APIs and present summary data clearly.
- Ensure the outputs come only from stored shopping data.

Control Point: reports, statistics, and purchase records match stored history and business rules.

## Phase 6 - Backend Hardening and API Verification
- Validate the REST API contracts against the JSF integration points.
- Finalize conflict-prevention and consistency rules for simultaneous edits where they touch owned backend data.
- Complete backend test coverage, security checks, and production hardening for the owned features.
- Support integration verification for the full application without taking ownership of Developer B's features.

Control Point: backend behavior is production-ready and each owned feature works end to end.

## Developer A Deliverables
- Stable backend architecture and service-layer logic.
- Security, concurrency, and reporting behavior.
- Working REST APIs for the frontend to consume.
- Final verification that business rules are correctly enforced server-side.
