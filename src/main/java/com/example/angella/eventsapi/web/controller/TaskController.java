package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.aop.AccessCheckType;
import com.example.angella.eventsapi.aop.Accessible;
import com.example.angella.eventsapi.mapper.TaskMapper;
import com.example.angella.eventsapi.service.TaskService;
import com.example.angella.eventsapi.utils.AuthUtils;
import com.example.angella.eventsapi.web.dto.CreateTaskRequest;
import com.example.angella.eventsapi.web.dto.TaskDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @GetMapping("/{eventId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Accessible(checkBy = AccessCheckType.PARTICIPANT)
    public ResponseEntity<List<TaskDto>> getTasks(@PathVariable Long eventId) {
        return ResponseEntity.ok(
                taskService.getTasksForEvent(eventId).stream()
                        .map(taskMapper::toDto)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Accessible(checkBy = AccessCheckType.PARTICIPANT)
    public ResponseEntity<TaskDto> createTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long eventId,
            @Valid @RequestBody CreateTaskRequest request) {

        var createdTask = taskService.createTask(
                request.getDescription(),
                eventId,
                AuthUtils.getCurrentUserId(userDetails)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskMapper.toDto(createdTask));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<TaskDto> updateTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId,
            @RequestBody TaskDto taskDto) {
        var updatedTask = taskService.updateTask(
                taskId,
                taskDto.getDescription(),
                taskDto.isCompleted(),
                AuthUtils.getCurrentUserId(userDetails)
        );
        return ResponseEntity.ok(taskMapper.toDto(updatedTask));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> deleteTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId) {
        taskService.deleteTask(taskId, AuthUtils.getCurrentUserId(userDetails));
        return ResponseEntity.noContent().build();
    }
}