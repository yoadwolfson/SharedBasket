# Shared Basket Execution Split Plan

This plan keeps the required stack unchanged: Java, Spring Boot, Spring MVC, Spring Data JPA, Hibernate, Spring Security, REST API, JSF, HTML, CSS, JavaScript, and PostgreSQL.

## Team Split Rules
- Both Developer A and Developer B work as full-stack engineers.
- Work is split by exclusive feature ownership, not by layer and not by shared feature implementation.
- Each feature owner implements the feature end to end: REST API, service logic, persistence, validation, and the matching JSF screen.
- The non-owner only reviews contracts, tests the integration, and implements downstream dependent work.
- No feature should be built by both developers at the same time.
- Start each feature with an agreed API contract, then implement backend and JSF together under one owner.

## Feature Ownership
| Phase | Feature Area | Owner | What the Owner Delivers |
|---|---|---|---|
| Phase 1 | Core platform foundation | Developer A | Spring Boot setup, package structure, PostgreSQL config, base entities, DTOs, validation, and shared exception handling |
| Phase 1 | JSF application shell and navigation | Developer B | Page templates, navigation, layout, shared UI components, and frontend contract handling |
| Phase 2 | Authentication and profile management | Developer A | Registration, login, security, password hashing, profile APIs, and the matching JSF screens |
| Phase 3 | Group creation and membership management | Developer B | Group CRUD, invitation codes, join requests, approval/rejection flow, member removal, and group-admin screens |
| Phase 4 | Categories and shopping items | Developer A | Category CRUD, shopping item CRUD, purchased state, quantities, prices, and the matching JSF shopping screens |
| Phase 5 | Purchase history and reporting | Developer A | Purchase persistence, spending reports, history APIs, analytics services, and report screens |
| Phase 5 | Recommendations | Developer B | Recommendation engine, suggestion APIs, basket rebuild support, and recommendation UI |
| Phase 6 | Real-time collaboration | Developer B | Live updates, client refresh behavior, WebSocket or messaging UI integration, and conflict presentation |

## Execution Order
1. Build the core platform foundation.
2. Deliver authentication and profile management.
3. Deliver group creation and membership management.
4. Build the JSF shell in parallel with the backend features that depend on it.
5. Build categories and shopping items.
6. Build purchase history and reporting.
7. Build recommendations.
8. Add real-time collaboration and finish with full-system verification.

## Acceptance Criteria For The Whole Plan
- Every feature has one owner and one implementation path.
- Business logic stays in services.
- Controllers only validate and delegate.
- Repositories only persist data.
- JSF communicates with the backend only through REST APIs.
- PostgreSQL remains the database.
- Real-time collaboration prevents lost updates and conflicting edits.
- The final application covers the full blueprint without changing the stack or model intent.
