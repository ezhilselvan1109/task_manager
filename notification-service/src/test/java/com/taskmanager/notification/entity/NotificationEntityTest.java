package com.taskmanager.notification.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEntityTest {

    @Test
    @DisplayName("Should create NotificationEntity with builder and verify fields")
    void testNotificationEntityCreation() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Instant now = Instant.now();

        NotificationEntity notification = NotificationEntity.builder()
                .id(id)
                .userId(userId)
                .taskId(taskId)
                .eventType("TASK_CREATED")
                .title("Task Created")
                .message("Task 'Build Notification Service' was created")
                .read(false)
                .createdAt(now)
                .build();

        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getTaskId()).isEqualTo(taskId);
        assertThat(notification.getEventType()).isEqualTo("TASK_CREATED");
        assertThat(notification.getTitle()).isEqualTo("Task Created");
        assertThat(notification.getMessage()).isEqualTo("Task 'Build Notification Service' was created");
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getReadAt()).isNull();
        assertThat(notification.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should mark notification as read and set readAt timestamp")
    void testMarkAsRead() {
        NotificationEntity notification = NotificationEntity.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .eventType("TASK_COMPLETED")
                .title("Task Completed")
                .message("Task completed")
                .read(false)
                .build();

        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getReadAt()).isNull();

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
    }
}

