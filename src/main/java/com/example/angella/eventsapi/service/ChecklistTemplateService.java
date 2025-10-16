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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChecklistTemplateService {

    private final ChecklistTemplateRepository templateRepository;
    private final TemplateItemRepository templateItemRepository;
    private final EventRepository eventRepository;
    private final ChecklistService checklistService;

    public List<ChecklistTemplate> getAllTemplates() {
        return templateRepository.findAll();
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

        if (!eventRepository.existsByIdAndParticipantsId(eventId, userId)) {
            throw new SecurityException("Only event participants can apply templates");
        }

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

    public ChecklistTemplate createTemplate(ChecklistTemplate template) {
        return templateRepository.save(template);
    }

    public void deleteTemplate(Long templateId) {
        templateItemRepository.deleteByTemplateId(templateId);
        templateRepository.deleteById(templateId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ChecklistTemplate getTemplateById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template not found"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ChecklistTemplate updateTemplate(Long id, ChecklistTemplate updatedTemplate) {
        ChecklistTemplate template = getTemplateById(id);
        template.setName(updatedTemplate.getName());
        template.setDescription(updatedTemplate.getDescription());
        template.setCategory(updatedTemplate.getCategory());
        return templateRepository.save(template);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ChecklistTemplate createTemplate(ChecklistTemplate template) {
        return templateRepository.save(template);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTemplate(Long templateId) {
        templateRepository.deleteById(templateId);
    }
}