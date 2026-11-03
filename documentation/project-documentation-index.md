# Project Documentation Index

| Field | Value |
|---|---|
| **Document ID** | REG-IDX-001 |
| **Project** | Task Management System – Microservices Architecture |
| **Status** | Complete (All 15 SDLC Phases + Final Review Approved Baseline) |
| **Last Updated** | 2026-11-03 |

---

## 1. Documentation Map & Navigation

This master index provides links, identifiers, versions, statuses, and cross-references for every document across all 15 SDLC phases.

```
documentation/
├── 00-project-overview/
├── 01-project-initiation/
├── 02-requirements/
├── 03-business-analysis/
├── 04-high-level-architecture/
├── 05-low-level-design/
├── 06-database-design/
├── 07-api-design/
├── 08-kafka-event-design/
├── 09-security-resilience/
├── 10-antigravity-automation/
├── 11-infrastructure/
├── 12-development/
├── 13-testing-qa/
├── 14-devops-deployment/
├── 15-observability-maintenance/
├── architecture-decisions/
├── diagrams/
├── templates/
├── project-documentation-index.md
├── project-progress.md
├── requirements-traceability-matrix.md
├── open-questions-register.md
└── final-documentation-review.md
```

---

## 2. Master Document Catalogue

### Phase 1: Project Initiation
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-INIT-001` | Project Charter | [01-project-charter.md](file:///d:/learning/task_manager/documentation/01-project-initiation/01-project-charter.md) | 1.0.0 | Approved Baseline | BR-001 to BR-010 |
| `DOC-INIT-002` | Project Overview | [02-project-overview.md](file:///d:/learning/task_manager/documentation/01-project-initiation/02-project-overview.md) | 1.0.0 | Approved Baseline | BR-001 to BR-010 |
| `DOC-INIT-003` | Project Vision and Objectives | [03-project-vision-and-objectives.md](file:///d:/learning/task_manager/documentation/01-project-initiation/03-project-vision-and-objectives.md) | 1.0.0 | Approved Baseline | BR-001 to BR-010 |
| `DOC-INIT-004` | Project Scope Document | [04-project-scope-document.md](file:///d:/learning/task_manager/documentation/01-project-initiation/04-project-scope-document.md) | 1.0.0 | Approved Baseline | BR-001 to BR-010 |
| `DOC-INIT-005` | In-Scope and Out-of-Scope Document | [05-in-scope-and-out-of-scope.md](file:///d:/learning/task_manager/documentation/01-project-initiation/05-in-scope-and-out-of-scope.md) | 1.0.0 | Approved Baseline | BR-001 to BR-010 |
| `DOC-INIT-006` | Stakeholder Identification | [06-stakeholder-identification.md](file:///d:/learning/task_manager/documentation/01-project-initiation/06-stakeholder-identification.md) | 1.0.0 | Approved Baseline | All |
| `DOC-INIT-007` | Stakeholder Responsibility Matrix | [07-stakeholder-responsibility-matrix.md](file:///d:/learning/task_manager/documentation/01-project-initiation/07-stakeholder-responsibility-matrix.md) | 1.0.0 | Approved Baseline | All |
| `DOC-INIT-008` | Project Assumptions | [08-project-assumptions.md](file:///d:/learning/task_manager/documentation/01-project-initiation/08-project-assumptions.md) | 1.0.0 | Approved Baseline | BRULE-001 to 010 |
| `DOC-INIT-009` | Project Constraints | [09-project-constraints.md](file:///d:/learning/task_manager/documentation/01-project-initiation/09-project-constraints.md) | 1.0.0 | Approved Baseline | NFR-001 to NFR-007 |
| `DOC-INIT-010` | Project Dependencies | [10-project-dependencies.md](file:///d:/learning/task_manager/documentation/01-project-initiation/10-project-dependencies.md) | 1.0.0 | Approved Baseline | All |
| `DOC-INIT-011` | Initial Risk Register | [11-initial-risk-register.md](file:///d:/learning/task_manager/documentation/01-project-initiation/11-initial-risk-register.md) | 1.0.0 | Approved Baseline | All |
| `DOC-INIT-012` | Project Glossary | [12-project-glossary.md](file:///d:/learning/task_manager/documentation/01-project-initiation/12-project-glossary.md) | 1.0.0 | Approved Baseline | All |
| `DOC-INIT-013` | Phase 1 Review Report | [phase-01-review-report.md](file:///d:/learning/task_manager/documentation/01-project-initiation/phase-01-review-report.md) | 1.0.0 | Approved Baseline | All |

### Phase 2: Requirements Gathering and Analysis
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-REQ-001` | Business Requirements Document (BRD) | [01-business-requirements-document.md](file:///d:/learning/task_manager/documentation/02-requirements/01-business-requirements-document.md) | 1.0.0 | Approved Baseline | BR-001 to BR-010 |
| `DOC-REQ-002` | Product Requirements Document (PRD) | [02-product-requirements-document.md](file:///d:/learning/task_manager/documentation/02-requirements/02-product-requirements-document.md) | 1.0.0 | Approved Baseline | FR-001 to FR-020 |
| `DOC-REQ-003` | Software Requirements Specification (SRS) | [03-software-requirements-specification.md](file:///d:/learning/task_manager/documentation/02-requirements/03-software-requirements-specification.md) | 1.0.0 | Approved Baseline | FR-001 to FR-020, NFR-001 to NFR-010 |
| `DOC-REQ-004` | Functional Requirements Document | [04-functional-requirements-document.md](file:///d:/learning/task_manager/documentation/02-requirements/04-functional-requirements-document.md) | 1.0.0 | Approved Baseline | FR-001 to FR-020 |
| `DOC-REQ-005` | Non-Functional Requirements Document | [05-non-functional-requirements-document.md](file:///d:/learning/task_manager/documentation/02-requirements/05-non-functional-requirements-document.md) | 1.0.0 | Approved Baseline | NFR-001 to NFR-010 |
| `DOC-REQ-006` | User Stories Document | [06-user-stories.md](file:///d:/learning/task_manager/documentation/02-requirements/06-user-stories.md) | 1.0.0 | Approved Baseline | US-001 to US-016 |
| `DOC-REQ-007` | Use Case Document | [07-use-case-document.md](file:///d:/learning/task_manager/documentation/02-requirements/07-use-case-document.md) | 1.0.0 | Approved Baseline | UC-001 to UC-012 |
| `DOC-REQ-008` | Acceptance Criteria Document | [08-acceptance-criteria.md](file:///d:/learning/task_manager/documentation/02-requirements/08-acceptance-criteria.md) | 1.0.0 | Approved Baseline | All FRs & USs |
| `DOC-REQ-009` | Business Rules Document | [09-business-rules-document.md](file:///d:/learning/task_manager/documentation/02-requirements/09-business-rules-document.md) | 1.0.0 | Approved Baseline | BRULE-001 to BRULE-010 |
| `DOC-REQ-010` | Requirements Prioritization Matrix | [10-requirements-prioritization-matrix.md](file:///d:/learning/task_manager/documentation/02-requirements/10-requirements-prioritization-matrix.md) | 1.0.0 | Approved Baseline | MoSCoW Scoring |
| `DOC-REQ-011` | Requirements Traceability Linkage | [11-requirements-traceability-linkage.md](file:///d:/learning/task_manager/documentation/02-requirements/11-requirements-traceability-linkage.md) | 1.0.0 | Approved Baseline | Cross-Phase Mapping |
| `DOC-REQ-012` | Phase 2 Review Report | [phase-02-review-report.md](file:///d:/learning/task_manager/documentation/02-requirements/phase-02-review-report.md) | 1.0.0 | Approved Baseline | Phase 2 Sign-off |

### Phase 3: Business Analysis
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-BA-001` | Business Process Document | [01-business-process-document.md](file:///d:/learning/task_manager/documentation/03-business-analysis/01-business-process-document.md) | 1.0.0 | Approved Baseline | BR-001 to BR-008 |
| `DOC-BA-002` | As-Is and To-Be Process Document | [02-as-is-and-to-be-process.md](file:///d:/learning/task_manager/documentation/03-business-analysis/02-as-is-and-to-be-process.md) | 1.0.0 | Approved Baseline | All |
| `DOC-BA-003` | Business Workflow Diagrams | [03-business-workflow-diagrams.md](file:///d:/learning/task_manager/documentation/03-business-analysis/03-business-workflow-diagrams.md) | 1.0.0 | Approved Baseline | FR-001 to FR-020 |
| `DOC-BA-004` | User Journey Diagrams | [04-user-journey-diagrams.md](file:///d:/learning/task_manager/documentation/03-business-analysis/04-user-journey-diagrams.md) | 1.0.0 | Approved Baseline | US-001 to US-016 |
| `DOC-BA-005` | Use Case Diagrams | [05-use-case-diagrams.md](file:///d:/learning/task_manager/documentation/03-business-analysis/05-use-case-diagrams.md) | 1.0.0 | Approved Baseline | UC-001 to UC-012 |
| `DOC-BA-006` | Activity Diagrams | [06-activity-diagrams.md](file:///d:/learning/task_manager/documentation/03-business-analysis/06-activity-diagrams.md) | 1.0.0 | Approved Baseline | All Workflows |
| `DOC-BA-007` | Business Rules Catalogue | [07-business-rules-catalogue.md](file:///d:/learning/task_manager/documentation/03-business-analysis/07-business-rules-catalogue.md) | 1.0.0 | Approved Baseline | BRULE-001 to 010 |
| `DOC-BA-008` | Business Data Dictionary | [08-business-data-dictionary.md](file:///d:/learning/task_manager/documentation/03-business-analysis/08-business-data-dictionary.md) | 1.0.0 | Approved Baseline | All Data Entities |
| `DOC-BA-009` | Functional Decomposition | [09-functional-decomposition.md](file:///d:/learning/task_manager/documentation/03-business-analysis/09-functional-decomposition.md) | 1.0.0 | Approved Baseline | All Domains |
| `DOC-BA-010` | MVP Prioritization Document | [10-mvp-prioritization-document.md](file:///d:/learning/task_manager/documentation/03-business-analysis/10-mvp-prioritization-document.md) | 1.0.0 | Approved Baseline | Scope & Releases |
| `DOC-BA-011` | Phase 3 Review Report | [phase-03-review-report.md](file:///d:/learning/task_manager/documentation/03-business-analysis/phase-03-review-report.md) | 1.0.0 | Approved Baseline | Phase 3 Sign-off |

### Phase 4: High-Level System Architecture
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-ARCH-001` | High-Level Architecture Document | [01-high-level-architecture-document.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/01-high-level-architecture-document.md) | 1.0.0 | Approved Baseline | All Architecture Goals |
| `DOC-ARCH-002` | Microservices Architecture Document | [02-microservices-architecture-document.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/02-microservices-architecture-document.md) | 1.0.0 | Approved Baseline | Microservices Baseline |
| `DOC-ARCH-003` | System Context Diagram (C4 Level 1) | [03-system-context-diagram.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/03-system-context-diagram.md) | 1.0.0 | Approved Baseline | System Boundaries |
| `DOC-ARCH-004` | Container Diagram (C4 Level 2) | [04-container-diagram.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/04-container-diagram.md) | 1.0.0 | Approved Baseline | Container Topology |
| `DOC-ARCH-005` | Component Diagram (C4 Level 3) | [05-component-diagram.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/05-component-diagram.md) | 1.0.0 | Approved Baseline | Internal Service Modules |
| `DOC-ARCH-006` | Deployment Architecture Diagram | [06-deployment-architecture-diagram.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/06-deployment-architecture-diagram.md) | 1.0.0 | Approved Baseline | Docker / Local Runtime |
| `DOC-ARCH-007` | Service Responsibility Matrix | [07-service-responsibility-matrix.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/07-service-responsibility-matrix.md) | 1.0.0 | Approved Baseline | Service Ownership |
| `DOC-ARCH-008` | Service Boundary Document | [08-service-boundary-document.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/08-service-boundary-document.md) | 1.0.0 | Approved Baseline | Bounded Contexts |
| `DOC-ARCH-009` | Inter-Service Communication Design | [09-inter-service-communication-design.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/09-inter-service-communication-design.md) | 1.0.0 | Approved Baseline | REST & Kafka Protocols |
| `DOC-ARCH-010` | Synchronous vs Asynchronous Communication Decision | [10-sync-vs-async-communication.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/10-sync-vs-async-communication.md) | 1.0.0 | Approved Baseline | Trade-off Analysis |
| `DOC-ARCH-011` | Architecture Decision Records (ADRs) | [11-architecture-decision-records.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/11-architecture-decision-records.md) | 1.0.0 | Approved Baseline | ADR-001 to ADR-008 |
| `DOC-ARCH-012` | Architecture Risk Assessment | [12-architecture-risk-assessment.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/12-architecture-risk-assessment.md) | 1.0.0 | Approved Baseline | Architectural Safeguards |
| `DOC-ARCH-013` | Phase 4 Review Report | [phase-04-review-report.md](file:///d:/learning/task_manager/documentation/04-high-level-architecture/phase-04-review-report.md) | 1.0.0 | Approved Baseline | Phase 4 Sign-off |

### Phase 5: Low-Level Design
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-LLD-001` | Low-Level Design Document | [01-low-level-design-document.md](file:///d:/learning/task_manager/documentation/05-low-level-design/01-low-level-design-document.md) | 1.0.0 | Approved Baseline | All LLD Implementations |
| `DOC-LLD-002` | Package Structure Document | [02-package-structure-document.md](file:///d:/learning/task_manager/documentation/05-low-level-design/02-package-structure-document.md) | 1.0.0 | Approved Baseline | Code Organization |
| `DOC-LLD-003` | Class Design Document | [03-class-design-document.md](file:///d:/learning/task_manager/documentation/05-low-level-design/03-class-design-document.md) | 1.0.0 | Approved Baseline | Domain Model & Classes |
| `DOC-LLD-004` | Component Interaction Document | [04-component-interaction-document.md](file:///d:/learning/task_manager/documentation/05-low-level-design/04-component-interaction-document.md) | 1.0.0 | Approved Baseline | Internal Invocations |
| `DOC-LLD-005` | Sequence Diagrams | [05-sequence-diagrams.md](file:///d:/learning/task_manager/documentation/05-low-level-design/05-sequence-diagrams.md) | 1.0.0 | Approved Baseline | Detailed Method Calls |
| `DOC-LLD-006` | Service Layer Design | [06-service-layer-design.md](file:///d:/learning/task_manager/documentation/05-low-level-design/06-service-layer-design.md) | 1.0.0 | Approved Baseline | Business Rules Logic |
| `DOC-LLD-007` | Repository Layer Design | [07-repository-layer-design.md](file:///d:/learning/task_manager/documentation/05-low-level-design/07-repository-layer-design.md) | 1.0.0 | Approved Baseline | Spring Data JPA Specs |
| `DOC-LLD-008` | DTO Design | [08-dto-design.md](file:///d:/learning/task_manager/documentation/05-low-level-design/08-dto-design.md) | 1.0.0 | Approved Baseline | Request / Response Contracts |
| `DOC-LLD-009` | Entity Design | [09-entity-design.md](file:///d:/learning/task_manager/documentation/05-low-level-design/09-entity-design.md) | 1.0.0 | Approved Baseline | JPA Entity Mapping |
| `DOC-LLD-010` | Exception Handling Design | [10-exception-handling-design.md](file:///d:/learning/task_manager/documentation/05-low-level-design/10-exception-handling-design.md) | 1.0.0 | Approved Baseline | Global Error Standards |
| `DOC-LLD-011` | Validation Design | [11-validation-design.md](file:///d:/learning/task_manager/documentation/05-low-level-design/11-validation-design.md) | 1.0.0 | Approved Baseline | Jakarta Bean Validation |
| `DOC-LLD-012` | Configuration Management Design | [12-configuration-management-design.md](file:///d:/learning/task_manager/documentation/05-low-level-design/12-configuration-management-design.md) | 1.0.0 | Approved Baseline | Spring Boot application.yml |
| `DOC-LLD-013` | Logging and Correlation ID Design | [13-logging-and-correlation-id-design.md](file:///d:/learning/task_manager/documentation/05-low-level-design/13-logging-and-correlation-id-design.md) | 1.0.0 | Approved Baseline | Slf4j / MDC Tracing |
| `DOC-LLD-014` | Phase 5 Review Report | [phase-05-review-report.md](file:///d:/learning/task_manager/documentation/05-low-level-design/phase-05-review-report.md) | 1.0.0 | Approved Baseline | Phase 5 Sign-off |

### Phase 6: Database Design
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-DB-001` | Database Architecture Document | [01-database-architecture-document.md](file:///d:/learning/task_manager/documentation/06-database-design/01-database-architecture-document.md) | 1.0.0 | Approved Baseline | All Database Requirements |
| `DOC-DB-002` | Database per Service Design | [02-database-per-service-design.md](file:///d:/learning/task_manager/documentation/06-database-design/02-database-per-service-design.md) | 1.0.0 | Approved Baseline | Schema Boundaries |
| `DOC-DB-003` | Entity Relationship Diagrams | [03-entity-relationship-diagrams.md](file:///d:/learning/task_manager/documentation/06-database-design/03-entity-relationship-diagrams.md) | 1.0.0 | Approved Baseline | All ERDs |
| `DOC-DB-004` | Table Design Document | [04-table-design-document.md](file:///d:/learning/task_manager/documentation/06-database-design/04-table-design-document.md) | 1.0.0 | Approved Baseline | Table DDL Specifications |
| `DOC-DB-005` | Column Data Dictionary | [05-column-data-dictionary.md](file:///d:/learning/task_manager/documentation/06-database-design/05-column-data-dictionary.md) | 1.0.0 | Approved Baseline | Columns & Data Types |
| `DOC-DB-006` | Primary Key and Foreign Key Strategy | [06-primary-and-foreign-key-strategy.md](file:///d:/learning/task_manager/documentation/06-database-design/06-primary-and-foreign-key-strategy.md) | 1.0.0 | Approved Baseline | Keys & Referentials |
| `DOC-DB-007` | Indexing Strategy | [07-indexing-strategy.md](file:///d:/learning/task_manager/documentation/06-database-design/07-indexing-strategy.md) | 1.0.0 | Approved Baseline | B-Tree & Query Plans |
| `DOC-DB-008` | Constraints and Validation Rules | [08-constraints-and-validation-rules.md](file:///d:/learning/task_manager/documentation/06-database-design/08-constraints-and-validation-rules.md) | 1.0.0 | Approved Baseline | DB Level Integrity |
| `DOC-DB-009` | Database Normalization Analysis | [09-database-normalization-analysis.md](file:///d:/learning/task_manager/documentation/06-database-design/09-database-normalization-analysis.md) | 1.0.0 | Approved Baseline | 1NF, 2NF, 3NF, BCNF |
| `DOC-DB-010` | Transaction Management Design | [10-transaction-management-design.md](file:///d:/learning/task_manager/documentation/06-database-design/10-transaction-management-design.md) | 1.0.0 | Approved Baseline | Isolation Levels & Locks |
| `DOC-DB-011` | Flyway Migration Strategy | [11-flyway-migration-strategy.md](file:///d:/learning/task_manager/documentation/06-database-design/11-flyway-migration-strategy.md) | 1.0.0 | Approved Baseline | Versioned SQL Scripts |
| `DOC-DB-012` | Database Backup and Recovery Strategy | [12-backup-and-recovery-strategy.md](file:///d:/learning/task_manager/documentation/06-database-design/12-backup-and-recovery-strategy.md) | 1.0.0 | Approved Baseline | RPO / RTO & pg_dump |
| `DOC-DB-013` | Data Retention Strategy | [13-data-retention-strategy.md](file:///d:/learning/task_manager/documentation/06-database-design/13-data-retention-strategy.md) | 1.0.0 | Approved Baseline | Archival Policies |
| `DOC-DB-014` | Database Security Design | [14-database-security-design.md](file:///d:/learning/task_manager/documentation/06-database-design/14-database-security-design.md) | 1.0.0 | Approved Baseline | Role Grants & Passwords |
| `DOC-DB-015` | Phase 6 Review Report | [phase-06-review-report.md](file:///d:/learning/task_manager/documentation/06-database-design/phase-06-review-report.md) | 1.0.0 | Approved Baseline | Phase 6 Sign-off |

### Phase 7: API Design
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-API-001` | API Architecture Document | [01-api-architecture-document.md](file:///d:/learning/task_manager/documentation/07-api-design/01-api-architecture-document.md) | 1.0.0 | Approved Baseline | All API Specifications |
| `DOC-API-002` | API Standards and Conventions | [02-api-standards-and-conventions.md](file:///d:/learning/task_manager/documentation/07-api-design/02-api-standards-and-conventions.md) | 1.0.0 | Approved Baseline | REST Conventions |
| `DOC-API-003` | REST API Specification | [03-rest-api-specification.md](file:///d:/learning/task_manager/documentation/07-api-design/03-rest-api-specification.md) | 1.0.0 | Approved Baseline | All Endpoints Details |
| `DOC-API-004` | Endpoint Catalogue | [04-endpoint-catalogue.md](file:///d:/learning/task_manager/documentation/07-api-design/04-endpoint-catalogue.md) | 1.0.0 | Approved Baseline | Master Endpoint Index |
| `DOC-API-005` | Request and Response Schemas | [05-request-and-response-schemas.md](file:///d:/learning/task_manager/documentation/07-api-design/05-request-and-response-schemas.md) | 1.0.0 | Approved Baseline | JSON Schemas & Examples |
| `DOC-API-006` | HTTP Status Code Catalogue | [06-http-status-code-catalogue.md](file:///d:/learning/task_manager/documentation/07-api-design/06-http-status-code-catalogue.md) | 1.0.0 | Approved Baseline | Status Mapping |
| `DOC-API-007` | Error Response Standards | [07-error-response-standards.md](file:///d:/learning/task_manager/documentation/07-api-design/07-error-response-standards.md) | 1.0.0 | Approved Baseline | RFC 7807 Standard |
| `DOC-API-008` | Pagination Design | [08-pagination-design.md](file:///d:/learning/task_manager/documentation/07-api-design/08-pagination-design.md) | 1.0.0 | Approved Baseline | Pageable Contracts |
| `DOC-API-009` | Filtering and Sorting Design | [09-filtering-and-sorting-design.md](file:///d:/learning/task_manager/documentation/07-api-design/09-filtering-and-sorting-design.md) | 1.0.0 | Approved Baseline | Multi-Criteria Queries |
| `DOC-API-010` | API Authentication Design | [10-api-authentication-design.md](file:///d:/learning/task_manager/documentation/07-api-design/10-api-authentication-design.md) | 1.0.0 | Approved Baseline | Bearer JWT Flow |
| `DOC-API-011` | API Authorization Design | [11-api-authorization-design.md](file:///d:/learning/task_manager/documentation/07-api-design/11-api-authorization-design.md) | 1.0.0 | Approved Baseline | Role & Tenant Guard |
| `DOC-API-012` | API Versioning Strategy | [12-api-versioning-strategy.md](file:///d:/learning/task_manager/documentation/07-api-design/12-api-versioning-strategy.md) | 1.0.0 | Approved Baseline | URI / Header Policies |
| `DOC-API-013` | OpenAPI / Swagger Specification | [13-openapi-swagger-specification.md](file:///d:/learning/task_manager/documentation/07-api-design/13-openapi-swagger-specification.md) | 1.0.0 | Approved Baseline | Full OpenAPI 3.0 YAML |
| `DOC-API-014` | API Gateway Routing Document | [14-api-gateway-routing-document.md](file:///d:/learning/task_manager/documentation/07-api-design/14-api-gateway-routing-document.md) | 1.0.0 | Approved Baseline | Predicates & Filters |
| `DOC-API-015` | Phase 7 Review Report | [phase-07-review-report.md](file:///d:/learning/task_manager/documentation/07-api-design/phase-07-review-report.md) | 1.0.0 | Approved Baseline | Phase 7 Sign-off |

### Phase 8: Kafka and Event Design
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-EVT-001` | Event-Driven Architecture Document | [01-event-driven-architecture-document.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/01-event-driven-architecture-document.md) | 1.0.0 | Approved Baseline | All Event Requirements |
| `DOC-EVT-002` | Kafka Architecture Document | [02-kafka-architecture-document.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/02-kafka-architecture-document.md) | 1.0.0 | Approved Baseline | Broker Topology |
| `DOC-EVT-003` | Topic Catalogue | [03-topic-catalogue.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/03-topic-catalogue.md) | 1.0.0 | Approved Baseline | Topics, Partitions, Retention |
| `DOC-EVT-004` | Event Catalogue | [04-event-catalogue.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/04-event-catalogue.md) | 1.0.0 | Approved Baseline | Task Event Types |
| `DOC-EVT-005` | Event JSON Schema | [05-event-json-schema.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/05-event-json-schema.md) | 1.0.0 | Approved Baseline | Envelope & Payload Schemas |
| `DOC-EVT-006` | Producer Design | [06-producer-design.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/06-producer-design.md) | 1.0.0 | Approved Baseline | Outbox Poller Publisher |
| `DOC-EVT-007` | Consumer Design | [07-consumer-design.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/07-consumer-design.md) | 1.0.0 | Approved Baseline | KafkaListener Adapter |
| `DOC-EVT-008` | Consumer Group Strategy | [08-consumer-group-strategy.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/08-consumer-group-strategy.md) | 1.0.0 | Approved Baseline | Rebalancing & Groups |
| `DOC-EVT-009` | Partitioning Strategy | [09-partitioning-strategy.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/09-partitioning-strategy.md) | 1.0.0 | Approved Baseline | Aggregate Key Ordering |
| `DOC-EVT-010` | Retry Strategy | [10-retry-strategy.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/10-retry-strategy.md) | 1.0.0 | Approved Baseline | Exponential Backoff |
| `DOC-EVT-011` | Dead Letter Topic Design | [11-dead-letter-topic-design.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/11-dead-letter-topic-design.md) | 1.0.0 | Approved Baseline | DLT Isolation & Recovery |
| `DOC-EVT-012` | Event Versioning Strategy | [12-event-versioning-strategy.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/12-event-versioning-strategy.md) | 1.0.0 | Approved Baseline | Schema Evolution |
| `DOC-EVT-013` | Idempotency Design | [13-idempotency-design.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/13-idempotency-design.md) | 1.0.0 | Approved Baseline | Consumed Events Dedup |
| `DOC-EVT-014` | Transactional Outbox Design | [14-transactional-outbox-design.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/14-transactional-outbox-design.md) | 1.0.0 | Approved Baseline | Atomicity & Poller |
| `DOC-EVT-015` | Event Failure Handling | [15-event-failure-handling.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/15-event-failure-handling.md) | 1.0.0 | Approved Baseline | Poison Pills & Failures |
| `DOC-EVT-016` | Event Sequence Diagrams | [16-event-sequence-diagrams.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/16-event-sequence-diagrams.md) | 1.0.0 | Approved Baseline | End-to-End Event Flows |
| `DOC-EVT-017` | Phase 8 Review Report | [phase-08-review-report.md](file:///d:/learning/task_manager/documentation/08-kafka-event-design/phase-08-review-report.md) | 1.0.0 | Approved Baseline | Phase 8 Sign-off |

### Phase 9: Security and Resilience Design
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-SEC-001` | Security Architecture Document | [01-security-architecture-document.md](file:///d:/learning/task_manager/documentation/09-security-resilience/01-security-architecture-document.md) | 1.0.0 | Approved Baseline | SEC-001 to SEC-010, NFR-003 |
| `DOC-SEC-002` | Authentication Design Document | [02-authentication-design-document.md](file:///d:/learning/task_manager/documentation/09-security-resilience/02-authentication-design-document.md) | 1.0.0 | Approved Baseline | FR-001, FR-002, SEC-001 |
| `DOC-SEC-003` | Authorization Design Document | [03-authorization-design-document.md](file:///d:/learning/task_manager/documentation/09-security-resilience/03-authorization-design-document.md) | 1.0.0 | Approved Baseline | FR-004, SEC-002, BRULE-002 |
| `DOC-SEC-004` | JWT Lifecycle and Token Management | [04-jwt-lifecycle-and-token-management.md](file:///d:/learning/task_manager/documentation/09-security-resilience/04-jwt-lifecycle-and-token-management.md) | 1.0.0 | Approved Baseline | SEC-001, SEC-004, ADR-005 |
| `DOC-SEC-005` | Password Security and Hashing Design | [05-password-security-and-hashing-design.md](file:///d:/learning/task_manager/documentation/09-security-resilience/05-password-security-and-hashing-design.md) | 1.0.0 | Approved Baseline | SEC-003, BRULE-008 |
| `DOC-SEC-006` | Secrets Management Design | [06-secrets-management-design.md](file:///d:/learning/task_manager/documentation/09-security-resilience/06-secrets-management-design.md) | 1.0.0 | Approved Baseline | SEC-005, NFR-003 |
| `DOC-SEC-007` | API Security Design | [07-api-security-design.md](file:///d:/learning/task_manager/documentation/09-security-resilience/07-api-security-design.md) | 1.0.0 | Approved Baseline | SEC-006, SEC-007 |
| `DOC-SEC-008` | Input Validation and Data Protection | [08-input-validation-and-data-protection.md](file:///d:/learning/task_manager/documentation/09-security-resilience/08-input-validation-and-data-protection.md) | 1.0.0 | Approved Baseline | SEC-007, SEC-008 |
| `DOC-SEC-009` | Threat Model and Attack Surface (STRIDE) | [09-threat-model-and-attack-surface-analysis.md](file:///d:/learning/task_manager/documentation/09-security-resilience/09-threat-model-and-attack-surface-analysis.md) | 1.0.0 | Approved Baseline | STRIDE Threat Matrix |
| `DOC-SEC-010` | Security Risk Register | [10-security-risk-register.md](file:///d:/learning/task_manager/documentation/09-security-resilience/10-security-risk-register.md) | 1.0.0 | Approved Baseline | Risk Scoring & Mitigations |
| `DOC-RES-001` | Resilience Architecture Document | [11-resilience-architecture-document.md](file:///d:/learning/task_manager/documentation/09-security-resilience/11-resilience-architecture-document.md) | 1.0.0 | Approved Baseline | NFR-002, NFR-006 |
| `DOC-RES-002` | Timeout Strategy Document | [12-timeout-strategy-document.md](file:///d:/learning/task_manager/documentation/09-security-resilience/12-timeout-strategy-document.md) | 1.0.0 | Approved Baseline | Gateway & Client Timeouts |
| `DOC-RES-003` | Retry Strategy Document | [13-retry-strategy-document.md](file:///d:/learning/task_manager/documentation/09-security-resilience/13-retry-strategy-document.md) | 1.0.0 | Approved Baseline | Idempotent HTTP Retries |
| `DOC-RES-004` | Circuit Breaker Design Document | [14-circuit-breaker-design-document.md](file:///d:/learning/task_manager/documentation/09-security-resilience/14-circuit-breaker-design-document.md) | 1.0.0 | Approved Baseline | Resilience4j Circuit Breaker |
| `DOC-RES-005` | Rate Limiting Design Document | [15-rate-limiting-design-document.md](file:///d:/learning/task_manager/documentation/09-security-resilience/15-rate-limiting-design-document.md) | 1.0.0 | Approved Baseline | Spring Cloud Gateway Limiter |
| `DOC-RES-006` | Bulkhead Pattern Design | [16-bulkhead-pattern-design.md](file:///d:/learning/task_manager/documentation/09-security-resilience/16-bulkhead-pattern-design.md) | 1.0.0 | Approved Baseline | Thread & Semaphore Isolation |
| `DOC-RES-007` | Graceful Degradation Strategy | [17-graceful-degradation-strategy.md](file:///d:/learning/task_manager/documentation/09-security-resilience/17-graceful-degradation-strategy.md) | 1.0.0 | Approved Baseline | Fallbacks & Degradation |
| `DOC-RES-008` | Failure Recovery Design | [18-failure-recovery-design.md](file:///d:/learning/task_manager/documentation/09-security-resilience/18-failure-recovery-design.md) | 1.0.0 | Approved Baseline | Self-Healing & MTTR |
| `DOC-SEC-011` | Phase 9 Review Report | [phase-09-review-report.md](file:///d:/learning/task_manager/documentation/09-security-resilience/phase-09-review-report.md) | 1.0.0 | Approved Baseline | Phase 9 Sign-off |

### Phase 10: Antigravity Automation Foundation
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-AUTO-001` | Antigravity Automation Strategy | [01-antigravity-automation-strategy.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/01-antigravity-automation-strategy.md) | 1.0.0 | Approved Baseline | Automation Baseline |
| `DOC-AUTO-002` | Antigravity Rules Specification | [02-antigravity-rules.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/02-antigravity-rules.md) | 1.0.0 | Approved Baseline | Engineering Standards |
| `DOC-AUTO-003` | Agent Instructions and Guidelines | [03-agent-instructions-and-guidelines.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/03-agent-instructions-and-guidelines.md) | 1.0.0 | Approved Baseline | AI Agent Operating Roles |
| `DOC-AUTO-004` | Automated Development Workflow | [04-automated-development-workflow.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/04-automated-development-workflow.md) | 1.0.0 | Approved Baseline | TDD / Incremental SDLC |
| `DOC-AUTO-005` | Documentation Automation Workflow | [05-documentation-automation-workflow.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/05-documentation-automation-workflow.md) | 1.0.0 | Approved Baseline | Living Traceability Sync |
| `DOC-AUTO-006` | Automated Code Review Standards | [06-automated-code-review-standards.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/06-automated-code-review-standards.md) | 1.0.0 | Approved Baseline | Static Analysis & PR Gates |
| `DOC-AUTO-007` | Automated Testing Workflow | [07-automated-testing-workflow.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/07-automated-testing-workflow.md) | 1.0.0 | Approved Baseline | CI Automated Test Suites |
| `DOC-AUTO-008` | Definition of Ready (DoR) | [08-definition-of-ready.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/08-definition-of-ready.md) | 1.0.0 | Approved Baseline | Story Ingestion Gate |
| `DOC-AUTO-009` | Definition of Done (DoD) | [09-definition-of-done.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/09-definition-of-done.md) | 1.0.0 | Approved Baseline | Release / Merge Sign-off |
| `DOC-AUTO-010` | Coding Standards and Style Guide | [10-coding-standards-and-style-guide.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/10-coding-standards-and-style-guide.md) | 1.0.0 | Approved Baseline | Java 21 & React Clean Code |
| `DOC-AUTO-011` | Git Branching Strategy | [11-git-branching-strategy.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/11-git-branching-strategy.md) | 1.0.0 | Approved Baseline | GitFlow & Feature Branches |
| `DOC-AUTO-012` | Commit and PR Standards | [12-commit-and-pr-standards.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/12-commit-and-pr-standards.md) | 1.0.0 | Approved Baseline | Conventional Commits & PRs |
| `DOC-AUTO-013` | Quality Gate and Verification Checklist | [13-quality-gate-checklist.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/13-quality-gate-checklist.md) | 1.0.0 | Approved Baseline | Quality Gates Matrix |
| `DOC-AUTO-014` | Phase 10 Review Report | [phase-10-review-report.md](file:///d:/learning/task_manager/documentation/10-antigravity-automation/phase-10-review-report.md) | 1.0.0 | Approved Baseline | Phase 10 Sign-off |

### Phase 11: Infrastructure and Environment Design
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-INF-001` | Infrastructure Architecture Document | [01-infrastructure-architecture-document.md](file:///d:/learning/task_manager/documentation/11-infrastructure/01-infrastructure-architecture-document.md) | 1.0.0 | Approved Baseline | NFR-007, ADR-002, ADR-003 |
| `DOC-INF-002` | Development Setup Guide | [02-development-setup-guide.md](file:///d:/learning/task_manager/documentation/11-infrastructure/02-development-setup-guide.md) | 1.0.0 | Approved Baseline | Developer Environment |
| `DOC-INF-003` | Local Environment Configuration | [03-local-environment-configuration.md](file:///d:/learning/task_manager/documentation/11-infrastructure/03-local-environment-configuration.md) | 1.0.0 | Approved Baseline | Docker & Host Profiles |
| `DOC-INF-004` | Docker Architecture Document | [04-docker-architecture-document.md](file:///d:/learning/task_manager/documentation/11-infrastructure/04-docker-architecture-document.md) | 1.0.0 | Approved Baseline | Containerization Model |
| `DOC-INF-005` | Multi-Container Docker Compose Design | [05-multi-container-docker-compose-design.md](file:///d:/learning/task_manager/documentation/11-infrastructure/05-multi-container-docker-compose-design.md) | 1.0.0 | Approved Baseline | Full docker-compose.yml |
| `DOC-INF-006` | Container Configuration Specification | [06-container-configuration-specification.md](file:///d:/learning/task_manager/documentation/11-infrastructure/06-container-configuration-specification.md) | 1.0.0 | Approved Baseline | Memory, CPU, Restart |
| `DOC-INF-007` | Environment Variable Catalogue | [07-environment-variable-catalogue.md](file:///d:/learning/task_manager/documentation/11-infrastructure/07-environment-variable-catalogue.md) | 1.0.0 | Approved Baseline | All Service Env Vars |
| `DOC-INF-008` | Network and Port Allocation Document | [08-network-and-port-allocation-document.md](file:///d:/learning/task_manager/documentation/11-infrastructure/08-network-and-port-allocation-document.md) | 1.0.0 | Approved Baseline | Port & Bridge Network Map |
| `DOC-INF-009` | PostgreSQL Infrastructure Design | [09-postgresql-infrastructure-design.md](file:///d:/learning/task_manager/documentation/11-infrastructure/09-postgresql-infrastructure-design.md) | 1.0.0 | Approved Baseline | Dedicated Databases |
| `DOC-INF-010` | Apache Kafka Infrastructure Design | [10-kafka-infrastructure-design.md](file:///d:/learning/task_manager/documentation/11-infrastructure/10-kafka-infrastructure-design.md) | 1.0.0 | Approved Baseline | KRaft/Zookeeper Broker |
| `DOC-INF-011` | Eureka Infrastructure Design | [11-eureka-infrastructure-design.md](file:///d:/learning/task_manager/documentation/11-infrastructure/11-eureka-infrastructure-design.md) | 1.0.0 | Approved Baseline | Service Registry Node |
| `DOC-INF-012` | Environment Separation Strategy | [12-environment-separation-strategy.md](file:///d:/learning/task_manager/documentation/11-infrastructure/12-environment-separation-strategy.md) | 1.0.0 | Approved Baseline | Local, CI, Staging, Prod |
| `DOC-INF-013` | Local Startup & Shutdown Runbook | [13-local-startup-and-shutdown-runbook.md](file:///d:/learning/task_manager/documentation/11-infrastructure/13-local-startup-and-shutdown-runbook.md) | 1.0.0 | Approved Baseline | Operational Runbook |
| `DOC-INF-014` | Phase 11 Review Report | [phase-11-review-report.md](file:///d:/learning/task_manager/documentation/11-infrastructure/phase-11-review-report.md) | 1.0.0 | Approved Baseline | Phase 11 Sign-off |

### Phase 12: Development Documentation
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-DEV-001` | Backend Development Guide | [01-backend-development-guide.md](file:///d:/learning/task_manager/documentation/12-development/01-backend-development-guide.md) | 1.0.0 | Approved Baseline | Java 21, Spring Boot 3.3 |
| `DOC-DEV-002` | Frontend Development Guide | [02-frontend-development-guide.md](file:///d:/learning/task_manager/documentation/12-development/02-frontend-development-guide.md) | 1.0.0 | Approved Baseline | React 18, Vite, Tailwind |
| `DOC-DEV-003` | API Gateway Implementation Guide | [03-api-gateway-implementation-guide.md](file:///d:/learning/task_manager/documentation/12-development/03-api-gateway-implementation-guide.md) | 1.0.0 | Approved Baseline | Gateway Routing & Filters |
| `DOC-DEV-004` | User Service Implementation Guide | [04-user-service-implementation-guide.md](file:///d:/learning/task_manager/documentation/12-development/04-user-service-implementation-guide.md) | 1.0.0 | Approved Baseline | Auth & User Domain |
| `DOC-DEV-005` | Task Service Implementation Guide | [05-task-service-implementation-guide.md](file:///d:/learning/task_manager/documentation/12-development/05-task-service-implementation-guide.md) | 1.0.0 | Approved Baseline | Task Domain & Outbox |
| `DOC-DEV-006` | Notification Service Implementation Guide | [06-notification-service-implementation-guide.md](file:///d:/learning/task_manager/documentation/12-development/06-notification-service-implementation-guide.md) | 1.0.0 | Approved Baseline | Kafka Consumer & In-App Notif |
| `DOC-DEV-007` | Service Discovery Implementation Guide | [07-service-discovery-implementation-guide.md](file:///d:/learning/task_manager/documentation/12-development/07-service-discovery-implementation-guide.md) | 1.0.0 | Approved Baseline | Eureka Server & Client Config |
| `DOC-DEV-008` | Database Implementation Guide | [08-database-implementation-guide.md](file:///d:/learning/task_manager/documentation/12-development/08-database-implementation-guide.md) | 1.0.0 | Approved Baseline | JPA Entities & Flyway Migrations |
| `DOC-DEV-009` | Kafka Integration Guide | [09-kafka-integration-guide.md](file:///d:/learning/task_manager/documentation/12-development/09-kafka-integration-guide.md) | 1.0.0 | Approved Baseline | Producer, Consumer, DLT |
| `DOC-DEV-010` | Security Implementation Guide | [10-security-implementation-guide.md](file:///d:/learning/task_manager/documentation/12-development/10-security-implementation-guide.md) | 1.0.0 | Approved Baseline | Spring Security & JWT Filter |
| `DOC-DEV-011` | Error Handling & Logging Guide | [11-error-handling-and-logging-guide.md](file:///d:/learning/task_manager/documentation/12-development/11-error-handling-and-logging-guide.md) | 1.0.0 | Approved Baseline | RFC 7807 & Slf4j MDC Tracing |
| `DOC-DEV-012` | Phase 12 Review Report | [phase-12-review-report.md](file:///d:/learning/task_manager/documentation/12-development/phase-12-review-report.md) | 1.0.0 | Approved Baseline | Phase 12 Sign-off |

### Phase 13: Testing and Quality Assurance
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-TEST-001` | Test Strategy Document | [01-test-strategy-document.md](file:///d:/learning/task_manager/documentation/13-testing-qa/01-test-strategy-document.md) | 1.0.0 | Approved Baseline | QA Baseline, NFR-001 to NFR-010 |
| `DOC-TEST-002` | Master Test Plan | [02-master-test-plan.md](file:///d:/learning/task_manager/documentation/13-testing-qa/02-master-test-plan.md) | 1.0.0 | Approved Baseline | IEEE 829 Test Plan |
| `DOC-TEST-003` | Test Scenarios Document | [03-test-scenarios-document.md](file:///d:/learning/task_manager/documentation/13-testing-qa/03-test-scenarios-document.md) | 1.0.0 | Approved Baseline | End-to-End Test Scenarios |
| `DOC-TEST-004` | Test Case Catalogue | [04-test-case-catalogue.md](file:///d:/learning/task_manager/documentation/13-testing-qa/04-test-case-catalogue.md) | 1.0.0 | Approved Baseline | TC-001 to TC-040 |
| `DOC-TEST-005` | Unit Testing Strategy | [05-unit-testing-strategy.md](file:///d:/learning/task_manager/documentation/13-testing-qa/05-unit-testing-strategy.md) | 1.0.0 | Approved Baseline | JUnit 5, Mockito, AssertJ |
| `DOC-TEST-006` | Integration Testing Strategy | [06-integration-testing-strategy.md](file:///d:/learning/task_manager/documentation/13-testing-qa/06-integration-testing-strategy.md) | 1.0.0 | Approved Baseline | Testcontainers Postgres & Kafka |
| `DOC-TEST-007` | API Testing Strategy | [07-api-testing-strategy.md](file:///d:/learning/task_manager/documentation/13-testing-qa/07-api-testing-strategy.md) | 1.0.0 | Approved Baseline | MockMvc & REST-Assured |
| `DOC-TEST-008` | Contract Testing Strategy | [08-contract-testing-strategy.md](file:///d:/learning/task_manager/documentation/13-testing-qa/08-contract-testing-strategy.md) | 1.0.0 | Approved Baseline | Consumer-Driven Contracts |
| `DOC-TEST-009` | Kafka and Event Testing Strategy | [09-kafka-testing-strategy.md](file:///d:/learning/task_manager/documentation/13-testing-qa/09-kafka-testing-strategy.md) | 1.0.0 | Approved Baseline | EmbeddedKafka & DLT Tests |
| `DOC-TEST-010` | Database Testing Strategy | [10-database-testing-strategy.md](file:///d:/learning/task_manager/documentation/13-testing-qa/10-database-testing-strategy.md) | 1.0.0 | Approved Baseline | Flyway Migrations & Rollback |
| `DOC-TEST-011` | End-to-End (E2E) Testing Strategy | [11-e2e-testing-strategy.md](file:///d:/learning/task_manager/documentation/13-testing-qa/11-e2e-testing-strategy.md) | 1.0.0 | Approved Baseline | Cypress / Playwright E2E |
| `DOC-TEST-012` | Security Testing Strategy | [12-security-testing-strategy.md](file:///d:/learning/task_manager/documentation/13-testing-qa/12-security-testing-strategy.md) | 1.0.0 | Approved Baseline | OWASP Top 10 & IDOR |
| `DOC-TEST-013` | Performance Testing Strategy | [13-performance-testing-strategy.md](file:///d:/learning/task_manager/documentation/13-testing-qa/13-performance-testing-strategy.md) | 1.0.0 | Approved Baseline | Gatling & JMeter Load Tests |
| `DOC-TEST-014` | Resilience Testing Strategy | [14-resilience-testing-strategy.md](file:///d:/learning/task_manager/documentation/13-testing-qa/14-resilience-testing-strategy.md) | 1.0.0 | Approved Baseline | Circuit Breaker & Chaos Tests |
| `DOC-TEST-015` | Test Data Management Strategy | [15-test-data-management-strategy.md](file:///d:/learning/task_manager/documentation/13-testing-qa/15-test-data-management-strategy.md) | 1.0.0 | Approved Baseline | Fixtures, Factories, Cleanup |
| `DOC-TEST-016` | Defect Management Strategy | [16-defect-management-strategy.md](file:///d:/learning/task_manager/documentation/13-testing-qa/16-defect-management-strategy.md) | 1.0.0 | Approved Baseline | Defect Life Cycle & Severity |
| `DOC-TEST-017` | Test Execution Report Template | [17-test-execution-report-template.md](file:///d:/learning/task_manager/documentation/13-testing-qa/17-test-execution-report-template.md) | 1.0.0 | Approved Baseline | Quality Assurance Metrics |
| `DOC-TEST-018` | Phase 13 Review Report | [phase-13-review-report.md](file:///d:/learning/task_manager/documentation/13-testing-qa/phase-13-review-report.md) | 1.0.0 | Approved Baseline | Phase 13 Sign-off |

### Phase 14: DevOps and Deployment
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-OPS-001` | DevOps Architecture Document | [01-devops-architecture-document.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/01-devops-architecture-document.md) | 1.0.0 | Approved Baseline | CI/CD Baseline, NFR-007 |
| `DOC-OPS-002` | CI/CD Strategy Document | [02-cicd-strategy-document.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/02-cicd-strategy-document.md) | 1.0.0 | Approved Baseline | Pipeline Stages & Gates |
| `DOC-OPS-003` | Jenkins Architecture Document | [03-jenkins-architecture-document.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/03-jenkins-architecture-document.md) | 1.0.0 | Approved Baseline | Master-Agent Runner Model |
| `DOC-OPS-004` | Declarative Jenkinsfile Pipeline | [04-jenkinsfile-pipeline-specification.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/04-jenkinsfile-pipeline-specification.md) | 1.0.0 | Approved Baseline | Full Jenkinsfile Syntax |
| `DOC-OPS-005` | Build and Packaging Strategy | [05-build-and-packaging-strategy.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/05-build-and-packaging-strategy.md) | 1.0.0 | Approved Baseline | Maven Jars & Vite Build |
| `DOC-OPS-006` | Automated Testing in CI/CD | [06-automated-testing-in-cicd.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/06-automated-testing-in-cicd.md) | 1.0.0 | Approved Baseline | Surefire & Failsafe Execution |
| `DOC-OPS-007` | Code Quality Integration | [07-code-quality-integration.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/07-code-quality-integration.md) | 1.0.0 | Approved Baseline | SonarQube & Checkstyle |
| `DOC-OPS-008` | Docker Build Strategy | [08-docker-build-strategy.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/08-docker-build-strategy.md) | 1.0.0 | Approved Baseline | Multi-Stage & Image Tagging |
| `DOC-OPS-009` | Deployment Strategy Document | [09-deployment-strategy-document.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/09-deployment-strategy-document.md) | 1.0.0 | Approved Baseline | Rolling Update & Blue-Green |
| `DOC-OPS-010` | Environment Promotion Strategy | [10-environment-promotion-strategy.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/10-environment-promotion-strategy.md) | 1.0.0 | Approved Baseline | Artifact Immutability |
| `DOC-OPS-011` | Rollback Strategy and Runbook | [11-rollback-strategy-and-runbook.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/11-rollback-strategy-and-runbook.md) | 1.0.0 | Approved Baseline | Fast Rollback Runbook |
| `DOC-OPS-012` | Release Management Strategy | [12-release-management-strategy.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/12-release-management-strategy.md) | 1.0.0 | Approved Baseline | SemVer 2.0 & Git Tagging |
| `DOC-OPS-013` | Deployment Runbook | [13-deployment-runbook.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/13-deployment-runbook.md) | 1.0.0 | Approved Baseline | Production Deployment Steps |
| `DOC-OPS-014` | Production Readiness Checklist | [14-production-readiness-checklist.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/14-production-readiness-checklist.md) | 1.0.0 | Approved Baseline | Go/No-Go Launch Gate |
| `DOC-OPS-015` | Phase 14 Review Report | [phase-14-review-report.md](file:///d:/learning/task_manager/documentation/14-devops-deployment/phase-14-review-report.md) | 1.0.0 | Approved Baseline | Phase 14 Sign-off |

### Phase 15: Observability and Maintenance
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-OBS-001` | Observability Architecture Document | [01-observability-architecture-document.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/01-observability-architecture-document.md) | 1.0.0 | Approved Baseline | Three Pillars, NFR-004 |
| `DOC-OBS-002` | Logging Strategy and MDC Tracing | [02-logging-strategy-and-mdc-tracing.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/02-logging-strategy-and-mdc-tracing.md) | 1.0.0 | Approved Baseline | Structured JSON & Masking |
| `DOC-OBS-003` | Metrics Strategy and Micrometer Design | [03-metrics-strategy-and-micrometer-design.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/03-metrics-strategy-and-micrometer-design.md) | 1.0.0 | Approved Baseline | Custom Metrics & Timers |
| `DOC-OBS-004` | Distributed Tracing Specification | [04-distributed-tracing-specification.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/04-distributed-tracing-specification.md) | 1.0.0 | Approved Baseline | Correlation ID Propagation |
| `DOC-OBS-005` | Prometheus Configuration Specification | [05-prometheus-configuration-specification.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/05-prometheus-configuration-specification.md) | 1.0.0 | Approved Baseline | Scrape Jobs & prometheus.yml |
| `DOC-OBS-006` | Grafana Dashboard Design Document | [06-grafana-dashboard-design-document.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/06-grafana-dashboard-design-document.md) | 1.0.0 | Approved Baseline | Dashboards & Panels JSON |
| `DOC-OBS-007` | Alerting Strategy and Rules Catalogue | [07-alerting-strategy-and-rules-catalogue.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/07-alerting-strategy-and-rules-catalogue.md) | 1.0.0 | Approved Baseline | Prometheus Alert Rules |
| `DOC-OBS-008` | Health Check Strategy and Actuator Probes | [08-health-check-strategy-and-actuator-probes.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/08-health-check-strategy-and-actuator-probes.md) | 1.0.0 | Approved Baseline | Liveness & Readiness Probes |
| `DOC-OBS-009` | Service Level Indicators (SLI) Catalogue | [09-sli-catalogue.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/09-sli-catalogue.md) | 1.0.0 | Approved Baseline | Latency, Availability, Errors |
| `DOC-OBS-010` | Service Level Objectives (SLO) Proposals | [10-slo-proposals.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/10-slo-proposals.md) | 1.0.0 | Approved Baseline | 99.9% Availability Target |
| `DOC-OBS-011` | Incident Management Procedure | [11-incident-management-procedure.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/11-incident-management-procedure.md) | 1.0.0 | Approved Baseline | P1-P4 Triage & Post-Mortem |
| `DOC-OBS-012` | Troubleshooting Guide | [12-troubleshooting-guide.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/12-troubleshooting-guide.md) | 1.0.0 | Approved Baseline | Diagnostic Runbook |
| `DOC-OBS-013` | Operational Runbook | [13-operational-runbook.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/13-operational-runbook.md) | 1.0.0 | Approved Baseline | Routine Maintenance & Jobs |
| `DOC-OBS-014` | Backup and Disaster Recovery Runbook | [14-backup-and-disaster-recovery-runbook.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/14-backup-and-disaster-recovery-runbook.md) | 1.0.0 | Approved Baseline | pg_dump RPO & RTO |
| `DOC-OBS-015` | Phase 15 Review Report | [phase-15-review-report.md](file:///d:/learning/task_manager/documentation/15-observability-maintenance/phase-15-review-report.md) | 1.0.0 | Approved Baseline | Phase 15 Sign-off |

---

### Final Documentation Review & Project Handover
| Doc ID | Document Title | File Path | Version | Status | Related Requirements |
|---|---|---|---|---|---|
| `DOC-REV-001` | Final Documentation Review & Handover Document | [final-documentation-review.md](file:///d:/learning/task_manager/documentation/final-documentation-review.md) | 1.0.0 | Approved Baseline | Master SDLC Sign-Off, Implementation Readiness |
