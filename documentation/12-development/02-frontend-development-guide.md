# Frontend Development Guide

**Document Identifier:** DOC-DEV-002  
**Traceability References:** Master Technology Stack Baseline, DOC-AUTO-010  
**Stack Baseline:** React 18, TypeScript 5.x, Vite, TailwindCSS, Axios, TanStack Query  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document provides the frontend development guide for building the Task Management single-page application (SPA). It outlines the module architecture, state management, HTTP interceptor design, component hierarchies, and routing guards.

---

## 2. Frontend Directory Structure

Located under `frontend/`:

```
frontend/src/
├── api/                 # Axios HTTP client, interceptors, base API endpoints
│   ├── apiClient.ts     # Central configured Axios instance
│   └── taskApi.ts       # Service-specific API call methods
├── assets/              # Static SVG icons, images, brand assets
├── components/          # Reusable design system UI components
│   ├── common/          # Button, Input, Modal, Badge, Dropdown, Table
│   └── layout/          # Navbar, Sidebar, AppLayout, NotificationBell
├── features/            # Feature-sliced domain modules
│   ├── auth/            # LoginForm, RegisterForm, AuthProvider
│   ├── tasks/           # TaskList, TaskCard, TaskFilter, TaskModal
│   ├── dashboard/       # MetricsSummaryCards, OverdueTaskChart
│   └── notifications/   # NotificationTray, NotificationItem
├── hooks/               # Custom React hooks (useTasks, useAuth, useDebounce)
├── routes/              # React Router v6 definitions & ProtectedRoute
├── store/               # Zustand / Context state (authStore, uiStore)
├── types/               # TypeScript interfaces matching backend DTO schemas
└── utils/               # Date formatters, string truncation, validation schemas
```

---

## 3. Axios Interceptor & Authentication Flow

Configured in `src/api/apiClient.ts`:

```typescript
import axios from 'axios';
import { useAuthStore } from '../store/authStore';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080',
  timeout: 8000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach JWT Bearer token
apiClient.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().accessToken;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Catch 401 Unauthorized
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      window.location.href = '/login?expired=true';
    }
    return Promise.reject(error);
  }
);
```

---

## 4. Protected Route Guard

Configured in `src/routes/ProtectedRoute.tsx`:

```tsx
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export const ProtectedRoute: React.FC = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};
```

---

## 5. Development and Build Commands

```bash
# Install dependencies
npm install

# Start local Vite development server (hot-reload on http://localhost:5173)
npm run dev

# Run TypeScript type check and ESLint
npm run lint

# Compile production-ready static assets to /dist
npm run build
```

