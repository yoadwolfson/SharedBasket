---
goal: Group Management for Developer B
version: 1.0
date_created: 2026-06-14
last_updated: 2026-06-14
owner: Developer B
status: Planned
tags:
  - feature
  - groups
  - jsf
  - backend
  - permissions
---

# Introduction

![Status: Planned](https://img.shields.io/badge/status-Planned-blue)

This plan defines the Developer B-owned group management work, including the JSF group creation and membership screens, the related backend flows, and the permission-aware UI behaviors.

## 1. Requirements & Constraints

- **REQ-001**: Implement the group-creation flow end to end, including REST API, service logic, persistence, and the matching JSF screen.
- **REQ-002**: Implement group membership flows end to end, including join request submission, approval, rejection, removal, and leave-group actions.
- **REQ-003**: Enforce admin and member permissions in the backend and mirror the same restrictions in the JSF actions and visibility rules.
- **REQ-004**: Keep the UI synchronized with backend membership states such as pending, accepted, rejected, and removed.
- **REQ-005**: Ensure group screens stay consistent with the REST responses and validation rules.
- **CON-001**: Treat Developer A-owned security, core platform, and any adjacent shared identity behavior as black-box contracts.
- **CON-002**: Do not change Developer A-owned feature logic while implementing group screens.
- **NOTE-001**: Any shared contract needed from Developer A, such as authenticated user context or navigation state, must be confirmed before UI wiring begins.
- **GUD-001**: Keep admin actions explicit and visibly separated from member-only views.
- **PAT-001**: Model membership transitions as a finite set of backend states and reflect those states directly in the JSF layer.

## 2. Implementation Steps

### Implementation Phase 1

- GOAL-001: Implement group creation, invitation code handling, and group data persistence.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-001 | Define the group entity, repository, and service workflow for creation and metadata updates. |  |  |
| TASK-002 | Implement invitation code generation and storage rules. |  |  |
| TASK-003 | Build the JSF group creation and group details screens with the required input and feedback states. |  |  |

### Implementation Phase 2

- GOAL-002: Implement membership lifecycle flows and permission-aware UI guards.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-004 | Implement join-by-code and private-group request submission flows. |  |  |
| TASK-005 | Implement admin review actions for accept, reject, and remove membership transitions. |  |  |
| TASK-006 | Implement JSF visibility rules and action guards for admin-only and member-only operations. |  |  |

## 3. Alternatives

- **ALT-001**: Implement membership management as UI-only state changes. Rejected because permissions and state transitions must be enforced in the backend.
- **ALT-002**: Merge group creation and membership review into one screen. Rejected because the flows have different users, permissions, and validation paths.

## 4. Dependencies

- **DEP-001**: Developer A authentication and security behavior for identifying the current user.
- **DEP-002**: Developer A shared authorization expectations for role-aware navigation.
- **DEP-003**: PostgreSQL persistence for group and membership records.

## 5. Files

- **FILE-001**: Planned group entity, repository, and service classes.
- **FILE-002**: Planned REST controller and DTO files for group creation and membership lifecycle endpoints.
- **FILE-003**: Planned JSF views and backing beans for group creation, group details, and member management.

## 6. Testing

- **TEST-001**: Verify a user can create a group and see it in the JSF interface.
- **TEST-002**: Verify join-request and invitation-code flows transition through the expected statuses.
- **TEST-003**: Verify admin-only actions are blocked for members both in backend responses and in the JSF action surface.
- **TEST-004**: Verify leave-group behavior preserves group integrity and membership state consistency.

## 7. Risks & Assumptions

- **RISK-001**: Role enforcement may diverge between backend checks and JSF visibility rules if the shared contract is not kept in sync.
- **RISK-002**: Invitation-code handling can become inconsistent if code regeneration and invalidation rules are not centralized.
- **ASSUMPTION-001**: Developer A will keep the shared security and identity contract stable while Developer B builds the group experience.
- **ASSUMPTION-002**: Membership states required by the UI will be available from the backend as explicit response values.

## 8. Related Specifications / Further Reading

- [Developer B Execution Plan](DeveloperB.md)
- [Phase 3 - Groups, Invitations, Join Requests, and Roles](../phases/phase3.md)
- [Shared Basket Project Specification](../BluePrint)