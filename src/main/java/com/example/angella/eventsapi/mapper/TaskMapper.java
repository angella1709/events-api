package com.example.angella.eventsapi.mapper;

import com.example.angella.eventsapi.entity.Task;
import com.example.angella.eventsapi.web.dto.TaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TaskMapper {
    @Mapping(target = "creator", source = "creator.username")
    TaskDto toDto(Task task);
}