package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.entity.*;
import com.example.angella.eventsapi.service.ChecklistTemplateService;
import com.example.angella.eventsapi.service.EventService;
import com.example.angella.eventsapi.service.UserService;
import com.example.angella.eventsapi.web.dto.ChecklistTemplateRequest;
import com.example.angella.eventsapi.web.dto.TemplateItemRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;
    private final EventService eventService;
    private final ChecklistTemplateService templateService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> users = userService.findAllUsers();
        List<Event> events = eventService.findAll();
        List<Event> upcomingEvents = eventService.findUpcomingEvents();
        List<ChecklistTemplate> templates = templateService.getAllTemplates();

        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalEvents", events.size());
        model.addAttribute("upcomingEvents", upcomingEvents.size());
        model.addAttribute("recentUsers", users.size() > 5 ? users.subList(0, 5) : users);
        model.addAttribute("templates", templates);
        model.addAttribute("templatesCount", templates.size());

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("success", "Пользователь успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении пользователя: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/toggle-admin")
    public String toggleAdminRole(@PathVariable Long userId,
                                  @RequestParam boolean makeAdmin,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (makeAdmin) {
                userService.addAdminRole(userId);
                redirectAttributes.addFlashAttribute("success", "Права администратора добавлены");
            } else {
                userService.removeAdminRole(userId);
                redirectAttributes.addFlashAttribute("success", "Права администратора удалены");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при изменении прав: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/events")
    public String eventManagement(Model model) {
        try {
            // Получаем ВСЕ мероприятия
            List<Event> allEvents = eventService.findAll();

            // УПРОЩЕННАЯ СТАТИСТИКА
            long totalParticipants = allEvents.stream()
                    .mapToLong(event -> event.getParticipants() != null ? event.getParticipants().size() : 0)
                    .sum();

            Instant now = Instant.now();
            long activeEvents = allEvents.stream()
                    .filter(event -> {
                        if (event.getStartTime() == null || event.getEndTime() == null) return false;
                        return event.getStartTime().isBefore(now) && event.getEndTime().isAfter(now);
                    })
                    .count();

            long upcomingEvents = allEvents.stream()
                    .filter(event -> event.getStartTime() != null && event.getStartTime().isAfter(now))
                    .count();

            long completedEvents = allEvents.stream()
                    .filter(event -> event.getEndTime() != null && event.getEndTime().isBefore(now))
                    .count();

            model.addAttribute("events", allEvents);
            model.addAttribute("totalEvents", allEvents.size());
            model.addAttribute("totalParticipants", totalParticipants);
            model.addAttribute("activeEvents", activeEvents);
            model.addAttribute("upcomingEvents", upcomingEvents);
            model.addAttribute("completedEvents", completedEvents);

            return "admin/events";
        } catch (Exception e) {
            log.error("Error in event management", e);
            model.addAttribute("error", "Ошибка загрузки мероприятий: " + e.getMessage());
            model.addAttribute("events", List.of()); // ДОБАВЛЕНО: пустой список при ошибке
            model.addAttribute("totalEvents", 0);
            model.addAttribute("totalParticipants", 0);
            model.addAttribute("activeEvents", 0);
            model.addAttribute("upcomingEvents", 0);
            model.addAttribute("completedEvents", 0);
            return "admin/events";
        }
    }

    @PostMapping("/events/{eventId}/delete")
    public String deleteEvent(@PathVariable Long eventId, RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEventByAdmin(eventId);
            redirectAttributes.addFlashAttribute("success", "Мероприятие успешно удалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении мероприятия: " + e.getMessage());
        }
        return "redirect:/admin/events";
    }

    @GetMapping("/templates")
    public String templateManagement(Model model) {
        try {
            List<ChecklistTemplate> templates = templateService.getAllTemplates();

            // УПРОЩЕННАЯ СТАТИСТИКА - убираем сложные вычисления
            long totalItems = templates.stream()
                    .mapToLong(t -> t.getItems() != null ? t.getItems().size() : 0)
                    .sum();

            long categoriesCount = templates.stream()
                    .map(ChecklistTemplate::getCategory)
                    .distinct()
                    .count();

            model.addAttribute("templates", templates);
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("categoriesCount", categoriesCount);

            return "admin/templates";
        } catch (Exception e) {
            log.error("Error in template management", e);
            model.addAttribute("error", "Ошибка загрузки шаблонов: " + e.getMessage());
            model.addAttribute("templates", List.of()); // ДОБАВЛЕНО: пустой список при ошибке
            model.addAttribute("totalItems", 0);
            model.addAttribute("categoriesCount", 0);
            return "admin/templates";
        }
    }

    @GetMapping("/templates/create")
    public String createTemplateForm(Model model) {
        try {
            model.addAttribute("templateRequest", new ChecklistTemplateRequest());
            model.addAttribute("categories", TemplateCategory.values());
            model.addAttribute("templateId", null);
            return "admin/template-form";
        } catch (Exception e) {
            log.error("Error loading template creation form", e);
            return "redirect:/admin/templates?error=load_failed";
        }
    }

    @PostMapping("/templates/create")
    public String createTemplate(@ModelAttribute ChecklistTemplateRequest templateRequest,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Преобразуем DTO в Entity
            ChecklistTemplate template = new ChecklistTemplate();
            template.setName(templateRequest.getName());
            template.setDescription(templateRequest.getDescription());
            template.setCategory(templateRequest.getCategory());

            // Сохраняем шаблон сначала без элементов
            ChecklistTemplate savedTemplate = templateService.createTemplate(template);

            // Добавляем элементы если они есть
            if (templateRequest.getItems() != null && !templateRequest.getItems().isEmpty()) {
                Set<TemplateItem> items = templateRequest.getItems().stream()
                        .map(itemRequest -> {
                            TemplateItem item = new TemplateItem();
                            item.setName(itemRequest.getName());
                            item.setDescription(itemRequest.getDescription());
                            item.setDefaultQuantity(itemRequest.getDefaultQuantity());
                            item.setTemplate(savedTemplate);
                            return item;
                        })
                        .collect(Collectors.toSet());

                savedTemplate.setItems(items);
                templateService.updateTemplate(savedTemplate.getId(), savedTemplate);
            }

            redirectAttributes.addFlashAttribute("success", "Шаблон успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании шаблона: " + e.getMessage());
        }
        return "redirect:/admin/templates";
    }

    @GetMapping("/templates/{id}/edit")
    public String editTemplateForm(@PathVariable Long id, Model model) {
        try {
            ChecklistTemplate template = templateService.getTemplateById(id);

            ChecklistTemplateRequest templateRequest = new ChecklistTemplateRequest();
            templateRequest.setName(template.getName());
            templateRequest.setDescription(template.getDescription());
            templateRequest.setCategory(template.getCategory());

            // БЕЗОПАСНАЯ обработка items
            if (template.getItems() != null && !template.getItems().isEmpty()) {
                List<TemplateItemRequest> items = template.getItems().stream()
                        .map(item -> {
                            TemplateItemRequest itemRequest = new TemplateItemRequest();
                            itemRequest.setName(item.getName());
                            itemRequest.setDescription(item.getDescription());
                            itemRequest.setDefaultQuantity(item.getDefaultQuantity() != null ? item.getDefaultQuantity() : 1);
                            return itemRequest;
                        })
                        .collect(Collectors.toList());
                templateRequest.setItems(items);
            } else {
                templateRequest.setItems(new ArrayList<>()); // ДОБАВЛЕНО: пустой список если нет items
            }

            model.addAttribute("templateRequest", templateRequest);
            model.addAttribute("templateId", id);
            model.addAttribute("categories", TemplateCategory.values());
            return "admin/template-form";
        } catch (Exception e) {
            log.error("Error loading edit form for template: {}", id, e);
            return "redirect:/admin/templates?error=not_found";
        }
    }

    @PostMapping("/templates/{id}/edit")
    public String updateTemplate(@PathVariable Long id,
                                 @ModelAttribute ChecklistTemplateRequest templateRequest,
                                 RedirectAttributes redirectAttributes) {
        try {
            ChecklistTemplate existingTemplate = templateService.getTemplateById(id);
            existingTemplate.setName(templateRequest.getName());
            existingTemplate.setDescription(templateRequest.getDescription());
            existingTemplate.setCategory(templateRequest.getCategory());

            // Обновляем элементы
            if (templateRequest.getItems() != null) {
                Set<TemplateItem> items = templateRequest.getItems().stream()
                        .map(itemRequest -> {
                            TemplateItem item = new TemplateItem();
                            item.setName(itemRequest.getName());
                            item.setDescription(itemRequest.getDescription());
                            item.setDefaultQuantity(itemRequest.getDefaultQuantity());
                            item.setTemplate(existingTemplate);
                            return item;
                        })
                        .collect(Collectors.toSet());

                existingTemplate.setItems(items);
            }

            templateService.updateTemplate(id, existingTemplate);
            redirectAttributes.addFlashAttribute("success", "Шаблон успешно обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении шаблона: " + e.getMessage());
        }
        return "redirect:/admin/templates";
    }

    @PostMapping("/templates/{id}/delete")
    public String deleteTemplate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            templateService.deleteTemplate(id);
            redirectAttributes.addFlashAttribute("success", "Шаблон успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении шаблона: " + e.getMessage());
        }
        return "redirect:/admin/templates";
    }
}