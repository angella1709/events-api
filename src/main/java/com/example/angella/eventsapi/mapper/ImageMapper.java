package com.example.angella.eventsapi.mapper;

import com.example.angella.eventsapi.entity.Image;
import com.example.angella.eventsapi.web.dto.ImageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ImageMapper {

    @Mapping(target = "url", expression = "java(\"/images/\" + image.getFilename())")
    ImageDto toDto(Image image);
}