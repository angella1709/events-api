package com.example.angella.eventsapi.mapper;

import com.example.angella.eventsapi.entity.ChatMessage;
import com.example.angella.eventsapi.web.dto.ChatMessageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ChatMessageMapper {
    @Mapping(target = "author", source = "author.username")
    ChatMessageDto toDto(ChatMessage chatMessage);
}