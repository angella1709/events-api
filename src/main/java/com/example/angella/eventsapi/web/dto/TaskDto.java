package com.example.angella.eventsapi.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private Long id;
    private String description;
    private boolean completed;
    private String creator;
    private Instant createdAt;
}