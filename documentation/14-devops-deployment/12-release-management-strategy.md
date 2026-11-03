# Release Management Strategy

**Document Identifier:** DOC-OPS-012  
**Traceability References:** SemVer 2.0.0, DOC-AUTO-011, DOC-AUTO-012  
**Standard:** Semantic Versioning 2.0.0 & Automated Release Notes  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the release management lifecycle, semantic versioning rules, Git release tagging protocols, and automated changelog generation workflows for the Task Management System.

---

## 2. Semantic Versioning Specification (SemVer 2.0.0)

Version identifiers follow the format:

$$\text{v}\mathbf{X}\mathbf{.}\mathbf{Y}\mathbf{.}\mathbf{Z}$$

| Version Segment | When to Increment | Examples |
|---|---|---|
| **MAJOR ($X$)** | Incompatible API changes, breaking database migrations, or removal of deprecated endpoints. | `v1.0.0` $\rightarrow$ `v2.0.0` |
| **MINOR ($Y$)** | Adding new backward-compatible functional features, new endpoints, or optional filters. | `v1.0.0` $\rightarrow$ `v1.1.0` |
| **PATCH ($Z$)** | Backward-compatible bug fixes, security patches, or internal performance refactoring. | `v1.0.0` $\rightarrow$ `v1.0.1` |

---

## 3. Release Lifecycle & Automated Tagging

```mermaid
gitGraph
    commit id: "feat: user registration"
    commit id: "feat: task CRUD"
    branch release/v1.0.0
    checkout release/v1.0.0
    commit id: "chore: bump version to 1.0.0"
    checkout main
    merge release/v1.0.0 id: "merge release v1.0.0" tag: "v1.0.0"
    checkout develop
    merge release/v1.0.0 id: "sync develop"
```

1. **Release Branch Creation**: A release candidate branch (`release/vX.Y.Z`) branches off `develop` at the end of a sprint.
2. **Version Bump**: Maven versions in `pom.xml` are updated via `mvn versions:set -DnewVersion=X.Y.Z`.
3. **Automated Changelog Generation**: Conventional Commits are parsed to produce standardized GitHub Release Notes:
   ```markdown
   ## Release v1.1.0 (2026-11-03)
   ### Features
   - **task**: add multi-attribute search and priority filtering (#45)
   - **notification**: add in-app unread notification tray (#48)
   ### Fixes
   - **auth**: return 401 instead of 500 on expired jwt (#52)
   ```
4. **Git Tagging**: Merging to `main` creates an annotated cryptographic Git tag `git tag -a v1.1.0 -m "Release v1.1.0"`.

