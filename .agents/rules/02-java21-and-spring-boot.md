# Rule 02: Java 21 & Spring Boot Development Standards

## Java 21 Coding Standards
1. **Language Level**: Source and target bytecode level is Java 21 (`<java.version>21</java.version>`).
2. **Modern Features**:
   - Prefer Java `record` for immutable DTOs, API request/response payloads, and Kafka event payloads.
   - Use Pattern Matching for `switch` and `instanceof`.
   - Use Text Blocks (`"""..."""`) for multi-line SQL or JSON in tests.
   - Use `var` only when the type is obvious from the right-hand side (e.g. `var task = new Task()`).
3. **Immutability & Null Safety**:
   - DTOs and value objects must be immutable.
   - Use `java.util.Optional` for return types that may be absent in repository or service layers. Never return null lists; return `Collections.emptyList()`.
   - Never use `Optional` for field members or method parameters.

## Spring Boot Architecture & Packaging Standards
1. **Package By Feature/Layer**:
   - `com.taskmanager.<service>.config` (Spring configuration classes)
   - `com.taskmanager.<service>.controller` (REST Controllers)
   - `com.taskmanager.<service>.dto` (Request and Response Records)
   - `com.taskmanager.<service>.entity` (JPA Entities)
   - `com.taskmanager.<service>.exception` (Custom exceptions and GlobalExceptionHandler)
   - `com.taskmanager.<service>.mapper` (MapStruct or custom transformation mappers)
   - `com.taskmanager.<service>.repository` (Spring Data JPA Repositories)
   - `com.taskmanager.<service>.service` (Service Interfaces and implementations)
2. **Dependency Injection**:
   - Always use constructor injection (`@RequiredArgsConstructor` with `final` fields or explicit constructor).
   - Never use field injection (`@Autowired private Service service;` is prohibited).
3. **Transaction Management**:
   - Annotate service mutation methods with `@Transactional`.
   - Annotate read-only queries with `@Transactional(readOnly = true)`.
4. **Validation**:
   - Enforce Jakarta Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Email`) on DTO fields and annotate controller methods with `@Valid`.

