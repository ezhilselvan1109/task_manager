---
name: rest-api-design-implementation
description: >-
  Use this skill when designing or implementing RESTful HTTP endpoints, OpenAPI 3.0 documentation,
  request validation, and RFC 7807 Problem Details error responses.
---

# REST API Design and Implementation Skill

## Purpose
Governs the creation and exposure of RESTful endpoints, ensuring strict alignment with OpenAPI specifications, standardized HTTP status codes, pagination, and RFC 7807 error responses.

## When to Use
- Implementing controller endpoints in `user-service`, `task-service`, or `notification-service`.
- Adding request validation rules using Jakarta Validation annotations.
- Defining or updating OpenAPI 3.0 annotations (`@Operation`, `@ApiResponse`).

## Required Inputs
- Target endpoint specification from [OpenAPI Specification](file:///d:/learning/task_manager/documentation/07-api-design/03-openapi-3-0-yaml-specification.md).
- Request/Response record classes.

## Step-by-Step Procedure
1. **Define Controller Annotations**:
   - Annotate with `@RestController`, `@RequestMapping("/api/v1/<resources>")`, `@Validated`, `@RequiredArgsConstructor`.
2. **Apply HTTP Method Mapping**:
   - `POST` $\rightarrow$ `HttpStatus.CREATED` (`201`): Return created object with `Location` header.
   - `GET` $\rightarrow$ `HttpStatus.OK` (`200`): Return DTO or `PageResponse<T>`.
   - `PUT` $\rightarrow$ `HttpStatus.OK` (`200`): Full resource replacement.
   - `PATCH` $\rightarrow$ `HttpStatus.OK` (`200`): Partial update.
   - `DELETE` $\rightarrow$ `HttpStatus.NO_CONTENT` (`204`): Return void.
3. **Validate Incoming Request Payloads**:
   - Annotate request records with Jakarta Validation: `@NotNull`, `@NotBlank`, `@Size`, `@Email`.
   - Annotate `@RequestBody @Valid <RecordName> request`.
4. **Extract Authenticated User ID**:
   - Read `X-User-Id` header injected by API Gateway:
     ```java
     @GetMapping("/{id}")
     public ResponseEntity<TaskResponse> getTaskById(
         @PathVariable("id") UUID id,
         @RequestHeader("X-User-Id") UUID userId
     ) { ... }
     ```
5. **Handle Exceptions via Problem Details**:
   - Ensure exceptions are intercepted by `@RestControllerAdvice` and rendered as `application/problem+json`.

## Applicable Project Standards
- [Rule 05: REST API & Security Standards](file:///d:/learning/task_manager/.agents/rules/05-rest-api-and-security.md)
- [Rule 07: Exception Handling & Logging Standards](file:///d:/learning/task_manager/.agents/rules/07-exception-handling-and-logging.md)

## Validation Checklist
- [ ] Endpoint URI follows plural noun convention (`/api/v1/tasks`).
- [ ] Returns `404 Not Found` when resource does not exist or belongs to another user.
- [ ] Validation errors return `400 Bad Request` with field details.
- [ ] Responses are type-safe Java records.

## Common Mistakes
- Returning `200 OK` on resource creation instead of `201 Created`.
- Exposing internal database entity objects directly instead of DTO records.

## Completion Criteria
Endpoint matches OpenAPI contract, passes MockMvc integration tests, and enforces validation and tenant isolation.

