# Rule 07: Exception Handling & Logging Standards

## Exception Handling Standards
1. **RFC 7807 Problem Details**:
   - All REST APIs must return structured error representations conforming to RFC 7807 (`application/problem+json`).
   - Fields:
     - `type`: URI identifying problem type (e.g. `https://api.taskmanager.com/errors/not-found`).
     - `title`: Short human-readable summary.
     - `status`: HTTP status code (integer).
     - `detail`: Specific explanation of this occurrence.
     - `instance`: URI of the endpoint invoked.
     - `timestamp`: UTC ISO-8601 timestamp.
     - `correlationId`: Distributed correlation UUID.
     - `errors`: Optional list of field-level validation errors (`field`, `message`, `rejectedValue`).
2. **Controller Advice**:
   - Each microservice must implement `@RestControllerAdvice` handling:
     - `MethodArgumentNotValidException` $\rightarrow$ `400 Bad Request`
     - `ResourceNotFoundException` $\rightarrow$ `404 Not Found`
     - `BadCredentialsException` $\rightarrow$ `401 Unauthorized`
     - `AccessDeniedException` $\rightarrow$ `403 Forbidden`
     - `ConflictException` $\rightarrow$ `409 Conflict`
     - Generic `Exception` $\rightarrow$ `500 Internal Server Error` (with sanitized error message, never exposing stack traces to clients).

## Logging & Correlation ID Standards
1. **Structured JSON Logging**:
   - Production logs must use Logstash Logback JSON encoder.
   - Standard log fields: `timestamp`, `level`, `service`, `correlationId`, `userId`, `thread`, `logger`, `message`.
2. **MDC Correlation Propagation**:
   - `X-Correlation-ID` header must be extracted in HTTP filters and injected into `org.slf4j.MDC`.
   - Clear MDC in `finally` blocks to prevent thread-pool leakage.
3. **Sensitive Data Masking**:
   - Never log passwords, JWT tokens, email addresses in plaintext, or authorization headers.
   - Log at `INFO` for state transitions; `DEBUG` for developer diagnostics; `ERROR` for unexpected failures with stack traces.

