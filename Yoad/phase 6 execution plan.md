# Phase 6 Plan - Real-Time Collaboration, Integration, and Hardening (Execution Guide for Human or Agent)

Owner: Developer B
Phase Scope: Real-time UI sync, conflict UX, final JSF completion, and integrated hardening.
Goal: Deliver all Developer B Phase 6 work while explicitly managing Developer A dependencies and using mocks where valid.

## 1. Scope and Boundaries

In scope:
- Real-time client subscription behavior and UI refresh logic.
- Conflict UX for concurrent edits and stale data events.
- Final JSF wiring, navigation/message polish, and regression hardening.
- Multi-client UI integration test scenarios.

Out of scope:
- Implementing server-side WebSocket/event broker internals owned by Developer A.
- Implementing backend concurrency control internals owned by Developer A.
- Replacing Developer A security/performance hardening responsibilities.

## 2. Current Baseline in This Repository

Current planning and ownership references:
- integration/phases/phase6.md
- integration/INTEGRATION_EXPECTATIONS_BY_PHASE.md
- Yoad/feature-real-time-collaboration-plan.md

Current practical state:
- Phase 6 architecture/ownership is documented.
- Real-time production contracts are not yet finalized in this codebase.
- Existing UI foundations and error DTO patterns are available for reuse.

## 3. Phase 6 Execution Strategy (Order Matters)

Step 1: Contract freeze and abstraction boundaries
- Define and freeze event contract shape with Developer A.
- Create a client-side event-consumer interface (adapter boundary) so implementation can switch from mock to real transport without UI rewrites.
- Define stale/conflict/system message taxonomy in UI.

Step 2: Implement UI event consumption and reconciliation
- Wire subscription lifecycle: connect, reconnect, disconnected, degraded.
- Implement list-delta and fallback full-refresh handling.
- Implement user-visible conflict prompts and safe refresh actions.

Step 3: Finalize JSF integration and polish
- Connect remaining screens to final contracts.
- Ensure permission-aware actions and consistent alerts/messages.
- Remove placeholder behaviors accepted as temporary.

Step 4: Multi-client verification and hardening
- Run side-by-side client scenarios and verify update ordering behavior.
- Validate conflict handling and no-silent-overwrite UX.
- Validate error paths and degraded mode behavior.

## 4. Dev A Dependency Matrix with Mockability

Legend:
- Mock viable: YES means you can simulate enough to continue B-owned implementation/testing.
- Mock viable: PARTIAL means some progress possible, but acceptance cannot be completed.
- Mock viable: NO means meaningful validation depends on real Dev A backend behavior.

| Dev A Dependency | Exactly What Is Needed | When Needed in Your Plan | Where Consumed (B side) | Mock Viable? | What Mock Can Validate | What Mock Cannot Validate |
|---|---|---|---|---|---|---|
| Real-time server event delivery | Final transport choice (WebSocket/SSE/etc.), subscription endpoint/topic names, auth handshake, heartbeat/reconnect semantics | Step 1 before coding real client adapter | Real-time client adapter and shopping-list JSF update controller logic | PARTIAL | UI subscription states, render updates, reconnect banners, event parsing | Real broker behavior under load, real auth/session edge cases, network-level delivery guarantees |
| Event payload contract | Exact event schema: eventType, groupId, itemId, version, actor, timestamp, delta payload, optional snapshot hint | Step 1 contract freeze | UI event parser + reconciliation layer | YES | Parser correctness, mapping, conditional UI updates by event type | Whether backend emits complete/correct payloads in all real scenarios |
| Event ordering and idempotency rules | Sequence/version semantics and duplicate-event policy | Step 2 before reconciliation finalization | Client-side reconcile logic | PARTIAL | Client algorithm behavior with synthetic out-of-order/duplicate events | True ordering behavior across clustered backend instances |
| Backend concurrency safeguards | Final optimistic-lock/conflict policy and response code matrix for concurrent update attempts | Step 2 and Step 4 | Conflict UX, retry/refresh flows, message catalog | PARTIAL | UX responses to mocked 409/412-style errors, retry workflows | Actual DB-level race outcomes and true no-data-loss guarantees |
| Final REST consistency contracts | Definitive response bodies for list refresh endpoints after conflict or reconnect | Step 2 and Step 3 | Fallback full-refresh path and stale-state recovery | YES | UI recovers from stale state and renders post-refresh state | Correctness of backend snapshot assembly after heavy concurrent writes |
| Security contract verification | Authorization behavior for realtime channels and event visibility by user/group | Step 4 pre-exit | Access-gated UI actions and connection handling | PARTIAL | UI behavior on mocked unauthorized/forbidden responses | Real security boundary validation, token/session propagation issues |
| Performance and hardening outcomes | Throughput/latency targets, backpressure behavior, failure modes, stability criteria | Step 4 pre-exit | UX thresholds for degraded mode and alerts | NO | N/A beyond synthetic smoke checks | Real production-like performance, memory pressure, event storm handling |
| Contract verification sign-off | Final approved contract docs and compatibility commitments from Developer A | End of Step 4 | All integration-facing controllers/views | NO | N/A | Formal cross-team acceptance and merge gate compliance |

