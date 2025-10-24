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

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final EventService eventService;
    private final ChecklistTemplateService templateService; // ADD THIS

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Статистика
        List<User> users = userService.findAllUsers();
        List<Event> events = eventService.findAll();

        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalEvents", events.size());

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/events")
    public String eventManagement(Model model) {
        List<Event> events = eventService.findAll();
        model.addAttribute("events", events);
        return "admin/events";
    }

    @PostMapping("/events/{eventId}/delete")
    public String deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEventByAdmin(eventId);
        return "redirect:/admin/events?success=Event deleted";
    }

    @GetMapping("/templates")
    public String templateManagement(Model model) {
        model.addAttribute("templates", templateService.getAllTemplates());
        return "admin/templates";
    }

    @GetMapping("/templates/create")
    public String createTemplateForm(Model model) {
        model.addAttribute("template", new ChecklistTemplate());
        model.addAttribute("categories", TemplateCategory.values());
        return "admin/template-form";
    }

    @PostMapping("/templates/create")
    public String createTemplate(@ModelAttribute ChecklistTemplate template) {
        templateService.createTemplate(template);
        return "redirect:/admin/templates?success=Template created";
    }

    @GetMapping("/templates/{id}/edit")
    public String editTemplateForm(@PathVariable Long id, Model model) {
        ChecklistTemplate template = templateService.getTemplateById(id);
        model.addAttribute("template", template);
        model.addAttribute("categories", TemplateCategory.values());
        return "admin/template-form";
    }

    @PostMapping("/templates/{id}/edit")
    public String updateTemplate(@PathVariable Long id, @ModelAttribute ChecklistTemplate template) {
        templateService.updateTemplate(id, template);
        return "redirect:/admin/templates?success=Template updated";
    }

    @PostMapping("/templates/{id}/delete")
    public String deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return "redirect:/admin/templates?success=Template deleted";
    }
}