# Phase 5 Plan - Recommendations and Decision Support (Execution Guide for Human or Agent)

Owner: Developer B
Phase Scope: Purchase history presentation, reporting dashboards, recommendation UX, and quick rebuild flows.
Goal: Deliver Phase 5 frontend and integration work end to end while consuming Developer A analytics and recommendation contracts as black-box dependencies.

## 1. Scope and Boundaries

In scope:
- Recommendation and decision-support UX in JSF.
- Reports dashboard UX in JSF.
- Dashboard statistic wiring and recommendation cards/tables.
- Quick basket rebuild action flow from recommendation payloads.
- Exact frontend mapping of backend analytics values without local recomputation drift.

Out of scope:
- Rewriting purchase history persistence internals owned by Developer A.
- Rewriting reporting aggregation logic owned by Developer A.
- Rewriting recommendation algorithms owned by Developer A.

## 2. Current Baseline in This Repository

Existing files already in place:
- src/main/WEB-INF/views/recommendations.xhtml (placeholder)
- src/main/WEB-INF/views/reports.xhtml (placeholder)
- src/main/java/com/sharedbasket/controller/DashboardController.java (placeholder statistics/recommendations)
- src/main/java/com/sharedbasket/dto/StatisticsDTO.java (minimal DTO)
- src/main/java/com/sharedbasket/dto/RecommendationDTO.java (minimal DTO)
- src/main/java/com/sharedbasket/util/FrontendDtoAdapter.java (placeholder data)
- Yoad/DEV_A_INTEGRATION_CHECKLIST.md (existing dependency notes)

Key gap:
- UI currently uses placeholder data and cannot be finalized until Developer A delivers stable reporting and recommendation contracts.

## 3. Phase 5 Delivery Strategy (Order Matters)

Step 1: Contract-first alignment
- Freeze API contracts with Developer A before deep UI wiring.
- Lock DTO field names, enum values, pagination/filter params, and error payload shape.
- Document sample payloads for reports, history, and recommendations.

Step 2: Build recommendation and reports backing beans
- Add dedicated controllers/backing beans for recommendations and reports views.
- Replace placeholder adapter usage with service calls through agreed contracts.
- Preserve strict separation: no local analytics recomputation in JSF layer.

Step 3: Build decision-support UI
- Recommendations view: suggested items, reason/context fields, expected cost, history references.
- Reports view: total spend, by category/product, budget-vs-actual, date filters.
- Dashboard cards: consume same backend values to avoid drift.

Step 4: Implement quick rebuild flow
- Add rebuild action command and request payload mapping.
- Add optimistic UX handling: success, partial success, validation error, permission error.

Step 5: Verification and hardening
- Validate every displayed value against backend payload values.
- Validate empty/loading/error states.
- Add integration checklist evidence for Phase 5 exit criteria.

## 4. Exactly What Is Needed from Developer A, Where, and When

### 4.1 Dependency Timeline Matrix

| When You Need It | What You Need from Dev A | Where It Is Consumed in Your Code | Why It Is Blocking at That Step | Required Acceptance Signal |
|---|---|---|---|---|
| Before Step 2 starts | Auth/session user and group scoping contract (current user identity, active group rules) | src/main/java/com/sharedbasket/controller/jsf/DashboardController.java and new recommendations/reports controllers | All analytics/recommendation calls must be scoped to correct user/group | Written contract or existing stable AuthenticationService behavior confirmed |
| Before Step 2 starts | Purchase history read endpoints and query params (date range, group scope, paging) | src/main/WEB-INF/views/reports.xhtml and reports backing bean | History section cannot be wired without final endpoint and filter contract | Endpoint path + params + sample response approved |
| Before Step 2 starts | Reporting endpoints: totals, category breakdown, product breakdown, budget-vs-actual | src/main/WEB-INF/views/reports.xhtml, src/main/WEB-INF/views/dashboard.xhtml, src/main/java/com/sharedbasket/dto/StatisticsDTO.java | Cards/tables cannot be finalized until field set and numeric semantics are fixed | Field dictionary with meaning and units (currency, period) approved |
| Before Step 3 starts | Recommendation endpoint payload from real history (suggested items, rationale/context, confidence/frequency, estimated cost) | src/main/WEB-INF/views/recommendations.xhtml, src/main/java/com/sharedbasket/dto/RecommendationDTO.java | Recommendation UX structure depends on payload shape and explanation fields | JSON examples and error cases approved |
| Before Step 4 starts | Quick rebuild endpoint contract (request body, idempotency/duplicate rules, response summary) | recommendations backing bean action method and recommendations.xhtml action buttons | Rebuild actions cannot be completed safely without server-side contract | Request/response schema and state-transition rules approved |
| During Step 5 validation | Error contract for analytics/recommendation endpoints (400/401/403/404/409 shape) | Alerts and field feedback in recommendations/reports/dashboard views | UX cannot show precise error handling without consistent error payloads | Error response examples for each code approved |
| During Step 5 validation | Deterministic test dataset and expected output snapshots | UI verification for dashboard, reports, recommendations | Required to prove frontend values exactly match backend values | Golden dataset and expected result sheet provided |

