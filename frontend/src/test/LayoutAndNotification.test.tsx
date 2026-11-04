import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { Navbar } from '../components/layout/Navbar';
import { NotificationDrawer } from '../components/features/notifications/NotificationDrawer';
import { notificationApi } from '../api/notificationApi';
import { NotificationItem } from '../types/notification';

vi.mock('../api/notificationApi');

const mockUser = {
  id: 'user-1',
  email: 'john@example.com',
  fullName: 'John Doe',
  role: 'ROLE_USER',
};

const mockNotifications: NotificationItem[] = [
  {
    id: 'n-1',
    userId: 'user-1',
    eventType: 'TASK_CREATED',
    title: 'Task Created',
    message: "Task 'Write Integration Tests' was created",
    read: false,
    createdAt: new Date().toISOString(),
  },
  {
    id: 'n-2',
    userId: 'user-1',
    eventType: 'TASK_COMPLETED',
    title: 'Task Completed',
    message: "Task 'Deploy to Staging' was marked COMPLETED",
    read: true,
    createdAt: new Date().toISOString(),
  },
];

describe('Layout and Notification Drawer', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Navbar', () => {
    it('should render brand logo, navigation links, user name, and unread notification badge', () => {
      const handleToggleDrawer = vi.fn();
      const handleLogout = vi.fn();

      render(
        <MemoryRouter>
          <Navbar
            user={mockUser}
            unreadCount={3}
            onToggleNotificationDrawer={handleToggleDrawer}
            onLogout={handleLogout}
          />
        </MemoryRouter>
      );

      expect(screen.getByText(/task manager/i)).toBeInTheDocument();
      expect(screen.getByText(/dashboard/i)).toBeInTheDocument();
      expect(screen.getByText(/tasks/i)).toBeInTheDocument();
      expect(screen.getByText(/john doe/i)).toBeInTheDocument();
      expect(screen.getByText('3')).toBeInTheDocument();

      const bellButton = screen.getByRole('button', { name: /open notifications/i });
      fireEvent.click(bellButton);
      expect(handleToggleDrawer).toHaveBeenCalledTimes(1);

      const logoutButton = screen.getByRole('button', { name: /log out/i });
      fireEvent.click(logoutButton);
      expect(handleLogout).toHaveBeenCalledTimes(1);
    });
  });

  describe('NotificationDrawer', () => {
    it('should render notifications list when open and allow marking single notification as read', async () => {
      vi.mocked(notificationApi.getNotifications).mockResolvedValue({
        content: mockNotifications,
        page: 0,
        size: 10,
        totalElements: 2,
        totalPages: 1,
        unreadCount: 1,
      });

      vi.mocked(notificationApi.markAsRead).mockResolvedValue({
        ...mockNotifications[0],
        read: true,
      });

      const handleClose = vi.fn();
      const handleRefreshCount = vi.fn();

      render(
        <NotificationDrawer
          isOpen={true}
          onClose={handleClose}
          onNotificationUpdated={handleRefreshCount}
        />
      );

      await waitFor(() => {
        expect(screen.getByText("Task 'Write Integration Tests' was created")).toBeInTheDocument();
        expect(screen.getByText("Task 'Deploy to Staging' was marked COMPLETED")).toBeInTheDocument();
      });

      // Click on the unread notification to mark as read
      const unreadItem = screen.getByText("Task 'Write Integration Tests' was created");
      fireEvent.click(unreadItem);

      await waitFor(() => {
        expect(notificationApi.markAsRead).toHaveBeenCalledWith('n-1');
        expect(handleRefreshCount).toHaveBeenCalled();
      });
    });

    it('should allow marking all notifications as read', async () => {
      vi.mocked(notificationApi.getNotifications).mockResolvedValue({
        content: mockNotifications,
        page: 0,
        size: 10,
        totalElements: 2,
        totalPages: 1,
        unreadCount: 1,
      });

      vi.mocked(notificationApi.markAllAsRead).mockResolvedValue({ updatedCount: 1 });

      render(
        <NotificationDrawer
          isOpen={true}
          onClose={vi.fn()}
          onNotificationUpdated={vi.fn()}
        />
      );

      await waitFor(() => {
        expect(screen.getByText(/mark all as read/i)).toBeInTheDocument();
      });

      const markAllBtn = screen.getByRole('button', { name: /mark all as read/i });
      fireEvent.click(markAllBtn);

      await waitFor(() => {
        expect(notificationApi.markAllAsRead).toHaveBeenCalledTimes(1);
      });
    });
  });
});
