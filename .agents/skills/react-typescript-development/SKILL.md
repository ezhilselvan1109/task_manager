---
name: react-typescript-development
description: >-
  Use this skill when developing the React 18 TypeScript Single Page Application (SPA),
  including Vite setup, TailwindCSS styling, Axios client, and state management.
---

# React & TypeScript Development Skill

## Purpose
Governs the implementation of the frontend Single Page Application (SPA) using React 18, TypeScript 5, Vite, TailwindCSS, and Axios.

## When to Use
- Implementing user authentication UI (login, register, profile).
- Implementing task management views (board/list, creation modal, filter bars, pagination).
- Implementing the productivity dashboard and in-app notification center.
- Integrating frontend with the API Gateway endpoints (`/api/v1/*`).

## Required Inputs
- Frontend root directory `frontend/`.
- UI/UX specs and wireframes from [Phase 3 Business Analysis](file:///d:/learning/task_manager/documentation/03-business-analysis/04-wireframes-and-ui-mockups.md).

## Step-by-Step Procedure
1. **Maintain Project Directory Structure**:
   ```
   frontend/src/
   ├── api/          # Axios instance and API service functions
   ├── components/   # Reusable UI components (Button, Modal, Card, Input)
   ├── context/      # AuthContext, NotificationContext
   ├── hooks/        # Custom hooks (useTasks, useAuth, useDebounce)
   ├── pages/        # LoginPage, RegisterPage, DashboardPage, TasksPage
   ├── types/        # TypeScript interfaces matching backend DTO records
   └── App.tsx
   ```
2. **Define Strict TypeScript Interfaces**:
   Mirror backend Java records:
   ```typescript
   export interface Task {
     id: string;
     userId: string;
     title: string;
     description?: string;
     status: 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
     priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
     dueDate?: string;
     createdAt: string;
     updatedAt: string;
   }
   ```
3. **Configure Axios with JWT Interceptor**:
   - Attach stored JWT Bearer token to request headers.
   - Intercept `401 Unauthorized` responses to clear token and redirect to `/login`.
4. **Style with TailwindCSS**:
   - Use utility classes for responsive design (`sm:`, `md:`, `lg:`).
   - Ensure clean visual cues for task priority (e.g., Red for `URGENT`, Green for `LOW`).

## Applicable Project Standards
- [Rule 01: Architecture & Stack](file:///d:/learning/task_manager/.agents/rules/01-architecture-and-stack.md)
- [Frontend Development Guide](file:///d:/learning/task_manager/documentation/12-development/08-frontend-development-guide.md)

## Validation Checklist
- [ ] TypeScript compilation succeeds with `tsc --noEmit` and zero type errors.
- [ ] No `any` types; all API models have explicit interfaces.
- [ ] Responsive design functions cleanly on mobile and desktop viewports.
- [ ] JWT tokens are safely stored in memory / secure storage, discarded on logout.

## Common Mistakes
- Using `any` type escapes instead of defining backend-matching interfaces.
- Storing unvalidated passwords in React state longer than necessary.

## Completion Criteria
Frontend builds cleanly via `npm run build`, passes ESLint checks, and seamlessly interacts with API Gateway endpoints.

