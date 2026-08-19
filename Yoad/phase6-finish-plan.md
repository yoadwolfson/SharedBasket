# Phase 6 Finish Plan - Shopping List Screen Completion and Real Integration

## Goal
Finish the remaining Phase 6 work by completing the shopping-list screen around the real item-creation API already provided by Developer A, removing demo/mock behavior, and validating the list works as a real app flow.

This plan assumes the backend add-item API already exists and the remaining gap is the frontend completion and final hardening.

## Scope
In scope:
- JSF shopping-list screen completion
- item add flow using real REST API
- refresh/reload behavior after create/update/delete
- conflict and stale-data UX
- removal of mock-only realtime demo controls
- final validation with a real group and real item data

Out of scope:
- building the backend item API from scratch
- backend WebSocket/event delivery design if Developer A owns that work
- performance benchmarking beyond basic validation

## Current repo reality
The relevant code is already in place:
- item create API: [src/main/java/com/sharedbasket/controller/rest/ShoppingItemController.java](../src/main/java/com/sharedbasket/controller/rest/ShoppingItemController.java)
- screen/controller: [src/main/java/com/sharedbasket/controller/jsf/ShoppingListController.java](../src/main/java/com/sharedbasket/controller/jsf/ShoppingListController.java)
- screen markup: [src/main/webapp/shopping-list.xhtml](../src/main/webapp/shopping-list.xhtml)

The key fact is that the app is already wired to the real add-item endpoint, but the page still includes mock realtime diagnostics and demo behavior that should be removed before final acceptance.

## Execution plan

### 1. Finalize the real item-add flow
Tasks:
- verify the user is in a selected group before submitting
- ensure admin users create items through POST /api/groups/{groupId}/items
- ensure non-admin users use the request-add workflow correctly
- reload the list immediately after a successful create
- show success or error messages after submission
- reset the item form after a successful save

Acceptance criteria:
- a user can add an item to the selected group without page refresh
- the new item appears in the current list immediately
- validation and server errors are surfaced clearly

### 2. Clean up the UI around the real app flow
Tasks:
- remove or hide mock realtime controls from the shopping page
- keep only production-facing status and recovery controls
- make the item form feel like a standard app form rather than a live-demo console
- ensure alerts are visible and user-friendly

Acceptance criteria:
- the shopping-list page no longer presents mock event buttons or scenario controls
- users see the real app flow only

### 3. Finish edit, delete, and purchased-state behavior
Tasks:
- verify edit calls use the correct PUT endpoint and version conflict handling
- verify delete calls trigger immediate refresh and alerting
- verify mark purchased / unpurchased still refreshes the visible list correctly
- ensure stale versions show a conflict message and reload snapshot when needed

Acceptance criteria:
- item updates remain consistent after edit and refresh
- no silent overwrite occurs on version conflict
- stale data recovers cleanly without broken UI state

### 4. Add the real recovery UX for stale/conflict cases
Tasks:
- keep the existing degraded state logic but map it to actual server responses
- if a conflict occurs, present a clear warning and refresh snapshot
- if a reconnect occurs, re-establish the listed connection and reload latest data
- ensure the screen remains stable during recovery

Acceptance criteria:
- conflict events do not leave the list in a broken state
- users can recover from stale data without manual page reload

### 5. Validate with real data and real users
Test flow:
1. select an existing group
2. add an item using the real form
3. reload or open another client in the same group
4. verify the item appears in the other client without manual refresh of the page
5. attempt simultaneous edit of the same item from two users
6. verify conflict handling triggers and data is preserved
7. disconnect and reconnect and verify list refreshes correctly

Acceptance criteria:
- live item list behavior is stable in a real multi-user flow
- no silent data loss occurs on conflicts
- list state remains accurate after reconnect

## Definition of done for this phase
The phase is complete when all of these are true:
- the item add flow works through the real API
- the shopping-list screen is fully functional and no longer looks like a mock demo
- the UI handles refresh, conflict, and degraded states cleanly
- real group-based item operations are validated end to end
- the screen is ready for final phase sign-off with Developer A

## Recommended order
1. Fix item-add flow and page reload behavior
2. Remove mock demo controls
3. Validate edit/delete/toggle behavior
4. Validate conflict and stale-data recovery
5. Final multi-user verification and sign-off

## Notes for implementation
Focus on the actual JSF lifecycle and data flow instead of reworking the backend. The main remaining work is to make the screen behave correctly with the API that already exists and to remove the remaining mockup state from the page.
