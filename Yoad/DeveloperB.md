# Developer B Execution Plan

Developer B works as a full-stack contributor across the entire Shared Basket project. This plan is organized by the phases where Developer B is the phase lead or the main frontend and integration owner, while still covering backend participation in every stage.

## Operating Rule
- Developer B owns the full implementation of the features listed below.
- Each owned feature must be delivered end to end: REST API, service logic, persistence, validation, and the matching JSF screen.
- Developer A only consumes the exposed contracts for dependent work and does not co-implement these features.
- Feature ownership is exclusive so the team works like a startup with clear responsibility boundaries.
- Developer A owns the core platform, authentication, categories and shopping items, and purchase reporting.

## Phase 1 - JSF Application Shell V
- Build the JSF shell, base layout, and reusable page templates.
- Create the shared styling foundation, navigation structure, and standard UI components.
- Align the frontend data structures with the agreed REST DTOs and validation responses.
- Implement the JSF-side contract handling for shared errors and form states.
- Verify the shell renders cleanly without depending on unfinished features.

Control Point: the frontend shell renders cleanly and matches the agreed application structure.

## Phase 3 - Group Management V
- Build the group-creation feature end to end, including the REST API, service logic, persistence, invitation codes, and the JSF group-creation screen.
- Build the group-membership feature end to end, including join-request approval, rejection, removal, and leave-group flows.
- Enforce admin and member permissions in the backend and mirror them in the matching JSF actions.
- Verify the group screens and API responses stay consistent.

Control Point: role restrictions are enforced in the backend and reflected correctly in the UI.

## Phase 5 - Recommendations and Decision Support (Dashboard) V
- Build the recommendation feature end to end with the REST API, service logic, persistence, and the JSF recommendation view.
- Build the dashboard and decision-support screens for suggested items, quick basket rebuilds, and recommendation history.
- Present recommendation results in a clear UI flow that helps users rebuild recurring shopping lists.
- Verify the frontend values match the backend recommendation data exactly.

Control Point: recommendation results are readable and derived from stored history.

## Phase 6 - Real-Time Collaboration
- Build the real-time shopping-list UI feature end to end, including live update subscriptions, frontend refresh behavior, and the matching JSF screens.
- Connect the frontend to backend events so visible list state updates immediately.
- Close remaining JSF gaps and polish the final navigation, messages, and layout.
- Test the UI under concurrent edits and verify the app remains stable.

Control Point: the UI updates in real time, remains stable, and supports the finished product.

## Cross-Phase Frontend Integration Support
- Consume the authentication, category, shopping-item, and reporting APIs owned by Developer A.
- Keep the UI contracts aligned with backend responses and validation states.
- Review integration behavior where owned screens depend on Developer A's features.
- Do not co-implement Developer A's feature logic.

## Developer B Deliverables
- Clean JSF screens and user flows for owned features.
- Group management, recommendation, and real-time collaboration flows.
- Frontend validation and permission-aware navigation.
- Final UI polish and integration coverage across the owned feature set.
