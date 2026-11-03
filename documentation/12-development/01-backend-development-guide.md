# Backend Development Guide

**Document Identifier:** DOC-DEV-001  
**Traceability References:** Master Technology Stack Baseline, DOC-AUTO-010  
**Stack Baseline:** Java 21 LTS, Spring Boot 3.3.x, Spring Cloud 2023.x, Maven  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document serves as the master engineering guide for developing Spring Boot microservices within the Task Management System. It defines the parent POM dependency management, hexagonal/layered architecture, component conventions, and development practices required across all backend services.

---

## 2. Parent POM and Dependency Management

The root `pom.xml` establishes unified version governance across all five microservices:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <groupId>com.taskmanager</groupId>
    <artifactId>task-manager-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>task-manager-parent</name>
    <description>Task Management Microservices Parent Project</description>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.3</spring-cloud.version>
        <org.mapstruct.version>1.5.5.Final</org.mapstruct.version>
        <lombok.version>1.18.30</lombok.version>
        <jjwt.version>0.12.5</jjwt.version>
        <testcontainers.version>1.19.7</testcontainers.version>
    </properties>

    <modules>
        <module>service-discovery</module>
        <module>api-gateway</module>
        <module>user-service</module>
        <module>task-service</module>
        <module>notification-service</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.testcontainers</groupId>
                <artifactId>testcontainers-bom</artifactId>
                <version>${testcontainers.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

---

## 3. Standard Microservice Layered Architecture

Each microservice organizes packages into clean, cohesive layers:

```
com.taskmanager.<service>/
├── controller/          # REST Endpoints (@RestController, OpenAPI annotations)
├── service/             # Business Logic Interfaces & Implementations (@Service)
│   └── impl/
├── repository/          # Spring Data JPA Repositories (@Repository)
├── entity/              # JPA Entities (@Entity, @Table, @Version)
├── dto/                 # Request & Response Contracts (Java 21 records)
│   ├── request/
│   └── response/
├── mapper/              # MapStruct Bean Mappers (@Mapper(componentModel = "spring"))
├── exception/           # Custom Domain Exceptions & @RestControllerAdvice
├── config/              # Security, Web, Kafka, Actuator Configurations
└── event/               # Kafka Producers, Listeners, and Outbox Handlers
```

---

## 4. Backend Engineering Rules of Thumb

1. **Immutable Records for Data Transfer**:
   Never use mutable classes for DTOs. Declare all request/response models as Java 21 `record` types with Jakarta Validation annotations on fields.
2. **Transaction Scoping**:
   - Apply `@Transactional(readOnly = true)` at the class level for read-dominant services.
   - Override with `@Transactional` on write methods (`create`, `update`, `delete`).
   - Keep transactions concise: never wrap network I/O, email sending, or Kafka calls within `@Transactional`.
3. **MapStruct for Bean Mapping**:
   Do not write manual getter/setter mapping loops. Use compile-time MapStruct interfaces with `unmappedTargetPolicy = ReportingPolicy.ERROR` to guarantee compile-time detection of missing field mappings.
4. **RFC 7807 Error Handling**:
   All exception responses must inherit or emit Spring 6 / Boot 3 `ProblemDetail`. Controllers must never return ad-hoc error JSON objects.

