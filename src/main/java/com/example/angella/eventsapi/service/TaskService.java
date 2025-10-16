package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.entity.Task;
import com.example.angella.eventsapi.entity.Event;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.repository.EventRepository;
import com.example.angella.eventsapi.repository.TaskRepository;
import com.example.angella.eventsapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public List<Task> getTasksForEvent(Long eventId) {
        return taskRepository.findAllByEventId(eventId);
    }

    public Task createTask(String description, Long eventId, Long userId, Long assignedUserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (!eventRepository.existsByIdAndParticipantsId(eventId, userId)) {
            throw new AccessDeniedException("Only event participants can create tasks");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User assignedUser = null;
        if (assignedUserId != null) {
            assignedUser = userRepository.findById(assignedUserId)
                    .orElseThrow(() -> new EntityNotFoundException("Assigned user not found"));
            // Проверяем, что назначенный пользователь является участником события
            if (!eventRepository.existsByIdAndParticipantsId(eventId, assignedUserId)) {
                throw new AccessDeniedException("Assigned user must be event participant");
            }
        }

        Task task = new Task();
        task.setDescription(description);
        task.setEvent(event);
        task.setCreator(user);
        task.setAssignedUser(assignedUser); // ДОБАВЛЕНО НАЗНАЧЕНИЕ ПОЛЬЗОВАТЕЛЯ
        task.setCompleted(false);

        return taskRepository.save(task);
    }

    // Обновить метод updateTask для поддержки назначения пользователя
    public Task updateTask(Long taskId, String newDescription, Boolean completed,
                           Long assignedUserId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (!task.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("Only task creator can update the task");
        }

        if (newDescription != null) {
            task.setDescription(newDescription);
        }
        if (completed != null) {
            task.setCompleted(completed);
        }
        if (assignedUserId != null) {
            User assignedUser = userRepository.findById(assignedUserId)
                    .orElseThrow(() -> new EntityNotFoundException("Assigned user not found"));
            if (!eventRepository.existsByIdAndParticipantsId(task.getEvent().getId(), assignedUserId)) {
                throw new AccessDeniedException("Assigned user must be event participant");
            }
            task.setAssignedUser(assignedUser);
        }

        return taskRepository.save(task);
    }

    // Остальные методы остаются без изменений
    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Task with id {0} not found!", taskId)
                ));

        if (!task.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("Only task creator can delete the task");
        }

        taskRepository.deleteById(taskId);
    }

    public boolean isTaskCreator(Long taskId, Long userId) {
        return taskRepository.existsByIdAndCreatorId(taskId, userId);
    }
}