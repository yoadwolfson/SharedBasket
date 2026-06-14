# Phase 4 - Categories and Shopping Item Management

This phase implements the core shopping list workflow. It covers reusable categories, shopping item CRUD, quantity and price updates, purchase marking, and the first level of concurrency protection for list edits. This is the main day-to-day feature set used by group members.

Tools used in this phase are Spring MVC REST APIs, Spring Data JPA, Hibernate, JSF shopping list pages, HTML, CSS, JavaScript, PostgreSQL, and optimistic concurrency support in the persistence layer.

## Sub-stages
### 4.1 Category management
- Implement category creation, listing, update, and deletion rules.
- Keep categories reusable and independent from products.
- Prevent invalid category states and duplicates.

### 4.2 Shopping item CRUD
- Implement item creation, update, deletion, and status change operations.
- Support quantity and price updates.
- Store creator and creation timestamp information.

### 4.3 Concurrency control
- Add optimistic locking or equivalent update protection.
- Prevent lost updates when two users edit the same item.
- Return clear conflict responses for the frontend.

### 4.4 Frontend integration
- Build the shopping list screens, edit forms, category selectors, and purchase actions.
- Show live validation and conflict messages in the UI.

## Work split
- Developer B owns the shopping list JSF screens, category forms, item editing UI, and user interaction flow.
- Developer A owns the backend category and item services, repository logic, and concurrency rules.

## Control checks
- Categories can be managed without coupling them to a single product.
- Shopping items can be created, edited, purchased, and deleted.
- Concurrent edits do not silently overwrite each other.
- The frontend shows the correct state after each update.
