import React, { useState, useEffect, useCallback } from 'react';
import { Outlet } from 'react-router-dom';
import { Navbar } from './Navbar';
import { NotificationDrawer } from '../features/notifications/NotificationDrawer';
import { useAuth } from '../../context/AuthContext';
import { notificationApi } from '../../api/notificationApi';

export const AppLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [isDrawerOpen, setIsDrawerOpen] = useState<boolean>(false);

  const fetchUnreadCount = useCallback(async () => {
    try {
      const response = await notificationApi.getUnreadCount();
      setUnreadCount(response.unreadCount);
    } catch {
      // Ignored
    }
  }, []);

  useEffect(() => {
    fetchUnreadCount();
  }, [fetchUnreadCount]);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col text-slate-900">
      <Navbar
        user={user}
        unreadCount={unreadCount}
        onToggleNotificationDrawer={() => setIsDrawerOpen((prev) => !prev)}
        onLogout={logout}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>

      <NotificationDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        onNotificationUpdated={fetchUnreadCount}
      />
    </div>
  );
};

