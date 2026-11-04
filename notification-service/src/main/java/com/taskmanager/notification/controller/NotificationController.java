package com.taskmanager.notification.controller;

import com.taskmanager.common.constants.SecurityConstants;
import com.taskmanager.notification.dto.response.BulkReadResponse;
import com.taskmanager.notification.dto.response.NotificationPageResponse;
import com.taskmanager.notification.dto.response.NotificationResponse;
import com.taskmanager.notification.dto.response.UnreadCountResponse;
import com.taskmanager.notification.service.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * REST Controller exposing user notification management endpoints.
 * Enforces anti-enumeration security and tenant isolation via trusted X-User-Id (RULE-05).
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<NotificationPageResponse> listNotifications(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sortParam,
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader) {

        UUID userId = extractUserId(userIdHeader);
        Pageable pageable = parsePageable(page, size, sortParam);
        NotificationPageResponse response = notificationService.listNotifications(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(
            @PathVariable("id") UUID id,
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader) {

        UUID userId = extractUserId(userIdHeader);
        NotificationResponse response = notificationService.getNotification(id, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable("id") UUID id,
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader) {

        UUID userId = extractUserId(userIdHeader);
        NotificationResponse response = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<BulkReadResponse> markAllAsRead(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader) {

        UUID userId = extractUserId(userIdHeader);
        BulkReadResponse response = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader) {

        UUID userId = extractUserId(userIdHeader);
        UnreadCountResponse response = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable("id") UUID id,
            @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = false) String userIdHeader) {

        UUID userId = extractUserId(userIdHeader);
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing trusted user identity header");
        }
        try {
            return UUID.fromString(userIdHeader.trim());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user identity header format");
        }
    }

    private Pageable parsePageable(int page, int size, String sortParam) {
        int boundedSize = Math.max(1, Math.min(size, 100));
        int boundedPage = Math.max(0, page);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sortParam != null && !sortParam.isBlank()) {
            String[] parts = sortParam.split(",");
            String field = parts[0].trim();
            Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            if ("createdAt".equals(field) || "read".equals(field)) {
                sort = Sort.by(direction, field);
            }
        }

        return PageRequest.of(boundedPage, boundedSize, sort);
    }
}

