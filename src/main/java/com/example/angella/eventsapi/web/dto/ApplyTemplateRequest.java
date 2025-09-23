package com.example.angella.eventsapi.web.dto;

import lombok.Data;

@Data
public class ApplyTemplateRequest {
    private Long templateId;
    private Long eventId;
}