---
goal: JSF Application Shell for Developer B
version: 1.0
date_created: 2026-06-14
last_updated: 2026-06-16
owner: Developer B
status: Completed
tags:
  - feature
  - jsf
  - frontend
  - foundation
---

# Introduction

![Status: Completed](https://img.shields.io/badge/status-Completed-brightgreen)

This plan defines the Developer B-owned JSF application shell, base layout, shared UI components, and frontend contract handling needed to support the Shared Basket product.

## 1. Requirements & Constraints

- **REQ-001**: Create a reusable JSF shell with a base layout, navigation, and shared page templates.
- **REQ-002**: Provide a consistent styling foundation for all Developer B-owned screens.
- **REQ-003**: Align frontend models and form states with the agreed REST DTOs and validation error shapes.
- **REQ-004**: Implement shared error rendering and form feedback behavior in the JSF layer.
- **REQ-005**: Ensure the shell renders cleanly without requiring unfinished business features.
- **CON-001**: Treat Developer A-owned authentication, categories, shopping items, and purchase reporting as black-box dependencies.
- **CON-002**: Do not implement or infer Developer A internal service logic.
- **NOTE-001**: Any screen or component that consumes Developer A contracts must synchronize expected request and response fields before implementation.
- **GUD-001**: Keep layout components small, reusable, and screen-agnostic.
- **PAT-001**: Centralize JSF layout fragments and shared validation display rules.

## 2. Implementation Steps

### Implementation Phase 1

- GOAL-001: Establish the JSF shell structure and shared page composition.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-001 | Define the base JSF layout, template regions, and shared navigation structure. | X | 2026-06-16 |
| TASK-002 | Create reusable UI fragments for headers, side navigation, footers, and alert areas. | X | 2026-06-16 |
| TASK-003 | Add the screen-level page composition pattern for owned views. | X | 2026-06-16 |

### Implementation Phase 2

- GOAL-002: Align frontend data handling and validation presentation with shared REST contracts.

| Task     | Description | Completed | Date |
| -------- | ----------- | --------- | ---- |
| TASK-004 | Define shared frontend DTO adapters for request, response, and validation payload mapping. | X | 2026-06-16 |
| TASK-005 | Implement JSF form error rendering and field-level validation display behavior. | X | 2026-06-16 |
| TASK-006 | Add placeholder-safe screen states that do not depend on unfinished backend features. | X | 2026-06-16 |

## 3. Alternatives

- **ALT-001**: Build a page-by-page JSF implementation without shared layout fragments. Rejected because it increases duplication and makes contract changes harder to propagate.
- **ALT-002**: Delay shell work until all backend endpoints are finished. Rejected because the UI contract layer needs to stabilize early for parallel development.

## 4. Dependencies

- **DEP-001**: Developer A authentication contract and session state exposure.
- **DEP-002**: Developer A category, shopping item, and reporting response formats.
- **DEP-003**: JSF runtime support, HTML, CSS, and JavaScript assets used by the shell.

## 5. Files

- **FILE-001**: Planned JSF layout and template files under the frontend view folder, such as a base template and shared fragments.
- **FILE-002**: Planned backing beans or view models for shell-level navigation and error state handling.
- **FILE-003**: Planned shared frontend contract adapters for DTO and validation payload mapping.

## 6. Testing

- **TEST-001**: Verify the shell renders with the shared layout fragments and no unresolved template references.
- **TEST-002**: Verify validation errors display consistently for field-level and global form states.
- **TEST-003**: Verify shell pages can load without requiring unfinished feature endpoints.

## 7. Risks & Assumptions

- **RISK-001**: Contract drift between JSF payload expectations and backend DTOs may break form submission flows.
- **RISK-002**: Shared layout changes may cascade into multiple screens if fragment boundaries are not stable.
- **ASSUMPTION-001**: Developer A will publish stable contract shapes for auth and shared data before integration-dependent screens are finalized.
- **ASSUMPTION-002**: The JSF runtime and supporting static assets are available in the target application structure.

## 8. Related Specifications / Further Reading

- [Developer B Execution Plan](DeveloperB.md)
- [Phase 1 - Foundation, Architecture, and Shared Contracts](../phases/phase1.md)
- [Shared Basket Project Specification](../BluePrint)