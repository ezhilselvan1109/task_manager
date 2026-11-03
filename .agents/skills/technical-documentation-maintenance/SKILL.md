---
name: technical-documentation-maintenance
description: >-
  Use this skill when creating, updating, or synchronizing technical documentation,
  maintaining the Requirements Traceability Matrix (RTM), or authoring ADRs.
---

# Technical Documentation Maintenance Skill

## Purpose
Governs the creation, maintenance, synchronization, and versioning of all technical documentation artifacts across the project lifecycle.

## When to Use
- Synchronizing documentation after an architectural change, schema evolution, or API update.
- Updating `requirements-traceability-matrix.md` when adding new tests or endpoints.
- Authoring an Architecture Decision Record (`ADR-xxx.md`).
- Maintaining phase review reports and progress tracking files.

## Required Inputs
- Target documentation path in `documentation/`.
- Requirement ID or architectural decision context.

## Step-by-Step Procedure
1. **Consult Approved Documentation Hierarchy**:
   - Locate the owning phase directory (`01` through `15`).
2. **Apply Consistent Metadata & Headers**:
   Every document must include the standard metadata header table:
   ```markdown
   # Document Title

   | Field | Value |
   |---|---|
   | **Document ID** | DOC-XXX-YYY |
   | **Traceability** | BR-xxx, FR-xxx |
   | **Status** | Approved Baseline |
   | **Version** | 1.0.0 |
   ```
3. **Update Traceability Matrix**:
   - If a new requirement, API, or test case is added, update [requirements-traceability-matrix.md](file:///d:/learning/task_manager/documentation/requirements-traceability-matrix.md).
4. **Author Architecture Decision Records (ADR)**:
   - File path: `documentation/architecture-decisions/ADR-XXX-<title>.md`.
   - Sections: Status, Context, Decision, Consequences, Compliance.
5. **Update Master Index and Progress Tracker**:
   - Register new files in `documentation/project-documentation-index.md`.
   - Update `documentation/project-progress.md`.

## Applicable Project Standards
- [Rule 01: Architecture & Stack](file:///d:/learning/task_manager/.agents/rules/01-architecture-and-stack.md)
- [Rule 12: Security & Documentation Standards](file:///d:/learning/task_manager/.agents/rules/12-security-and-documentation.md)

## Validation Checklist
- [ ] Document ID adheres to project taxonomy (`DOC-XXX-YYY`).
- [ ] Bi-directional traceability to requirements is explicitly linked.
- [ ] No placeholder stubs or unverified assumptions.
- [ ] Indexed in `project-documentation-index.md`.

## Common Mistakes
- Modifying code or schemas without updating corresponding documentation.
- Changing requirements without reflecting updates in the master RTM.

## Completion Criteria
Documentation remains synchronized with code, all cross-references are valid, and RTM maintains 100% bi-directional traceability.

