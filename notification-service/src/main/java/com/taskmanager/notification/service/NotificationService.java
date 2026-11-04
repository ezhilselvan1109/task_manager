package com.taskmanager.notification.service;

import com.taskmanager.common.event.TaskEventEnvelope;
import com.taskmanager.notification.dto.event.TaskEventPayload;
import com.taskmanager.notification.dto.response.BulkReadResponse;
import com.taskmanager.notification.dto.response.NotificationPageResponse;
import com.taskmanager.notification.dto.response.NotificationResponse;
import com.taskmanager.notification.dto.response.UnreadCountResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service contract for in-app notification processing, queries, and lifecycle management.
 */
public interface NotificationService {

    void processTaskEvent(TaskEventEnvelope<TaskEventPayload> envelope);

    NotificationPageResponse listNotifications(UUID userId, Pageable pageable);

    NotificationResponse getNotification(UUID id, UUID userId);

    NotificationResponse markAsRead(UUID id, UUID userId);

    BulkReadResponse markAllAsRead(UUID userId);

    UnreadCountResponse getUnreadCount(UUID userId);

    void deleteNotification(UUID id, UUID userId);
}

