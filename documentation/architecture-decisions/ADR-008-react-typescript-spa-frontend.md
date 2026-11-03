# ADR-008: React 18 and TypeScript for Frontend Single Page Application

| Metadata Item | Details |
|---|---|
| **ADR ID** | ADR-008 |
| **Status** | Approved |
| **Date** | 2026-11-03 |
| **Deciders** | Product Manager, Frontend Lead |
| **Related Documents** | `DOC-REQ-002`, `DOC-ARCH-004` |

---

## 1. Context & Problem Statement
The user experience demands an ultra-fast, reactive web client capable of optimistic UI updates (instant task status toggles, inline sorting, debounced search) without full-page browser reloads, combined with strict compile-time type safety across DTOs.

## 2. Decision
Implement the frontend as a **Single Page Application (SPA)** using **React 18** and **TypeScript 5**:
1. Tooling: Vite build tool for rapid development and optimized production bundling.
2. Styling: Tailwind CSS for lightweight utility styling and WCAG 2.1 AA accessible color palettes.
3. Network / State Management: Axios with interceptors for automatic JWT Bearer token attachment and 401 refresh token retry flows; TanStack React Query for server-state caching and optimistic mutations.

## 3. Consequences
- **Positive**:
  - Sub-second UI responsiveness and zero page reloads.
  - End-to-end type safety: TypeScript interfaces match backend DTO schemas, preventing runtime property mismatch errors.
  - Static container packaging: SPA compiles to static HTML/JS/CSS served via lightweight Nginx container consuming $< 128\text{MB}$ RAM.
- **Negative / Trade-offs**:
  - Client-side rendering (CSR) requires browser JavaScript execution (acceptable for authenticated internal productivity applications).

