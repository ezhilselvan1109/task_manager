export interface NotificationItem {
  id: string;
  userId: string;
  taskId?: string;
  eventType: string;
  title: string;
  message: string;
  read: boolean;
  readAt?: string;
  createdAt: string;
}

export interface NotificationPageResponse {
  content: NotificationItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  unreadCount: number;
}

export interface BulkReadResponse {
  updatedCount: number;
}

export interface UnreadCountResponse {
  unreadCount: number;
}

