import { apiClient } from './apiClient';
import {
  BulkReadResponse,
  NotificationItem,
  NotificationPageResponse,
  UnreadCountResponse,
} from '../types/notification';

export const notificationApi = {
  getNotifications: async (page = 0, size = 10): Promise<NotificationPageResponse> => {
    const response = await apiClient.get<NotificationPageResponse>('/api/v1/notifications', {
      params: { page, size },
    });
    return response.data;
  },

  getNotification: async (id: string): Promise<NotificationItem> => {
    const response = await apiClient.get<NotificationItem>(`/api/v1/notifications/${id}`);
    return response.data;
  },

  markAsRead: async (id: string): Promise<NotificationItem> => {
    const response = await apiClient.patch<NotificationItem>(`/api/v1/notifications/${id}/read`);
    return response.data;
  },

  markAllAsRead: async (): Promise<BulkReadResponse> => {
    const response = await apiClient.patch<BulkReadResponse>('/api/v1/notifications/read-all');
    return response.data;
  },

  getUnreadCount: async (): Promise<UnreadCountResponse> => {
    const response = await apiClient.get<UnreadCountResponse>('/api/v1/notifications/unread-count');
    return response.data;
  },

  deleteNotification: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/v1/notifications/${id}`);
  },
};

