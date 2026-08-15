---
goal: Step-by-step debugging plan to verify a valid user actually logs in end-to-end
version: 1.1
date_created: 2026-08-14
last_updated: 2026-08-14
owner: Dev A
status: 'Fixed and verified'
tags: [process, debugging, authentication, session, jsf, spring-security]
---

# Introduction

![Status: Fixed and verified](https://img.shields.io/badge/status-Fixed%20and%20verified-brightgreen)

> **Update (2026-08-14): This plan has been executed end-to-end and the confirmed root cause has been fixed and verified. See section 9, "Findings from executed debugging session", for the evidence trail and the applied fix. The phases below are kept as the executed procedure/checklist for reference and regression re-testing.**

This plan is a deterministic, phase-by-phase procedure to verify whether a valid user "really" logs in, by isolating and testing each layer of the login pipeline independently before testing the pipeline end-to-end. The current architecture (post `refactor-auth-jsf-rest-http-1.md`) has the JSF layer call the app's own REST endpoints over real HTTP via `RestTemplate`, and manually forwards the `Set-Cookie` header back to the browser. This self-HTTP-call pattern has multiple independent failure points: (1) the data/password layer, (2) the REST `/api/auth/login` endpoint and session binding, (3) the `LoginBean` self-HTTP call and cookie-forwarding, (4) the browser actually storing/re-sending the new session cookie, and (5) `ShellController` re-resolving the current user on the next request. Each phase below isolates one failure point and provides an explicit pass/fail check.

## 1. Requirements & Constraints

- **REQ-001**: Determine, with evidence (logs, DB query results, HTTP response headers, browser devtools), which layer of the login pipeline fails when a "valid" login does not result in an authenticated session.
- **REQ-002**: Do not change production authentication behavior while debugging; only add temporary diagnostic logging/breakpoints that can be trivially removed or are already guarded (e.g. behind `DEBUG`/`TRACE` log level).
- **REQ-003**: Confirm each phase's pass criteria before proceeding to the next phase. If a phase fails, stop and remediate before continuing.
- **CON-001**: Use the existing H2 in-memory database (`spring.jpa.hibernate.ddl-auto=create-drop`); data does not survive an application restart, so re-register the test user after any restart.
- **CON-002**: Do not modify `AuthController`, `CookieAuthenticationService`, `SecurityConfig`, or `JsfRestClientSupport` as part of this debugging pass unless a phase explicitly identifies a bug and remediation is approved separately.
- **GUD-001**: Prefer non-invasive verification (SQL queries, curl/Postman, browser devtools Network/Application tabs, existing log statements) before attaching a debugger.
- **GUD-002**: When a debugger is required, set breakpoints at the exact file/line references listed per phase.
- **FIX-001**: Use a single fixed test user for every phase in this plan: `email = logintest@gmail.com`, `password = 123456`, `username = logintest`, `firstName = logintest`, `lastName = logintest`. Register this user first (register.xhtml or `POST /api/auth/register`), then use it for every login attempt described below.

## 2. Implementation Steps

### Implementation Phase 1: Verify the user record and password hash in the database

- GOAL-001: Confirm the "valid user" actually exists, is enabled, and has a password hash that matches the password being typed, independent of any HTTP/session code.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-001 | Start the app and register the test user via `POST /api/auth/register` (curl/Postman) or the register.xhtml form using `email=logintest@gmail.com`, `password=123456`, `username=logintest`, `firstName=logintest`, `lastName=logintest`. Capture the JSON response and confirm HTTP 200 with `email = "logintest@gmail.com"` and `fullName = "logintest logintest"`. If registration fails with a conflict (409), the user already exists from a prior run of the still-running app — skip straight to TASK-002. | | |
| TASK-002 | Open the H2 console (`/h2-console`, permitted in [SecurityConfig.java](../src/main/java/com/sharedbasket/config/SecurityConfig.java#L24)) and run `SELECT id, email, username, enabled, password_hash FROM users WHERE email = 'logintest@gmail.com';`. Confirm exactly one row, `enabled = TRUE`, and `password_hash` is a non-null BCrypt string (starts with `$2a$` or `$2b$`). | | |
| TASK-003 | In a scratch test or REPL, call `passwordEncoder.matches("123456", "<password_hash from DB>")` (BCryptPasswordEncoder) and confirm it returns `true`. If `false`, the registration path is hashing/storing the wrong value — stop here and fix registration before proceeding. | | |

**Phase 1 pass criteria**: user row exists, `enabled = TRUE`, and `passwordEncoder.matches(...)` returns `true` for the exact credentials being tested.

### Implementation Phase 2: Verify the REST login endpoint in isolation (no JSF, no browser)

- GOAL-002: Confirm `POST /api/auth/login` authenticates correctly and returns a session cookie, completely bypassing the JSF layer.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-004 | Using curl with cookie-jar capture: `curl -i -c cookies.txt -H "Content-Type: application/json" -d "{\"email\":\"logintest@gmail.com\",\"password\":\"123456\"}" http://localhost:8080/api/auth/login` (PowerShell equivalent already run: `Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"logintest@gmail.com","password":"123456"}' -SessionVariable sess`). Confirm HTTP 200 and a JSON body containing the user's `id`, `email`, `fullName`. | | |
| TASK-005 | Inspect the response headers from TASK-004 for a `Set-Cookie: JSESSIONID=...` header. If absent, set a breakpoint in [CookieAuthenticationService.java](../src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java#L122) at `attachSession(...)` and confirm `request.getSession(true)` is invoked and returns a non-null session. | | |
| TASK-006 | Using the same cookie jar, call `curl -i -b cookies.txt http://localhost:8080/api/auth/me` (PowerShell equivalent: `Invoke-WebRequest -Uri "http://localhost:8080/api/auth/me" -Method Get -WebSession $sess`). Confirm HTTP 200 with the same user's JSON. This proves the login endpoint's session survives a second HTTP request when the cookie is actually resent. | | |
| TASK-007 | If TASK-004 returns HTTP 403/401, set a breakpoint in [AuthController.java](../src/main/java/com/sharedbasket/controller/AuthController.java#L64) at `authenticationService.authenticate(...)` and step through `CookieAuthenticationService.authenticate()` at [CookieAuthenticationService.java](../src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java#L100) to see whether `userRepository.findByEmail(...)` returns the user and whether `passwordEncoder.matches(...)` is `true` or `false` at runtime (cross-check against Phase 1 TASK-003). | | |

**Phase 2 pass criteria**: TASK-004 returns 200 with a `Set-Cookie` header, and TASK-006 returns 200 for `/api/auth/me` using that cookie. This isolates that the backend REST login + session binding is correct, independent of JSF.

### Implementation Phase 3: Verify SecurityContext/session binding server-side

- GOAL-003: Confirm `attachSession()` and `bindAuthentication()` write both the plain session attribute and the `SecurityContext` correctly, so `getCurrentUser()` can resolve the user from either path.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-008 | Set a breakpoint in [CookieAuthenticationService.java](../src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java#L128) inside `attachSession(...)` and confirm, after `session.setAttribute(AUTHENTICATED_USER_ID, user.getId())` executes, that `session.getId()` matches the `JSESSIONID` value returned in the Phase 2 `Set-Cookie` header. | | |
| TASK-009 | Set a breakpoint in `bindAuthentication(...)` at [CookieAuthenticationService.java](../src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java#L163) and confirm `SecurityContextHolder.setContext(securityContext)` is called with a non-anonymous `UsernamePasswordAuthenticationToken`, and that the same session object receives the `SPRING_SECURITY_CONTEXT` attribute. | | |
| TASK-010 | Repeat the `/api/auth/me` curl call from TASK-006 with a debugger attached; step into `getCurrentUser()` at [CookieAuthenticationService.java](../src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java#L44) and confirm `resolveFromSession(request.getSession(false))` returns a non-null `User` on the first attempt (this is the primary path per the resolution order fixed in `fix-login-session-header-1.md`). | | |

**Phase 3 pass criteria**: the session attribute and `SecurityContext` are both bound to the same `HttpSession` instance, and `resolveFromSession()` alone (without falling back to `resolveFromSecurityContext()`) resolves the correct user.

### Implementation Phase 4: Verify the JSF `LoginBean` self-HTTP call and cookie forwarding

- GOAL-004: Confirm that when the login form is submitted through the browser, `LoginBean.login()` correctly performs the internal HTTP call and forwards the resulting `Set-Cookie` header to the real browser response.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-011 | Set a breakpoint in [LoginBean.java](../src/main/java/com/sharedbasket/controller/jsf/LoginBean.java#L44) at the `restTemplate.exchange(...)` call. Submit the login form in the browser with valid credentials and confirm `response.getBody()` is non-null and contains the correct user. | | |
| TASK-012 | Inspect `response.getHeaders()` at the same breakpoint and confirm a `Set-Cookie` entry is present (mirrors Phase 2 TASK-005, but now triggered through the internal `RestTemplate` call instead of curl). | | |
| TASK-013 | Set a breakpoint in `forwardSessionCookie(...)` at [LoginBean.java](../src/main/java/com/sharedbasket/controller/jsf/LoginBean.java#L78) and confirm `httpResponse.addHeader(HttpHeaders.SET_COOKIE, setCookie)` executes for each cookie value before the method returns, and that `FacesContext.getCurrentInstance().getExternalContext().getResponse()` is not already committed (`httpResponse.isCommitted() == false`) at that point. | | |
| TASK-014 | **Known risk to confirm**: immediately after `forwardSessionCookie(...)`, `login()` calls `syncShellUser()` → `ShellController.syncCurrentUserFromAuthService()` → `/api/auth/me`, which builds its request cookie header from `JsfRestClientSupport.buildJsonHeaders()` at [JsfRestClientSupport.java](../src/main/java/com/sharedbasket/util/JsfRestClientSupport.java#L50) — this reads the **incoming browser request's** `Cookie` header (the pre-login/anonymous session), not the newly issued cookie. Set a breakpoint there and confirm whether the `Cookie` header used for this call is the OLD session id, not the new one from TASK-012. If confirmed, this call will resolve `currentUser = null` at this point in the request lifecycle — this is expected/non-fatal only if the subsequent full-page load (Phase 5) succeeds using the new cookie; otherwise it is a bug. | | |

**Phase 4 pass criteria**: the internal REST call succeeds and returns a `Set-Cookie` header, and that header is written to the real `HttpServletResponse` before it is committed. Document whether TASK-014's stale-cookie sync occurs (informational, not necessarily fatal).

### Implementation Phase 5: Verify the browser stores and re-sends the new session cookie

- GOAL-005: Confirm the actual browser (not curl, not the server-side RestTemplate) receives, stores, and resends the new `JSESSIONID` on the redirect to `dashboard.xhtml`.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-015 | Open browser DevTools → Network tab, enable "Preserve log", and submit the login form. Find the `loginForm` POST request/response (the JSF postback, not the internal server-side call — that one is invisible to the browser). Confirm its response has a `Set-Cookie: JSESSIONID=<value>` header with correct `Path` (matches app context path) and no unexpected `Domain` restricting it. | | |
| TASK-016 | Confirm the browser's next request (the redirect to `/dashboard.xhtml`) sends `Cookie: JSESSIONID=<same value from TASK-015>` in its request headers. | | |
| TASK-017 | Open DevTools → Application/Storage tab → Cookies for the app's origin and confirm exactly one `JSESSIONID` cookie is present after login (not two conflicting values, which would indicate a domain/path mismatch or a stale cookie was not overwritten). | | |

**Phase 5 pass criteria**: exactly one `JSESSIONID` cookie exists after login, and its value matches the one issued by the login response and is resent on the following request.

### Implementation Phase 6: Verify `ShellController` resolves the user on the dashboard page load

- GOAL-006: Confirm that once the browser has the new cookie, the dashboard's header actually renders the authenticated user (the original symptom reported in `fix-login-session-header-1.md`).

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-018 | Set a breakpoint in [ShellController.java](../src/main/java/com/sharedbasket/controller/ShellController.java#L134) inside `syncCurrentUserFromAuthService()` when `dashboard.xhtml` renders after the redirect. Confirm `restTemplate.exchange(... "/me" ...)` is called with the new `JSESSIONID` (from Phase 5) in `buildJsonHeaders()`. | | |
| TASK-019 | Confirm the call in TASK-018 returns HTTP 200 with the correct `UserDTO`, and `currentUser` is set to a non-null value (not caught by the `HttpStatusCodeException` or generic `Exception` branches at [ShellController.java](../src/main/java/com/sharedbasket/controller/ShellController.java#L142)). | | |
| TASK-020 | Visually confirm in the rendered page that the header shows the user's email/full name and the Profile/Logout actions instead of "Guest user / (not logged in)". | | |

**Phase 6 pass criteria**: `/api/auth/me` succeeds using the post-login cookie and the dashboard visually reflects the authenticated state.

### Implementation Phase 7: End-to-end regression confirmation

- GOAL-007: Confirm the full pipeline works repeatably and existing automated tests still pass.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-021 | Repeat Phases 4–6 with a second, different valid user (register a distinct email such as `logintest2@gmail.com`) to rule out state left over from the `logintest@gmail.com` test (e.g. stale `SecurityContextHolder` thread-local in a pooled request-handling thread). | | |
| TASK-022 | Run the existing integration test suite: `AuthFlowIntegrationTest`, `DevAuthenticationProfileIntegrationTest` (confirm no regressions from this debugging session, since no production code should have changed). | | |
| TASK-023 | Test the negative case: submit the login form with a wrong password and confirm the JSF page shows "Invalid email or password." and no session cookie is issued (repeat the DevTools check from Phase 5 to confirm no `Set-Cookie` appears). | | |
| TASK-024 | Test logout: click Logout, confirm `POST /api/auth/logout` returns success, the `JSESSIONID` cookie is cleared/invalidated (per `SecurityConfig.logout().deleteCookies("JSESSIONID")`), and the header reverts to "Guest user / (not logged in)". | | |

**Phase 7 pass criteria**: two independent valid users can log in and see their authenticated header, invalid credentials are rejected without issuing a session, logout clears the session, and all existing automated tests pass.

## 3. Alternatives

- **ALT-001**: Add temporary `System.out.println`/log statements at each phase's breakpoint location instead of attaching a debugger. Viable for environments where a debugger cannot be attached (e.g. containerized run), but breakpoints give more precise state inspection and are preferred when available.
- **ALT-002**: Skip isolation phases and debug only at the browser level (Phase 5/6). Rejected because the self-HTTP-call architecture has enough independent failure points (data layer, REST layer, session binding, cookie forwarding, browser storage) that skipping isolation makes root-causing a failure far slower.

## 4. Dependencies

- **DEP-001**: H2 console (`/h2-console`) for direct DB inspection, already permitted in [SecurityConfig.java](../src/main/java/com/sharedbasket/config/SecurityConfig.java#L23).
- **DEP-002**: curl or Postman for isolated REST calls with explicit cookie jar control.
- **DEP-003**: Browser DevTools (Network + Application/Storage tabs) for cookie verification.
- **DEP-004**: A Java debugger attached to the running Spring Boot process (e.g. via the IDE's remote debug or local run configuration) for breakpoint-based phases.

## 5. Files

- **FILE-001**: [src/main/java/com/sharedbasket/controller/AuthController.java](../src/main/java/com/sharedbasket/controller/AuthController.java) — REST login/me/logout endpoints (Phase 2).
- **FILE-002**: [src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java](../src/main/java/com/sharedbasket/service/impl/CookieAuthenticationService.java) — password check, session attribute + SecurityContext binding, current-user resolution (Phases 1–3).
- **FILE-003**: [src/main/java/com/sharedbasket/controller/jsf/LoginBean.java](../src/main/java/com/sharedbasket/controller/jsf/LoginBean.java) — JSF login action, self-HTTP call, cookie forwarding (Phase 4).
- **FILE-004**: [src/main/java/com/sharedbasket/util/JsfRestClientSupport.java](../src/main/java/com/sharedbasket/util/JsfRestClientSupport.java) — cookie/header construction for all internal REST calls (Phase 4).
- **FILE-005**: [src/main/java/com/sharedbasket/controller/ShellController.java](../src/main/java/com/sharedbasket/controller/ShellController.java) — current-user sync on every page render (Phase 6).
- **FILE-006**: [src/main/java/com/sharedbasket/config/SecurityConfig.java](../src/main/java/com/sharedbasket/config/SecurityConfig.java) — session management policy and endpoint authorization rules (Phase 2/3 context).
- **FILE-007**: [src/test/java/com/sharedbasket/controller/AuthFlowIntegrationTest.java](../src/test/java/com/sharedbasket/controller/AuthFlowIntegrationTest.java) — existing regression test to re-run in Phase 7.

## 6. Testing

- **TEST-001**: Phase 2 curl-based login + `/me` check (manual, isolates REST layer).
- **TEST-002**: Phase 5 browser DevTools cookie verification (manual, isolates browser/cookie layer).
- **TEST-003**: Phase 6 visual header check on dashboard (manual, confirms symptom resolution).
- **TEST-004**: Re-run `AuthFlowIntegrationTest` and `DevAuthenticationProfileIntegrationTest` (automated regression, Phase 7 TASK-022).
- **TEST-005**: Negative-path manual test with wrong password (Phase 7 TASK-023).
- **TEST-006**: Logout manual test confirming cookie invalidation (Phase 7 TASK-024).

## 7. Risks & Assumptions

- **RISK-001**: The internal `RestTemplate` self-call in `LoginBean`/`ShellController` creates a session that is distinct from the browser-facing JSF request's own container session until the `Set-Cookie` is forwarded and the browser resends it — the immediate `syncShellUser()` call after login (TASK-014) may read stale/anonymous state even when the overall login is correct. This is a known architectural quirk of the self-HTTP-call pattern, not necessarily a bug, but must be explicitly confirmed rather than assumed during debugging.
- **RISK-002**: If the app runs behind a reverse proxy or in Docker with port mapping, `JsfRestClientSupport.apiBaseUrl()` derives the callback URL from `request.getScheme()/getServerName()/getServerPort()`, which can be wrong behind a proxy that does not forward `X-Forwarded-*` headers, causing the internal login call to fail with a connection error rather than a credentials error.
- **RISK-003**: H2's `create-drop` DDL mode means any restart between registration and login testing removes the test user, producing a false "invalid credentials" result that is actually a missing-user condition — always re-check Phase 1 after any app restart.
- **ASSUMPTION-001**: The user being tested was registered against the currently running application instance (not a previous run).
- **ASSUMPTION-002**: No corporate proxy, browser extension, or `SameSite`/`Secure` cookie policy blocks the `JSESSIONID` cookie from being stored (test in an incognito window with extensions disabled if Phase 5 fails unexpectedly).

## 8. Related Specifications / Further Reading

- [plan/fix-login-session-header-1.md](fix-login-session-header-1.md) — prior investigation into the header-not-updating symptom and the session resolution order fix.
- [plan/refactor-auth-jsf-rest-http-1.md](refactor-auth-jsf-rest-http-1.md) — the refactor that introduced the self-HTTP-call + cookie-forwarding pattern being debugged here.
- [plan/feature-authentication-phase2-1.md](feature-authentication-phase2-1.md) — original Phase 2 authentication implementation plan.

## 9. Findings from executed debugging session (2026-08-14)

This plan was executed live against the running application using the fixed test user (`logintest@gmail.com` / `123456`). Summary verdict: **the backend login is fully correct and a valid user's credentials genuinely authenticate; the bug is entirely in the JSF UI layer, which can never display that authenticated state.**

### 9.1 Phase 1–3 results: backend is correct (PASS)

- TASK-001/002: user registers successfully, `enabled = TRUE`, BCrypt hash present.
- TASK-004/005/006: `POST /api/auth/login` returns HTTP 200 with the correct `UserDTO` and a `Set-Cookie: JSESSIONID=...` header; the same cookie then successfully calls `GET /api/auth/me` and gets the same authenticated user back. Repeated 5/5 with a realistic auto-cookie-jar client simulating a browser (including the case where the browser already had a pre-login anonymous session from visiting `login.xhtml`, which is what really happens) — 5/5 succeeded with no session-fixation or cookie mismatch.
- **Conclusion**: `AuthController`, `CookieAuthenticationService`, and `SecurityConfig` all work correctly. The password check, session creation, and cookie issuance are not the problem. The fixes described in `fix-login-session-header-1.md` (session-first resolution order, `sessionManagement` policy) are in place and functioning.

### 9.2 Real browser reproduction of the reported symptom (CONFIRMED BUG)

Using the integrated browser (not curl), the fixed test user was registered and logged in through the actual `login.xhtml` form:

1. Login form submitted with correct credentials → app redirects to `/dashboard.xhtml` (no error message — the login action itself reports success).
2. The header still displays **"Guest user" / "(not logged in)"** and the "Login" link, instead of the authenticated user's email/name and the Profile/Logout actions.
3. Opening a second browser tab (same cookies) and navigating directly to `GET /api/auth/me` **correctly returns the authenticated user's JSON** — proving the session/cookie is valid and the backend recognizes the login.
4. Reproduced identically across three independent clean rebuilds/restarts of the application (ruling out a one-off fluke or stale build artifact).

This confirms the exact symptom already described in `fix-login-session-header-1.md` is **still present** despite that plan's fixes, because the actual defect is one layer deeper than that plan investigated.

### 9.3 Root cause (confirmed via instrumented build)

`ShellController` — the JSF `@SessionScoped` bean solely responsible for rendering the authenticated header (`header.xhtml` uses `#{shellController.currentUser}` / `#{shellController.authenticated}`) — is **never constructed by the JSF runtime**.

Evidence: temporary `LOGGER.warn(...)` statements were added at the very first line of `ShellController.init()` (`@PostConstruct`) and `getCurrentUser()`, and the app was rebuilt with `mvn clean compile` (verified 0 compile errors) and restarted fresh three times. Across every page load — anonymous `login.xhtml`, a cache-busted URL never requested before, and the post-login `dashboard.xhtml` render — **zero log lines ever appeared**, even though the page fully rendered every time with the `currentUser == null` fallback text. Since JSF's Unified EL performs null-safe navigation (`shellController.currentUser` evaluates to `null` without error when `shellController` itself resolves to `null`), a header that always shows "Guest user" regardless of real session state is exactly what happens when the `shellController` bean reference cannot be resolved at all — not when it resolves and returns `null` from a failed REST call.

Why the bean isn't found: [faces-config.xml](../src/main/webapp/WEB-INF/faces-config.xml) explicitly registers `loginBean` and `registerBean` via `<managed-bean>` XML entries, and both of those beans demonstrably work (the login POST is visible end-to-end in the server logs). `ShellController` and `ProfileBean` have **no XML registration** and rely entirely on the `@ManagedBean(name = "shellController")` annotation being discovered by Mojarra's classpath annotation scanner. In this project's Spring Boot embedded-Tomcat layout (classes served from an exploded `target/classes` directory rather than a packaged WAR/JAR), that annotation scan is not finding/registering the `@SessionScoped` `ShellController` bean, so every EL reference to `shellController` on every page silently resolves to `null`.

### 9.4 Why this matters / answer to "does a valid user really log in"

**Yes — a valid user's credentials are genuinely verified and a real authenticated session is established.** The REST API layer (`/api/auth/login`, `/api/auth/me`) proves this unambiguously. However, **the JSF UI can never reflect that authenticated state**, because the one bean responsible for showing it is never instantiated. From an end user's perspective this looks indistinguishable from "login doesn't work": no error is shown, the redirect to the dashboard happens, but the UI permanently displays "Guest user / not logged in" no matter what. Any other page/feature that depends on `#{shellController...}` (profile link, logout, per-page auth guards via `redirectToLogin()`) is equally broken for the same reason.

### 9.5 Recommended remediation (APPLIED and VERIFIED — 2026-08-14)

- **FIX-A (applied)**: Added an explicit `<managed-bean>` entry for `shellController` to [faces-config.xml](../src/main/webapp/WEB-INF/faces-config.xml), mirroring the existing `loginBean`/`registerBean` entries, with `<managed-bean-scope>session</managed-bean-scope>`:
  ```xml
  <managed-bean>
      <managed-bean-name>shellController</managed-bean-name>
      <managed-bean-class>com.sharedbasket.controller.ShellController</managed-bean-class>
      <managed-bean-scope>session</managed-bean-scope>
  </managed-bean>
  ```
  No application code changes were needed — `ShellController` already implements `Serializable` and its `@PostConstruct init()` was already correct; it simply needed to be discoverable.
- **Verification performed**: rebuilt with `mvn clean compile` (0 errors), restarted the app, and repeated the real-browser login test with the fixed test user. Result: the header now correctly shows `logintest@gmail.com` / `logintest logintest` with Profile/Logout links immediately after login, and correctly reverts to "Guest user / (not logged in)" after clicking Logout. Server logs during the test also show `GET /api/auth/me` calls now firing from the JSF layer (previously completely absent) and returning `200 OK` with the authenticated user.
- **Regression check**: ran `mvn test`. One pre-existing failure, `ShoppingListControllerTest.ignoresDuplicateAndOutOfOrderEvents` (expected `<9>` but was `<4>`), was confirmed via `git stash`/`git stash pop` to occur identically with and without this fix — it is unrelated to this change (a pre-existing issue in shopping-list event-ordering logic, outside the scope of this login investigation). No other tests were affected; `AuthFlowIntegrationTest` and `DevAuthenticationProfileIntegrationTest` continue to pass.
- **FIX-B (not applied, optional follow-up)**: `ProfileBean` (`@ManagedBean(name = "profileBean")`, `@ViewScoped`) has the same annotation-only registration and was not verified/fixed here since it was outside the reported login symptom; it may need the same treatment if it exhibits similar issues (note: the legacy `<managed-bean-scope>` XML element traditionally only supports `request`/`session`/`application`/`none`, not `view`, so registering a view-scoped bean this way would need further validation or a different approach, e.g. investigating why Mojarra's annotation scan itself isn't finding `target/classes` in this Spring Boot embedded-Tomcat layout).
