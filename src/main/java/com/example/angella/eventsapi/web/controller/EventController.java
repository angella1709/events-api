package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.aop.AccessCheckType;
import com.example.angella.eventsapi.aop.Access;
import com.example.angella.eventsapi.entity.Event;
import com.example.angella.eventsapi.mapper.EventMapper;
import com.example.angella.eventsapi.service.EventService;
import com.example.angella.eventsapi.utils.AuthUtils;
import com.example.angella.eventsapi.web.dto.CreateEventRequest;
import com.example.angella.eventsapi.web.dto.EventDto;
import com.example.angella.eventsapi.web.dto.UpdateEventRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    @Access(checkBy = AccessCheckType.EVENT)
    public ResponseEntity<EventDto> updateEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request) {

        Long currentUserId = AuthUtils.getCurrentUserId(userDetails);
        Event updatedEvent = eventService.updateEvent(id, request, currentUserId);
        EventDto dto = eventMapper.toDto(updatedEvent);

        return ResponseEntity.ok(dto);
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

    @DeleteMapping("/{eventId}/participant/{participantId}") // Новый путь
    @PreAuthorize("hasRole('ROLE_USER')")
    @Access(checkBy = AccessCheckType.PARTICIPANT_REMOVAL) // Новая аннотация
    public ResponseEntity<String> removeParticipantFromEvent(
            @PathVariable Long eventId,
            @PathVariable Long participantId) {

        boolean isRemoved = eventService.removeParticipant(eventId, participantId);

        return isRemoved
                ? ResponseEntity.ok("Участник успешно удалён")
                : ResponseEntity.badRequest().body("Ошибка удаления");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Access(checkBy = AccessCheckType.EVENT)
    public ResponseEntity<Void> deleteEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long currentUserId = AuthUtils.getCurrentUserId(userDetails);
        eventService.deleteById(id, currentUserId);

        return ResponseEntity.noContent().build();
    }
}