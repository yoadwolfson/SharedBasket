---
goal: Fix login session propagation so authenticated user appears in header after successful login
version: 1.0
date_created: 2026-08-13
last_updated: 2026-08-13
owner: Dev A
status: 'Planned'
tags: [bug, authentication, session, jsf, spring-security]
---

# Introduction

![Status: Planned](https://img.shields.io/badge/status-Planned-blue)

After a valid user logs in they are correctly redirected to the dashboard, but the header still shows "Guest user / (not logged in)" and the Login link instead of the user's email, full name, and the Profile/Logout actions.

The login-to-session-to-header pipeline involves five layers:

1. `LoginBean.login()` (JSF backing bean) calls `AuthController.login()` as a Spring bean (not via HTTP).
2. `AuthController.login()` calls `CookieAuthenticationService.attachSession()` which sets two session attributes (`authenticatedUserId`, `authenticatedUserEmail`) and writes the `UsernamePasswordAuthenticationToken` into both `SecurityContextHolder` and the HTTP session under `SPRING_SECURITY_CONTEXT`.
3. JSF sends a client-side redirect (`faces-redirect=true`) to `/dashboard.xhtml`. Spring Security's `SaveContextOnUpdateOrErrorResponseWrapper` intercepts `sendRedirect` and saves the current SecurityContext to the session.
4. On the next request for `/dashboard.xhtml`, Spring Security's `SecurityContextPersistenceFilter` is expected to load the `SecurityContext` from the session into `SecurityContextHolder`.
5. `ShellController.getCurrentUser()` (called during view rendering) delegates to `CookieAuthenticationService.getCurrentUser()`, which reads from `SecurityContextHolder` first and falls back to the session attribute `authenticatedUserId`. If both paths return `null` the header renders as unauthenticated.

The investigation found **five distinct bugs** across three files that together prevent the authenticated state from being visible after login.

---

## 1. Requirements & Constraints

- **REQ-001**: After a successful login the header must immediately show the authenticated user's email, full name, and the Profile/Logout action panel.
- **REQ-002**: The existing cookie/session authentication model must not change.
- **REQ-003**: No new HTTP round-trips may be introduced in the login flow.
- **SEC-001**: Session fixation protection must remain enabled.
- **CON-001**: The fix must not break the existing passing integration tests (`AuthFlowIntegrationTest`, `DevAuthenticationProfileIntegrationTest`, `ShoppingListControllerTest`).
- **CON-002**: Do not switch from JSF-direct bean invocation to a REST call for the login action.

---

## 2. Implementation Steps

### Implementation Phase 1 — Root cause: SecurityContext not loaded on redirect (SecurityConfig)

- **GOAL-001**: Spring Security has no explicit `sessionManagement` stanza. By default the `SessionManagementFilter` runs with `migrateSession` fixation strategy. Because `LoginBean` calls `AuthController` as a Spring bean (in-process), the authentication change happens **inside** the Servlet, which means it is invisible to the filters that already ran before the Servlet. Under certain Spring Security 5 builds, `SecurityContextPersistenceFilter` wraps the response with `SaveContextOnUpdateOrErrorResponseWrapper`; when JSF calls `response.sendRedirect()` that wrapper commits the response and saves the SecurityContext. However, the session identity depends on whether the current `JSESSIONID` session is the same session `bindAuthentication()` wrote to. Without an explicit `sessionManagement` policy, edge cases can cause a mismatch. Fix: pin `IF_REQUIRED` + `changeSessionId` and ensure the session is not silently changed between login and redirect.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-001 | In `src/main/java/com/sharedbasket/config/SecurityConfig.java`, add `.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).sessionFixation().changeSessionId())` inside the `filterChain` builder. Add the missing import `org.springframework.security.config.http.SessionCreationPolicy`. This pins the session policy to the same JSESSIONID cookie approach used by `CookieAuthenticationService` and prevents silent session migration from discarding the auth attributes that were written to the original session. | | |

### Implementation Phase 2 — Root cause: SecurityContext path silently returns null (CookieAuthenticationService)

- **GOAL-002**: `resolveFromSecurityContext()` is tried first inside `getCurrentUser()`. If the SecurityContext stored in the session is not re-loaded into `SecurityContextHolder` (e.g., due to the race described in Phase 1), `resolveFromSecurityContext()` returns `null` and the code falls through to `resolveFromSession()`. `resolveFromSession()` calls `request.getSession(false)`. In the JSF rendering context `getCurrentRequest()` returns `(HttpServletRequest) fc.getExternalContext().getRequest()` — this is the actual container request and `getSession(false)` will only return `null` if **no** session exists. Because the user just logged in, a session **does** exist. However there is a second defect: `resolveFromSecurityContext()` matches `details instanceof Long` to retrieve the user, but the `details` field is set via the raw generic setter `setDetails(user.getId())`. If the session is serialised and deserialised (e.g., a future Redis session store), the `Long` can come back as an `Integer`, causing `instanceof Long` to return `false` and falling through to the email lookup path instead. Fix both paths to be unambiguous.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-002 | In `src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java`, swap the resolution order in `getCurrentUser()` so that `resolveFromSession()` is tried first (it only depends on a plain session attribute that is always written by `attachSession()`), and `resolveFromSecurityContext()` is the fallback. This makes the happy-path immune to any Spring Security SecurityContextHolder propagation issue. | | |
| TASK-003 | In `resolveFromSecurityContext()` replace `details instanceof Long` with an explicit `Long.valueOf(details.toString())` conversion (after null-checking `details`), so the method works regardless of the boxed numeric type returned by session deserialisation. | | |

### Implementation Phase 3 — Root cause: exception swallowed in ShellController (ShellController)

- **GOAL-003**: `ShellController.syncCurrentUserFromAuthService()` catches `Exception` and silently sets `currentUser = null`. If `CookieAuthenticationService.getCurrentUser()` throws (e.g., a `JpaSystemException` because there is no active JPA transaction in the JSF rendering thread, or a `NoSuchBeanDefinitionException` on first load), the user appears as unauthenticated with zero diagnostic information. Fix: log the exception so the root cause is observable, then still null-guard to keep the UI from crashing.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-004 | In `src/main/java/com/sharedbasket/controller/ShellController.java`, add a `java.util.logging.Logger` (or `org.slf4j.Logger` if the project already imports SLF4J — check existing imports) and in the `catch (Exception e)` block of `syncCurrentUserFromAuthService()` log the exception at `WARN` level with a meaningful message (`"syncCurrentUser failed: " + e.getMessage()`) before setting `currentUser = null`. | | |

### Implementation Phase 4 — Root cause: `syncShellUser()` in LoginBean may not sync the correct bean instance (LoginBean)

- **GOAL-004**: `LoginBean.syncShellUser()` uses `FacesContext.getApplication().evaluateExpressionGet(facesContext, "#{shellController}", ShellController.class)`. Because `ShellController` is `@SessionScoped`, JSF will return the existing session-scoped instance. The call to `shellController.syncCurrentUserFromAuthService()` inside `syncShellUser()` re-reads from the auth service **within the same request** as the login action, so `SecurityContextHolder` still holds the authenticated context at that point. This part works. However, the result is immediately overwritten on the **next** request when `getCurrentUser()` re-reads from the auth service again. Therefore `syncShellUser()` is redundant but harmless **as long as Phases 1 and 2 are correct**. The task here is simply to ensure the import and type match after the package refactor (there was a `jsf/ShellController.java` stub that was deleted — confirm no import drift).

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-005 | In `src/main/java/com/sharedbasket/controller/jsf/LoginBean.java`, verify the import on line 5 reads `import com.sharedbasket.controller.ShellController;` (the only existing class with that name). Verify `evaluateExpressionGet(facesContext, "#{shellController}", ShellController.class)` will not throw a `ClassCastException` at runtime. If any discrepancy exists, correct the import. | | |

### Implementation Phase 5 — Root cause: logout does not clear session attributes before invalidation

- **GOAL-005**: `CookieAuthenticationService.logout()` calls `SecurityContextHolder.clearContext()` first, then `session.invalidate()`. The `clearContext()` call clears the in-memory thread-local holder. The `session.invalidate()` removes all session attributes including `AUTHENTICATED_USER_ID`. This is correct. However `AuthController.logout()` ignores the `HttpServletRequest` parameter it receives and calls `authenticationService.logout()` — which internally calls `getCurrentRequest()` to re-resolve the request from `FacesContext`. In a REST client call (e.g., Postman or future frontend), `FacesContext` is null, and `RequestContextHolder` is the fallback. Ensure the logout path is wired to use the supplied request directly.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-006 | In `src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java`, add an overloaded `logout(HttpServletRequest request)` method that skips the `getCurrentRequest()` call and directly invalidates the supplied request's session. Update `AuthController.logout()` to call `authenticationService.logout(servletRequest)` instead of `authenticationService.logout()`. Keep the no-arg `logout()` for the JSF `ShellController.logout()` path. | | |

---

## 3. Alternatives

- **ALT-001**: Route the JSF login form to a real HTTP POST to `/api/auth/login` via JavaScript `fetch` and handle the response in JS. Rejected because it introduces AJAX complexity and a cross-context request, while the existing direct bean call is simpler and sufficient.
- **ALT-002**: Replace `@SessionScoped ShellController` with a request-scoped bean that queries `CookieAuthenticationService` on each request without any cached state. Rejected because it changes the existing architecture and adds DB round-trips on every request.
- **ALT-003**: Disable `SecurityContextPersistenceFilter` entirely and rely purely on session attributes (`AUTHENTICATED_USER_ID`). Rejected because it removes Spring Security's session-backed SecurityContext, breaking `SecurityContextHolder.getContext()` for all REST endpoint authorization checks.

---

## 4. Dependencies

- **DEP-001**: `org.springframework.security:spring-security-web` — `SecurityContextPersistenceFilter`, `HttpSessionSecurityContextRepository`, `SessionCreationPolicy`.
- **DEP-002**: `javax.faces` (MyFaces) — `FacesContext`, `ExternalContext`.
- **DEP-003**: H2 in-memory database via `spring.jpa.hibernate.ddl-auto=create-drop` — user records exist only for the lifetime of the application; restart clears all users.

---

## 5. Files

- **FILE-001**: `src/main/java/com/sharedbasket/config/SecurityConfig.java` — TASK-001: add `sessionManagement`.
- **FILE-002**: `src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java` — TASK-002, TASK-003, TASK-006: resolution order, Long-safe cast, logout overload.
- **FILE-003**: `src/main/java/com/sharedbasket/controller/ShellController.java` — TASK-004: add warning log.
- **FILE-004**: `src/main/java/com/sharedbasket/controller/jsf/LoginBean.java` — TASK-005: verify import.
- **FILE-005**: `src/main/java/com/sharedbasket/controller/AuthController.java` — TASK-006: call `logout(servletRequest)`.

---

## 6. Testing

- **TEST-001**: Manual smoke test — register a new user, log in, confirm the header shows the user's email and full name, and the Profile/Logout panel is rendered.
- **TEST-002**: Manual smoke test — click Logout, confirm redirect to login page, confirm header shows "Guest user / (not logged in)".
- **TEST-003**: Re-run `AuthFlowIntegrationTest` — all existing assertions must still pass.
- **TEST-004**: Re-run `DevAuthenticationProfileIntegrationTest` — `CookieAuthenticationService` must still be injected.
- **TEST-005**: Add a new test case to `AuthFlowIntegrationTest` that calls `GET /api/auth/me` immediately after `POST /api/auth/login` within the same session and asserts a `200` response with the user's email — this verifies that the session attributes survive across requests at the HTTP layer.

---

## 7. Risks & Assumptions

- **RISK-001**: Swapping resolution order in `getCurrentUser()` (TASK-002) means the session attribute path becomes primary. If `attachSession()` is ever called without `resolveFromSession()` having access to the request (e.g., background thread), the user would not be found. This risk is already present and unchanged.
- **RISK-002**: Adding `changeSessionId` session fixation strategy (TASK-001) means the JSESSIONID cookie value changes on first authentication. Any hard-coded JSESSIONID in test fixtures must be updated.
- **ASSUMPTION-001**: The JSF `ExternalContext.getRequest()` inside the MyFaces implementation returns an object that is castable to `javax.servlet.http.HttpServletRequest` in the embedded Tomcat context. This is true for all standard servlet-container JSF integrations.
- **ASSUMPTION-002**: The H2 in-memory database is running and the user being used to log in was created in the same JVM session (i.e., not restarted between register and login).

---

## 8. Related Specifications / Further Reading

- [plan/feature-authentication-phase2-1.md](feature-authentication-phase2-1.md) — original Phase 2 authentication implementation plan
- [Daniel/phase2-devA.md](../Daniel/phase2-devA.md) — Dev A Phase 2 execution notes
- [Spring Security Reference — Session Management](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html)
- [Spring Security HttpSessionSecurityContextRepository](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/context/HttpSessionSecurityContextRepository.html)
