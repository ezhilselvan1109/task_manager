package com.taskmanager.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.common.constants.SecurityConstants;
import com.taskmanager.notification.dto.response.BulkReadResponse;
import com.taskmanager.notification.dto.response.NotificationPageResponse;
import com.taskmanager.notification.dto.response.NotificationResponse;
import com.taskmanager.notification.dto.response.UnreadCountResponse;
import com.taskmanager.notification.exception.GlobalExceptionHandler;
import com.taskmanager.notification.exception.ResourceNotFoundException;
import com.taskmanager.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("GET /api/v1/notifications - Should return 200 OK with paginated notifications")
    void testListNotificationsSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();

        NotificationResponse item = new NotificationResponse(
                notifId, userId, UUID.randomUUID(), "TASK_CREATED", "Task Created", "Task 'Test' was created", false, null, Instant.now()
        );
        NotificationPageResponse pageResponse = new NotificationPageResponse(
                List.of(item), 0, 10, 1L, 1, 1L
        );

        when(notificationService.listNotifications(eq(userId), any(PageRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/notifications")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.unreadCount").value(1))
                .andExpect(jsonPath("$.content[0].id").value(notifId.toString()))
                .andExpect(jsonPath("$.content[0].eventType").value("TASK_CREATED"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications - Should return 401 when X-User-Id header is missing")
    void testListNotificationsMissingUserId() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications - Should return 401 when X-User-Id is invalid UUID")
    void testListNotificationsInvalidUserId() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .header(SecurityConstants.HEADER_USER_ID, "not-a-uuid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications - Should return 401 when X-User-Id is blank")
    void testListNotificationsBlankUserId() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .header(SecurityConstants.HEADER_USER_ID, "   "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications - Should handle custom sorting asc, read field, unknown field, and bounded pagination")
    void testListNotificationsCustomSortAndBounds() throws Exception {
        UUID userId = UUID.randomUUID();
        NotificationPageResponse emptyPage = new NotificationPageResponse(List.of(), 0, 100, 0, 0, 0);

        when(notificationService.listNotifications(eq(userId), any(PageRequest.class))).thenReturn(emptyPage);

        // Test sort=createdAt,asc with negative page and oversized size
        mockMvc.perform(get("/api/v1/notifications")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .param("sort", "createdAt,asc")
                        .param("page", "-1")
                        .param("size", "500"))
                .andExpect(status().isOk());

        // Test sort=read,desc with undersized size
        mockMvc.perform(get("/api/v1/notifications")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .param("sort", "read,desc")
                        .param("size", "-2"))
                .andExpect(status().isOk());

        // Test sort with no comma
        mockMvc.perform(get("/api/v1/notifications")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .param("sort", "read"))
                .andExpect(status().isOk());

        // Test sort with unknown field (falls back to default sort)
        mockMvc.perform(get("/api/v1/notifications")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString())
                        .param("sort", "unknownField,asc"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{id} - Should return 200 OK for owner")
    void testGetNotificationSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();

        NotificationResponse response = new NotificationResponse(
                notifId, userId, null, "TASK_COMPLETED", "Task Done", "Msg", true, Instant.now(), Instant.now()
        );

        when(notificationService.getNotification(notifId, userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/notifications/{id}", notifId)
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notifId.toString()))
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/{id} - Should return 404 on cross-user access (Anti-Enumeration)")
    void testGetNotificationCrossUserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();

        when(notificationService.getNotification(notifId, userId))
                .thenThrow(new ResourceNotFoundException("Notification not found with ID: " + notifId));

        mockMvc.perform(get("/api/v1/notifications/{id}", notifId)
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ERR_RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/{id}/read - Should return 200 OK")
    void testMarkAsReadSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();

        NotificationResponse response = new NotificationResponse(
                notifId, userId, null, "TASK_UPDATED", "Title", "Msg", true, Instant.now(), Instant.now()
        );

        when(notificationService.markAsRead(notifId, userId)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notifId)
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/notifications/read-all - Should return 200 OK with updated count")
    void testMarkAllAsReadSuccess() throws Exception {
        UUID userId = UUID.randomUUID();

        when(notificationService.markAllAsRead(userId)).thenReturn(new BulkReadResponse(4));

        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/unread-count - Should return 200 OK with unread badge count")
    void testGetUnreadCountSuccess() throws Exception {
        UUID userId = UUID.randomUUID();

        when(notificationService.getUnreadCount(userId)).thenReturn(new UnreadCountResponse(7L));

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(7));
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{id} - Should return 204 No Content for owner")
    void testDeleteNotificationSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();

        doNothing().when(notificationService).deleteNotification(notifId, userId);

        mockMvc.perform(delete("/api/v1/notifications/{id}", notifId)
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{id} - Should return 404 for cross-user delete")
    void testDeleteNotificationCrossUserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();

        doThrow(new ResourceNotFoundException("Notification not found with ID: " + notifId))
                .when(notificationService).deleteNotification(notifId, userId);

        mockMvc.perform(delete("/api/v1/notifications/{id}", notifId)
                        .header(SecurityConstants.HEADER_USER_ID, userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ERR_RESOURCE_NOT_FOUND"));
    }
}

