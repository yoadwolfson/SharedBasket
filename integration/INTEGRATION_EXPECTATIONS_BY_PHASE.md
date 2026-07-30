# Shared Basket Integration Expectations by Phase

Purpose: define exactly what Developer A and Developer B must deliver to each other at every phase, and what must be verified before a phase is considered integration-ready.
---

## Ownership Rules (Apply to All Phases)

- Developer A owns core platform, authentication/profile, categories/shopping items, purchase history/reporting hardening.
- Developer B owns JSF shell, group management, recommendation UX, real-time collaboration UX, and cross-phase frontend integration.
- Black-box rule: each developer consumes the other developer's contracts without implementing the other side's internal logic.
- Contract change rule: any DTO, endpoint, status code, role value, or event payload change must be announced before merge.

---

## Phase 1: Foundation, Architecture, Shared Contracts

### Developer A must deliver
- Spring Boot baseline, package structure, PostgreSQL connection, base entities and repositories.
- Initial REST contract conventions and standardized error response structure.
- Validation and exception mapping conventions used by all endpoints.

### Developer B must deliver
- JSF shell foundation: base layout, reusable templates/fragments, styling baseline.
- Frontend contract assumptions documented for DTO and validation/error handling.
- Placeholder-safe pages that render without unfinished backend features.

### Integration checklist
- [ ] App starts locally with no startup errors.
- [ ] Database connection and base mappings are stable.
- [ ] Error response contract is documented and shared.
- [ ] JSF shell renders with no unresolved template references.
- [ ] @DevAConnection markers exist for cross-team dependencies.

### Phase exit criteria
- Shared contract baseline is stable enough for independent phase work.

---

## Phase 2: Authentication and Profile Management

### Developer A must deliver
- Registration/login/profile APIs and service rules.
- Password hashing, session behavior, and auth/authorization responses.
- UserDTO contract including id, email, fullName, role (if role is present in session context).

### Developer B must deliver
- JSF registration/login/profile screens and flows.
- Clear rendering for validation/auth failures using shared error contract.
- Session-aware navigation behavior based on authenticated user state.

### Integration checklist
- [ ] Register flow works end to end.
- [ ] Login flow works end to end.
- [ ] Passwords are never stored in plain text.
- [ ] Unauthorized requests are rejected and shown correctly in UI.
- [ ] Profile read/update contract matches UI fields.

### Phase exit criteria
- Trusted identity and session lifecycle are stable for later role/group work.

---

## Phase 3: Groups, Invitations, Join Requests, Roles

### Developer A must deliver
- Security and authorization support required for group role enforcement.
- Stable identity/session contract consumed by group endpoints.
- Validation and error responses consistent with global contract.

### Developer B must deliver
- Group feature end to end: entities/services/controllers/views for create, invite, join request lifecycle, member management, leave group.
- Explicit backend role enforcement and mirrored UI permission guards.
- UI states for pending, accepted, rejected, removed.

### Integration checklist
- [ ] Group creation works and persists correctly.
- [ ] Invitation-code flow works for join.
- [ ] Request approval/rejection/removal transitions are valid.
- [ ] Member cannot execute admin-only actions (backend and UI).
- [ ] Role values used by frontend match backend contract exactly.

### Phase exit criteria
- Multi-user group collaboration and permission boundaries are reliable.

---

## Phase 4: Categories and Shopping Item Management

### Developer A must deliver
- Category management APIs and rules.
- Shopping item CRUD APIs including quantity/price/purchased state and timestamps.
- Concurrency protection (optimistic locking or equivalent) and conflict responses.

### Developer B must deliver
- Shopping list and category JSF interactions wired to A-owned APIs.
- Validation and conflict UX (retry/refresh guidance, visible conflict messaging).
- UI state consistency after each update.

### Integration checklist
- [ ] Category create/list/update/delete works and prevents invalid duplicates.
- [ ] Item create/edit/delete/purchased-state works end to end.
- [ ] Conflict responses are returned on simultaneous edits.
- [ ] UI shows conflict feedback and refresh behavior.
- [ ] No silent overwrite is possible from UI workflows.

### Phase exit criteria
- Core daily shopping workflows are stable under normal and concurrent usage.

---

## Phase 5: Purchase History, Reporting, Recommendations

### Developer A must deliver
- Purchase history persistence and endpoints.
- Reporting endpoints (totals, category/product breakdown, budget-vs-actual).
- Recommendation service outputs derived from stored history.

### Developer B must deliver
- Recommendation and dashboard UI flows end to end.
- Quick rebuild actions and recommendation history presentation.
- Exact frontend mapping of backend analytics values (no local recomputation drift).

### Integration checklist
- [ ] Purchase history created only for completed purchases.
- [ ] Reporting outputs match stored records.
- [ ] Recommendation payload fields are stable and fully documented.
- [ ] Dashboard values in UI match backend payload values exactly.
- [ ] Empty/loading/error states are handled gracefully.

### Phase exit criteria
- Analytics and recommendations are trusted, explainable, and UI-accurate.

---

## Phase 6: Real-Time Sync, Full Integration, Hardening

### Developer A must deliver
- Real-time server event delivery (WebSocket or equivalent).
- Final concurrency consistency rules and backend safeguards.
- Contract verification, security review, and performance hardening.

### Developer B must deliver
- Real-time client subscriptions and immediate list refresh UX.
- Final JSF wiring, navigation polish, message polish, and conflict UX.
- Multi-client integration testing from UI perspective.

