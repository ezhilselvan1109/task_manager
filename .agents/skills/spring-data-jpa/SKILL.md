---
name: spring-data-jpa
description: >-
  Use this skill when defining JPA entities, Spring Data repositories, custom JPQL queries,
  pagination, and tenant-scoped lookups.
---

# Spring Data JPA Skill

## Purpose
Provides guidelines and code patterns for implementing JPA entities, Spring Data repositories, and tenant-isolated data queries with Hibernate 6.5+.

## When to Use
- Mapping database tables to Java JPA entities.
- Writing repository interfaces and custom `@Query` JPQL/native methods.
- Implementing paginated and filtered queries (`Pageable`, `Specification`).
- Enforcing anti-enumeration data access (`WHERE id = :id AND userId = :userId`).

## Required Inputs
- Target database table schema.
- Repository query specifications.

## Step-by-Step Procedure
1. **Define JPA Entity**:
   - Annotate with `@Entity`, `@Table(name = "...")`, `@Getter`, `@Setter`, `@NoArgsConstructor`.
   - Never use Lombok `@Data` or `@EqualsAndHashCode` on JPA entities (leads to infinite recursion with relationships).
   - Use UUID identifier with `@Id`:
     ```java
     @Id
     @Column(name = "id", nullable = false, updatable = false)
     private UUID id;
     ```
2. **Implement Entity Auditing**:
   - Annotate timestamps with `@CreationTimestamp` and `@UpdateTimestamp` (`Instant createdAt, Instant updatedAt`).
3. **Define Repository Interface**:
   - Extend `JpaRepository<Entity, UUID>` and `JpaSpecificationExecutor<Entity>`.
4. **Enforce Tenant Isolation in Queries**:
   - Single item query:
     ```java
     Optional<Task> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);
     ```
   - Collection query with pagination:
     ```java
     Page<Task> findAllByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);
     ```
5. **Optimize Query Performance**:
   - Use projection interfaces or records for read-only aggregation (e.g. Dashboard counts).
   - Avoid N+1 query problems using `JOIN FETCH` when fetching related associations.

## Applicable Project Standards
- [Rule 02: Java 21 & Spring Boot Standards](file:///d:/learning/task_manager/.agents/rules/02-java21-and-spring-boot.md)
- [Rule 05: REST API & Security Standards](file:///d:/learning/task_manager/.agents/rules/05-rest-api-and-security.md)
- [Phase 5 Low-Level Design](file:///d:/learning/task_manager/documentation/05-low-level-design/04-entity-relationship-classes.md)

## Validation Checklist
- [ ] No `@Data` on JPA entities.
- [ ] Repository methods always scope queries by `userId`.
- [ ] All collection queries accept `Pageable`.
- [ ] Soft deletion condition (`isDeleted = false`) is applied to queries.

## Common Mistakes
- Querying by `id` alone without verifying `userId`, creating an IDOR vulnerability.
- Modifying entities outside a `@Transactional` boundary and expecting automatic dirty checking.

## Completion Criteria
JPA entity correctly maps to the PostgreSQL table and repository methods execute performant, tenant-isolated queries.

