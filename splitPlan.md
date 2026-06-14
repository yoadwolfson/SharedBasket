# Shared Basket Execution Split Plan

This plan keeps the required stack unchanged: Java, Spring Boot, Spring MVC, Spring Data JPA, Hibernate, Spring Security, REST API, JSF, HTML, CSS, JavaScript, and PostgreSQL.

## Team Split Rules
- Developer A focuses on backend architecture, domain logic, security, persistence, concurrency, analytics, and integration contracts.
- Developer B focuses on JSF pages, client-side interaction, form flow, UI authorization states, and frontend integration with the REST APIs.
- Each phase has one primary owner and one support owner.
- No phase should start without the previous phase's contract being clear, even if parts can be built in parallel.
- Backend must expose stable REST endpoints before the JSF layer depends on them.

## Phase Overview
| Phase | Main Goal | Primary Owner | Support Owner | Control Point |
|---|---|---|---|---|
| Phase 1 | Foundation, architecture, project setup, base entities, shared contracts | Developer A | Developer B | Project compiles, packages, and exposes agreed API/model skeleton |
| Phase 2 | Authentication and user profile management | Developer A | Developer B | Registration, login, and profile flow works end to end |
| Phase 3 | Groups, invitations, join requests, and role control | Developer A | Developer B | Admin/member permissions and membership flows are enforced |
| Phase 4 | Categories and shopping item management | Developer B | Developer A | Shopping list CRUD works with validation and concurrency rules |
| Phase 5 | Purchase history, expense tracking, analytics, and recommendations | Developer A | Developer B | Reports and recommendation endpoints return stored-data results |
| Phase 6 | Real-time synchronization, frontend integration, testing, and hardening | Developer B | Developer A | Multi-user editing is synchronized and the full app is verified |

## Execution Order
1. Build the foundation and shared contracts first.
2. Lock authentication before adding group permissions.
3. Finish group membership before product operations.
4. Finish product operations before analytics and recommendations.
5. Add real-time sync and then validate the full system end to end.

## Acceptance Criteria For The Whole Plan
- The architecture remains layered and clean.
- Business logic stays in services.
- Controllers only validate and delegate.
- Repositories only persist data.
- JSF communicates with the backend only through REST APIs.
- PostgreSQL remains the database.
- Real-time collaboration prevents lost updates and conflicting edits.
- The final application covers the full blueprint without changing the stack or model intent.