### Integration checklist
- [ ] Multiple clients receive live shopping-list updates.
- [ ] Concurrent edits do not lose data.
- [ ] Remaining placeholders are removed or formally accepted as deferred scope.
- [ ] End-to-end tests cover auth, groups, shopping, reporting, recommendations, realtime.
- [ ] Security and validation checks pass in integrated environment.

### Phase exit criteria
- Product is integration-complete and stable for release candidate testing.

---

## Feature-Specific Definition of Done (Requested Example)

## JSF Shell Plan (Developer B) - Done when all are true

- [x] Base layout, navigation, and shared templates are implemented.
- [x] Shared styling foundation exists for B-owned screens.
- [x] Frontend models/form states are aligned to agreed DTO/error shapes.
- [x] Shared error rendering and field feedback behavior is implemented in JSF.
- [x] Shell pages render without requiring unfinished backend features.
- [x] Developer A dependencies are treated as black-box contracts only.
- [x] Contract assumptions are documented and traceable to integration points.

Evidence references:
- Yoad/feature-jsf-shell-plan.md
- src/main/webapp/WEB-INF/templates/layouts/base-layout.xhtml
- src/main/webapp/WEB-INF/templates/fragments/alerts.xhtml
- src/main/webapp/WEB-INF/views/dashboard.xhtml
- DEV_A_INTEGRATION_CHECKLIST.md

---

## Merge Gate (Use Before Every Cross-Team Merge)

- [ ] Contract diff reviewed (DTOs, endpoint paths, status codes, roles, event payloads).
- [ ] Backward compatibility confirmed for affected UI/backend consumers.
- [ ] Integration tests executed for changed phase scope.
- [ ] Error and permission scenarios validated.
- [ ] Placeholder/TODO markers reviewed and accepted for remaining scope.
- [ ] Both developers sign off on phase exit criteria.

---

## Developer A Integration Touchpoints (@DevAConnection)

Use this list as the primary "files to update" checklist for Developer A integration work.
These are the files where @DevAConnection markers were added.

### Java contracts and integration classes

- [ ] src/main/java/com/sharedbasket/service/AuthenticationService.java
- [ ] src/main/java/com/sharedbasket/controller/jsf/ShellController.java
- [ ] src/main/java/com/sharedbasket/util/ErrorHandlingUtil.java
- [ ] src/main/java/com/sharedbasket/dto/UserDTO.java
- [ ] src/main/java/com/sharedbasket/dto/ErrorResponseDTO.java
- [ ] src/main/java/com/sharedbasket/dto/FieldErrorDTO.java

### JSF templates and views that consume Developer A contracts

- [ ] src/main/webapp/WEB-INF/templates/layouts/base-layout.xhtml
- [ ] src/main/webapp/WEB-INF/templates/fragments/header.xhtml
- [ ] src/main/webapp/WEB-INF/templates/fragments/navigation.xhtml
- [ ] src/main/webapp/WEB-INF/templates/fragments/alerts.xhtml
- [ ] src/main/webapp/WEB-INF/views/shopping-list.xhtml
- [ ] src/main/webapp/WEB-INF/views/reports.xhtml
- [ ] src/main/webapp/WEB-INF/views/recommendations.xhtml
- [ ] src/main/webapp/WEB-INF/views/groups.xhtml

### What Developer A should verify in these files

- [ ] Method names and signatures used by JSF/backing beans match final service interfaces.
- [ ] DTO field names and enum values (especially roles) match API payloads exactly.
- [ ] ErrorResponse and FieldError payload shapes match backend responses.
- [ ] Role-based visibility assumptions in navigation match backend authorization.
- [ ] Placeholder comments and TODO-DEV-A markers are replaced with real integration behavior where applicable.

### Same touchpoints mapped by phase

Phase 1
- [ ] src/main/webapp/WEB-INF/templates/layouts/base-layout.xhtml
- [ ] src/main/webapp/WEB-INF/templates/fragments/header.xhtml
- [ ] src/main/webapp/WEB-INF/templates/fragments/navigation.xhtml
- [ ] src/main/webapp/WEB-INF/templates/fragments/alerts.xhtml
- [ ] src/main/java/com/sharedbasket/dto/ErrorResponseDTO.java
- [ ] src/main/java/com/sharedbasket/dto/FieldErrorDTO.java

Phase 2
- [ ] src/main/java/com/sharedbasket/service/AuthenticationService.java
- [ ] src/main/java/com/sharedbasket/controller/jsf/ShellController.java
- [ ] src/main/java/com/sharedbasket/dto/UserDTO.java

Phase 3
- [ ] src/main/webapp/WEB-INF/views/groups.xhtml
- [ ] src/main/webapp/WEB-INF/templates/fragments/navigation.xhtml

Phase 4
- [ ] src/main/webapp/WEB-INF/views/shopping-list.xhtml

Phase 5
- [ ] src/main/webapp/WEB-INF/views/reports.xhtml
- [ ] src/main/webapp/WEB-INF/views/recommendations.xhtml

Cross-phase
- [ ] src/main/java/com/sharedbasket/util/ErrorHandlingUtil.java

---

## Suggested Maintenance Process

- Keep this file updated at the end of each phase.
- Update DEV_A_INTEGRATION_CHECKLIST.md with concrete endpoint names and examples as contracts finalize.
- If ownership changes, update both execution plans and this checklist in the same PR.
