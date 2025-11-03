package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.entity.ChatMessage;
import com.example.angella.eventsapi.entity.Task;
import com.example.angella.eventsapi.entity.ChecklistItem;
import com.example.angella.eventsapi.mapper.ChatMessageMapper;
import com.example.angella.eventsapi.mapper.ChecklistMapper;
import com.example.angella.eventsapi.mapper.TaskMapper;
import com.example.angella.eventsapi.service.ChatService;
import com.example.angella.eventsapi.service.TaskService;
import com.example.angella.eventsapi.service.ChecklistService;
import com.example.angella.eventsapi.web.dto.ChatMessageDto;
import com.example.angella.eventsapi.web.dto.ChecklistItemDto;
import com.example.angella.eventsapi.web.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final TaskService taskService;
    private final ChecklistService checklistService;
    private final ChatMessageMapper chatMessageMapper;
    private final TaskMapper taskMapper;
    private final ChecklistMapper checklistMapper;

    private Long getUserId(Principal principal) {
        // Логика получения ID пользователя из principal
        return 1L; // Заглушка
    }

    @MessageMapping("/chat/{eventId}/send")
    @SendTo("/topic/chat/{eventId}")
    public ChatMessageDto sendMessage(@DestinationVariable Long eventId,
                                      @Payload ChatMessage message,
                                      Principal principal) {
        // Сохраняем сообщение в БД
        ChatMessage savedMessage = chatService.createMessage(
                message.getContent(), eventId, getUserId(principal)
        );
        return chatMessageMapper.toDto(savedMessage);
    }

    @MessageMapping("/tasks/{eventId}/update")
    @SendTo("/topic/tasks/{eventId}")
    public List<TaskDto> updateTasks(@DestinationVariable Long eventId) {
        List<Task> tasks = taskService.getTasksForEvent(eventId);
        return tasks.stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    @MessageMapping("/checklist/{eventId}/update")
    @SendTo("/topic/checklist/{eventId}")
    public List<ChecklistItemDto> updateChecklist(@DestinationVariable Long eventId) {
        List<ChecklistItem> checklist = checklistService.getChecklistForEvent(eventId);
        return checklist.stream()
                .map(checklistMapper::toDto)
                .collect(Collectors.toList());
    }
}