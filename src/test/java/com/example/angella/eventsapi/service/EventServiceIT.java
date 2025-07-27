package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.ServiceIntegrationTest;
import com.example.angella.eventsapi.entity.*;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.web.dto.UpdateEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceIT extends ServiceIntegrationTest {

    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("eventcreator");
        testUser.setEmail("creator@example.com");
        testUser.setPassword("password");
        testUser = userService.registerUser(testUser);

        testCategory = new Category();
        testCategory.setName("TestCategory");
        testCategory = categoryService.upsertCategories(Set.of(testCategory)).iterator().next();
    }

    @Test
    void createEvent_ShouldSaveWithAllRelations() {
        Event event = buildTestEvent();
        Event savedEvent = eventService.create(event, testUser.getId());

        assertNotNull(savedEvent.getId());
        assertEquals("Test Event", savedEvent.getName());
        assertEquals(testUser.getId(), savedEvent.getCreator().getId());
        assertEquals(1, savedEvent.getCategories().size());
    }

    @Test
    void addParticipant_ShouldAddUserToEvent() {
        Event event = createTestEvent();
        User participant = createTestUser("participant");

        boolean result = eventService.addParticipant(event.getId(), participant.getId());

        assertTrue(result);
        assertTrue(eventService.hasParticipant(event.getId(), participant.getId()));
    }

    @Test
    void updateEvent_ShouldThrowWhenNotCreator() {
        Event event = createTestEvent();
        User anotherUser = createTestUser("anotheruser");

        UpdateEventRequest updateRequest = new UpdateEventRequest();
        updateRequest.setName("Updated Name");

        assertThrows(AccessDeniedException.class, () ->
                eventService.updateEvent(event.getId(), updateRequest, anotherUser.getId()));
    }

    private Event createTestEvent() {
        Event event = buildTestEvent();
        return eventService.create(event, testUser.getId());
    }

    private Event buildTestEvent() {
        Event event = new Event();
        event.setName("Test Event");
        event.setStartTime(Instant.now().plusSeconds(3600));
        event.setEndTime(Instant.now().plusSeconds(7200));
        event.setCategories(Set.of(testCategory));

        Location location = new Location();
        location.setCity("Test City");
        location.setStreet("Test Street");
        event.setLocation(location);

        Schedule schedule = new Schedule();
        schedule.setDescription("Test Schedule");
        event.setSchedule(schedule);

        event.setCreator(testUser);
        return event;
    }

    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password");
        return userService.registerUser(user);
    }
}