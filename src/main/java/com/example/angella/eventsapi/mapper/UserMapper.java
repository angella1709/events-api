package com.example.angella.eventsapi.mapper;

import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.web.dto.CreateUserRequest;
import com.example.angella.eventsapi.web.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    User toEntity(CreateUserRequest request);

    UserDto toDto(User user);
}
