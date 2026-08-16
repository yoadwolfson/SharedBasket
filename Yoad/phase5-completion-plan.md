---
goal: Complete Phase 5 - Recommendations, Reports and Dashboard data wiring
version: 1.0
date_created: 2026-08-16
owner: Developer B
status: Ready to implement
tags:
  - feature
  - recommendations
  - reports
  - dashboard
  - jsf
---

# Phase 5 Completion Plan

## 0. Baseline findings (verified against current code)

What already exists and can be reused directly (no waiting on anyone):
- `domain/PurchaseRecord.java` + `repo/PurchaseRecordRepository.java` — persisted purchase history per group (`shoppingGroup`, `category`, `itemName`, `quantity`, `pricePaid`, `purchasedAt`). This is the source of truth for both reports and recommendations.
- `domain/ShoppingItem.java` + `repo/ShoppingItemRepository.java` — active list items, `purchased` flag, `estimatedPrice`, `category`.
- `domain/Category.java`, `domain/SbGroup.java`, `domain/GroupMember.java` — group/category scoping already modeled.
- `service/AuthenticationService.java` — `getCurrentUser()` gives the logged-in `UserDTO`.
- `service/GroupService.java` + `GroupServiceImpl` — `listMyGroups(userId)` is the only way today to resolve which group(s) a user belongs to. There is **no shared "current group" concept yet** — `GroupsController` keeps its own `selectedGroupId` locally.
- `controller/jsf/GroupsController.java` — the established JSF pattern: `@ViewScoped` bean, injects `RestTemplate` via `JsfRestClientSupport.resolveBean`, calls the app's own REST API (never calls services directly). Recommendations/Reports/Dashboard beans must follow this same pattern for consistency.
- `DashboardController`, `recommendations.xhtml`, `reports.xhtml`, `RecommendationDTO`, `StatisticsDTO`, `FrontendDtoAdapter` are all placeholders with hard-coded sample data — nothing here is wired to real persistence.
- There is no `ReportController`/`RecommendationController` REST endpoint, no `ReportService`/`RecommendationService`, and no aggregation queries in `PurchaseRecordRepository` yet.

Conclusion: the DB-level building blocks (Phase 5.1 persistence) are already done. What's missing is entirely 5.2/5.3/5.4 — reporting aggregation, the recommendation algorithm, REST endpoints, and real JSF wiring. This can be implemented end-to-end now without any external blocker.

## 1. Shared prerequisite: "current group" resolution

Dashboard, Reports and Recommendations all need a group scope, but no shared mechanism exists yet.

