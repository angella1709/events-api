package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.TestConfig;
import com.example.angella.eventsapi.entity.*;
import com.example.angella.eventsapi.service.EventService;
import com.example.angella.eventsapi.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class PublicEventControllerIT extends TestConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user = userService.registerUser(user);

        Location location = new Location();
        location.setCity("Test City");
        location.setStreet("Test Street");

        Schedule schedule = new Schedule();
        schedule.setDescription("Test Schedule");

        testEvent = new Event();
        testEvent.setName("Integration Test Event");
        testEvent.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        testEvent.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));
        testEvent.setLocation(location);
        testEvent.setSchedule(schedule);
        testEvent.setCreator(user);

        Category category = new Category();
        category.setName("Integration Test Category");
        testEvent.setCategories(Set.of(category));

        eventService.create(testEvent, user.getId());
    }

    @Test
    void getEventById_ShouldReturnEvent() throws Exception {
        mockMvc.perform(get("/api/v1/public/event/{id}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testEvent.getId()))
                .andExpect(jsonPath("$.name").value("Integration Test Event"));
    }

    @Test
    void getEvents_ShouldReturnEvents() throws Exception {
        mockMvc.perform(get("/api/v1/public/event"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Integration Test Event"));
    }

    @Test
    void getEventsWithFilter_ShouldFilterEvents() throws Exception {
        mockMvc.perform(get("/api/v1/public/event/filter")
                        .param("name", "Integration Test Event"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Integration Test Event"));
    }
}