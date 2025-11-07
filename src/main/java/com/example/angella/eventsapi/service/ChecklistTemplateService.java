package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.entity.ChecklistItem;
import com.example.angella.eventsapi.entity.ChecklistTemplate;
import com.example.angella.eventsapi.entity.Event;
import com.example.angella.eventsapi.entity.TemplateItem;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.repository.ChecklistTemplateRepository;
import com.example.angella.eventsapi.repository.EventRepository;
import com.example.angella.eventsapi.repository.TemplateItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChecklistTemplateService {

    private final ChecklistTemplateRepository templateRepository;
    private final TemplateItemRepository templateItemRepository;
    private final EventRepository eventRepository;
    private final ChecklistService checklistService;

    @PreAuthorize("hasRole('ADMIN')")
    public List<ChecklistTemplate> getAllTemplates() {
        try {
            return templateRepository.findAll();
        } catch (Exception e) {
            log.error("Error loading all templates", e);
            return List.of();
        }
    }

    public List<ChecklistTemplate> getTemplatesByCategory(String category) {
        return templateRepository.findByCategory(
                com.example.angella.eventsapi.entity.TemplateCategory.valueOf(category.toUpperCase())
        );
    }

    public List<ChecklistItem> applyTemplateToEvent(Long templateId, Long eventId, Long userId) {
        ChecklistTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        List<TemplateItem> templateItems = templateItemRepository.findByTemplateId(templateId);

        return templateItems.stream()
                .map(templateItem -> checklistService.createItem(
                        templateItem.getName(),
                        templateItem.getDescription(),
                        templateItem.getDefaultQuantity(),
                        eventId,
                        userId,
                        null // assignedUserId - можно назначить позже
                ))
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ChecklistTemplate getTemplateById(Long id) {
        ChecklistTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template not found"));

        // БЕЗОПАСНАЯ инициализация ленивой коллекции
        if (template.getItems() != null) {
            template.getItems().size(); // Это инициализирует коллекцию
        }

        return template;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ChecklistTemplate updateTemplate(Long id, ChecklistTemplate updatedTemplate) {
        ChecklistTemplate template = getTemplateById(id);
        template.setName(updatedTemplate.getName());
        template.setDescription(updatedTemplate.getDescription());
        template.setCategory(updatedTemplate.getCategory());

        // Очищаем старые элементы и добавляем новые
        if (template.getItems() != null) {
            template.getItems().clear();
        }

        if (updatedTemplate.getItems() != null) {
            for (TemplateItem item : updatedTemplate.getItems()) {
                item.setTemplate(template);
                template.getItems().add(item);
            }
        }

        return templateRepository.save(template);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ChecklistTemplate createTemplate(ChecklistTemplate template) {
        return templateRepository.save(template);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTemplate(Long templateId) {
        templateItemRepository.deleteByTemplateId(templateId);
        templateRepository.deleteById(templateId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ChecklistTemplate> getAllTemplatesWithItems() {
        try {
            List<ChecklistTemplate> templates = templateRepository.findAll();

            // БЕЗОПАСНАЯ инициализация ленивых коллекций
            templates.forEach(template -> {
                try {
                    if (template.getItems() != null) {
                        template.getItems().size(); // Это инициализирует коллекцию
                    }
                } catch (Exception e) {
                    log.warn("Failed to initialize items for template {}: {}", template.getId(), e.getMessage());
                }
            });

            return templates;
        } catch (Exception e) {
            log.error("Error loading templates with items", e);
            return List.of(); // Возвращаем пустой список при ошибке
        }
    }
}