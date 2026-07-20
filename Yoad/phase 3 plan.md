# Phase 3 Plan - Group Management (Execution Guide for Human or Agent)

Owner: Developer B
Phase Scope: Groups, invitation codes, join requests, membership lifecycle, and role enforcement.
Goal: Deliver the full feature end to end (REST API, service logic, persistence, validation, and JSF screens) while consuming Developer A contracts as black boxes.

## 1. Scope and Boundaries

In scope:
- Group creation and group settings updates.
- Invitation code generation/regeneration and join-by-code.
- Join request lifecycle for private groups.
- Membership lifecycle: approve, reject, remove, leave group.
- Admin/member permission enforcement in backend and UI.
- JSF groups screen behavior that mirrors backend states.

Out of scope:
- Rewriting authentication or session internals owned by Developer A.
- Rewriting categories, shopping item logic, or reporting internals.

## 2. Contracts to Respect

Required from Developer A (consume only):
- Authenticated user identity and session contract.
- Security role semantics used by backend authorization.
- Shared validation and error response shape.

Frontend/backend contract assumptions:
- Role values are explicit and stable (admin/member).
- Membership statuses are explicit and stable (pending/accepted/rejected/removed).
- Error responses follow shared DTO conventions for message and field-level errors.

## 3. Target Domain Model and States

Core entities:
- Group: id, name, description, visibility, ownerId, invitationCode, createdAt, updatedAt.
- GroupMember: id, groupId, userId, role, status, requestedAt, decidedAt, decidedBy.

Recommended enums:
- GroupVisibility: PUBLIC, PRIVATE
- MembershipRole: ADMIN, MEMBER
- MembershipStatus: PENDING, ACCEPTED, REJECTED, REMOVED

State rules:
- Group creator becomes ADMIN with ACCEPTED membership.
- PRIVATE groups require request flow before ACCEPTED.
- PUBLIC groups may support immediate join or request flow based on final policy.
- Only ADMIN can approve/reject/remove other members.
- A member can leave group unless business rule prevents last admin exit.

## 4. API Surface (Implementation Checklist)

Group endpoints:
- POST /api/groups
- GET /api/groups
- GET /api/groups/{groupId}
- PUT /api/groups/{groupId}
- POST /api/groups/{groupId}/invitation-code/regenerate

Membership endpoints:
- POST /api/groups/join-by-code
- POST /api/groups/{groupId}/join-requests
- GET /api/groups/{groupId}/join-requests
- POST /api/groups/{groupId}/join-requests/{requestId}/approve
- POST /api/groups/{groupId}/join-requests/{requestId}/reject
- DELETE /api/groups/{groupId}/members/{memberUserId}
- POST /api/groups/{groupId}/leave
- GET /api/groups/{groupId}/members

Minimum response behaviors:
- 200/201 for success.
- 400 for validation failures.
- 401/403 for auth/permission failures.
- 404 for missing group/request/member.
- 409 for invalid state transitions.

## 5. Backend Work Plan (Order Matters)

Step 1: Persistence foundation
- Create or finalize group and membership entities.
- Add repositories with queries for group ownership, membership by user/group, and pending requests.
- Add DB constraints for uniqueness and referential integrity.

Step 2: Service layer
- Implement GroupService for create/update/list/get and invitation code operations.
- Implement MembershipService for join/request/approve/reject/remove/leave transitions.
- Centralize authorization checks in service methods (do not rely on UI checks).

Step 3: Validation and transitions
- Validate group name constraints and invitation code format.
- Validate every transition with clear error messages.
- Enforce invariant: no unauthorized role action can mutate membership state.

Step 4: Controllers
- Implement REST endpoints and DTO mappings.
- Return standardized error payloads for all failures.
- Ensure DTO field names and enum values are stable for JSF consumption.

## 6. Frontend (JSF) Work Plan

Main screen responsibility:
- Groups page supports create group, list groups, join by code, request join, pending request view, admin review actions, member list, remove/leave actions.

UI rules:
- Admin-only actions are hidden and disabled for non-admin users.
- Show clear badges for membership state (pending/accepted/rejected/removed).
- Render field and global errors using shared error contract components.

Implementation tasks:
- Update group backing bean(s) to call REST endpoints and map responses.
- Add form models and validation feedback for group creation and join actions.
- Add action handlers for approve/reject/remove/leave with success and failure messages.
- Keep navigation and screen state synchronized after each mutation.

## 7. Security and Permission Matrix

Required policy matrix:
- Create group: authenticated user.
- Update group settings: group admin only.
- Regenerate invitation code: group admin only.
- Join by invitation code: authenticated user (subject to policy).
- Approve/reject requests: group admin only.
- Remove member: group admin only, with business rules for admin edge cases.
- Leave group: current member.

Enforcement rule:
- Backend is source of truth.
- JSF mirrors permissions for UX but never replaces backend authorization.

## 8. Edge Cases to Implement

- Duplicate membership requests by same user.
- Re-joining after rejection/removal (policy-driven path).
- Last-admin leaving group (block or transfer ownership).
- Invitation code regeneration invalidates old code behavior.
- Concurrent approval/rejection attempts on same request.

## 9. Testing Plan

Backend tests:
- Service tests for all transitions and forbidden actions.
- Controller tests for status codes and response payload shapes.
- Repository tests for key membership and request queries.

Integration tests:
- Full flow: create group -> invite/request -> approve -> member visible -> leave/remove.
- Permission checks for every admin-only endpoint.
- Conflict/state transition tests returning 409 where applicable.

Frontend checks:
- JSF rendering for all membership statuses.
- Error display for validation/auth/permission failures.
- Role-based visibility and action enablement behavior.

## 10. Definition of Done

Phase 3 is done only when all are true:
- Group creation and join flows are functional end to end.
- Request lifecycle transitions are correct and persisted.
- Admin/member restrictions are enforced in backend and mirrored in UI.
- API responses and JSF state remain consistent for success and failure scenarios.
- Integration checklist for Phase 3 is checked and reviewed.

## 11. Handoff Notes for Another Agent or Human

Execution sequence:
1. Implement data model and repository queries first.
2. Implement service transitions with explicit permission checks.
3. Expose stable REST endpoints and DTOs.
4. Wire JSF flows to these endpoints.
5. Add tests before final polish.

Handoff checklist:
- Confirm role and status enums are identical across backend and UI.
- Confirm all endpoints return standardized error responses.
- Confirm groups screen never shows admin actions to non-admin users.
- Confirm Phase 3 integration checklist is updated with evidence.

## 12. Suggested Commit Strategy

- Commit 1: Entity/repository foundation.
- Commit 2: Service logic and transition rules.
- Commit 3: REST controllers and DTO mappings.
- Commit 4: JSF UI flow and backing bean wiring.
- Commit 5: Tests, fixes, and documentation updates.
