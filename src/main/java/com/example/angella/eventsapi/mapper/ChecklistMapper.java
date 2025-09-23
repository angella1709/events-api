package com.example.angella.eventsapi.mapper;

import com.example.angella.eventsapi.entity.ChecklistItem;
import com.example.angella.eventsapi.web.dto.ChecklistItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ChecklistMapper {

    @Mapping(target = "createdBy", source = "createdBy.username")
    @Mapping(target = "assignedUser", source = "assignedUser.username")
    ChecklistItemDto toDto(ChecklistItem checklistItem);

    List<ChecklistItemDto> toDtoList(List<ChecklistItem> checklistItems);
}