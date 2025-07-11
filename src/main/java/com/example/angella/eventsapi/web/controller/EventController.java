package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.aop.AccessCheckType;
import com.example.angella.eventsapi.aop.Accessible;
import com.example.angella.eventsapi.mapper.EventMapper;
import com.example.angella.eventsapi.service.EventService;
import com.example.angella.eventsapi.utils.AuthUtils;
import com.example.angella.eventsapi.web.dto.CreateEventRequest;
import com.example.angella.eventsapi.web.dto.EventDto;
import com.example.angella.eventsapi.web.dto.UpdateEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<EventDto> createEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateEventRequest request) {

        Long creatorId = AuthUtils.getCurrentUserId(userDetails);
        request.setCreatorId(creatorId);

        EventDto createdEvent = eventMapper.toDto(
                eventService.create(eventMapper.toEntity(request), creatorId)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Accessible(checkBy = AccessCheckType.EVENT)
    public ResponseEntity<EventDto> updateEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody UpdateEventRequest request) {

        Long currentUserId = AuthUtils.getCurrentUserId(userDetails);
        EventDto updatedEvent = eventMapper.toDto(
                eventService.update(id, eventMapper.toEntity(request), currentUserId)
        );

        return ResponseEntity.ok(updatedEvent);
    }

    @PutMapping("/{id}/participant")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> addParticipantToEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long participantId = AuthUtils.getCurrentUserId(userDetails);
        boolean isAdded = eventService.addParticipant(id, participantId);

        return isAdded ?
                ResponseEntity.ok("User was added to event") :
                ResponseEntity.badRequest().body("Can't add user to event");
    }

    @DeleteMapping("/{id}/participant")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Accessible(checkBy = AccessCheckType.PARTICIPANT)
    public ResponseEntity<String> removeParticipantFromEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long participantId = AuthUtils.getCurrentUserId(userDetails);
        boolean isRemoved = eventService.removeParticipant(id, participantId);

        return isRemoved ?
                ResponseEntity.ok("User was removed from event") :
                ResponseEntity.badRequest().body("Can't remove user from event");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Accessible(checkBy = AccessCheckType.EVENT)
    public ResponseEntity<Void> deleteEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long currentUserId = AuthUtils.getCurrentUserId(userDetails);
        eventService.deleteById(id, currentUserId);

        return ResponseEntity.noContent().build();
    }
}