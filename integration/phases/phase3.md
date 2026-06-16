# Phase 3 - Groups, Invitations, Join Requests, and Roles

This phase builds the collaborative workspace model. It covers group creation, invitation codes, join requests, accepting or rejecting members, leaving groups, and enforcing admin and member permissions. This is the phase where multi-user collaboration becomes real.

Tools used in this phase are Spring MVC REST endpoints, Spring Data JPA, Hibernate, Spring Security authorization rules, JSF group-management pages, HTML, CSS, JavaScript, and PostgreSQL group and membership tables.

## Sub-stages
### 3.1 Group creation and settings
- Implement group creation and update endpoints.
- Generate and manage invitation codes.
- Store group metadata and ownership.

### 3.2 Membership lifecycle
- Implement join by invitation code.
- Implement access requests for private groups.
- Allow admins to accept, reject, and remove members.
- Allow users to leave groups safely.

### 3.3 Role enforcement
- Define admin and member roles.
- Enforce backend authorization rules for every group action.
- Mirror permissions in the JSF layer so users only see allowed actions.

### 3.4 Frontend integration
- Build group listing, group details, member management, and request review screens.
- Add UI states for pending, accepted, rejected, and unauthorized actions.

## Work split
- Both Developer A and Developer B work full-stack on groups and membership.
- Phase Lead: Developer A — Co-Lead: Developer B.
- Sub-stage ownership:
	- 3.1 Group creation and settings: Lead Developer A (backend model, invitation code logic), Co-Lead Developer B (group UI and settings pages).
	- 3.2 Membership lifecycle: Joint; Developer B leads join-request UI flows, Developer A leads backend state transitions.
	- 3.3 Role enforcement: Lead Developer A (authorization rules), Co-Lead Developer B (UI permission visibility and guards).
	- 3.4 Frontend integration: Developer B leads implementation, Developer A validates endpoints and provides test data.

## Control checks
- A user can create a group and join it with the correct flow.
- Admin-only actions are blocked for members.
- Join requests move through the expected statuses.
- Invitation codes behave consistently and securely.
