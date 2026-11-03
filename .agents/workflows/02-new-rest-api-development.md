# Workflow 02: New REST API Development

## 1. Overview & Purpose
Defines the TDD red-green-refactor procedure for implementing a new REST API endpoint in an existing microservice.

- **Workflow ID**: `WF-002`
- **Trigger**: New API requirement (e.g. `API-001` through `API-016`) or functional endpoint addition.

## 2. Preconditions
- Owning microservice module exists and compiles cleanly.
- Endpoint contract specified in OpenAPI 3.0 specification (Phase 7).

## 3. Required Inputs
- HTTP Method (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).
- Endpoint URI (e.g. `/api/v1/tasks/{id}/status`).
- Request and Response payload schemas.
- Validation and authorization constraints.

## 4. Execution Steps
1. **Red Stage - Author MockMvc Test**:
   - Create controller test verifying endpoint route, request validation, status codes, and JSON response structure.
   - Run test and observe failure.
2. **Implement DTO Records**:
   - Create immutable Java records for request and response in `dto` package.
   - Add Jakarta validation annotations (`@NotBlank`, `@NotNull`).
3. **Implement Service Layer Method**:
   - Define method signature in service interface.
   - Implement business logic, applying tenant isolation (`WHERE id = :id AND userId = :userId`).
4. **Implement REST Controller Method**:
   - Annotate with `@PostMapping`, `@GetMapping`, etc.
   - Map `@PathVariable`, `@Valid @RequestBody`, `@RequestHeader("X-User-Id")`.
5. **Green Stage - Verify Test Passes**:
   - Re-run MockMvc and unit tests; confirm green status.
6. **Refactor Stage**:
   - Extract mapping logic to MapStruct mappers; clean up code formatting.
7. **Document Endpoint**:
   - Add OpenAPI annotations (`@Operation`, `@ApiResponse`).

## 5. Skills to Use
- [`rest-api-design-implementation`](file:///d:/learning/task_manager/.agents/skills/rest-api-design-implementation/SKILL.md)
- [`java-21-development`](file:///d:/learning/task_manager/.agents/skills/java-21-development/SKILL.md)
- [`unit-testing-junit-mockito`](file:///d:/learning/task_manager/.agents/skills/unit-testing-junit-mockito/SKILL.md)

## 6. Rules to Follow
- [Rule 05: REST API & Security Standards](file:///d:/learning/task_manager/.agents/rules/05-rest-api-and-security.md)
- [Rule 07: Exception Handling & Logging Standards](file:///d:/learning/task_manager/.agents/rules/07-exception-handling-and-logging.md)
- [Rule 08: Testing & TDD Standards](file:///d:/learning/task_manager/.agents/rules/08-testing-and-tdd.md)

## 7. Tests and Validation
- MockMvc slice tests verifying HTTP 200/201, 400 (validation error), and 404 (anti-enumeration).
- Unit tests verifying service logic isolation.

## 8. Failure Handling
- If validation returns 500 instead of 400, verify `MethodArgumentNotValidException` handler in `GlobalExceptionHandler`.
- If cross-tenant lookup returns 403, change exception to `ResourceNotFoundException` to enforce anti-enumeration.

## 9. Review Checklist
- [ ] Returns `404 Not Found` when resource belongs to another user.
- [ ] Request parameters are validated with `@Valid`.
- [ ] Response is a Java `record`, not a JPA entity.
- [ ] API is mapped in Gateway routes.

## 10. Expected Outputs
- Working controller endpoint.
- Request/Response records.
- Passing unit and slice test suite.

