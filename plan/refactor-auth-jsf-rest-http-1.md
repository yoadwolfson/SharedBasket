---
goal: Convert JSF authentication beans (register, login, logout, profile) from direct Spring bean calls to REST/HTTP calls via RestTemplate
version: 1.0
date_created: 2026-08-13
last_updated: 2026-08-13
owner: Dev A
status: 'Completed'
tags: [refactor, authentication, jsf, rest, http, spring]
---

# Introduction

![Status: Completed](https://img.shields.io/badge/status-Completed-brightgreen)

`LoginBean`, `RegisterBean`, `ShellController`, and `ProfileBean` currently invoke `AuthController` / `ProfileController` / `AuthenticationService` as in-process Spring beans (direct Java method calls). `GroupsController` instead calls its backend through real HTTP requests using `RestTemplate`, targeting the actual `@RestController` endpoints (`/api/groups/**`) and manually forwarding the session cookie. This plan converts the four auth-related JSF beans to the same REST/HTTP pattern, targeting the existing `/api/auth/**` and `/api/profile` endpoints exposed by `AuthController` and `ProfileController`.

## 1. Requirements & Constraints

- **REQ-001**: `RegisterBean.register()` must call `POST /api/auth/register` over HTTP instead of invoking `AuthController.register(...)` directly.
- **REQ-002**: `LoginBean.login()` must call `POST /api/auth/login` over HTTP instead of invoking `AuthController.login(...)` directly.
- **REQ-003**: `ShellController.logout()` must call `POST /api/auth/logout` over HTTP instead of invoking `AuthenticationService.logout()` directly.
- **REQ-004**: `ShellController.syncCurrentUserFromAuthService()` must call `GET /api/auth/me` over HTTP instead of invoking `AuthenticationService.getCurrentUser()` directly.
- **REQ-005**: `ProfileBean` (`loadProfile()`, `save()`) must call `GET /api/profile` and `PUT /api/profile` over HTTP instead of invoking `ProfileController` directly.
- **SEC-001**: All outgoing HTTP requests must forward the browser's session cookie (`HttpHeaders.COOKIE`) from the incoming `HttpServletRequest`, exactly as `GroupsController.buildJsonHeaders()` does, so the backend can identify the caller's session.
- **SEC-002**: The `Set-Cookie` header returned by a successful `POST /api/auth/login` response must be captured and re-applied to the outgoing JSF response so the browser stores the new session cookie; without this, the user will appear logged out on the next page.
- **SEC-003**: Do not log or expose cookie header values or password fields in error messages surfaced to the user.
- **CON-001**: The existing REST endpoints (`AuthController`, `ProfileController`) must not be modified; only the JSF bean layer changes.
- **CON-002**: `RestTemplate` must be resolved from the Spring `WebApplicationContext` the same way `GroupsController.resolveSpringBean(RestTemplate.class)` does. ***Amendment (2026-08-13)***: the actual `GroupsController` in this codebase calls `GroupService` directly (not `RestTemplate`), so no `RestTemplate` bean previously existed; a new `RestTemplateConfig` `@Bean` was added (see FILE-006).
- **CON-003**: Existing method signatures used by `.xhtml` views (e.g. `login()`, `register()`, `logout()`, `save()`, getters/setters) must remain unchanged so no view files need to be edited.
- **GUD-001**: Follow the exact structural pattern already established in `GroupsController`: a `resolveSpringBean` helper, a `buildJsonHeaders()` helper, a `*ApiBaseUrl()` helper built from the current `HttpServletRequest`, and `try/catch` around `restTemplate.exchange(...)` catching `HttpStatusCodeException`.
- **GUD-002**: Extract the duplicated helper logic (`resolveSpringBean`, `buildJsonHeaders`, base URL construction, error message extraction) into a single shared class (`JsfRestClientSupport`) instead of copy-pasting it into all four beans, to avoid four near-identical private method blocks.
- **PAT-001**: Use `restTemplate.exchange(url, HttpMethod.X, new HttpEntity<>(body, headers), ResponseType.class)` for all calls, matching `GroupsController`.

## 2. Implementation Steps

### Implementation Phase 1

- GOAL-001: Create a shared HTTP-client support utility so the four beans do not duplicate cookie/header/base-URL/error-extraction logic.

| Task     | Description                                                                                                                                                                                                 | Completed | Date |
| -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------- | ---- |
| TASK-001 | Create `src/main/java/com/sharedbasket/util/JsfRestClientSupport.java` with static methods: `resolveBean(Class<T>)`, `buildJsonHeaders()`, `apiBaseUrl(String pathSuffix)`, `extractErrorMessage(Exception)`. | Yes | 2026-08-13 |
| TASK-002 | Implement `buildJsonHeaders()` to set `Content-Type: application/json`, `Accept: application/json`, and forward the incoming request's `Cookie` header, mirroring `GroupsController.buildJsonHeaders()`. | Yes | 2026-08-13 |
| TASK-003 | Implement `apiBaseUrl(String pathSuffix)` to read scheme/host/port/context-path from the current `HttpServletRequest` and append `pathSuffix` (e.g. `"/api/auth"`, `"/api/profile"`), mirroring `GroupsController.groupsApiBaseUrl()`. | Yes | 2026-08-13 |
| TASK-004 | Implement `extractErrorMessage(Exception ex)` to unwrap `HttpStatusCodeException.getResponseBodyAsString()`, additionally parsing the JSON `message` field from `GlobalExceptionHandler`'s `ErrorResponseDTO` body, falling back to raw body / `ex.getMessage()`. | Yes | 2026-08-13 |
| TASK-005 | Add a static `getCurrentRequest()` helper that reads `HttpServletRequest` from `FacesContext.getCurrentInstance().getExternalContext().getRequest()`, returning `null` safely if unavailable. | Yes | 2026-08-13 |
| TASK-005b | *(Added)* Create `src/main/java/com/sharedbasket/config/RestTemplateConfig.java` with a `@Bean RestTemplate restTemplate()` since no `RestTemplate` bean previously existed in the application context. | Yes | 2026-08-13 |

### Implementation Phase 2

- GOAL-002: Convert `LoginBean` to call `POST /api/auth/login` via `RestTemplate`, including capturing and forwarding the login `Set-Cookie` header.

| Task     | Description                                                                                                                                                                                                                     | Completed | Date |
| -------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------- | ---- |
| TASK-006 | In `LoginBean`, remove the `AuthController authController` field and its `JsfSpringBeanResolver.resolve(AuthController.class)` initialization in `init()`; add a `transient RestTemplate restTemplate` field resolved via `JsfRestClientSupport.resolveBean(RestTemplate.class)`. | Yes | 2026-08-13 |
| TASK-007 | In `login()`, replace `authController.login(loginRequest, servletRequest).getBody()` with `restTemplate.exchange(JsfRestClientSupport.apiBaseUrl("/api/auth") + "/login", HttpMethod.POST, new HttpEntity<>(loginRequest, JsfRestClientSupport.buildJsonHeaders()), UserDTO.class)`. | Yes | 2026-08-13 |
| TASK-008 | Replace the `catch (ForbiddenOperationException ex)` block with `catch (HttpStatusCodeException ex)`, calling `addError(...)` for any 4xx/5xx response (login failure), and keep a generic `catch (Exception ex)` for network-level failures with message "Login is currently unavailable.". | Yes | 2026-08-13 |
| TASK-009 | After a successful response, read the response's `HttpHeaders.get(HttpHeaders.SET_COOKIE)` from the `ResponseEntity` and, for each value, add it to the outgoing JSF response via `((HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse()).addHeader("Set-Cookie", value)` so the browser stores the new session cookie. | Yes | 2026-08-13 |
| TASK-010 | Remove the now-unused `getCurrentRequest()` private method from `LoginBean` (superseded by `JsfRestClientSupport.getCurrentRequest()`), keeping `syncShellUser()`, `addError()`, `isBlank()`, and the email/password getters/setters unchanged. | Yes | 2026-08-13 |
| TASK-011 | Remove the now-unused `import com.sharedbasket.controller.AuthController;` and `import com.sharedbasket.exception.ForbiddenOperationException;`; add `import org.springframework.http.HttpEntity;`, `import org.springframework.http.HttpMethod;`, `import org.springframework.http.HttpHeaders;`, `import org.springframework.http.ResponseEntity;`, `import org.springframework.web.client.HttpStatusCodeException;`, `import org.springframework.web.client.RestTemplate;`, `import javax.servlet.http.HttpServletResponse;`. | Yes | 2026-08-13 |

### Implementation Phase 3

- GOAL-003: Convert `RegisterBean` to call `POST /api/auth/register` via `RestTemplate`.

| Task     | Description                                                                                                                                                                                     | Completed | Date |
| -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------- | ---- |
| TASK-012 | In `RegisterBean`, remove the `AuthController authController` field and its resolution in `init()`; add a `transient RestTemplate restTemplate` field resolved via `JsfRestClientSupport.resolveBean(RestTemplate.class)`. | Yes | 2026-08-13 |
| TASK-013 | In `register()`, replace `authController.register(request).getBody()` with `restTemplate.exchange(JsfRestClientSupport.apiBaseUrl("/api/auth") + "/register", HttpMethod.POST, new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()), UserDTO.class)`. | Yes | 2026-08-13 |
| TASK-014 | Replace `catch (ConflictOperationException ex)` with `catch (HttpStatusCodeException ex)`, calling `addGlobalError(JsfRestClientSupport.extractErrorMessage(ex))`; keep a generic `catch (Exception ex)` for network failures. | Yes | 2026-08-13 |
| TASK-015 | Remove unused `import com.sharedbasket.controller.AuthController;` and `import com.sharedbasket.exception.ConflictOperationException;`; add the same `org.springframework.http.*` and `org.springframework.web.client.*` imports as Phase 2. | Yes | 2026-08-13 |

### Implementation Phase 4

- GOAL-004: Convert `ShellController.logout()` and `syncCurrentUserFromAuthService()` to call `POST /api/auth/logout` and `GET /api/auth/me` via `RestTemplate`.

| Task     | Description                                                                                                                                                                                                       | Completed | Date |
| -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------- | ---- |
| TASK-016 | In `ShellController`, remove the `AuthenticationService authenticationService` field and its resolution in `init()`; add a `transient RestTemplate restTemplate` field resolved via `JsfRestClientSupport.resolveBean(RestTemplate.class)`. | Yes | 2026-08-13 |
| TASK-017 | Locate `syncCurrentUserFromAuthService()` (referenced from `LoginBean.syncShellUser()`); replace its direct `authenticationService.getCurrentUser()` call with `restTemplate.exchange(JsfRestClientSupport.apiBaseUrl("/api/auth") + "/me", HttpMethod.GET, new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()), UserDTO.class).getBody()`, catching `HttpStatusCodeException` to treat a 403 as "no current user" (`currentUser = null`). | Yes | 2026-08-13 |
| TASK-018 | In `logout()`, replace `authenticationService.logout()` with `restTemplate.exchange(JsfRestClientSupport.apiBaseUrl("/api/auth") + "/logout", HttpMethod.POST, new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()), Void.class)`, keeping the existing `try/catch (Exception e)` that calls `addErrorAlert(...)`. | Yes | 2026-08-13 |
| TASK-019 | Remove unused `import com.sharedbasket.service.AuthenticationService;`; add the `org.springframework.http.*` and `org.springframework.web.client.*` imports needed for the `RestTemplate` calls. | Yes | 2026-08-13 |

### Implementation Phase 5

- GOAL-005: Convert `ProfileBean` to call `GET /api/profile` and `PUT /api/profile` via `RestTemplate`.

| Task     | Description                                                                                                                                                                                                     | Completed | Date |
| -------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------- | ---- |
| TASK-020 | In `ProfileBean`, remove the `ProfileController profileController` field and its resolution in `init()`; add a `transient RestTemplate restTemplate` field resolved via `JsfRestClientSupport.resolveBean(RestTemplate.class)`. | Yes | 2026-08-13 |
| TASK-021 | In `loadProfile()`, replace `profileController.getProfile().getBody()` with `restTemplate.exchange(JsfRestClientSupport.apiBaseUrl("/api/profile"), HttpMethod.GET, new HttpEntity<>(JsfRestClientSupport.buildJsonHeaders()), UserDTO.class).getBody()`. | Yes | 2026-08-13 |
| TASK-022 | In `save()`, replace `profileController.updateProfile(request).getBody()` with `restTemplate.exchange(JsfRestClientSupport.apiBaseUrl("/api/profile"), HttpMethod.PUT, new HttpEntity<>(request, JsfRestClientSupport.buildJsonHeaders()), UserDTO.class).getBody()`. | Yes | 2026-08-13 |
| TASK-023 | Replace `catch (BadRequestException ex)` / `catch (ForbiddenOperationException ex)` blocks in `save()` with `catch (HttpStatusCodeException ex)`, inspecting `ex.getStatusCode()` (403 → "Authentication required" / redirect to login, other 4xx → `JsfRestClientSupport.extractErrorMessage(ex)`); `loadProfile()` keeps a generic `catch (Exception ex)`. | Yes | 2026-08-13 |
| TASK-024 | Remove unused `import com.sharedbasket.controller.ProfileController;`, `import com.sharedbasket.exception.BadRequestException;`, `import com.sharedbasket.exception.ForbiddenOperationException;`; add the `org.springframework.http.*` and `org.springframework.web.client.*` imports needed. | Yes | 2026-08-13 |

### Implementation Phase 6

- GOAL-006: Validate the end-to-end HTTP-based auth flow compiles and behaves identically to the direct-call implementation.

| Task     | Description                                                                                                                                        | Completed | Date |
| -------- | --------------------------------------------------------------------------------------------------------------------------------------------------- | --------- | ---- |
| TASK-025 | Run `mvn -q -DskipTests compile` and resolve any compilation errors introduced by the import/field changes across the four beans and the new support class. | Yes | 2026-08-13 |
| TASK-026 | Manually verify: register a new user, log in with that user, confirm the header shows the logged-in user (session cookie applied via TASK-009), view/edit profile, and log out. | No (manual, needs a running app) | |
| TASK-027 | Verify invalid login credentials, duplicate registration email/username, and profile validation errors still surface the same user-facing messages as before the refactor. | No (manual, needs a running app) | |

## 3. Alternatives

- **ALT-001**: Duplicate `buildJsonHeaders()`/`apiBaseUrl()`/`extractErrorMessage()` into each of the four beans individually (exact copy of the `GroupsController` style). Rejected because it would quadruple near-identical private helper methods across `LoginBean`, `RegisterBean`, `ShellController`, and `ProfileBean`, increasing maintenance risk if the header/cookie logic ever changes.
- **ALT-002**: Keep direct Spring bean calls (current state) and only add HTTP calls for new features. Rejected because the user explicitly requested the REST/HTTP pattern for consistency with `GroupsController`.
- **ALT-003**: Use `RestTemplate` with a configured base URL from application properties instead of deriving it from the incoming `HttpServletRequest`. Rejected to stay consistent with the existing `GroupsController.groupsApiBaseUrl()` pattern already in the codebase.

## 4. Dependencies

- **DEP-001**: `org.springframework.web.client.RestTemplate` — already used by `GroupsController`, must already be registered as a Spring bean in the application context.
- **DEP-002**: `com.sharedbasket.util.JsfSpringBeanResolver` / `WebApplicationContextUtils` — used to resolve `RestTemplate` from the servlet context, same as `GroupsController.resolveSpringBean(...)`.
- **DEP-003**: Existing REST endpoints in `AuthController` (`/api/auth/register`, `/api/auth/login`, `/api/auth/me`, `/api/auth/logout`) and `ProfileController` (`/api/profile` GET/PUT) — must remain unchanged and reachable from within the same application (self-call over HTTP).

## 5. Files

- **FILE-001**: `src/main/java/com/sharedbasket/util/JsfRestClientSupport.java` — new shared helper class (headers, base URL, error extraction, bean resolution). ✅ Created.
- **FILE-002**: `src/main/java/com/sharedbasket/controller/jsf/LoginBean.java` — convert to `RestTemplate` call to `POST /api/auth/login`, add `Set-Cookie` forwarding. ✅ Updated.
- **FILE-003**: `src/main/java/com/sharedbasket/controller/jsf/RegisterBean.java` — convert to `RestTemplate` call to `POST /api/auth/register`. ✅ Updated.
- **FILE-004**: `src/main/java/com/sharedbasket/controller/ShellController.java` — convert `logout()` and `syncCurrentUserFromAuthService()` to `RestTemplate` calls to `POST /api/auth/logout` and `GET /api/auth/me`. ✅ Updated.
- **FILE-005**: `src/main/java/com/sharedbasket/controller/jsf/ProfileBean.java` — convert `loadProfile()`/`save()` to `RestTemplate` calls to `GET /api/profile` / `PUT /api/profile`. ✅ Updated.
- **FILE-006**: *(Added)* `src/main/java/com/sharedbasket/config/RestTemplateConfig.java` — new `@Configuration` class registering the `RestTemplate` bean, since none previously existed. ✅ Created.

## 6. Testing

- **TEST-001**: Register a new unique email/username via `/register.xhtml` and confirm a `UserDTO` is returned and the redirect to `/login.xhtml` occurs.
- **TEST-002**: Attempt registration with a duplicate email/username and confirm the same `ConflictOperationException`-equivalent message now surfaces via `HttpStatusCodeException` handling.
- **TEST-003**: Log in with valid credentials and confirm the session cookie from the login response is applied (header shows the logged-in user without a page refresh needed beyond the redirect).
- **TEST-004**: Log in with invalid credentials and confirm "Invalid email or password." is still displayed.
- **TEST-005**: Log out and confirm the session is invalidated (subsequent `GET /api/auth/me` returns 403 and the shell redirects to `/login.xhtml`).
- **TEST-006**: Load `/profile.xhtml` while authenticated and confirm existing profile fields populate correctly via `GET /api/profile`.
- **TEST-007**: Update profile fields via `save()` and confirm `PUT /api/profile` persists changes and returns the updated `UserDTO`.
- **TEST-008**: Access `/profile.xhtml` without an active session and confirm the 403 response triggers a redirect to `/login.xhtml`.

## 7. Risks & Assumptions

- **RISK-001**: If the `Set-Cookie` header is not correctly captured and re-applied after login (TASK-009), users will appear logged out despite a successful login response, reproducing the exact symptom described in [fix-login-session-header-1.md](fix-login-session-header-1.md).
- **RISK-002**: Self-invoking HTTP calls (the app calling its own REST endpoints over HTTP) add latency and a new failure mode (connection refused) compared to direct method calls; network-level `catch (Exception ex)` blocks must be present in every converted method.
- **RISK-003**: `RestTemplate` calls block the request thread; under load this could behave differently than direct in-JVM calls, though this is already accepted by the existing `GroupsController` pattern.
- **ASSUMPTION-001**: `RestTemplate` is already configured and available as a Spring-managed bean in the application context (as implied by its use in `GroupsController`).
- **ASSUMPTION-002**: The application server allows the app to make an outbound HTTP call back to itself (same host/port) without being blocked by a firewall or connection-pool limit.

## 8. Related Specifications / Further Reading

[fix-login-session-header-1.md](fix-login-session-header-1.md)
[feature-login-authentication-2.md](feature-login-authentication-2.md)
[feature-authentication-phase2-1.md](feature-authentication-phase2-1.md)
