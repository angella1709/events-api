package com.example.angella.eventsapi.web.dto;

import com.example.angella.eventsapi.entity.TemplateCategory;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChecklistTemplateRequest {
    private String name;
    private String description;
    private TemplateCategory category;
    private List<TemplateItemRequest> items = new ArrayList<>();
}