## 5. Exactly What to Request from Dev A

Required contract packet for Phase 6:
- Realtime connection spec:
  - transport type
  - endpoint URL/path
  - auth headers/cookies/session expectations
  - heartbeat/timeout/reconnect policy
- Event schema spec:
  - event types and required fields
  - version/sequence behavior
  - duplicate-event and out-of-order guarantees
- Concurrency/error spec:
  - exact conflict status codes and payload shape
  - stale-data recovery expectations
- Security spec:
  - who can subscribe to which group channels
  - expected forbidden/unauthorized responses
- Hardening spec:
  - expected performance envelope
  - degraded mode triggers and server signaling

## 6. Mock Strategy You Can Implement Now

Recommended mock components:
- MockRealtimeEventSource interface + implementation that replays scripted events.
- EventScenarioCatalog for deterministic sequences:
  - normal updates
  - duplicate events
  - out-of-order events
  - disconnect/reconnect bursts
  - conflict response injections
- MockConflictApiResponder for synthetic 409-like flows.

Recommended test tiers:
- Tier A (no Dev A required):
  - UI rendering under event sequences
  - reconnect UX
  - conflict prompt UX
  - fallback refresh UX
- Tier B (Dev A required):
  - true end-to-end multi-client sync
  - real authorization boundaries
  - real no-data-loss concurrency proof
  - performance/stability sign-off

## 7. Decision Table: Can We Proceed Without Dev A?

- Real-time UX implementation: YES, with mock source.
- Conflict UX implementation: YES, with mocked conflict responses.
- Final acceptance of real-time correctness: NO, requires Dev A backend.
- Final acceptance of no-data-loss concurrency: NO, requires Dev A backend safeguards.
- Final security and performance sign-off: NO, requires Dev A verification.

## 8. Definition of Done for Phase 6 (B-owned + integration reality)

B-owned done:
- Client subscription abstraction implemented.
- UI handles connect/reconnect/disconnect/degraded states.
- Conflict UX and fallback refresh are complete.
- Multi-client simulated scenarios are automated and stable.

Phase-6 fully done (cross-team):
- Real server events validated across multiple real clients.
- Concurrent edits verified with no silent overwrite.
- Security/validation checks pass in integrated environment.
- Contract and performance hardening sign-off completed with Dev A.

## 9. Suggested Commit Strategy

- Commit 1: Realtime client abstraction and mock event source.
- Commit 2: UI subscription states and reconciliation logic.
- Commit 3: Conflict UX and fallback refresh flows.
- Commit 4: Simulated multi-client scenario suite.
- Commit 5: Dev A integration wiring and final hardening adjustments.
