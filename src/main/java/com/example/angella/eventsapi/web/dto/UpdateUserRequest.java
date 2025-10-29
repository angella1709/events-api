package com.example.angella.eventsapi.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String firstName;

    private String lastName;
}