package com.taskmanager.notification.mapper;

import com.taskmanager.notification.dto.response.NotificationResponse;
import com.taskmanager.notification.entity.NotificationEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper transforming NotificationEntity into REST API response DTOs.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(NotificationEntity entity);
}

