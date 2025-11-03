package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.entity.ChecklistTemplate;
import com.example.angella.eventsapi.entity.Event;
import com.example.angella.eventsapi.entity.TemplateCategory;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.service.ChecklistTemplateService;
import com.example.angella.eventsapi.service.EventService;
import com.example.angella.eventsapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
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
        // Используем метод с инициализацией изображений
        List<Event> events = eventService.findAllWithImages();
        List<Event> upcomingEvents = eventService.findUpcomingEvents();

        long totalParticipants = events.stream()
                .mapToLong(event -> event.getParticipants().size())
                .sum();

        long activeEvents = events.stream()
                .filter(event -> event.getStartTime().isBefore(java.time.Instant.now())
                        && event.getEndTime().isAfter(java.time.Instant.now()))
                .count();

        model.addAttribute("events", events);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("totalParticipants", totalParticipants);
        model.addAttribute("activeEvents", activeEvents);

        return "admin/events";
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
        List<ChecklistTemplate> templates = templateService.getAllTemplatesWithItems();

        // Добавляем статистику для отображения
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
    }

    @GetMapping("/templates/create")
    public String createTemplateForm(Model model) {
        model.addAttribute("template", new ChecklistTemplate());
        model.addAttribute("categories", TemplateCategory.values());
        return "admin/template-form";
    }

    @PostMapping("/templates/create")
    public String createTemplate(@ModelAttribute ChecklistTemplate template, RedirectAttributes redirectAttributes) {
        try {
            templateService.createTemplate(template);
            redirectAttributes.addFlashAttribute("success", "Шаблон успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании шаблона: " + e.getMessage());
        }
        return "redirect:/admin/templates";
    }

    @GetMapping("/templates/{id}/edit")
    public String editTemplateForm(@PathVariable Long id, Model model) {
        ChecklistTemplate template = templateService.getTemplateById(id);
        model.addAttribute("template", template);
        model.addAttribute("categories", TemplateCategory.values());
        return "admin/template-form";
    }

    @PostMapping("/templates/{id}/edit")
    public String updateTemplate(@PathVariable Long id, @ModelAttribute ChecklistTemplate template, RedirectAttributes redirectAttributes) {
        try {
            templateService.updateTemplate(id, template);
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