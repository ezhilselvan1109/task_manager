import React, { useEffect, useState, useCallback } from 'react';
import { X, CheckCheck, Trash2, CheckCircle2, PlusCircle, RefreshCw, BellOff } from 'lucide-react';
import { NotificationItem } from '../../../types/notification';
import { notificationApi } from '../../../api/notificationApi';
import { LoadingSpinner } from '../../common/LoadingSpinner';

export interface NotificationDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  onNotificationUpdated?: () => void;
}

export const NotificationDrawer: React.FC<NotificationDrawerProps> = ({
  isOpen,
  onClose,
  onNotificationUpdated,
}) => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const fetchNotifications = useCallback(async () => {
    setIsLoading(true);
    try {
      const response = await notificationApi.getNotifications(0, 30);
      setNotifications(response.content);
    } catch {
      // Ignored: Error handled gracefully in UI
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isOpen) {
      fetchNotifications();
    }
  }, [isOpen, fetchNotifications]);

  const handleMarkAsRead = async (id: string, currentlyRead: boolean) => {
    if (currentlyRead) return;
    try {
      await notificationApi.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true, readAt: new Date().toISOString() } : n))
      );
      onNotificationUpdated?.();
    } catch {
      // Ignored
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      setNotifications((prev) =>
        prev.map((n) => ({ ...n, read: true, readAt: new Date().toISOString() }))
      );
      onNotificationUpdated?.();
    } catch {
      // Ignored
    }
  };

  const handleDelete = async (e: React.MouseEvent, id: string) => {
    e.stopPropagation();
    try {
      await notificationApi.deleteNotification(id);
      setNotifications((prev) => prev.filter((n) => n.id !== id));
      onNotificationUpdated?.();
    } catch {
      // Ignored
    }
  };

  if (!isOpen) return null;

  const renderIcon = (eventType: string) => {
    switch (eventType) {
      case 'TASK_COMPLETED':
        return <CheckCircle2 className="w-4 h-4 text-emerald-500" />;
      case 'TASK_CREATED':
        return <PlusCircle className="w-4 h-4 text-sky-500" />;
      case 'TASK_DELETED':
        return <Trash2 className="w-4 h-4 text-rose-500" />;
      default:
        return <RefreshCw className="w-4 h-4 text-amber-500" />;
    }
  };

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <div className="fixed inset-0 z-50 overflow-hidden animate-in fade-in duration-200">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      />

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-md bg-white shadow-2xl flex flex-col border-l border-slate-200">
          {/* Header */}
          <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
            <div className="flex items-center gap-2">
              <h2 className="text-base font-bold text-slate-800">Notifications</h2>
              {unreadCount > 0 && (
                <span className="px-2 py-0.5 bg-rose-100 text-rose-700 text-xs font-semibold rounded-full">
                  {unreadCount} new
                </span>
              )}
            </div>
            <div className="flex items-center gap-1">
              {unreadCount > 0 && (
                <button
                  type="button"
                  onClick={handleMarkAllAsRead}
                  aria-label="Mark all as read"
                  className="flex items-center gap-1.5 px-2.5 py-1 text-xs font-medium text-sky-600 hover:text-sky-700 hover:bg-sky-50 rounded-md transition-colors"
                >
                  <CheckCheck className="w-3.5 h-3.5" />
                  Mark all as read
                </button>
              )}
              <button
                type="button"
                onClick={onClose}
                aria-label="Close drawer"
                className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
          </div>

          {/* Body */}
          <div className="flex-1 overflow-y-auto divide-y divide-slate-100">
            {isLoading ? (
              <LoadingSpinner label="Loading notifications..." />
            ) : notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center p-12 text-center text-slate-400">
                <BellOff className="w-10 h-10 mb-3 stroke-[1.5]" />
                <p className="text-sm font-medium text-slate-600">No notifications yet</p>
                <p className="text-xs text-slate-400 mt-1">
                  You will see updates here when task events occur.
                </p>
              </div>
            ) : (
              notifications.map((item) => (
                <div
                  key={item.id}
                  onClick={() => handleMarkAsRead(item.id, item.read)}
                  className={`p-4 transition-colors flex items-start gap-3.5 cursor-pointer hover:bg-slate-50 ${
                    !item.read ? 'bg-sky-50/30' : 'bg-white'
                  }`}
                >
                  <div className="mt-0.5 shrink-0 p-1.5 rounded-full bg-slate-50 border border-slate-100">
                    {renderIcon(item.eventType)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-2 mb-1">
                      <h4 className="text-sm font-semibold text-slate-800 truncate">
                        {item.title}
                      </h4>
                      <span className="text-[11px] text-slate-400 shrink-0">
                        {new Date(item.createdAt).toLocaleTimeString([], {
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </span>
                    </div>
                    <p className="text-xs text-slate-600 line-clamp-2 mb-2 leading-relaxed">
                      {item.message}
                    </p>
                    <div className="flex items-center justify-between text-[11px]">
                      <span className="text-slate-400">
                        {new Date(item.createdAt).toLocaleDateString()}
                      </span>
                      <button
                        type="button"
                        onClick={(e) => handleDelete(e, item.id)}
                        aria-label="Delete notification"
                        className="text-slate-400 hover:text-rose-600 p-1 rounded hover:bg-rose-50 transition-colors"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                  {!item.read && (
                    <span className="w-2 h-2 rounded-full bg-sky-500 mt-2 shrink-0 animate-pulse" />
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

