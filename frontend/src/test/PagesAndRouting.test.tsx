import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { DashboardPage } from '../pages/DashboardPage';
import { TasksPage } from '../pages/TasksPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { ProtectedRoute } from '../routes/ProtectedRoute';
import { taskApi } from '../api/taskApi';
import { notificationApi } from '../api/notificationApi';
import { Task, TaskMetrics, TaskPageResponse } from '../types/task';
import { NotificationPageResponse } from '../types/notification';

// Mock AuthContext
const mockUseAuth = vi.fn();

vi.mock('../context/AuthContext', () => ({
  useAuth: () => mockUseAuth(),
  AuthProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Mock the APIs
vi.mock('../api/taskApi', () => ({
  taskApi: {
    getTasks: vi.fn(),
    getTask: vi.fn(),
    createTask: vi.fn(),
    updateTask: vi.fn(),
    changeStatus: vi.fn(),
    completeTask: vi.fn(),
    deleteTask: vi.fn(),
    getTaskMetrics: vi.fn(),
  },
}));

vi.mock('../api/notificationApi', () => ({
  notificationApi: {
    getNotifications: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn(),
    getUnreadCount: vi.fn(),
    deleteNotification: vi.fn(),
  },
}));

const mockTask: Task = {
  id: 'task-101',
  userId: 'user-1',
  title: 'Implement Outbox Worker',
  description: 'Add scheduled poller with SKIP LOCKED',
  status: 'TODO',
  priority: 'HIGH',
  dueDate: '2026-11-10T12:00:00Z',
  isDeleted: false,
  createdAt: '2026-11-04T09:00:00Z',
  updatedAt: '2026-11-04T09:00:00Z',
};

const mockMetrics: TaskMetrics = {
  totalTasks: 42,
  todoCount: 15,
  inProgressCount: 12,
  completedCount: 10,
  cancelledCount: 5,
  highPriorityCount: 8,
  overdueCount: 3,
};

const mockTaskPage: TaskPageResponse = {
  content: [mockTask],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
  first: true,
  last: true,
  empty: false,
};

const mockNotifPage: NotificationPageResponse = {
  content: [
    {
      id: 'notif-1',
      userId: 'user-1',
      taskId: 'task-101',
      eventType: 'TASK_CREATED',
      title: 'Task Created',
      message: 'Task Implement Outbox Worker was created',
      read: false,
      createdAt: '2026-11-04T09:05:00Z',
    },
  ],
  page: 0,
  size: 5,
  totalElements: 1,
  totalPages: 1,
  unreadCount: 1,
};

describe('Pages and Routing Verification', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('LoginPage', () => {
    it('should render form and handle validation errors', () => {
      mockUseAuth.mockReturnValue({
        user: null,
        accessToken: null,
        isAuthenticated: false,
        isLoading: false,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
      });

      render(
        <MemoryRouter>
          <LoginPage />
        </MemoryRouter>
      );

      expect(screen.getByRole('heading', { name: /sign in to your account/i })).toBeInTheDocument();
      expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/password/i)).toBeInTheDocument();

      // Submit without values -> triggers validation
      const submitBtn = screen.getByRole('button', { name: /sign in/i });
      fireEvent.click(submitBtn);

      expect(screen.getByText(/email is required/i)).toBeInTheDocument();
      expect(screen.getByText(/password is required/i)).toBeInTheDocument();
    });

    it('should call login on form submit with valid inputs', async () => {
      const mockLogin = vi.fn().mockResolvedValue({
        accessToken: 'jwt-123',
        refreshToken: 'rt-123',
        tokenType: 'Bearer',
        expiresIn: 3600,
        user: { id: 'u1', email: 'test@example.com', fullName: 'Test User', role: 'ROLE_USER' },
      });

      mockUseAuth.mockReturnValue({
        user: null,
        accessToken: null,
        isAuthenticated: false,
        isLoading: false,
        login: mockLogin,
        register: vi.fn(),
        logout: vi.fn(),
      });

      render(
        <MemoryRouter>
          <LoginPage />
        </MemoryRouter>
      );

      fireEvent.change(screen.getByLabelText(/email address/i), {
        target: { value: 'test@example.com' },
      });
      fireEvent.change(screen.getByLabelText(/password/i), {
        target: { value: 'Password123!' },
      });

      fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

      await waitFor(() => {
        expect(mockLogin).toHaveBeenCalledWith({
          email: 'test@example.com',
          password: 'Password123!',
        });
      });
    });
  });

  describe('RegisterPage', () => {
    it('should validate password length and password match', () => {
      mockUseAuth.mockReturnValue({
        user: null,
        accessToken: null,
        isAuthenticated: false,
        isLoading: false,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
      });

      render(
        <MemoryRouter>
          <RegisterPage />
        </MemoryRouter>
      );

      fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: 'John Doe' } });
      fireEvent.change(screen.getByLabelText(/email address/i), {
        target: { value: 'john@example.com' },
      });
      fireEvent.change(screen.getByLabelText(/^password/i), { target: { value: 'short' } });
      fireEvent.change(screen.getByLabelText(/confirm password/i), {
        target: { value: 'mismatch' },
      });

      fireEvent.click(screen.getByRole('button', { name: /create account/i }));

      expect(screen.getByText(/password must be at least 8 characters/i)).toBeInTheDocument();
      expect(screen.getByText(/passwords do not match/i)).toBeInTheDocument();
    });
  });

  describe('DashboardPage', () => {
    it('should render KPI metrics and activity feed', async () => {
      mockUseAuth.mockReturnValue({
        user: { id: 'u1', email: 'alex@example.com', fullName: 'Alex Rivera', role: 'ROLE_USER' },
        accessToken: 'jwt-123',
        isAuthenticated: true,
        isLoading: false,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
      });

      vi.mocked(taskApi.getTaskMetrics).mockResolvedValue(mockMetrics);
      vi.mocked(notificationApi.getNotifications).mockResolvedValue(mockNotifPage);

      render(
        <MemoryRouter>
          <DashboardPage />
        </MemoryRouter>
      );

      expect(screen.getByText(/welcome back, alex rivera!/i)).toBeInTheDocument();

      await waitFor(() => {
        expect(screen.getByText('Total Tasks')).toBeInTheDocument();
        expect(screen.getByText('42')).toBeInTheDocument();
        expect(screen.getByText('Recent Activity')).toBeInTheDocument();
        expect(screen.getByText('Task Created')).toBeInTheDocument();
      });
    });
  });

  describe('TasksPage', () => {
    it('should render workspace, filters, and tasks board', async () => {
      vi.mocked(taskApi.getTasks).mockResolvedValue(mockTaskPage);

      render(
        <MemoryRouter>
          <TasksPage />
        </MemoryRouter>
      );

      expect(screen.getByText('Task Workspace')).toBeInTheDocument();
      expect(
        screen.getByPlaceholderText(/search tasks by title or description/i)
      ).toBeInTheDocument();

      await waitFor(() => {
        expect(screen.getByText('Implement Outbox Worker')).toBeInTheDocument();
      });
    });
  });

  describe('NotFoundPage', () => {
    it('should render 404 message and dashboard redirect link', () => {
      render(
        <MemoryRouter>
          <NotFoundPage />
        </MemoryRouter>
      );

      expect(screen.getByText('404')).toBeInTheDocument();
      expect(screen.getByText('Page Not Found')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /back to dashboard/i })).toBeInTheDocument();
    });
  });

  describe('ProtectedRoute', () => {
    it('should redirect unauthenticated users to login', () => {
      mockUseAuth.mockReturnValue({
        user: null,
        accessToken: null,
        isAuthenticated: false,
        isLoading: false,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
      });

      render(
        <MemoryRouter initialEntries={['/dashboard']}>
          <Routes>
            <Route path="/login" element={<div>Login Screen Mock</div>} />
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<div>Protected Dashboard Content</div>} />
            </Route>
          </Routes>
        </MemoryRouter>
      );

      expect(screen.getByText('Login Screen Mock')).toBeInTheDocument();
      expect(screen.queryByText('Protected Dashboard Content')).not.toBeInTheDocument();
    });

    it('should allow authenticated users to access protected routes', () => {
      mockUseAuth.mockReturnValue({
        user: { id: 'u1', email: 'test@example.com', fullName: 'Test User', role: 'ROLE_USER' },
        accessToken: 'jwt-123',
        isAuthenticated: true,
        isLoading: false,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
      });

      render(
        <MemoryRouter initialEntries={['/dashboard']}>
          <Routes>
            <Route path="/login" element={<div>Login Screen Mock</div>} />
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<div>Protected Dashboard Content</div>} />
            </Route>
          </Routes>
        </MemoryRouter>
      );

      expect(screen.getByText('Protected Dashboard Content')).toBeInTheDocument();
      expect(screen.queryByText('Login Screen Mock')).not.toBeInTheDocument();
    });
  });
});
