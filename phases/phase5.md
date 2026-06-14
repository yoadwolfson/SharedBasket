# Phase 5 - Purchase History, Expense Tracking, Analytics, and Recommendations

This phase turns stored shopping data into useful business insight. It implements purchase history persistence, spending dashboards, category and product statistics, budget versus actual views, and the recommendation engine that reuses historical purchase patterns.

Tools used in this phase are Spring MVC REST endpoints, Spring Data JPA, Hibernate, PostgreSQL aggregation queries, service-layer analytics logic, JSF dashboard pages, HTML, CSS, and JavaScript.

## Sub-stages
### 5.1 Purchase history
- Store completed purchases as historical records.
- Save product name, purchase price, purchase date, and group reference.
- Keep the history linked to the owning shopping group.

### 5.2 Expense reporting
- Build total spending, spending by category, and spending by product endpoints.
- Add budget versus actual calculation support.
- Support monthly and historical summaries.

### 5.3 Recommendation engine
- Recommend frequently purchased products.
- Suggest products previously bought by the same group.
- Estimate the expected cost of the current basket.
- Support quick rebuilding of recurring shopping lists.

### 5.4 Frontend dashboards
- Build report screens and recommendation views in JSF.
- Present summary cards, charts, and history tables using the REST outputs.

## Work split
- Both Developer A and Developer B work full-stack on analytics, history, and recommendations.
- Phase Lead: Developer A — Co-Lead: Developer B.
- Sub-stage ownership:
	- 5.1 Purchase history: Lead Developer A (persistence and schema), Co-Lead Developer B (UI for viewing history).
	- 5.2 Expense reporting: Joint; Developer A leads aggregation and endpoints, Developer B leads dashboard design and chart integration.
	- 5.3 Recommendation engine: Lead Developer A (service algorithms), Co-Lead Developer B (presentation and quick-list rebuild UX).
	- 5.4 Frontend dashboards: Developer B leads visual polish, Developer A provides test datasets and verifies calculations.

## Control checks
- Purchase history is written only for completed purchases.
- Reports match the stored data.
- Recommendations are derived from actual history, not hard-coded rules.
- Dashboard views load through REST and present consistent values.
