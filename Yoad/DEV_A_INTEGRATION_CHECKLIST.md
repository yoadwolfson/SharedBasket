# Developer A Dependencies Tracking

**Purpose**: This document tracks all integration points between Developer B's JSF shell and Developer A's authentication, categories, shopping items, and reporting services. Use this document during code merging to verify contracts and resolve any mismatches.

**Last Updated**: 2026-06-16  
**Owner**: Developer B  
**Status**: Foundation Phase - Awaiting Developer A Integration

---

## 1. Authentication Service Dependencies

### Connection Point: AuthenticationService

**Type**: Core Service Dependency  
**Priority**: CRITICAL (blocks all other features)  
**Status**: Awaiting Implementation

| Aspect | Details | File Location | Verification |
|--------|---------|----------------|--------------|
| **Service Contract** | Interface defining authentication operations | `src/main/java/com/sharedbasket/service/AuthenticationService.java` | ✓ Interface defined, awaiting implementation |
| **Expected Methods** | `getCurrentUser()`, `isAuthenticated()`, `logout()`, `invalidateUserSessions()` | AuthenticationService.java | Must verify all methods exist in Developer A's implementation |
| **Return Type** | `UserDTO` with fields: id, email, fullName, role | `src/main/java/com/sharedbasket/dto/UserDTO.java` | Shape must match Developer A's user response format |

### UserDTO Contract

**Location**: `src/main/java/com/sharedbasket/dto/UserDTO.java`

| Field | Type | Source | Notes |
|-------|------|--------|-------|
| `id` | Long | Developer A Auth Service | Unique user identifier |
| `email` | String | Developer A Auth Service | User email address |
| `fullName` | String | Developer A Auth Service | Display name |
| `role` | String | Developer A Auth Service | Expected values: "ADMIN" or "MEMBER" within group context |

**Verification Checklist**:
- [ ] Developer A's `GET /api/users/current` returns UserDTO matching this contract
- [ ] `role` field is correctly populated from group membership
- [ ] Session state is maintained correctly across page requests
- [ ] Logout operation clears session properly

### Shell Controller Integration

**Location**: `src/main/java/com/sharedbasket/controller/jsf/ShellController.java`

| Usage | Expected Behavior | Verification |
|-------|-------------------|--------------|
| `@PostConstruct init()` | Loads current user on session start | Verify currentUser is populated |
| `navigateToProfile()` | Redirects to profile page | Should route to Developer A's profile endpoint |
| `logout()` | Calls authService.logout() and redirects | Verify session is cleared after logout |

---

## 2. Shopping Items Service Dependencies

### Connection Point: ShoppingItemService (Placeholder)

**Type**: Data Dependency  
**Priority**: HIGH (needed for dashboard and shopping list features)  
**Status**: Placeholder Created

| Aspect | Details | Expected Endpoint |
|--------|---------|-------------------|
| **Service** | Provides shopping item data | Developer A (TBD) |
| **Usage** | Load items for dashboard widget | `GET /api/shopping-items` |
| **Return Type** | List of ShoppingItemDTO | Expected structure TBD |
| **Fields Required** | id, name, quantity, estimatedPrice, category | Awaiting specification |

### Current Placeholder

**Location**: `src/main/java/com/sharedbasket/controller/jsf/DashboardController.java`

```java
// TODO-DEV-A: Inject ShoppingItemService and load items
this.shoppingItems = new ArrayList<>();
```

**Verification Checklist**:
- [ ] Developer A defines ShoppingItemDTO structure
- [ ] `GET /api/shopping-items` endpoint returns list with required fields
- [ ] Response includes price information for cost calculations
- [ ] Error responses conform to ErrorResponseDTO contract

### Expected Data Structure (TBD)

```json
{
  "id": 1,
  "name": "Milk",
  "quantity": 2,
  "estimatedPrice": 3.99,
  "category": "Dairy",
  "purchased": false,
  "addedBy": "user@example.com",
  "addedDate": "2026-06-16T10:30:00Z"
}
```

---

## 3. Reporting & Statistics Service Dependencies

### Connection Point: ReportingService (Placeholder)

**Type**: Data Dependency  
**Priority**: HIGH (needed for dashboard statistics)  
**Status**: Placeholder Created

| Aspect | Details | Expected Endpoint |
|--------|---------|-------------------|
| **Service** | Provides shopping statistics and reports | Developer A (TBD) |
| **Usage** | Display statistics on dashboard | `GET /api/reports/statistics` |
| **Return Type** | StatisticsDTO (structure TBD) | Awaiting specification |
| **Fields Required** | totalItems, totalCost, itemsPurchased | Awaiting specification |

### Current Placeholder

**Location**: `src/main/java/com/sharedbasket/controller/jsf/DashboardController.java`

```java
// TODO-DEV-A: Inject ReportingService and load statistics
this.statistics = null;
```

**Verification Checklist**:
- [ ] Developer A defines StatisticsDTO structure
- [ ] `GET /api/reports/statistics` endpoint returns correct metrics
- [ ] Cost calculations are accurate
- [ ] Historical data is available for date-range queries

### Expected Data Structure (TBD)

```json
{
  "totalItems": 42,
  "totalCost": 156.78,
  "itemsPurchased": 28,
  "averagePrice": 3.73,
  "mostFrequentCategory": "Groceries"
}
```

