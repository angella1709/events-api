package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.aop.AccessCheckType;
import com.example.angella.eventsapi.aop.Accessible;
import com.example.angella.eventsapi.mapper.ChecklistMapper;
import com.example.angella.eventsapi.service.ChecklistService;
import com.example.angella.eventsapi.utils.AuthUtils;
import com.example.angella.eventsapi.web.dto.ChecklistItemDto;
import com.example.angella.eventsapi.web.dto.CreateChecklistItemRequest;
import com.example.angella.eventsapi.web.dto.UpdateChecklistItemRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/checklist")
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService checklistService;
    private final ChecklistMapper checklistMapper; // Нужно создать этот маппер

    @GetMapping("/{eventId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Accessible(checkBy = AccessCheckType.PARTICIPANT)
    public ResponseEntity<List<ChecklistItemDto>> getChecklist(@PathVariable Long eventId) {
        var items = checklistService.getChecklistForEvent(eventId);
        return ResponseEntity.ok(checklistMapper.toDtoList(items));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Accessible(checkBy = AccessCheckType.PARTICIPANT)
    public ResponseEntity<ChecklistItemDto> createItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long eventId,
            @Valid @RequestBody CreateChecklistItemRequest request) {

        var createdItem = checklistService.createItem(
                request.getName(),
                request.getDescription(),
                request.getQuantity(),
                eventId,
                AuthUtils.getCurrentUserId(userDetails),
                request.getAssignedUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checklistMapper.toDto(createdItem));
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ChecklistItemDto> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateChecklistItemRequest request) {

        var updatedItem = checklistService.updateItem(
                itemId,
                request.getName(),
                request.getDescription(),
                request.getQuantity(),
                request.getCompleted(),
                request.getAssignedUserId(),
                AuthUtils.getCurrentUserId(userDetails)
        );
        return ResponseEntity.ok(checklistMapper.toDto(updatedItem));
    }

    @PatchMapping("/{itemId}/toggle")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Accessible(checkBy = AccessCheckType.PARTICIPANT)
    public ResponseEntity<ChecklistItemDto> toggleItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {

        var toggledItem = checklistService.toggleItemCompletion(
                itemId,
                AuthUtils.getCurrentUserId(userDetails)
        );
        return ResponseEntity.ok(checklistMapper.toDto(toggledItem));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {

        checklistService.deleteItem(itemId, AuthUtils.getCurrentUserId(userDetails));
        return ResponseEntity.noContent().build();
    }
}