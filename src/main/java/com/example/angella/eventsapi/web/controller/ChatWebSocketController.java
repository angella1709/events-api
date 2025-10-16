package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.entity.ChatMessage;
import com.example.angella.eventsapi.entity.Task;
import com.example.angella.eventsapi.entity.ChecklistItem;
import com.example.angella.eventsapi.service.ChatService;
import com.example.angella.eventsapi.service.TaskService;
import com.example.angella.eventsapi.service.ChecklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final TaskService taskService;
    private final ChecklistService checklistService;

    @MessageMapping("/chat/{eventId}/send")
    @SendTo("/topic/chat/{eventId}")
    public ChatMessage sendMessage(@DestinationVariable Long eventId,
                                   ChatMessage message,
                                   Principal principal) {
        // Сохраняем сообщение и отправляем всем подписчикам
        ChatMessage savedMessage = chatService.createMessage(
                message.getContent(), eventId, getUserId(principal)
        );
        return savedMessage;
    }

    @MessageMapping("/task/{eventId}/update")
    public void updateTasks(@DestinationVariable Long eventId) {
        List<Task> tasks = taskService.getTasksForEvent(eventId);
        messagingTemplate.convertAndSend("/topic/tasks/" + eventId, tasks);
    }

    @MessageMapping("/checklist/{eventId}/update")
    public void updateChecklist(@DestinationVariable Long eventId) {
        List<ChecklistItem> checklist = checklistService.getChecklistForEvent(eventId);
        messagingTemplate.convertAndSend("/topic/checklist/" + eventId, checklist);
    }

    private Long getUserId(Principal principal) {
        // Логика получения ID пользователя из principal
        return 1L; // Заглушка
    }
}