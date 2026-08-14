---
goal: Stabilize and complete login implementation across REST, Spring Security, and JSF shell flows
version: 2.0
date_created: 2026-08-12
last_updated: 2026-08-13
owner: SharedBasket / Developer A
status: 'In Progress'
tags: ['feature', 'authentication', 'login', 'phase2', 'jsf', 'spring-security']
---

# Introduction

![Status: In progress](https://img.shields.io/badge/status-In%20progress-yellow)

This plan defines the exact remaining implementation work to make login deterministic and fully functional in the current branch. It is based on current repository state, failing integration tests, and duplicated authentication logic discovered in the codebase.

Verification update (2026-08-13): REST login is implemented in src/main/java/com/sharedbasket/controller/AuthController.java via POST /api/auth/login, and JSF login now reuses that endpoint logic through AuthController from src/main/java/com/sharedbasket/controller/jsf/LoginBean.java. Direct credential verification in LoginBean has been removed.
Verification update (2026-08-13): JSF register and profile flows now reuse existing REST controller logic from src/main/java/com/sharedbasket/controller/AuthController.java and src/main/java/com/sharedbasket/controller/ProfileController.java. Direct repository-level implementation in JSF beans has been removed.

## 1. Requirements & Constraints

- **REQ-001**: Login must succeed through POST /api/auth/login and allow immediate access to protected endpoints GET /api/auth/me and PUT /api/profile in the same session.
- **REQ-002**: JSF login flow on src/main/webapp/login.xhtml must authenticate a user and redirect to src/main/webapp/dashboard.xhtml without manual refresh.
- **REQ-003**: Session-backed authentication must be represented in one canonical mechanism consumed by Spring Security and JSF.
- **REQ-004**: Existing REST contracts in src/main/java/com/sharedbasket/controller/AuthController.java and src/main/java/com/sharedbasket/controller/ProfileController.java must remain backward-compatible.
- **REQ-005**: Profile page save operation must use the correct HTTP verb matching backend API contract.
- **REQ-006**: Duplicate auth implementations must be reduced so credential verification, session attachment, and user DTO mapping each have a single source of truth.
- **SEC-001**: Protected routes must return 401 or 403 for unauthenticated requests and 200 for authenticated requests, with no permissive bypass.
- **SEC-002**: Password hashing must remain BCrypt-based and plaintext passwords must never be persisted or returned.
- **TST-001**: Existing tests in src/test/java/com/sharedbasket/controller/AuthFlowIntegrationTest.java and src/test/java/com/sharedbasket/service/DevAuthenticationProfileIntegrationTest.java must pass.
- **CON-001**: No new framework introduction; implementation must stay within Spring Boot 2.7.x, Spring Security, JSF managed beans, and existing JPA repositories.
- **CON-002**: Maintain existing URL topology for public pages login.xhtml and register.xhtml.
- **GUD-001**: Reuse existing interface src/main/java/com/sharedbasket/service/AuthenticationService.java as the stable service contract.
- **PAT-001**: Keep controller-service-repository layering and centralize session logic inside authentication service implementation.

## 2. Implementation Steps

### Implementation Phase 1

- **GOAL-001**: Baseline and lock expected login behavior before refactoring.

| Task | Description | Completed | Date |
| ---- | ----------- | --------- | ---- |
| TASK-001 | Record current auth test failures from target/surefire-reports/com.sharedbasket.controller.AuthFlowIntegrationTest.txt and target/surefire-reports/com.sharedbasket.service.DevAuthenticationProfileIntegrationTest.txt as acceptance baseline artifacts in integration/phase2 notes. | Yes | 2026-08-13 |
| TASK-002 | Confirm endpoint contract map: POST /api/auth/register, POST /api/auth/login, GET /api/auth/me, POST /api/auth/logout, GET /api/profile, PUT /api/profile by reading src/main/java/com/sharedbasket/controller/AuthController.java and src/main/java/com/sharedbasket/controller/ProfileController.java. | Yes | 2026-08-13 |
| TASK-003 | Confirm JSF entry points and navigation actions in src/main/webapp/login.xhtml, src/main/webapp/register.xhtml, and src/main/java/com/sharedbasket/controller/ShellController.java, then document expected redirects and session dependencies. | Yes | 2026-08-13 |

### Implementation Phase 2

- **GOAL-002**: Unify runtime authentication state so Spring Security and application services agree on logged-in status.

| Task | Description | Completed | Date |
| ---- | ----------- | --------- | ---- |
| TASK-004 | In src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java, add a canonical method to create an authenticated SecurityContext Authentication token and bind it to SecurityContextHolder during login/session attachment. | Yes | 2026-08-13 |
| TASK-005 | In CookieAuthenticationService.attachSession(HttpServletRequest, User), set both session attributes authenticatedUserId/authenticatedUserEmail and the SecurityContext entry required by Spring Security persistence filter. | Yes | 2026-08-13 |
| TASK-006 | In CookieAuthenticationService.getCurrentUser(), support retrieval from SecurityContext principal first, then fallback to session attributes for backward compatibility. | Yes | 2026-08-13 |
| TASK-007 | In src/main/java/com/sharedbasket/config/SecurityConfig.java, preserve public endpoints but validate that protected endpoints rely on the unified authenticated principal and do not conflict with custom session attributes. |  |  |
| TASK-008 | Define deterministic unauthenticated response policy (401 or 403) and align all protected auth/profile endpoints to that policy through existing exception handling path in src/main/java/com/sharedbasket/exception/GlobalExceptionHandler.java. |  |  |

### Implementation Phase 3

- **GOAL-003**: Eliminate duplicate login and mapping logic without changing external behavior.

| Task | Description | Completed | Date |
| ---- | ----------- | --------- | ---- |
| TASK-009 | Move User to UserDTO mapping and display-name formatting to a shared mapper utility under src/main/java/com/sharedbasket/util/ (for example UserViewMapper) and replace duplicate private methods in AuthController, ProfileController, CookieAuthenticationService, and DevAuthenticationService. | Yes | 2026-08-13 |
| TASK-010 | Consolidate credential/session attach logic by making JSF LoginBean call one service method that both verifies credentials and sets canonical authenticated state; remove parallel logic paths authenticateUser(UserDTO) vs attachSession(HttpServletRequest, User). | Yes | 2026-08-13 |
| TASK-011 | Consolidate bean lookup helper duplication by introducing one shared Spring-bean resolver utility for JSF-managed beans and replacing duplicated resolveSpringBean methods in LoginBean, RegisterBean, ShellController, and Profile-related beans. | Yes | 2026-08-13 |
| TASK-012 | Remove unused imports and obsolete annotations flagged by diagnostics in auth-related classes to keep warnings from obscuring real auth regressions. | Partial | 2026-08-13 |

### Implementation Phase 4

- **GOAL-004**: Repair JSF-to-REST profile and login integration breakpoints.

| Task | Description | Completed | Date |
| ---- | ----------- | --------- | ---- |
| TASK-013 | In src/main/java/com/sharedbasket/controller/jsf/ProfileBean.java, replace postForObject call for profile update with a PUT request (exchange or put) to match @PutMapping in ProfileController.updateProfile(ProfileUpdateRequestDTO). | Yes | 2026-08-13 |
| TASK-014 | In ProfileBean, ensure internal REST calls include current session authentication context (JSESSIONID propagation or direct service call strategy) so authenticated JSF users are recognized by protected profile endpoints. | Yes | 2026-08-13 |
| TASK-015 | In src/main/java/com/sharedbasket/controller/jsf/LoginBean.java, ensure successful login updates shell session state through a single call path and avoids stale currentUser state in ShellController. | Yes | 2026-08-13 |
| TASK-016 | Validate login.xhtml and header logout links so navigation remains consistent after security-context unification, including direct access redirect behavior for unauthenticated users. |  |  |
| TASK-024 | Refactor src/main/java/com/sharedbasket/controller/jsf/LoginBean.java so login uses the existing REST endpoint POST /api/auth/login (AuthController) instead of direct cookieAuthService.authenticate(...), and remove duplicate credential-verification path from JSF layer. | Yes | 2026-08-13 |
| TASK-025 | Refactor src/main/java/com/sharedbasket/controller/jsf/RegisterBean.java so register uses the existing REST endpoint POST /api/auth/register (AuthController) instead of direct repository/password-encoder logic. | Yes | 2026-08-13 |
| TASK-026 | Refactor src/main/java/com/sharedbasket/controller/jsf/ProfileBean.java so profile load/save uses existing REST endpoint logic from ProfileController instead of direct repository/service persistence logic in JSF bean. | Yes | 2026-08-13 |

### Implementation Phase 5

- **GOAL-005**: Resolve dev-profile bean-selection conflict and stabilize environment-specific auth.

| Task | Description | Completed | Date |
| ---- | ----------- | --------- | ---- |
| TASK-017 | Decide and implement one explicit policy for dev profile auth bean precedence: either enable DevAuthenticationService as @Service @Profile("dev") @Primary and demote CookieAuthenticationService in dev, or update dev test expectations to target CookieAuthenticationService. | Yes | 2026-08-13 |
| TASK-018 | Update src/test/java/com/sharedbasket/service/DevAuthenticationProfileIntegrationTest.java to match the selected bean policy and verify getCurrentUser behavior under active dev profile. | Yes | 2026-08-13 |
| TASK-019 | Ensure bean selection policy is documented in src/main/resources/application-dev.properties comments or integration/phase docs to prevent future contradictory @Primary usage. | Yes | 2026-08-13 |

### Implementation Phase 6

- **GOAL-006**: Verify end-to-end login correctness and prevent regressions.

| Task | Description | Completed | Date |
| ---- | ----------- | --------- | ---- |
| TASK-020 | Update src/test/java/com/sharedbasket/controller/AuthFlowIntegrationTest.java assertions to explicitly verify session continuity across register -> login -> me -> profile update, including expected status code policy and role/fullName fields. | Yes | 2026-08-13 |
| TASK-021 | Add negative tests for invalid credentials, disabled user login attempt, and unauthenticated access to /api/auth/me and /api/profile. | Partial | 2026-08-13 |
| TASK-022 | Add JSF integration sanity test or scripted manual verification checklist covering login.xhtml submit, dashboard redirect, profile.xhtml load, profile save, and logout redirect. |  |  |
| TASK-023 | Run mvn -DskipTests compile and mvn "-Dtest=AuthFlowIntegrationTest,DevAuthenticationProfileIntegrationTest" test; record outputs and mark phase complete only when all targeted tests pass. | Yes | 2026-08-13 |

## 3. Alternatives

- **ALT-001**: Keep current custom session attributes only and relax SecurityConfig rules for protected endpoints. Rejected because it weakens security boundaries and hides the root integration bug.
- **ALT-002**: Replace JSF managed-bean auth interactions with direct JavaScript calls to REST endpoints. Rejected for Phase 2 because it adds front-end architecture churn unrelated to the login defect.
- **ALT-003**: Keep both DevAuthenticationService and CookieAuthenticationService as @Primary in different contexts without explicit profile gates. Rejected because current failing dev integration test demonstrates ambiguous precedence.
- **ALT-004**: Continue using internal loopback RestTemplate calls from JSF without session propagation controls. Rejected because it causes nondeterministic authenticated behavior for profile operations.

## 4. Dependencies

- **DEP-001**: src/main/java/com/sharedbasket/config/SecurityConfig.java route authorization and session handling behavior.
- **DEP-002**: src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java as canonical auth/session implementation.
- **DEP-003**: src/main/java/com/sharedbasket/controller/AuthController.java and src/main/java/com/sharedbasket/controller/ProfileController.java endpoint contracts.
- **DEP-004**: src/main/java/com/sharedbasket/controller/jsf/LoginBean.java, src/main/java/com/sharedbasket/controller/jsf/ProfileBean.java, and src/main/java/com/sharedbasket/controller/ShellController.java JSF shell integration points.
- **DEP-005**: Existing DTOs and validation classes under src/main/java/com/sharedbasket/dto/.
- **DEP-006**: Integration test harness in src/test/java/com/sharedbasket/controller/AuthFlowIntegrationTest.java and src/test/java/com/sharedbasket/service/DevAuthenticationProfileIntegrationTest.java.

## 5. Files

- **FILE-001**: src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java - canonical authentication and session attachment behavior.
- **FILE-002**: src/main/java/com/sharedbasket/config/SecurityConfig.java - protected/public endpoint security policy.
- **FILE-003**: src/main/java/com/sharedbasket/controller/AuthController.java - login, me, logout contract behavior.
- **FILE-004**: src/main/java/com/sharedbasket/controller/ProfileController.java - authenticated profile read/update behavior.
- **FILE-005**: src/main/java/com/sharedbasket/controller/jsf/LoginBean.java - JSF login submission and session sync behavior.
- **FILE-006**: src/main/java/com/sharedbasket/controller/jsf/ProfileBean.java - JSF profile load/save integration path.
- **FILE-007**: src/main/java/com/sharedbasket/controller/ShellController.java - shell-level authenticated user synchronization and redirects.
- **FILE-008**: src/main/webapp/login.xhtml - login UI and form binding.
- **FILE-009**: src/main/webapp/profile.xhtml - profile UI and backing bean interactions.
- **FILE-010**: src/test/java/com/sharedbasket/controller/AuthFlowIntegrationTest.java - login integration behavior.
- **FILE-011**: src/test/java/com/sharedbasket/service/DevAuthenticationProfileIntegrationTest.java - dev-profile auth bean policy validation.
- **FILE-012**: src/main/java/com/sharedbasket/util/ (new shared utility classes for mapper and bean resolution deduplication).

## 6. Testing

- **TEST-001**: Auth happy path test: register then login then GET /api/auth/me returns 200 and same email.
- **TEST-002**: Profile happy path test: authenticated PUT /api/profile returns 200 and updated fullName.
- **TEST-003**: Invalid credentials test: POST /api/auth/login with wrong password returns expected failure status and message.
- **TEST-004**: Unauthenticated me test: GET /api/auth/me without session returns expected unauthorized/forbidden status per policy.
- **TEST-005**: Unauthenticated profile test: GET /api/profile and PUT /api/profile without session return expected unauthorized/forbidden status.
- **TEST-006**: Dev profile test: active dev profile resolves expected AuthenticationService implementation and current-user semantics.
- **TEST-007**: JSF manual flow: login.xhtml -> dashboard.xhtml -> profile.xhtml load -> profile save -> logout -> login.xhtml redirect.

## 7. Risks & Assumptions

- **RISK-001**: Changing SecurityContext behavior can affect all protected controllers beyond auth/profile.
- **RISK-002**: Consolidating duplicated login/session methods may break JSF shell sync if not updated atomically.
- **RISK-003**: Dev-profile bean policy change can invalidate existing local workflows if not documented.
- **ASSUMPTION-001**: Current UserRepository data model remains stable (email and username unique constraints are enforced).
- **ASSUMPTION-002**: No external identity provider integration is required in Phase 2; local credential auth remains the source.
- **ASSUMPTION-003**: Existing exception framework remains the canonical API error envelope.

## 8. Related Specifications / Further Reading

- [plan/feature-authentication-phase2-1.md](plan/feature-authentication-phase2-1.md)
- [Daniel/phase2-devA.md](Daniel/phase2-devA.md)
- [integration/INTEGRATION_EXPECTATIONS_BY_PHASE.md](integration/INTEGRATION_EXPECTATIONS_BY_PHASE.md)
- [src/main/java/com/sharedbasket/config/SecurityConfig.java](src/main/java/com/sharedbasket/config/SecurityConfig.java)
- [src/main/java/com/sharedbasket/service/AuthenticationService.java](src/main/java/com/sharedbasket/service/AuthenticationService.java)
- [src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java](src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java)
- [src/main/java/com/sharedbasket/controller/AuthController.java](src/main/java/com/sharedbasket/controller/AuthController.java)
- [src/main/java/com/sharedbasket/controller/ProfileController.java](src/main/java/com/sharedbasket/controller/ProfileController.java)
- [src/main/java/com/sharedbasket/controller/jsf/LoginBean.java](src/main/java/com/sharedbasket/controller/jsf/LoginBean.java)
- [src/main/java/com/sharedbasket/controller/jsf/ProfileBean.java](src/main/java/com/sharedbasket/controller/jsf/ProfileBean.java)