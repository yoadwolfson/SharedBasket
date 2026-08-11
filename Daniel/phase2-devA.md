# Phase 2 Dev A Implementation Plan

This is the execution guide for Developer A in Phase 2.
It is written as a practical step-by-step checklist for implementation and verification.

## 1. Confirmed Context (from user)

- Authentication model: cookie-based session.
- Password policy for now: minimal (no strict complexity requirement yet).
- Priority: implement everything in Phase 2 that is not already done, end to end.

## 2. Phase 2 Goal

Deliver authentication and profile management end to end:

- Registration API + service + persistence + JSF flow.
- Login API + service + session handling + JSF flow.
- Profile read/update API + service + persistence + JSF flow.
- Security and authorization behavior aligned with cookie sessions.
- Error responses aligned with existing shared ErrorResponseDTO contract.

## 3. What Is Already There vs Missing

### Already available

- Base user entity and repository.
- AuthenticationService interface contract.
- Dev-only fallback auth implementation.
- Global exception mapping and ErrorResponseDTO/FieldErrorDTO contract.
- Placeholder login page and shell wiring points.

### Missing for true Phase 2 completion

- Real production authentication implementation (not dev fallback only).
- Registration endpoint and flow.
- Real login endpoint with credential verification and session creation.
- Current-user and logout endpoint behavior tied to real session state.
- Profile read/update endpoints and service logic.
- Security configuration that protects private endpoints.
- Real JSF registration/login/profile flows connected to backend APIs.

## 4. Clarification You Asked: "fields of what?"

This refers to two field sets:

### A) Auth request/response fields

These are fields sent by frontend to backend for register/login/profile APIs.

Proposed minimum set for Phase 2:

- Register request: email, username, password, firstName, lastName
- Login request: email, password
- Profile response: id, email, fullName, role
- Profile update request: firstName, lastName, username

### B) Profile editable fields

These are the user fields you allow update in profile screen.

Recommended for Phase 2:

- Editable: firstName, lastName, username
- Not editable in this phase: email, passwordHash, role
- Password change: keep as separate operation (future step), not mixed into profile update

## 5. Selected Role Strategy in UserDTO

For Phase 2, UserDTO.role will use a stable default value from auth context.

- Use role = MEMBER by default in Phase 2.
- Do not derive role from group membership yet.
- Move to group-context-aware role logic in Phase 3+ when membership context is fully active.

## 6. Step-by-Step Implementation Plan

## Step 0 - Create branch checkpoint and task list

- Confirm current branch is phase2devA.
- Track each step below as checkboxes in your working notes.

Done when:
- You can point to a single checklist and all tasks are traceable.

## Step 1 - Define Phase 2 DTOs

Create DTOs for:

- RegisterRequestDTO
- LoginRequestDTO
- Return UserDTO directly for login success response (no separate LoginResponseDTO in Phase 2)
- ProfileUpdateRequestDTO

Rules:

- Keep DTO names consistent with existing code style.
- Keep responses compatible with ErrorResponseDTO on failure.

Done when:
- Endpoints can be declared without using raw maps or ad hoc payloads.

## Step 2 - Implement real auth service (cookie session)

Create a production auth service implementation for AuthenticationService.

Responsibilities:

- getCurrentUser(): read user identity from active authenticated session.
- isAuthenticated(): true only if valid auth/session exists.
- logout(): invalidate current session.
- invalidateUserSessions(userId): basic support for future admin/security needs.

Notes:

- Keep dev fallback service available for development-only profile if needed.
- Ensure production implementation is primary in normal runtime.

Done when:
- Shell loads current user from real session, not hardcoded dev identity.

## Step 3 - Registration backend

Implement registration service and endpoint:

- Validate required fields.
- Reject duplicate email and duplicate username.
- Hash password before saving to passwordHash.
- Save user enabled=true.

Error handling:

- Use 400 for bad input.
- Use 409 for duplicates.
- Return ErrorResponseDTO shape.

Done when:
- New user can register and login with that account afterward.

## Step 4 - Login backend

Implement login endpoint:

- Accept email + password.
- Verify user exists and is enabled.
- Verify password hash.
- Establish cookie-based authenticated session.

Responses:

- Success: return current UserDTO and authenticated session cookie.
- Failure: return 401 for bad credentials, 403 for disabled/forbidden as needed.

Done when:
- Browser receives session cookie and subsequent protected calls resolve current user.

## Step 5 - Current user and logout endpoints

Add endpoints such as:

- GET current user
- POST logout

Behavior:

- Current user returns UserDTO from session.
- Logout invalidates session and cookie state.

Done when:
- After logout, protected endpoints reject access until new login.

## Step 6 - Profile read/update backend

Implement:

- GET profile
- PUT/PATCH profile update

Rules:

- Update only allowed fields for this phase.
- Validate non-empty and simple length constraints.
- Keep password change separate.

Done when:
- User can update profile and immediately read updated values.

## Step 7 - Security config hardening

Replace permissive security setup with Phase 2 behavior:

- Public: login, register, static resources.
- Protected: profile and other authenticated APIs.
- Keep cookie/session auth compatible with JSF frontend flows.

Done when:
- Unauthorized requests to protected endpoints are blocked consistently.

## Step 8 - JSF registration flow

Create registration page and backing controller logic:

- Submit register form to backend endpoint.
- Show field errors and global errors from ErrorResponseDTO.
- On success, redirect to login.

Done when:
- Registration works from UI without API manual testing.

## Step 9 - JSF login flow

Replace placeholder login page behavior:

- Accept email/password.
- Call backend login endpoint.
- Persist cookie session via browser automatically.
- Redirect authenticated users to dashboard.

Done when:
- Login page is fully functional and no longer placeholder-only.

## Step 10 - JSF profile flow

Create profile page and backing controller:

- Load profile on page open.
- Submit update request.
- Render success or validation errors clearly.

Done when:
- Profile screen is usable and reflects persisted backend state.

## Step 11 - Shell/session integration check

Validate shell behavior:

- Header shows authenticated user info.
- logout action clears session and redirects to login.
- guest placeholders disappear when authenticated.

Done when:
- Shell behavior matches authentication state at all times.

## Step 12 - End-to-end test checklist (must pass)

- Register new user successfully.
- Reject duplicate email and duplicate username.
- Login success with valid credentials.
- Login failure with wrong password.
- Protected API denied when not logged in.
- Profile fetch/update works after login.
- Logout invalidates session.
- Password is never stored or returned as plain text.

Done when:
- All tests above pass in local run with cookie sessions.

## 7. Implementation Order (do not skip)

1. DTO contracts
2. Real auth service
3. Registration backend
4. Login backend
5. Current-user + logout endpoints
6. Profile backend
7. Security hardening
8. JSF registration/login/profile
9. Full end-to-end verification

## 8. Definition of Done for Phase 2

Phase 2 is done only if all are true:

- Registration, login, and profile are functional from JSF UI to DB.
- Cookie session governs auth state.
- Protected routes reject unauthenticated access.
- Passwords are hashed and never exposed in API response.
- Error responses use existing shared error contract.
- Dev fallback auth is not the path used for normal Phase 2 behavior.
