package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.ServiceIntegrationTest;
import com.example.angella.eventsapi.entity.ChatMessage;
import com.example.angella.eventsapi.entity.Event;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.*;

class ChatServiceIT extends ServiceIntegrationTest {

    @Autowired
    private ChatService chatService;
    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("chattester");
        testUser.setEmail("chat@example.com");
        testUser.setPassword("password");
        testUser = userService.registerUser(testUser);

        testEvent = new Event();
        testEvent.setName("Chat Event");
        testEvent = eventService.create(testEvent, testUser.getId());
    }

    @Test
    void createMessage_ShouldSaveMessage() {
        ChatMessage message = chatService.createMessage("Test message", testEvent.getId(), testUser.getId());

        assertNotNull(message.getId());
        assertEquals("Test message", message.getContent());
        assertEquals(testUser.getId(), message.getAuthor().getId());
    }

    @Test
    void createMessage_ShouldThrowWhenNotParticipant() {
        User nonParticipant = createTestUser();

        assertThrows(AccessDeniedException.class, () -> {
            Long eventId = testEvent.getId();
            Long userId = nonParticipant.getId();
            chatService.createMessage("Test", eventId, userId);
        });
    }

    @Test
    void getMessages_ShouldReturnPaginatedMessages() {
        chatService.createMessage("Message 1", testEvent.getId(), testUser.getId());
        chatService.createMessage("Message 2", testEvent.getId(), testUser.getId());

        Page<ChatMessage> messages = chatService.getMessages(testEvent.getId(), null);

        assertEquals(2, messages.getTotalElements());
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername("nonparticipant");
        user.setEmail("non@example.com");
        user.setPassword("password");
        return userService.registerUser(user);
    }
}