# Phase 2 - Authentication and User Profile Management

This phase delivers registration, login, password hashing, authentication, and profile management. The user identity layer must be complete before group and permissions work begins, because every later module depends on a trusted authenticated user.

Tools used in this phase are Spring Security, Spring MVC REST endpoints, Spring Data JPA, Hibernate, JSF login and registration pages, HTML, CSS, JavaScript, and PostgreSQL user storage.

## Sub-stages
### 2.1 Registration flow
- Implement user registration endpoints and validation.
- Store passwords using secure hashing.
- Handle duplicate email and invalid input cases.

### 2.2 Login and session handling
- Implement login using email and password.
- Configure authentication and authorization entry points.
- Return stable responses for the frontend to consume.

### 2.3 Profile management (wont be used)
- Add profile retrieval and profile update operations.
- Allow users to view and update allowed identity fields.
- Keep password updates separate from profile updates.

### 2.4 Frontend integration
- Build JSF registration, login, and profile views.
- Connect the views to the backend REST APIs.
- Show validation and authorization messages clearly.

## Work split
- Both Developer A and Developer B work full-stack on authentication and profiles.
- Phase Lead: Developer B — Co-Lead: Developer A.
- Sub-stage ownership:
	- 2.1 Registration flow: Lead Developer B (frontend UX and validation), Co-Lead Developer A (secure persistence and hashing).
	- 2.2 Login and session handling: Lead Developer A (security configuration), Co-Lead Developer B (JSF login/session UX).
	- 2.3 Profile management: Joint; Developer A leads backend DTOs and rules, Developer B leads profile UI.
	- 2.4 Frontend integration: Developer B leads integration, Developer A provides test endpoints and mocks when needed.

## Control checks
- A new user can register successfully.
- A user can log in with email and password.
- Passwords are never stored in plain text.
- Profile updates persist correctly.
- Unauthorized access is rejected on both backend and frontend paths.
