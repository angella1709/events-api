package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.aop.AccessCheckType;
import com.example.angella.eventsapi.aop.Accessible;
import com.example.angella.eventsapi.mapper.ChatMessageMapper;
import com.example.angella.eventsapi.model.PageModel;
import com.example.angella.eventsapi.service.ChatService;
import com.example.angella.eventsapi.utils.AuthUtils;
import com.example.angella.eventsapi.web.dto.ChatMessageDto;
import com.example.angella.eventsapi.web.dto.CreateChatMessageRequest;
import com.example.angella.eventsapi.web.dto.PageResponse;
import com.example.angella.eventsapi.web.dto.UpdateChatMessageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatMessageMapper chatMessageMapper;

    @GetMapping("/{eventId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Accessible(checkBy = AccessCheckType.PARTICIPANT)
    public ResponseEntity<PageResponse<ChatMessageDto>> getMessages(
            @PathVariable Long eventId,
            PageModel pageModel) {
        var page = chatService.getMessages(eventId, pageModel);
        return ResponseEntity.ok(new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                page.map(chatMessageMapper::toDto).getContent()
        ));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Accessible(checkBy = AccessCheckType.PARTICIPANT)
    public ResponseEntity<ChatMessageDto> createMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long eventId,
            @Valid @RequestBody CreateChatMessageRequest request) {

        var createdMessage = chatService.createMessage(
                request.getContent(),
                eventId,
                AuthUtils.getCurrentUserId(userDetails)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatMessageMapper.toDto(createdMessage));
    }

    @PutMapping("/{messageId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ChatMessageDto> updateMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long messageId,
            @Valid @RequestBody UpdateChatMessageRequest request
    ) {
        var updatedMessage = chatService.updateMessage(
                messageId,
                request.getContent(),
                AuthUtils.getCurrentUserId(userDetails)
        );
        return ResponseEntity.ok(chatMessageMapper.toDto(updatedMessage));
    }

    @DeleteMapping("/{messageId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long messageId) {
        chatService.deleteMessage(messageId, AuthUtils.getCurrentUserId(userDetails));
        return ResponseEntity.noContent().build();
    }
}