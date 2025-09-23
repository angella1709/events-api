package com.example.angella.eventsapi.mapper;

import com.example.angella.eventsapi.entity.ChecklistTemplate;
import com.example.angella.eventsapi.web.dto.ChecklistTemplateDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ChecklistTemplateMapper {

    ChecklistTemplateDto toDto(ChecklistTemplate template);

    List<ChecklistTemplateDto> toDtoList(List<ChecklistTemplate> templates);
}