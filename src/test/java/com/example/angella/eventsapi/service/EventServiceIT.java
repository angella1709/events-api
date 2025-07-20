package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.TestConfig;
import com.example.angella.eventsapi.entity.*;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.model.EventFilterModel;
import com.example.angella.eventsapi.model.PageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class EventServiceIT extends TestConfig {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("eventuser");
        testUser.setEmail("event@example.com");
        testUser.setPassword("password");
        testUser = userService.registerUser(testUser);

        Location location = new Location();
        location.setCity("Test City");
        location.setStreet("Test Street");

        Schedule schedule = new Schedule();
        schedule.setDescription("Test Schedule");

        testEvent = new Event();
        testEvent.setName("Test Event");
        testEvent.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        testEvent.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));
        testEvent.setLocation(location);
        testEvent.setSchedule(schedule);
        testEvent.setCreator(testUser);

        Category category = new Category();
        category.setName("Test Category");
        testEvent.setCategories(Set.of(category));

        eventService.create(testEvent, testUser.getId());
    }

    @Test
    void createEvent_ShouldSuccessfullyCreateEvent() {
        assertNotNull(testEvent.getId());
        assertEquals("Test Event", testEvent.getName());
        assertEquals(testUser.getId(), testEvent.getCreator().getId());
    }

    @Test
    void getById_ShouldReturnEvent() {
        Event foundEvent = eventService.getById(testEvent.getId());

        assertEquals(testEvent.getId(), foundEvent.getId());
        assertEquals("Test Event", foundEvent.getName());
    }

    @Test
    void updateEvent_ShouldUpdateEvent() {
        testEvent.setName("Updated Event");
        Event updatedEvent = eventService.updateEvent(testEvent.getId(), testEvent, testUser.getId());

        assertEquals("Updated Event", updatedEvent.getName());
    }

    @Test
    void updateEvent_ShouldThrowExceptionWhenNotCreator() {
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        anotherUser = userService.registerUser(anotherUser);

        assertThrows(AccessDeniedException.class, () ->
                eventService.updateEvent(testEvent.getId(), testEvent, anotherUser.getId()));
    }

    @Test
    void filter_ShouldFilterEvents() {
        EventFilterModel filter = new EventFilterModel();
        filter.setName("Test Event");
        filter.setPage(new PageModel(0, 10));

        Page<Event> result = eventService.filter(filter);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Event", result.getContent().get(0).getName());
    }
}