package com.example.angella.eventsapi.mapper;

import com.example.angella.eventsapi.entity.ChatMessage;
import com.example.angella.eventsapi.web.dto.ChatMessageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.stream.Collectors;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ImageMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ChatMessageMapper {

    @Mapping(target = "author", source = "author.username")
    @Mapping(target = "images", source = "images")
    ChatMessageDto toDto(ChatMessage chatMessage);
}