---
name: unit-testing-junit-mockito
description: >-
  Use this skill when writing isolated unit tests for Spring Boot services, controllers,
  mappers, and utilities using JUnit 5, AssertJ, and Mockito.
---

# Unit Testing with JUnit 5 & Mockito Skill

## Purpose
Establishes standardized testing patterns for unit tests using JUnit 5 and Mockito, ensuring isolated execution, high branch coverage, and deterministic verification.

## When to Use
- Writing unit tests for service business logic in any microservice.
- Writing slice tests for controllers using `@WebMvcTest`.
- Verifying exception paths and business validation rules.

## Required Inputs
- Target class under test (e.g. `TaskServiceImpl`, `AuthController`).
- Test requirements and edge cases from [Phase 13 Test Cases](file:///d:/learning/task_manager/documentation/13-testing-qa/03-test-cases-catalogue.md).

## Step-by-Step Procedure
1. **Set Up Test Class**:
   - Annotate with `@ExtendWith(MockitoExtension.class)`.
   - Annotate dependencies with `@Mock` and target class with `@InjectMocks`.
2. **Structure Test with Given-When-Then**:
   Follow BDD structure:
   ```java
   @Test
   void shouldReturnTask_whenTaskExistsAndBelongsToUser() {
       // Given
       UUID taskId = UUID.randomUUID();
       UUID userId = UUID.randomUUID();
       Task task = buildSampleTask(taskId, userId);
       TaskResponse expectedResponse = buildSampleResponse(taskId, userId);

       given(taskRepository.findByIdAndUserIdAndIsDeletedFalse(taskId, userId))
           .willReturn(Optional.of(task));
       given(taskMapper.toResponse(task)).willReturn(expectedResponse);

       // When
       TaskResponse actualResponse = taskService.getTaskById(taskId, userId);

       // Then
       assertThat(actualResponse).isNotNull();
       assertThat(actualResponse.id()).isEqualTo(taskId);
       then(taskRepository).should(times(1)).findByIdAndUserIdAndIsDeletedFalse(taskId, userId);
   }
   ```
3. **Test Anti-Enumeration & Exception Paths**:
   ```java
   @Test
   void shouldThrowNotFound_whenTaskBelongsToAnotherUser() {
       // Given
       UUID taskId = UUID.randomUUID();
       UUID foreignUserId = UUID.randomUUID();
       given(taskRepository.findByIdAndUserIdAndIsDeletedFalse(taskId, foreignUserId))
           .willReturn(Optional.empty());

       // When / Then
       assertThatThrownBy(() -> taskService.getTaskById(taskId, foreignUserId))
           .isInstanceOf(ResourceNotFoundException.class)
           .hasMessageContaining("not found");
   }
   ```
4. **Run and Verify Coverage**:
   - Run tests: `mvn test -Dtest=*UnitTest`.
   - Verify that line coverage $\ge 80\%$ and branch coverage $\ge 75\%$.

## Applicable Project Standards
- [Rule 08: Testing & TDD Standards](file:///d:/learning/task_manager/.agents/rules/08-testing-and-tdd.md)
- [Unit Testing Guide](file:///d:/learning/task_manager/documentation/13-testing-qa/04-unit-testing-guide.md)

## Validation Checklist
- [ ] Uses JUnit 5 (`org.junit.jupiter.api.Test`), never JUnit 4.
- [ ] Uses AssertJ fluent assertions (`assertThat`).
- [ ] Verifies both happy paths and negative exception scenarios.
- [ ] No network or database calls in unit tests.

## Common Mistakes
- Using `@SpringBootTest` for unit tests (loads entire Spring context, slowing down test execution).
- Not asserting Mockito verification (`then().should()`).

## Completion Criteria
Unit tests execute in sub-seconds, achieve $\ge 80\%$ coverage of business logic, and verify all failure paths.

