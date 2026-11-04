package com.taskmanager.notification.service.impl;

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
import com.taskmanager.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Production implementation of NotificationService.
 * Enforces consumer idempotency (RULE-07), anti-enumeration isolation (RULE-05), and local ACID transactions.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final ConsumedEventRepository consumedEventRepository;
    private final NotificationMapper notificationMapper;
    private final String consumerGroup;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            ConsumedEventRepository consumedEventRepository,
            NotificationMapper notificationMapper,
            @Value("${spring.kafka.consumer.group-id:notification-service-group}") String consumerGroup) {
        this.notificationRepository = notificationRepository;
        this.consumedEventRepository = consumedEventRepository;
        this.notificationMapper = notificationMapper;
        this.consumerGroup = consumerGroup;
    }

    @Override
    @Transactional
    public void processTaskEvent(TaskEventEnvelope<TaskEventPayload> envelope) {
        UUID eventId = envelope.eventId();

        // 1. Idempotency Gate: check if event was already consumed
        if (consumedEventRepository.existsById(eventId)) {
            log.info("Duplicate event {} detected. Safely skipping notification generation.", eventId);
            return;
        }

        String eventType = envelope.eventType();
        String taskTitle = (envelope.payload() != null && envelope.payload().title() != null && !envelope.payload().title().isBlank())
                ? envelope.payload().title().trim()
                : "Task";

        String title;
        String message;

        if (TaskEventType.TASK_CREATED.name().equals(eventType)) {
            title = "Task Created";
            message = "Task '" + taskTitle + "' was created";
        } else if (TaskEventType.TASK_UPDATED.name().equals(eventType)) {
            title = "Task Updated";
            message = "Task '" + taskTitle + "' was updated";
        } else if (TaskEventType.TASK_COMPLETED.name().equals(eventType)) {
            title = "Task Completed";
            message = "Task '" + taskTitle + "' was marked COMPLETED";
        } else if (TaskEventType.TASK_DELETED.name().equals(eventType)) {
            title = "Task Deleted";
            message = "Task '" + taskTitle + "' was deleted";
        } else {
            log.warn("Unrecognized domain event type: {}. Discarding event safely without side effects.", eventType);
            return;
        }

        UUID taskId = envelope.payload() != null && envelope.payload().id() != null
                ? envelope.payload().id()
                : envelope.aggregateId();

        // 2. Persist Notification Entity
        NotificationEntity notification = NotificationEntity.builder()
                .userId(envelope.userId())
                .taskId(taskId)
                .eventType(eventType)
                .title(title)
                .message(message)
                .read(false)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);

        // 3. Persist ConsumedEventEntity atomically in the same local ACID transaction
        ConsumedEventEntity consumedEvent = new ConsumedEventEntity(
                eventId,
                eventType,
                consumerGroup,
                Instant.now()
        );
        consumedEventRepository.save(consumedEvent);

        log.info("Successfully ingested event {} as notification {} for user {}", eventId, notification.getId(), envelope.userId());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse listNotifications(UUID userId, Pageable pageable) {
        Page<NotificationEntity> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<NotificationResponse> content = page.getContent().stream()
                .map(notificationMapper::toResponse)
                .toList();

        long unreadCount = notificationRepository.countByUserIdAndReadFalse(userId);

        return new NotificationPageResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                unreadCount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(UUID id, UUID userId) {
        NotificationEntity entity = findNotificationForOwner(id, userId);
        return notificationMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID id, UUID userId) {
        NotificationEntity entity = findNotificationForOwner(id, userId);
        entity.markAsRead();
        NotificationEntity saved = notificationRepository.save(entity);
        log.info("Notification {} marked as read for user {}", id, userId);
        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BulkReadResponse markAllAsRead(UUID userId) {
        int updatedCount = notificationRepository.markAllAsReadForUser(userId, Instant.now());
        log.info("Bulk marked {} notifications as read for user {}", updatedCount, userId);
        return new BulkReadResponse(updatedCount);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID userId) {
        long unreadCount = notificationRepository.countByUserIdAndReadFalse(userId);
        return new UnreadCountResponse(unreadCount);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id, UUID userId) {
        NotificationEntity entity = findNotificationForOwner(id, userId);
        notificationRepository.delete(entity);
        log.info("Notification {} deleted for user {}", id, userId);
    }

    private NotificationEntity findNotificationForOwner(UUID id, UUID userId) {
        return notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
    }
}

