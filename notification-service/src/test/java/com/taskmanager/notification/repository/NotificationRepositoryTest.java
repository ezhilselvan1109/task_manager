package com.taskmanager.notification.repository;

import com.taskmanager.notification.entity.NotificationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("Should save and find notifications by userId ordered by createdAt desc")
    void testFindByUserIdOrderByCreatedAtDesc() {
        UUID userId = UUID.randomUUID();

        NotificationEntity n1 = NotificationEntity.builder()
                .userId(userId)
                .eventType("TASK_CREATED")
                .title("Task 1")
                .message("Task 1 created")
                .read(false)
                .createdAt(Instant.now().minusSeconds(60))
                .build();

        NotificationEntity n2 = NotificationEntity.builder()
                .userId(userId)
                .eventType("TASK_UPDATED")
                .title("Task 2")
                .message("Task 2 updated")
                .read(false)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(n1);
        notificationRepository.save(n2);

        Page<NotificationEntity> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Task 2");
        assertThat(page.getContent().get(1).getTitle()).isEqualTo("Task 1");
    }

    @Test
    @DisplayName("Should find notification by id and userId for tenant isolation")
    void testFindByIdAndUserId() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        NotificationEntity notification = NotificationEntity.builder()
                .userId(userId)
                .eventType("TASK_CREATED")
                .title("My Notification")
                .message("Details")
                .read(false)
                .createdAt(Instant.now())
                .build();

        NotificationEntity saved = notificationRepository.save(notification);

        // Owner can find it
        Optional<NotificationEntity> found = notificationRepository.findByIdAndUserId(saved.getId(), userId);
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("My Notification");

        // Non-owner cannot find it (Anti-enumeration isolation)
        Optional<NotificationEntity> notFound = notificationRepository.findByIdAndUserId(saved.getId(), otherUserId);
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("Should count unread notifications for a user")
    void testCountByUserIdAndReadFalse() {
        UUID userId = UUID.randomUUID();

        NotificationEntity unread1 = NotificationEntity.builder()
                .userId(userId).eventType("TASK_CREATED").title("T1").message("M1").read(false).createdAt(Instant.now()).build();
        NotificationEntity unread2 = NotificationEntity.builder()
                .userId(userId).eventType("TASK_UPDATED").title("T2").message("M2").read(false).createdAt(Instant.now()).build();
        NotificationEntity read = NotificationEntity.builder()
                .userId(userId).eventType("TASK_COMPLETED").title("T3").message("M3").read(true).readAt(Instant.now()).createdAt(Instant.now()).build();

        notificationRepository.save(unread1);
        notificationRepository.save(unread2);
        notificationRepository.save(read);

        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should bulk mark all unread notifications as read for a user")
    void testMarkAllAsReadForUser() {
        UUID userId = UUID.randomUUID();

        NotificationEntity n1 = NotificationEntity.builder()
                .userId(userId).eventType("TASK_CREATED").title("T1").message("M1").read(false).createdAt(Instant.now()).build();
        NotificationEntity n2 = NotificationEntity.builder()
                .userId(userId).eventType("TASK_UPDATED").title("T2").message("M2").read(false).createdAt(Instant.now()).build();

        notificationRepository.save(n1);
        notificationRepository.save(n2);

        Instant readAt = Instant.now();
        int updatedCount = notificationRepository.markAllAsReadForUser(userId, readAt);

        assertThat(updatedCount).isEqualTo(2);

        long unreadAfter = notificationRepository.countByUserIdAndReadFalse(userId);
        assertThat(unreadAfter).isEqualTo(0);
    }
}

