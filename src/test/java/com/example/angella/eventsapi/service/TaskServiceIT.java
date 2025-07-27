package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.ServiceIntegrationTest;
import com.example.angella.eventsapi.entity.Event;
import com.example.angella.eventsapi.entity.Task;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceIT extends ServiceIntegrationTest {

    @Autowired
    private TaskService taskService;
    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        // Создание пользователя (это работает)
        testUser = new User();
        testUser.setUsername("chattester");
        testUser.setEmail("chat@example.com");
        testUser.setPassword("password");
        testUser = userService.registerUser(testUser);

        // Создание события - нужно добавить все обязательные поля!
        testEvent = new Event();
        testEvent.setName("Chat Event");
        testEvent.setStartTime(LocalDateTime.now().plusDays(1)); // дата начала
        testEvent.setEndTime(LocalDateTime.now().plusDays(2));   // дата окончания
        // Возможно нужны еще поля: location, schedule и т.д.

        testEvent = eventService.create(testEvent, testUser.getId());
    }

    @Test
    void createTask_ShouldSaveTask() {
        Task task = taskService.createTask("Test task", testEvent.getId(), testUser.getId());

        assertNotNull(task.getId());
        assertEquals("Test task", task.getDescription());
        assertFalse(task.isCompleted());
    }

    @Test
    void createTask_ShouldThrowWhenNotParticipant() {
        User nonParticipant = createTestUser();

        assertThrows(AccessDeniedException.class, () -> {
            Long eventId = testEvent.getId();
            Long userId = nonParticipant.getId();
            taskService.createTask("Test", eventId, userId);
        });
    }

    @Test
    void getTasksForEvent_ShouldReturnAllTasks() {
        taskService.createTask("Task 1", testEvent.getId(), testUser.getId());
        taskService.createTask("Task 2", testEvent.getId(), testUser.getId());

        List<Task> tasks = taskService.getTasksForEvent(testEvent.getId());

        assertEquals(2, tasks.size());
    }

    @Test
    void updateTask_ShouldUpdateFields() {
        Task task = taskService.createTask("Original", testEvent.getId(), testUser.getId());

        Task updated = taskService.updateTask(task.getId(), "Updated", true, testUser.getId());

        assertEquals("Updated", updated.getDescription());
        assertTrue(updated.isCompleted());
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername("nonparticipant");
        user.setEmail("non@example.com");
        user.setPassword("password");
        return userService.registerUser(user);
    }
}