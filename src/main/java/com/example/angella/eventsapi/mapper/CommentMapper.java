package com.example.angella.eventsapi.mapper;

import com.example.angella.eventsapi.entity.Comment;
import com.example.angella.eventsapi.web.dto.CommentDto;
import com.example.angella.eventsapi.web.dto.CreateCommentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CommentMapper {

    Comment toEntity(CreateCommentRequest request);

    @Mapping(target = "author", source = "user.username")
    CommentDto toDto(Comment comment);

    List<CommentDto> toDtoList(List<Comment> comments);

}
