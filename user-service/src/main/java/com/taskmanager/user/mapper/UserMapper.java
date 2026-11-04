package com.taskmanager.user.mapper;

import com.taskmanager.user.dto.response.UserProfileResponse;
import com.taskmanager.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Compile-time MapStruct bean mapper converting User entities to sanitized response DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserProfileResponse toResponse(UserEntity user);
}

