package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.entity.Event;
import com.example.angella.eventsapi.entity.Image;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.mapper.EventMapper;
import com.example.angella.eventsapi.repository.EventRepository;
import com.example.angella.eventsapi.service.*;
import com.example.angella.eventsapi.web.dto.CreateEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final EventService eventService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final TaskService taskService;
    private final ChecklistService checklistService;
    private final CommentService commentService;
    private final ImageService imageService;

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        try {
            // Получаем ближайшие мероприятия (только будущие)
            var featuredEvents = eventService.findUpcomingEvents();

            // Подготавливаем статистику
            Map<String, Long> stats = new HashMap<>();
            stats.put("eventsCount", (long) eventService.findAll().size());
            stats.put("usersCount", (long) userService.findAllUsers().size());
            stats.put("categoriesCount", (long) categoryService.findAll().size());
            stats.put("citiesCount", (long) eventService.getAllCities().size());

            model.addAttribute("featuredEvents", featuredEvents);
            model.addAttribute("stats", stats);

            // Добавляем информацию об аутентификации
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
                         @RequestParam(required = false) String city,
                         @RequestParam(required = false) String date,
                         @RequestParam(required = false, defaultValue = "newest") String sort) {
        try {
            // Получаем только будущие мероприятия
            List<Event> events = eventService.findAllFutureEvents();

            // Применяем фильтры
            if (search != null && !search.isEmpty()) {
                events = events.stream()
                        .filter(event -> event.getName().toLowerCase().contains(search.toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (categoryId != null) {
                events = events.stream()
                        .filter(event -> event.getCategories().stream()
                                .anyMatch(category -> category.getId().equals(categoryId)))
                        .collect(Collectors.toList());
            }

            if (city != null && !city.isEmpty()) {
                events = events.stream()
                        .filter(event -> event.getLocation().getCity().equalsIgnoreCase(city))
                        .collect(Collectors.toList());
            }

            if ("oldest".equals(sort)) {
                events.sort(Comparator.comparing(Event::getStartTime).reversed()); // "Сначала дальние"
            } else {
                events.sort(Comparator.comparing(Event::getStartTime)); // "Сначала ближние" (по умолчанию)
            }

            // Получаем список уникальных городов для фильтра
            List<String> cities = eventService.getAllCities();

            model.addAttribute("events", events);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("cities", cities);
            model.addAttribute("searchTerm", search);
            model.addAttribute("selectedCategory", categoryId);
            model.addAttribute("selectedCity", city);
            model.addAttribute("selectedDate", date);
            model.addAttribute("selectedSort", sort);

            return "events/list";
        } catch (Exception e) {
            log.error("Error loading events page", e);
            model.addAttribute("error", "Не удалось загрузить список мероприятий");
            return "events/list";
        }
    }

    @GetMapping("/my-events")
    public String myEvents(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(authentication.getName());
            List<Event> events = eventService.findUserEventsWithImages(user.getId());

            // Сортируем по дате (новые сначала)
            events.sort(Comparator.comparing(Event::getStartTime).reversed());

            model.addAttribute("events", events);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("cities", eventService.getAllCities());

            return "events/my-events";
        } catch (Exception e) {
            log.error("Error loading my events page", e);
            model.addAttribute("error", "Не удалось загрузить ваши мероприятия");
            return "events/my-events";
        }
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

    // НОВЫЙ МЕТОД: Форма создания события
    @GetMapping("/event/create")
    public String createEventForm(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            model.addAttribute("event", new CreateEventRequest());
            model.addAttribute("categories", categoryService.findAll());
            return "events/create";
        } catch (Exception e) {
            log.error("Error loading event creation form", e);
            model.addAttribute("error", "Не удалось загрузить форму создания мероприятия");
            return "events/create";
        }
    }

    //Создание события
    @PostMapping("/event/create")
    public String createEvent(@ModelAttribute CreateEventRequest request,
                              @RequestParam("categories") List<String> categories,
                              @RequestParam(value = "eventImage", required = false) MultipartFile eventImage,
                              Authentication authentication,
                              Model model) {
        try {
            if (authentication == null) {
                return "redirect:/login";
            }

            User user = userService.findByUsername(authentication.getName());

            // Устанавливаем категории из параметра
            if (categories != null && !categories.isEmpty()) {
                request.setCategories(new HashSet<>(categories));
            }

            request.setCreatorId(user.getId());

            Event event = eventMapper.toEntity(request);
            Event savedEvent = eventService.create(event, user.getId());

            // ИСПРАВЛЕНИЕ: Правильная обработка изображения
            if (eventImage != null && !eventImage.isEmpty()) {
                try {
                    // Сохраняем изображение и связываем с событием
                    String imageUrl = imageService.uploadEventImage(eventImage, savedEvent.getId(), user.getId());
                    log.info("Image uploaded successfully: {}", imageUrl);
                } catch (Exception e) {
                    log.error("Failed to upload event image: {}", e.getMessage());
                    // Можно добавить сообщение об ошибке, но продолжаем создание события
                    model.addAttribute("warning", "Изображение не было загружено: " + e.getMessage());
                }
            }

            return "redirect:/events?created=true";
        } catch (Exception e) {
            log.error("Error creating event", e);
            model.addAttribute("error", "Ошибка при создании мероприятия: " + e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "events/create";
        }
    }

    // Детали события с явным путем
    @GetMapping("/event/details/{id}")
    public String eventDetail(@PathVariable Long id, Model model,
                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Event event = eventService.getByIdWithRelations(id);
            boolean isParticipant = false;
            boolean isCreator = false;

            if (userDetails != null) {
                User currentUser = userService.findByUsername(userDetails.getUsername());
                isParticipant = event.getParticipants().contains(currentUser);
                isCreator = event.getCreator().getId().equals(currentUser.getId());
            }

            // Получаем изображения мероприятия
            List<Image> eventImages = eventService.getEventImages(id);
            Image mainImage = eventService.getMainEventImage(id);

            model.addAttribute("event", event);
            model.addAttribute("isParticipant", isParticipant);
            model.addAttribute("isCreator", isCreator);
            model.addAttribute("tasks", taskService.getTasksForEvent(id));
            model.addAttribute("checklist", checklistService.getChecklistForEvent(id));
            model.addAttribute("comments", commentService.findAllByEventId(id));
            model.addAttribute("eventImages", eventImages);
            model.addAttribute("mainImage", mainImage);

            return "events/detail";
        } catch (Exception e) {
            log.error("Error loading event detail page for id: {}", id, e);
            model.addAttribute("error", "Мероприятие не найдено");
            return "error/404";
        }
    }

    @PostMapping("/event/{id}/join")
    public String joinEvent(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        eventService.addParticipant(id, user.getId());

        return "redirect:/event/details/" + id + "?joined=true";
    }

    @PostMapping("/event/{id}/leave")
    public String leaveEvent(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        eventService.removeParticipant(id, user.getId());

        return "redirect:/event/details/" + id + "?left=true";
    }
}