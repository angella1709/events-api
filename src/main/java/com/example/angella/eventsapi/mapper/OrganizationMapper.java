package com.example.angella.eventsapi.mapper;

import com.example.angella.eventsapi.entity.Organization;
import com.example.angella.eventsapi.web.dto.CreateOrganizationRequest;
import com.example.angella.eventsapi.web.dto.OrganizationDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrganizationMapper {

    Organization toEntity(CreateOrganizationRequest request);

    OrganizationDto toDto(Organization organization);

}
