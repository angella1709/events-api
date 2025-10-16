package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.entity.Event;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.service.EventService;
import com.example.angella.eventsapi.service.TaskService;
import com.example.angella.eventsapi.service.ChecklistService;
import com.example.angella.eventsapi.service.UserService;
import com.example.angella.eventsapi.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatPageController {

    private final EventService eventService;
    private final UserService userService;
    private final TaskService taskService;
    private final ChecklistService checklistService;

    @GetMapping
    public String chatsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Event> userEvents = user.getEvents().stream()
                .collect(Collectors.toList());

        model.addAttribute("events", userEvents);
        model.addAttribute("currentUser", user);
        return "chats/list";
    }

    @GetMapping("/{eventId}")
    public String chatRoom(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable Long eventId,
                           Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Event event = eventService.getByIdWithRelations(eventId);

        // Проверяем, что пользователь является участником события
        if (!event.getParticipants().contains(user)) {
            return "redirect:/chats?error=access_denied";
        }

        model.addAttribute("event", event);
        model.addAttribute("currentUser", user);
        model.addAttribute("tasks", taskService.getTasksForEvent(eventId));
        model.addAttribute("checklist", checklistService.getChecklistForEvent(eventId));
        model.addAttribute("participants", event.getParticipants());

        return "chats/room";
    }
}