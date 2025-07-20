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

    public Task createTask(String description, Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Event with id {0} not found!", eventId)
                ));

        if (!eventRepository.existsByIdAndParticipantsId(eventId, userId)) {
            throw new AccessDeniedException("Only event participants can create tasks");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("User with id {0} not found!", userId)
                ));

        Task task = new Task();
        task.setDescription(description);
        task.setEvent(event);
        task.setCreator(user);
        task.setCompleted(false);

        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, String newDescription, Boolean completed, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Task with id {0} not found!", taskId)
                ));

        if (!task.getCreator().getId().equals(userId)) {
            throw new AccessDeniedException("Only task creator can update the task");
        }

        if (newDescription != null) {
            task.setDescription(newDescription);
        }
        if (completed != null) {
            task.setCompleted(completed);
        }

        return taskRepository.save(task);
    }

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