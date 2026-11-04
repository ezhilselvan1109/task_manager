package com.taskmanager.notification.service;

import com.taskmanager.common.event.TaskEventEnvelope;
import com.taskmanager.common.event.TaskEventType;
import com.taskmanager.notification.dto.event.TaskEventPayload;
import com.taskmanager.notification.dto.response.BulkReadResponse;
import com.taskmanager.notification.dto.response.NotificationPageResponse;
import com.taskmanager.notification.dto.response.NotificationResponse;
import com.taskmanager.notification.dto.response.UnreadCountResponse;
import com.taskmanager.notification.entity.ConsumedEventEntity;
import com.taskmanager.notification.entity.NotificationEntity;
import com.taskmanager.notification.exception.ResourceNotFoundException;
import com.taskmanager.notification.mapper.NotificationMapper;
import com.taskmanager.notification.repository.ConsumedEventRepository;
import com.taskmanager.notification.repository.NotificationRepository;
import com.taskmanager.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ConsumedEventRepository consumedEventRepository;

    @Mock
    private NotificationMapper notificationMapper;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationRepository,
                consumedEventRepository,
                notificationMapper,
                "notification-service-group"
        );
    }

    @Test
    @DisplayName("Should process TASK_CREATED event and create in-app notification idempotently")
    void testProcessTaskCreatedEvent() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        TaskEventPayload payload = new TaskEventPayload(
                taskId, userId, "Refactor Core Architecture", "Implement Kafka", "TODO", "HIGH", Instant.now().plusSeconds(3600)
        );

        TaskEventEnvelope<TaskEventPayload> envelope = new TaskEventEnvelope<>(
                eventId, TaskEventType.TASK_CREATED.name(), "1.0", taskId, userId, "task-service", Instant.now(), "corr-1", payload
        );

        when(consumedEventRepository.existsById(eventId)).thenReturn(false);

        notificationService.processTaskEvent(envelope);

        // Verify notification saved
        ArgumentCaptor<NotificationEntity> notifCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(notifCaptor.capture());
        NotificationEntity savedNotif = notifCaptor.getValue();
        assertThat(savedNotif.getUserId()).isEqualTo(userId);
        assertThat(savedNotif.getTaskId()).isEqualTo(taskId);
        assertThat(savedNotif.getEventType()).isEqualTo("TASK_CREATED");
        assertThat(savedNotif.getTitle()).isEqualTo("Task Created");
        assertThat(savedNotif.getMessage()).isEqualTo("Task 'Refactor Core Architecture' was created");
        assertThat(savedNotif.isRead()).isFalse();

        // Verify consumed event saved in same transaction
        ArgumentCaptor<ConsumedEventEntity> consumedCaptor = ArgumentCaptor.forClass(ConsumedEventEntity.class);
        verify(consumedEventRepository).save(consumedCaptor.capture());
        ConsumedEventEntity savedConsumed = consumedCaptor.getValue();
        assertThat(savedConsumed.getEventId()).isEqualTo(eventId);
        assertThat(savedConsumed.getEventType()).isEqualTo("TASK_CREATED");
        assertThat(savedConsumed.getConsumerGroup()).isEqualTo("notification-service-group");
    }

    @Test
    @DisplayName("Should process TASK_UPDATED event and format message correctly")
    void testProcessTaskUpdatedEvent() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        TaskEventPayload payload = new TaskEventPayload(
                taskId, userId, "API Design", "Updated", "IN_PROGRESS", "MEDIUM", null
        );

        TaskEventEnvelope<TaskEventPayload> envelope = new TaskEventEnvelope<>(
                eventId, TaskEventType.TASK_UPDATED.name(), "1.0", taskId, userId, "task-service", Instant.now(), "corr-2", payload
        );

        when(consumedEventRepository.existsById(eventId)).thenReturn(false);

        notificationService.processTaskEvent(envelope);

        ArgumentCaptor<NotificationEntity> notifCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(notifCaptor.capture());
        assertThat(notifCaptor.getValue().getTitle()).isEqualTo("Task Updated");
        assertThat(notifCaptor.getValue().getMessage()).isEqualTo("Task 'API Design' was updated");
    }

    @Test
    @DisplayName("Should process TASK_COMPLETED event and format celebratory message")
    void testProcessTaskCompletedEvent() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        TaskEventPayload payload = new TaskEventPayload(
                taskId, userId, "Deploy to Staging", "Done", "COMPLETED", "URGENT", null
        );

        TaskEventEnvelope<TaskEventPayload> envelope = new TaskEventEnvelope<>(
                eventId, TaskEventType.TASK_COMPLETED.name(), "1.0", taskId, userId, "task-service", Instant.now(), "corr-3", payload
        );

        when(consumedEventRepository.existsById(eventId)).thenReturn(false);

        notificationService.processTaskEvent(envelope);

        ArgumentCaptor<NotificationEntity> notifCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(notifCaptor.capture());
        assertThat(notifCaptor.getValue().getTitle()).isEqualTo("Task Completed");
        assertThat(notifCaptor.getValue().getMessage()).isEqualTo("Task 'Deploy to Staging' was marked COMPLETED");
    }

    @Test
    @DisplayName("Should process TASK_DELETED event and format message")
    void testProcessTaskDeletedEvent() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        TaskEventPayload payload = new TaskEventPayload(
                taskId, userId, "Old Task", null, "CANCELLED", "LOW", null
        );

        TaskEventEnvelope<TaskEventPayload> envelope = new TaskEventEnvelope<>(
                eventId, TaskEventType.TASK_DELETED.name(), "1.0", taskId, userId, "task-service", Instant.now(), "corr-4", payload
        );

        when(consumedEventRepository.existsById(eventId)).thenReturn(false);

        notificationService.processTaskEvent(envelope);

        ArgumentCaptor<NotificationEntity> notifCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(notifCaptor.capture());
        assertThat(notifCaptor.getValue().getTitle()).isEqualTo("Task Deleted");
        assertThat(notifCaptor.getValue().getMessage()).isEqualTo("Task 'Old Task' was deleted");
    }

    @Test
    @DisplayName("Should safely skip duplicate event and omit saving new notification (Idempotency Gate)")
    void testProcessDuplicateEventSafelySkipped() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        TaskEventPayload payload = new TaskEventPayload(taskId, userId, "Dup Task", null, "TODO", "LOW", null);
        TaskEventEnvelope<TaskEventPayload> envelope = new TaskEventEnvelope<>(
                eventId, TaskEventType.TASK_CREATED.name(), "1.0", taskId, userId, "task-service", Instant.now(), "corr-dup", payload
        );

        when(consumedEventRepository.existsById(eventId)).thenReturn(true);

        notificationService.processTaskEvent(envelope);

        // Verification: Zero writes to repository
        verify(notificationRepository, never()).save(any());
        verify(consumedEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should ignore unrecognized event types without error")
    void testProcessUnrecognizedEventType() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        TaskEventPayload payload = new TaskEventPayload(taskId, userId, "Unknown", null, "TODO", "LOW", null);
        TaskEventEnvelope<TaskEventPayload> envelope = new TaskEventEnvelope<>(
                eventId, "UNKNOWN_EVENT_TYPE", "1.0", taskId, userId, "task-service", Instant.now(), "corr-unk", payload
        );

        when(consumedEventRepository.existsById(eventId)).thenReturn(false);

        notificationService.processTaskEvent(envelope);

        verify(notificationRepository, never()).save(any());
        verify(consumedEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should list paginated notifications and include unread count for user")
    void testListNotifications() {
        UUID userId = UUID.randomUUID();
        NotificationEntity n1 = NotificationEntity.builder()
                .id(UUID.randomUUID()).userId(userId).eventType("TASK_CREATED").title("T1").message("M1").read(false).createdAt(Instant.now()).build();

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(n1), PageRequest.of(0, 10), 1));
        when(notificationRepository.countByUserIdAndReadFalse(userId)).thenReturn(1L);

        NotificationResponse responseDto = new NotificationResponse(
                n1.getId(), userId, null, "TASK_CREATED", "T1", "M1", false, null, n1.getCreatedAt()
        );
        when(notificationMapper.toResponse(n1)).thenReturn(responseDto);

        NotificationPageResponse result = notificationService.listNotifications(userId, PageRequest.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.unreadCount()).isEqualTo(1L);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("T1");
    }

    @Test
    @DisplayName("Should retrieve notification for owner")
    void testGetNotificationSuccess() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        NotificationEntity entity = NotificationEntity.builder()
                .id(id).userId(userId).eventType("TASK_CREATED").title("Notice").message("Body").read(false).createdAt(Instant.now()).build();

        when(notificationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(entity));

        NotificationResponse expected = new NotificationResponse(id, userId, null, "TASK_CREATED", "Notice", "Body", false, null, entity.getCreatedAt());
        when(notificationMapper.toResponse(entity)).thenReturn(expected);

        NotificationResponse actual = notificationService.getNotification(id, userId);
        assertThat(actual.id()).isEqualTo(id);
        assertThat(actual.title()).isEqualTo("Notice");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException (404) on cross-tenant access in getNotification")
    void testGetNotificationCrossTenantNotFound() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(notificationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotification(id, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification not found with ID: " + id);
    }

    @Test
    @DisplayName("Should mark notification as read and save")
    void testMarkAsReadSuccess() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        NotificationEntity entity = NotificationEntity.builder()
                .id(id).userId(userId).eventType("TASK_CREATED").title("Notice").message("Body").read(false).createdAt(Instant.now()).build();

        when(notificationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(entity));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(entity);

        NotificationResponse expected = new NotificationResponse(id, userId, null, "TASK_CREATED", "Notice", "Body", true, Instant.now(), entity.getCreatedAt());
        when(notificationMapper.toResponse(entity)).thenReturn(expected);

        NotificationResponse result = notificationService.markAsRead(id, userId);
        assertThat(entity.isRead()).isTrue();
        assertThat(entity.getReadAt()).isNotNull();
        verify(notificationRepository).save(entity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException (404) on cross-tenant access in markAsRead")
    void testMarkAsReadCrossTenantNotFound() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(notificationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(id, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should bulk mark all unread notifications as read and return updated count")
    void testMarkAllAsRead() {
        UUID userId = UUID.randomUUID();

        when(notificationRepository.markAllAsReadForUser(eq(userId), any(Instant.class))).thenReturn(5);

        BulkReadResponse response = notificationService.markAllAsRead(userId);
        assertThat(response.updatedCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should return unread count for user")
    void testGetUnreadCount() {
        UUID userId = UUID.randomUUID();

        when(notificationRepository.countByUserIdAndReadFalse(userId)).thenReturn(3L);

        UnreadCountResponse response = notificationService.getUnreadCount(userId);
        assertThat(response.unreadCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should delete notification for owner")
    void testDeleteNotificationSuccess() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        NotificationEntity entity = NotificationEntity.builder()
                .id(id).userId(userId).eventType("TASK_CREATED").title("To Delete").message("Msg").read(false).createdAt(Instant.now()).build();

        when(notificationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(entity));

        notificationService.deleteNotification(id, userId);

        verify(notificationRepository).delete(entity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException (404) on cross-tenant access in deleteNotification")
    void testDeleteNotificationCrossTenantNotFound() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(notificationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.deleteNotification(id, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should handle event with null payload gracefully using aggregateId and fallback title")
    void testProcessEventWithNullPayload() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();

        TaskEventEnvelope<TaskEventPayload> envelope = new TaskEventEnvelope<>(
                eventId, "TASK_CREATED", "1.0", aggregateId, userId, "task-service", Instant.now(), "corr-null-p", null
        );

        when(consumedEventRepository.existsById(eventId)).thenReturn(false);

        notificationService.processTaskEvent(envelope);

        ArgumentCaptor<NotificationEntity> notifCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(notifCaptor.capture());
        assertThat(notifCaptor.getValue().getTaskId()).isEqualTo(aggregateId);
        assertThat(notifCaptor.getValue().getMessage()).isEqualTo("Task 'Task' was created");
    }

    @Test
    @DisplayName("Should fallback to Task title when title is blank or taskId is null in payload")
    void testProcessEventWithBlankTitleAndNullTaskId() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();

        TaskEventPayload payload = new TaskEventPayload(null, userId, "   ", null, "TODO", "LOW", null);
        TaskEventEnvelope<TaskEventPayload> envelope = new TaskEventEnvelope<>(
                eventId, "TASK_UPDATED", "1.0", aggregateId, userId, "task-service", Instant.now(), "corr-blank", payload
        );

        when(consumedEventRepository.existsById(eventId)).thenReturn(false);

        notificationService.processTaskEvent(envelope);

        ArgumentCaptor<NotificationEntity> notifCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(notifCaptor.capture());
        assertThat(notifCaptor.getValue().getTaskId()).isEqualTo(aggregateId);
        assertThat(notifCaptor.getValue().getMessage()).isEqualTo("Task 'Task' was updated");
    }
}

