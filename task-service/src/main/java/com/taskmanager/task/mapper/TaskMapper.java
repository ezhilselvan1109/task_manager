package com.taskmanager.task.mapper;

import com.taskmanager.task.dto.response.TaskResponse;
import com.taskmanager.task.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Compile-time MapStruct bean mapper converting TaskEntity to TaskResponse DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

    @Mapping(target = "isOverdue", expression = "java(task.isOverdue())")
    TaskResponse toResponse(TaskEntity task);
}