- TASK-000a: Add `currentGroupId` (and cached `currentGroup` name) to `ShellController` (session-scoped), resolved once per session as the first group from `GroupService.listMyGroups(currentUserId)` (or the group most recently selected in `GroupsController`, if we choose to also push selection there).
- TASK-000b: Expose a small `GET /api/groups/current` convenience endpoint (or reuse `GET /api/groups` and pick `[0]` client-side) so JSF beans stay consistent with the REST-only access pattern.
- Decision: keep it simple — reuse existing `GET /api/groups`, JSF beans pick the first entry, matching how `GroupsController.reloadGroups()` already works. No new endpoint required unless multi-group switching is in scope (it isn't for Phase 5).

## 2. Backend: Reporting (Phase 5.2)

### New repository queries (`PurchaseRecordRepository`)
- `findByShoppingGroup_IdAndPurchasedAtBetween(Long groupId, Instant from, Instant to)`
- Aggregation via JPQL `@Query`:
  - total spend + item count for a group/date range
  - spend grouped by `category.name`
  - spend grouped by `itemName`

### New DTOs (`dto` package)
- `StatisticsDTO` — extend with `periodStart`, `periodEnd`, `currency` (fixed `"USD"` or group setting if one exists), keep `totalItems`, `totalCost`, `itemsPurchased`.
- `SpendingByCategoryDTO` (`categoryName`, `amount`, `quantity`, `percentageOfTotal`)
- `SpendingByProductDTO` (`productName`, `amount`, `quantity`, `percentageOfTotal`)
- `BudgetVsActualDTO` (`categoryName`, `budgetAmount`, `actualAmount`, `variance`) — if no budget concept exists yet in the domain model, scope this out of Phase 5 v1 and mark as a stub returning `actualAmount` only with `budgetAmount = null`; do not invent a fake budget field in the DB.
- `PurchaseHistoryEntryDTO` (`itemName`, `categoryName`, `quantity`, `unit`, `pricePaid`, `purchasedAt`, `purchasedByUserName`)

### New service (`service/ReportService.java` + `service/impl/ReportServiceImpl.java`)
- `StatisticsDTO getStatistics(Long currentUserId, Long groupId, Instant from, Instant to)`
- `List<SpendingByCategoryDTO> getSpendingByCategory(Long currentUserId, Long groupId, Instant from, Instant to)`
- `List<SpendingByProductDTO> getSpendingByProduct(Long currentUserId, Long groupId, Instant from, Instant to)`
- `List<PurchaseHistoryEntryDTO> getPurchaseHistory(Long currentUserId, Long groupId, Instant from, Instant to)`
- Enforce membership check (user must be a `GroupMember` of `groupId`) — reuse the same `ForbiddenOperationException`/`ResourceNotFoundException` conventions as `GroupServiceImpl`.

### New REST controller (`controller/rest/ReportController.java`)
- `GET /api/reports/statistics?groupId=&from=&to=`
- `GET /api/reports/spending-by-category?groupId=&from=&to=`
- `GET /api/reports/spending-by-product?groupId=&from=&to=`
- `GET /api/purchase-history?groupId=&from=&to=`
- Default `from`/`to` to "last 30 days" when omitted.

## 3. Backend: Recommendations (Phase 5.3)

### Recommendation logic (no ML — deterministic, explainable rules per spec: "derived from actual history, not hard-coded rules")
Source: aggregate `PurchaseRecordRepository` rows per group.
- Frequency: count of purchase records per `itemName` (+ `category`) within a lookback window (e.g. last 90 days), grouped by group.
- Recency-aware ranking: combine frequency with days-since-last-purchase to prioritize recurring-but-due items.
- Estimated cost: average `pricePaid` for that item across history (fallback to latest `ShoppingItem.estimatedPrice` if never purchased).
- Exclude items already active/unpurchased on the current `ShoppingItem` list for that group (don't recommend what's already queued).

### New DTOs
- Extend `RecommendationDTO`: `recommendationId`, `itemName`, `categoryName`, `rationale` (e.g. "Bought 6 times in last 90 days"), `frequency`, `estimatedUnitPrice`, `estimatedTotalContribution`, `sourceWindowStart`, `sourceWindowEnd`.
- `RebuildRequestDTO` (`recommendationIds: List<Long>` or synthetic ids, `targetGroupId`)
- `RebuildResultDTO` (`createdItemCount`, `skippedItemCount`, `conflicts: List<String>`)

### New service (`service/RecommendationService.java` + impl)
- `List<RecommendationDTO> getRecommendations(Long currentUserId, Long groupId)`
- `RebuildResultDTO rebuildFromRecommendations(Long currentUserId, Long groupId, RebuildRequestDTO request)` — creates `ShoppingItem` rows (unpurchased) for the selected recommendations, skipping any item name already present and unpurchased in that group; wrap in `@Transactional`.

### New REST controller (`controller/rest/RecommendationController.java`)
- `GET /api/recommendations?groupId=`
- `POST /api/recommendations/rebuild`

## 4. Frontend: JSF wiring (Phase 5.4)

Follow the `GroupsController` pattern exactly (ViewScoped bean + RestTemplate + `JsfRestClientSupport`), not direct service injection, to stay consistent with the rest of the JSF layer.

- TASK-004a: Rewrite `DashboardController.loadDashboardData()` to call `GET /api/reports/statistics` and `GET /api/recommendations` for the resolved current group, replacing `FrontendDtoAdapter.createPlaceholderStatistics()`/`createPlaceholderRecommendations()`. Keep `shoppingItems` sourced from the real `GET /api/shopping-items` (or equivalent) list, unpurchased only.
- TASK-004b: New `RecommendationsController` (`@ManagedBean("recommendationsController")`, `@ViewScoped`) — loads `GET /api/recommendations`, exposes selection state (checkboxes) for quick-rebuild, calls `POST /api/recommendations/rebuild`, surfaces success/error alerts, and shows empty/loading/error states.
- TASK-004c: New `ReportsController` (`@ManagedBean("reportsController")`, `@ViewScoped`) — loads statistics, spending-by-category, spending-by-product, purchase history, with a simple date-range filter (default last 30 days); empty/loading/error states.
- TASK-004d: Rebuild `recommendations.xhtml` — replace the placeholder paragraph with: summary header, recommendation cards/table (item, category, rationale, frequency, estimated cost), checkbox selection + "Add selected to list" button wired to `recommendationsController.rebuild`, empty-state message when no recommendations, error alert region.
- TASK-004e: Rebuild `reports.xhtml` — replace the placeholder paragraph with: statistics summary cards, spending-by-category table, spending-by-product table, purchase history table, date-range filter form.
- TASK-004f: Update `dashboard.xhtml` bindings only if the underlying DTO field names change (e.g. new `StatisticsDTO`/`RecommendationDTO` fields) — otherwise it already renders `statistics`/`recommendations` generically and needs no structural change.

## 5. Testing

- Repository/service unit tests: `ReportServiceImplTest`, `RecommendationServiceImplTest` — cover empty history, single-group scoping, membership rejection, date-range filtering.
- REST layer tests: `ReportControllerTest`, `RecommendationControllerTest` — verify JSON shape and 403/404 on non-member access.
- Manual/JSF smoke check: log in, visit dashboard/recommendations/reports, confirm no placeholder text remains and values reflect seeded `PurchaseRecord` data (use existing Flyway migration / test data, or seed a few purchase records manually for verification).

## 6. Suggested commit order

1. Repository aggregation queries + report/recommendation DTOs.
2. `ReportService` + `ReportController` + tests.
3. `RecommendationService` + `RecommendationController` (incl. rebuild) + tests.
4. Shared "current group" resolution in `ShellController`.
5. `ReportsController` + `reports.xhtml`.
6. `RecommendationsController` + `recommendations.xhtml` + rebuild UI.
7. `DashboardController` real data wiring, remove now-unused placeholder methods from `FrontendDtoAdapter`.

## 7. Out of scope for this pass

- Charting/graphics libraries for reports (tables/cards only, per existing plain-JSF style).
- Budget-vs-actual unless a budget field is explicitly added to the domain model first.
- Multi-group switcher UI (only first/only group is used for now).