---

## 4. Category Service Dependencies

### Connection Point: CategoryService (Not Yet Implemented)

**Type**: Data Dependency  
**Priority**: MEDIUM (needed for recommendations)  
**Status**: Awaiting Requirements

| Aspect | Details | Expected Endpoint |
|--------|---------|-------------------|
| **Service** | Provides product categories | Developer A (TBD) |
| **Usage** | Display categories in recommendations | `GET /api/categories` |
| **Return Type** | List of CategoryDTO (TBD) | Awaiting specification |

---

## 5. Error Response Contract

### ErrorResponseDTO

**Location**: `src/main/java/com/sharedbasket/dto/ErrorResponseDTO.java`

**Purpose**: Standardized error response format from all Developer A REST endpoints

**Expected Structure**:
```json
{
  "timestamp": "2026-06-16T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    {
      "fieldName": "email",
      "message": "Email is invalid",
      "errorCode": "INVALID_EMAIL"
    }
  ]
}
```

**Verification Checklist**:
- [ ] All Developer A endpoints return this structure on error
- [ ] Field error codes are documented for frontend handling
- [ ] HTTP status codes are used consistently (400, 401, 403, 404, 409, 422, 500, 503)
- [ ] Error handling utility correctly parses responses

**Utility Class**: `src/main/java/com/sharedbasket/util/ErrorHandlingUtil.java`

---

## 6. Form & Validation Error Display

### Integration Point: Alerts Fragment

**Location**: `src/main/webapp/WEB-INF/templates/fragments/alerts.xhtml`

**Rendered Components**:
1. JSF framework messages (validation errors)
2. Field-level errors from backend
3. Application-level alerts (from ShellController)

**Verification Checklist**:
- [ ] Backend validation errors are displayed in correct field
- [ ] Error messages are clear and user-friendly
- [ ] Global errors are distinguished from field-level errors
- [ ] Success messages appear after operations complete

---

## 7. Session Management & Role-Based Access

### Connection Point: Role-Based Navigation

**Location**: `src/main/webapp/WEB-INF/templates/fragments/navigation.xhtml`

**Role Values Expected**:
- `ADMIN`: Full group management capabilities
- `MEMBER`: View-only permissions (per business logic)

**Role Display Logic**:
```xml
rendered="#{sessionScope.currentUser.role == 'ADMIN'}"
```

**Verification Checklist**:
- [ ] Role values match Developer A's role definitions exactly
- [ ] Role is correctly populated in UserDTO from session
- [ ] Navigation items show/hide based on role
- [ ] Backend API endpoints also enforce role restrictions

---

## 8. Integration Checklist - Pre-Merge Tasks

### For Developer B (Before Sending to Integration)

- [ ] All placeholder service classes have TODO-DEV-A markers
- [ ] DTOs match expected backend response shapes (verify with API docs)
- [ ] Error handling follows ErrorResponseDTO contract
- [ ] Session management uses provided AuthenticationService
- [ ] All @DevAConnection markers are in place and documented

### For Developer A (Before Integration)

- [ ] AuthenticationService implementation is provided
- [ ] UserDTO matches contract in `src/main/java/com/sharedbasket/dto/UserDTO.java`
- [ ] All REST endpoints return responses matching ErrorResponseDTO contract
- [ ] Role values in UserDTO are consistent ("ADMIN" | "MEMBER")
- [ ] Session state is properly managed across requests
- [ ] All error responses follow the standardized error format

### During Merge/Integration

- [ ] Run automated tests to verify API contracts
- [ ] Test shell rendering with actual Developer A data
- [ ] Verify error handling with various error scenarios
- [ ] Test role-based navigation shows/hides correctly
- [ ] Verify logout clears session properly
- [ ] Test error messages display correctly in alerts fragment

---

## 9. Known Placeholders & TODOs

| File | Location | Status | Action Item |
|------|----------|--------|-------------|
| DashboardController.java | `loadDashboardData()` method | TODO | Inject ShoppingItemService and ReportingService |
| alerts.xhtml | Global alerts section | TODO-DEV-A | Integrate with backend error responses |
| navigation.xhtml | Header section | TODO-DEV-A | Ensure role field matches Developer A's role enum |
| ShellController.java | `initializeCurrentUser()` method | Waiting | Verify currentUser loads correctly |

---

## 10. Communication & Status Updates

**Point of Contact**: Developer B  
**Communication Channel**: [To be established]  
**Expected Integration Timeline**: After Phase 1 completion

**Status Updates Required**:
1. When Developer A completes AuthenticationService implementation
2. When first API endpoints are ready for integration testing
3. When error response format is finalized
4. Upon any changes to UserDTO contract or role values

---

## Appendix: @DevAConnection Markers

All integration points are marked with @DevAConnection comments in source code. Use these to find all Developer A dependencies:

**Search Pattern**: `@DevAConnection`

**Common Locations**:
- `ShellController.java`: Session management
- `DashboardController.java`: Data loading
- `*.xhtml` template files: UI integration
- DTOs in `com.sharedbasket.dto` package: Contract definitions

---

**Document Version**: 1.0  
**Last Review**: 2026-06-16  
**Next Review**: Upon Developer A foundation completion
