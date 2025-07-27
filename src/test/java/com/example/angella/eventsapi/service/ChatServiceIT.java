package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.ServiceIntegrationTest;
import com.example.angella.eventsapi.entity.*;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.model.PageModel;
import com.example.angella.eventsapi.repository.LocationRepository;
import com.example.angella.eventsapi.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ChatServiceIT extends ServiceIntegrationTest {

    @Autowired
    private ChatService chatService;
    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("chatuser");
        testUser.setEmail("chat@example.com");
        testUser.setPassword("password");
        testUser = userService.registerUser(testUser);

        // Create complete Event with all required fields
        testEvent = buildTestEvent();
        testEvent = eventService.create(testEvent, testUser.getId());
    }

    private Event buildTestEvent() {
        Event event = new Event();
        event.setName("Chat Test Event");
        event.setStartTime(Instant.now());
        event.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));

        // Set location
        Location location = new Location();
        location.setCity("Test City");
        location.setStreet("Test Street");
        location = locationRepository.save(location);
        event.setLocation(location);

        // Set schedule
        Schedule schedule = new Schedule();
        schedule.setDescription("Test Schedule");
        schedule = scheduleRepository.save(schedule);
        event.setSchedule(schedule);

        event.setCreator(testUser);
        return event;
    }

    @Test
    void createMessage_ShouldSaveMessage() {
        ChatMessage message = chatService.createMessage(
                "Test message",
                testEvent.getId(),
                testUser.getId()
        );

        assertNotNull(message.getId());
        assertEquals("Test message", message.getContent());
        assertEquals(testUser.getId(), message.getAuthor().getId());
    }

    @Test
    void getMessages_ShouldReturnPaginatedMessages() {
        chatService.createMessage("Message 1", testEvent.getId(), testUser.getId());
        chatService.createMessage("Message 2", testEvent.getId(), testUser.getId());

        Page<ChatMessage> messages = chatService.getMessages(
                testEvent.getId(),
                new PageModel(0, 10)
        );

        assertEquals(2, messages.getTotalElements());
    }

    @Test
    void updateMessage_ShouldChangeContent() {
        ChatMessage message = chatService.createMessage(
                "Original",
                testEvent.getId(),
                testUser.getId()
        );

        ChatMessage updated = chatService.updateMessage(
                message.getId(),
                "Updated",
                testUser.getId()
        );

        assertEquals("Updated", updated.getContent());
        assertTrue(updated.isEdited());
    }

    @Test
    void updateMessage_ShouldThrowWhenNotAuthor() {
        ChatMessage message = chatService.createMessage(
                "Test",
                testEvent.getId(),
                testUser.getId()
        );

        User otherUser = new User();
        otherUser.setUsername("other");
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("pass");
        User registeredOtherUser = userService.registerUser(otherUser);

        assertThrows(AccessDeniedException.class, () -> {
            chatService.updateMessage(message.getId(), "Hacked", registeredOtherUser.getId());
        });
    }

    @Test
    void deleteMessage_ShouldRemoveMessage() {
        ChatMessage message = chatService.createMessage(
                "To delete",
                testEvent.getId(),
                testUser.getId()
        );
        chatService.deleteMessage(message.getId(), testUser.getId());

        Page<ChatMessage> messages = chatService.getMessages(
                testEvent.getId(),
                new PageModel(0, 10)
        );

        assertFalse(messages.getContent().stream()
                .anyMatch(m -> m.getId().equals(message.getId())));
    }
}