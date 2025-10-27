package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.ServiceIntegrationTest;
import com.example.angella.eventsapi.entity.*;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class TaskServiceIT extends ServiceIntegrationTest {

    // Сервисы и репозитории для работы с задачами, событиями и пользователями
    @Autowired private TaskService taskService;
    @Autowired private EventService eventService;
    @Autowired private UserService userService;
    @Autowired private LocationRepository locationRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private CategoryService categoryService;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        // Подготовка тестового пользователя
        testUser = new User();
        testUser.setUsername("taskuser");
        testUser.setEmail("task@example.com");
        testUser.setPassword("password");
        testUser = userService.registerUser(testUser);

        // Подготовка тестового события
        testEvent = buildTestEvent();
        testEvent = eventService.create(testEvent, testUser.getId());
    }

    private Event buildTestEvent() {
        // Создание объекта события с обязательными полями
        Event event = new Event();
        event.setName("Test Event");
        event.setStartTime(Instant.now());
        event.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));

        // Настройка локации
        Location location = new Location();
        location.setCity("Test City");
        location.setStreet("Test Street");
        location = locationRepository.save(location);
        event.setLocation(location);

        // Настройка расписания
        Schedule schedule = new Schedule();
        schedule.setDescription("Test Schedule");
        schedule = scheduleRepository.save(schedule);
        event.setSchedule(schedule);

        event.setCreator(testUser);
        return event;
    }

    @Test
    void createTask_ShouldSaveTask() {
        // Тест создания задачи
        Task task = taskService.createTask("Test task", testEvent.getId(), testUser.getId());

        // Проверки корректности сохранения
        assertNotNull(task.getId());
        assertEquals("Test task", task.getDescription());
        assertFalse(task.isCompleted());
        assertEquals(testUser.getId(), task.getCreator().getId());
    }

    @Test
    void createTask_ShouldThrowWhenNotParticipant() {
        // Тест проверки прав доступа при создании задачи
        User nonParticipant = new User();
        nonParticipant.setUsername("nonparticipant");
        nonParticipant.setEmail("non@example.com");
        nonParticipant.setPassword("password");
        User registeredNonParticipant = userService.registerUser(nonParticipant);

        assertThrows(AccessDeniedException.class, () -> {
            taskService.createTask("Test", testEvent.getId(), registeredNonParticipant.getId());
        });
    }

    @Test
    void getTasksForEvent_ShouldReturnAllTasks() {
        // Тест получения списка задач
        taskService.createTask("Task 1", testEvent.getId(), testUser.getId());
        taskService.createTask("Task 2", testEvent.getId(), testUser.getId());

        var tasks = taskService.getTasksForEvent(testEvent.getId());
        assertEquals(2, tasks.size());
    }

    @Test
    void updateTask_ShouldUpdateFields() {
        // Тест обновления задачи
        Task task = taskService.createTask("Original", testEvent.getId(), testUser.getId());
        Task updated = taskService.updateTask(
                task.getId(),
                "Updated",
                true,
                testUser.getId()
        );

        assertEquals("Updated", updated.getDescription());
        assertTrue(updated.isCompleted());
    }

    @Test
    void updateTask_ShouldThrowWhenNotCreator() {
        // Тест проверки прав доступа при обновлении
        Task task = taskService.createTask("Test", testEvent.getId(), testUser.getId());

        User otherUser = new User();
        otherUser.setUsername("other");
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("pass");
        User registeredOtherUser = userService.registerUser(otherUser);

        assertThrows(AccessDeniedException.class, () -> {
            taskService.updateTask(task.getId(), "Hacked", false, registeredOtherUser.getId());
        });
    }

    @Test
    void deleteTask_ShouldRemoveTask() {
        // Тест удаления задачи
        Task task = taskService.createTask("To delete", testEvent.getId(), testUser.getId());
        taskService.deleteTask(task.getId(), testUser.getId());

        var tasks = taskService.getTasksForEvent(testEvent.getId());
        assertFalse(tasks.stream().anyMatch(t -> t.getId().equals(task.getId())));
    }
}