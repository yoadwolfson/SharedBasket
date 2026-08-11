---
goal: Implement Phase 2 authentication and profile management end to end
version: 1.0
date_created: 2026-08-09
last_updated: 2026-08-09
owner: SharedBasket / Developer A
status: 'Planned'
tags: ['feature', 'authentication', 'phase2', 'jsf', 'spring-security']
---

# Introduction

![Status: Planned](https://img.shields.io/badge/status-Planned-blue)

This plan translates the Phase 2 checklist from the attachment into a concrete implementation sequence for the SharedBasket repository. It covers DTO contracts, backend authentication and session handling, security configuration, and JSF registration/login/profile flows.

## 1. Requirements & Constraints

- **REQ-001**: Implement registration, login, current-user, logout, and profile read/update flows end to end.
- **REQ-002**: Use the existing ErrorResponseDTO and FieldErrorDTO contract for all validation and business failures.
- **REQ-003**: Hash passwords before persistence and never return password values in API responses.
- **REQ-004**: Support cookie-based sessions that are recognized by both Spring endpoints and JSF views.
- **REQ-005**: Ensure the JSF UI can register, log in, view, and update profile data without manual API calls.
- **SEC-001**: Protected endpoints must reject unauthenticated access consistently.
- **CON-001**: Keep the Phase 2 role handling simple by defaulting to MEMBER unless explicit role assignment is introduced later.
- **GUD-001**: Keep DTO naming consistent with the existing package structure under src/main/java/com/sharedbasket/dto/.
- **PAT-001**: Follow the existing service/repository/controller layering used by the current Spring application.

## 2. Implementation Steps

### Implementation Phase 1: DTO and contract foundation

- **GOAL-001**: Create explicit request and response DTOs for authentication and profile operations.

| Task | Description | Completed | Date |
| ---- | ----------- | --------- | ---- |
| TASK-001 | Create src/main/java/com/sharedbasket/dto/RegisterRequestDTO.java with fields email, username, password, firstName, lastName and validation annotations for required values. |  |  |
| TASK-002 | Create src/main/java/com/sharedbasket/dto/LoginRequestDTO.java with fields email and password and validation annotations for required values. |  |  |
| TASK-003 | Create src/main/java/com/sharedbasket/dto/ProfileUpdateRequestDTO.java with fields firstName, lastName, username and validation annotations for non-empty and length constraints. |  |  |
| TASK-004 | Keep UserDTO in src/main/java/com/sharedbasket/dto/UserDTO.java as the response shape for login and current-user endpoints, using id, email, fullName, and role. |  |  |

### Implementation Phase 2: Backend authentication and session services

- **GOAL-002**: Replace the dev-only fallback auth path with a session-backed authentication service for normal runtime.

| Task | Description | Completed | Date |
| ---- | ----------- | --------- | ---- |
| TASK-005 | Implement src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java and implement AuthenticationService#getCurrentUser() by reading the active HttpSession or HttpServletRequest context and returning a UserDTO. |  |  |
| TASK-006 | Implement AuthenticationService#isAuthenticated() to return true only when a valid authenticated session exists, and implement logout() to invalidate the session and clear auth state. |  |  |
| TASK-007 | Implement AuthenticationService#invalidateUserSessions(Long userId) as a no-op placeholder for now or a basic session invalidation hook, keeping behavior explicit for future admin workflows. |  |  |
| TASK-008 | Update the Spring bean wiring so the production auth service is primary in normal runtime while keeping the existing DevAuthenticationService available for development-only fallback scenarios. |  |  |

### Implementation Phase 3: Registration and login endpoints

- **GOAL-003**: Add backend endpoints for registration and login with validation, password hashing, and session creation.

| Task | Description | Completed | Date |
| ---- | ----------- | --------- | ---- |
| TASK-009 | Create src/main/java/com/sharedbasket/controller/AuthController.java with POST /api/auth/register that validates the request, rejects duplicate email or username, hashes the password with BCryptPasswordEncoder, saves a new enabled user, and returns a UserDTO or ErrorResponseDTO based on outcome. |  |  |
| TASK-010 | Create POST /api/auth/login in AuthController that verifies the user by email, checks enabled status, validates the supplied password, and creates the authenticated cookie-based session. |  |  |
| TASK-011 | Add exception handling for bad input, duplicate user conflicts, and invalid credentials using the existing GlobalExceptionHandler in src/main/java/com/sharedbasket/exception/GlobalExceptionHandler.java and the existing exception classes. |  |  |
| TASK-012 | Add a password hashing utility or bean in src/main/java/com/sharedbasket/config/PasswordConfig.java or the auth service implementation so passwordHash is stored as a hashed value and never returned in responses. |  |  |

### Implementation Phase 4: Current-user, logout, and profile endpoints

- **GOAL-004**: Expose authenticated profile APIs that read from the session-backed user context and persist updates to the database.

| Task | Description | Completed | Date |
| ---- | ----------- | --------- | ---- |
| TASK-013 | Add GET /api/auth/me in AuthController that returns the current UserDTO from the active session or returns a 401/403-style error when unauthenticated. |  |  |
| TASK-014 | Add POST /api/auth/logout in AuthController that invalidates the current session and clears authentication state. |  |  |
| TASK-015 | Create src/main/java/com/sharedbasket/controller/ProfileController.java with GET /api/profile and PUT /api/profile that load the current authenticated user from the session service, update only firstName, lastName, and username, and persist changes through UserRepository. |  |  |
| TASK-016 | Enforce simple validation rules in the profile controller and reject empty values or overlong values before persisting the update. |  |  |

### Implementation Phase 5: Security and JSF integration

- **GOAL-005**: Harden Spring Security and connect the JSF pages to the backend authentication flow.

| Task | Description | Completed | Date |
| ---- | ----------- | --------- | ---- |
| TASK-017 | Replace the current permissive configuration in src/main/java/com/sharedbasket/config/SecurityConfig.java so /api/auth/register, /api/auth/login, /resources/**, and public JSF resources are allowed while protected APIs require authentication. |  |  |
| TASK-018 | Create src/main/webapp/register.xhtml and wire a form submission to the registration endpoint that shows field-level and global errors from the ErrorResponseDTO contract. |  |  |
| TASK-019 | Replace the placeholder content in src/main/webapp/login.xhtml with a functional form that submits email and password to the login endpoint and redirects authenticated users to dashboard.xhtml. |  |  |
| TASK-020 | Create src/main/webapp/profile.xhtml and a backing bean in src/main/java/com/sharedbasket/controller/ or src/main/java/com/sharedbasket/bean/ that loads profile data on page view and submits updates to the profile endpoint. |  |  |
| TASK-021 | Update src/main/webapp/WEB-INF/templates/fragments/header.xhtml so the shell shows authenticated user info and uses the logout action to clear the session and redirect to login.xhtml. |  |  |

## 3. Alternatives

- **ALT-001**: Keep using the existing DevAuthenticationService as the runtime implementation; rejected because it bypasses the real session model and does not satisfy the Phase 2 requirement for end-to-end auth behavior.
- **ALT-002**: Use a simple map-based request/response structure instead of DTOs; rejected because the repository already has an ErrorResponseDTO contract and the plan requires consistency with existing APIs.
- **ALT-003**: Mix password-change logic into profile update; rejected because the attachment explicitly keeps password change as a future separate operation.

## 4. Dependencies

- **DEP-001**: Spring MVC, Spring Security, and the existing JPA repository layer in src/main/java/com/sharedbasket/repo/UserRepository.java.
- **DEP-002**: The current JSF setup in src/main/webapp/ and the servlet session configuration in src/main/webapp/WEB-INF/web.xml.
- **DEP-003**: A password hashing implementation such as BCryptPasswordEncoder, which is compatible with the Spring Security stack already present in the project.

## 5. Files

- **FILE-001**: src/main/java/com/sharedbasket/dto/RegisterRequestDTO.java — registration request contract.
- **FILE-002**: src/main/java/com/sharedbasket/dto/LoginRequestDTO.java — login request contract.
- **FILE-003**: src/main/java/com/sharedbasket/dto/ProfileUpdateRequestDTO.java — profile update contract.
- **FILE-004**: src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java — production auth service implementation.
- **FILE-005**: src/main/java/com/sharedbasket/controller/AuthController.java — registration/login/current-user/logout endpoints.
- **FILE-006**: src/main/java/com/sharedbasket/controller/ProfileController.java — profile read/update endpoints.
- **FILE-007**: src/main/java/com/sharedbasket/config/SecurityConfig.java — protected/unprotected route rules.
- **FILE-008**: src/main/webapp/login.xhtml — functional login UI.
- **FILE-009**: src/main/webapp/register.xhtml — registration UI.
- **FILE-010**: src/main/webapp/profile.xhtml — profile UI.
- **FILE-011**: src/main/webapp/WEB-INF/templates/fragments/header.xhtml — shell/auth UI integration.

## 6. Testing

- **TEST-001**: Add an integration test for successful registration, duplicate email rejection, duplicate username rejection, and password hashing persistence.
- **TEST-002**: Add an integration test for successful login, failed login with the wrong password, and authenticated current-user retrieval after session creation.
- **TEST-003**: Add an integration test for profile fetch/update and for rejecting unauthenticated access to protected endpoints.
- **TEST-004**: Perform a manual end-to-end run through the JSF login, registration, profile, and logout flows in a local environment with cookie sessions enabled.

## 7. Risks & Assumptions

- **RISK-001**: The current security configuration is permissive, so changing it may affect existing JSF pages until explicit public routes are restored.
- **RISK-002**: The project currently uses a dev-only fallback auth service, so switching the runtime path may require careful bean selection to avoid breaking the shell UI.
- **ASSUMPTION-001**: The application already has a database-backed User entity and repository that can be used for registration and profile persistence.
- **ASSUMPTION-002**: The existing ErrorResponseDTO and GlobalExceptionHandler can be reused for both API and JSF error rendering.

## 8. Related Specifications / Further Reading

- [Daniel/phase2-devA.md](Daniel/phase2-devA.md)
- [src/main/java/com/sharedbasket/service/AuthenticationService.java](src/main/java/com/sharedbasket/service/AuthenticationService.java)
- [src/main/java/com/sharedbasket/service/impl/DevAuthenticationService.java](src/main/java/com/sharedbasket/service/impl/DevAuthenticationService.java)
- [src/main/java/com/sharedbasket/config/SecurityConfig.java](src/main/java/com/sharedbasket/config/SecurityConfig.java)
- [src/main/webapp/login.xhtml](src/main/webapp/login.xhtml)