### 4.2 Exact Endpoint Set to Request from Dev A

Minimum endpoint contract needed for Phase 5 completion:
- GET /api/reports/statistics
- GET /api/reports/spending-by-category
- GET /api/reports/spending-by-product
- GET /api/reports/budget-vs-actual
- GET /api/purchase-history
- GET /api/recommendations
- POST /api/recommendations/rebuild

If Developer A uses different paths, request a final path map and do not proceed with deep wiring until that map is frozen.

### 4.3 Exact DTO Fields to Freeze with Dev A

Statistics contract (minimum):
- totalItems
- totalCost
- itemsPurchased
- periodStart
- periodEnd
- currency

Report row contracts (minimum):
- categoryName or productName
- amount
- quantity
- percentageOfTotal

Recommendation contract (minimum):
- recommendationId
- itemName
- categoryName
- rationale
- frequency
- estimatedUnitPrice
- estimatedTotalContribution
- sourceWindowStart
- sourceWindowEnd

Quick rebuild contract (minimum):
- request: recommendationIds or recommendationBatchId, targetGroupId
- response: createdItemCount, skippedItemCount, conflicts

Error contract (minimum):
- message
- code
- fieldErrors[] (optional)
- timestamp

## 5. Dev A Request Pack (Send This Early)

Send this request to Developer A before Step 2:
- Final endpoint paths and HTTP methods for all Phase 5 APIs.
- Query parameters and defaults for date range/group scoping/pagination.
- Final DTO schemas with field types and enum values.
- Error payload schema and status code matrix.
- One golden test dataset plus expected outputs for:
  - statistics summary
  - spending by category/product
  - budget-vs-actual
  - recommendations list
  - quick rebuild result summary

## 6. Work You Can Do Immediately Without Waiting

Non-blocked work:
- Finalize page layout and component structure for recommendations and reports.
- Implement loading/empty/error skeleton states.
- Prepare backing bean method signatures and UI model adapters.
- Add placeholder-to-real adapter boundaries so only one layer changes when contracts arrive.

Partially blocked work:
- Numeric formatting and table definitions can be drafted, but final field mapping waits on Dev A DTO freeze.
- Rebuild action UI can be built, but endpoint integration waits on request/response freeze.

Blocked until Dev A delivers:
- Contract-accurate value rendering and final integration tests.
- Phase 5 exit proof that frontend values exactly match backend outputs.

## 7. Testing and Sign-off Plan

Contract verification:
- Validate JSON fields and types against frozen schemas.
- Validate each error status handling path in UI.

Functional verification:
- Reports values match backend payloads exactly.
- Recommendation cards and history match backend payloads exactly.
- Quick rebuild result summaries match backend response exactly.

Phase 5 done only when all are true:
- No placeholder data remains in reports/recommendations/dashboard paths.
- All Phase 5 API contracts from Dev A are frozen and consumed.
- Integration checklist in integration/INTEGRATION_EXPECTATIONS_BY_PHASE.md is fully checked for Phase 5.

## 8. Suggested Commit Strategy

- Commit 1: Phase 5 JSF structure and backing bean scaffolding.
- Commit 2: Reports and recommendations UI states (loading/empty/error).
- Commit 3: Contract adapters and endpoint integration.
- Commit 4: Quick rebuild action wiring.
- Commit 5: Contract-accuracy verification and integration evidence.
