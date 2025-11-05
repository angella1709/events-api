package com.example.angella.eventsapi.web.dto;

import lombok.Data;

@Data
public class TemplateItemRequest {
    private String name;
    private String description;
    private Integer defaultQuantity = 1;
}