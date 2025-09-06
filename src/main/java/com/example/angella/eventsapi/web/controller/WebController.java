package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.service.CategoryService;
import com.example.angella.eventsapi.service.EventService;
import com.example.angella.eventsapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final EventService eventService;
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        try {
            // Get featured events (first 6 events)
            var events = eventService.findAll();
            var featuredEvents = events.size() > 6 ? events.subList(0, 6) : events;

            // Prepare statistics
            Map<String, Long> stats = new HashMap<>();
            stats.put("eventsCount", (long) events.size());
            stats.put("usersCount", 500L); // Mock data, replace with actual count
            stats.put("categoriesCount", (long) categoryService.findAll().size());
            stats.put("citiesCount", 15L); // Mock data

            model.addAttribute("featuredEvents", featuredEvents);
            model.addAttribute("stats", stats);

            // Add authentication info
            if (authentication != null && authentication.isAuthenticated()) {
                model.addAttribute("currentUser", authentication.getName());
            }

            return "index";
        } catch (Exception e) {
            log.error("Error loading home page", e);
            model.addAttribute("error", "Не удалось загрузить главную страницу");
            return "index";
        }
    }

    @GetMapping("/events")
    public String events(Model model,
                         @RequestParam(required = false) String search,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) String date) {
        try {
            var events = eventService.findAll();

            // Apply filters if provided
            if (search != null && !search.isEmpty()) {
                events = events.stream()
                        .filter(event -> event.getName().toLowerCase().contains(search.toLowerCase()))
                        .toList();
            }

            if (categoryId != null) {
                events = events.stream()
                        .filter(event -> event.getCategories().stream()
                                .anyMatch(category -> category.getId().equals(categoryId)))
                        .toList();
            }

            model.addAttribute("events", events);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("searchTerm", search);
            model.addAttribute("selectedCategory", categoryId);
            model.addAttribute("selectedDate", date);

            return "events/list";
        } catch (Exception e) {
            log.error("Error loading events page", e);
            model.addAttribute("error", "Не удалось загрузить список мероприятий");
            return "events/list";
        }
    }

    @GetMapping("/event/{id}")
    public String eventDetail(@PathVariable Long id, Model model) {
        try {
            var event = eventService.getByIdWithRelations(id);
            model.addAttribute("event", event);
            return "events/detail";
        } catch (Exception e) {
            log.error("Error loading event detail page for id: {}", id, e);
            model.addAttribute("error", "Мероприятие не найдено");
            return "error/404";
        }
    }

    @GetMapping("/event/create")
    public String createEventForm(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "events/create";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "categories/list";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверные учетные данные");
        }
        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            var user = userService.findByUsername(authentication.getName());
            model.addAttribute("user", user);
            return "auth/profile";
        } catch (Exception e) {
            log.error("Error loading profile page", e);
            model.addAttribute("error", "Не удалось загрузить профиль");
            return "auth/profile";
        }
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}