# Shared Basket JSF Shell

Phase 1 foundation is done. This shell gives the app a shared layout, basic controllers, and clear integration points for the backend work.

## What is included

- Base layout template (header, nav, alerts, footer, content area)
- Reusable UI fragments for consistent pages
- Dashboard example page
- Global shell controller for user/session state
- Error and validation helpers
- Bootstrap-based responsive styling

## Main files to know

- `src/main/webapp/WEB-INF/templates/layouts/base-layout.xhtml` - master page layout
- `src/main/webapp/WEB-INF/templates/fragments/` - header, navigation, alerts, footer
- `src/main/java/com/sharedbasket/controller/jsf/ShellController.java` - session-level shell logic
- `src/main/java/com/sharedbasket/controller/jsf/DashboardController.java` - dashboard page controller
- `src/main/java/com/sharedbasket/util/ErrorHandlingUtil.java` - backend error to JSF messages
- `src/main/java/com/sharedbasket/util/ValidationUtil.java` - common validation helpers

## Quick page pattern

Use the base layout and fill the `content` section:

```xml
<ui:composition template="/WEB-INF/templates/layouts/base-layout.xhtml">
    <ui:param name="pageTitle">Dashboard - Shared Basket</ui:param>

    <ui:define name="content">
        <!-- Page content -->
    </ui:define>
</ui:composition>
```

## Integration points for Developer A

All handoff points are marked with `@DevAConnection` comments.

- Auth: `AuthenticationService` provides `UserDTO` (id, email, fullName, role)
- Dashboard data: shopping items + reporting totals
- Navigation: role-based visibility (`ADMIN` or `MEMBER`)
- Alerts: backend errors should map to `ErrorResponseDTO` + field errors

Full checklist: `DEV_A_INTEGRATION_CHECKLIST.md`

## DTOs used in shell

- `UserDTO` - logged-in user info
- `AlertDTO` - UI alert message (`success`, `error`, `warning`, `info`)
- `ErrorResponseDTO` - standard backend error payload
- `FieldErrorDTO` - single field validation error

## Placeholder pages

- `/dashboard.jsf`
- `/shopping-list.jsf`
- `/groups.jsf`
- `/reports.jsf`
- `/recommendations.jsf`

## Current status

- Phase: 1 (Foundation) complete
- Ready for: Developer A service integration
- Still placeholder: shopping, groups, reports, recommendations, real-time collaboration

## Next steps

1. Plug in real authentication service
2. Connect dashboard to real backend data
3. Replace placeholder pages with real forms and actions
4. Add REST client wiring and tests

## Related docs

- `phases/phase1.md`
- `Yoad/DeveloperB.md`
- `DEV_A_INTEGRATION_CHECKLIST.md`
- `BluePrint`

Last updated: 2026-06-16
