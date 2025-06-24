package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.aop.AccessCheckType;
import com.example.angella.eventsapi.aop.Accessible;
import com.example.angella.eventsapi.mapper.CommentMapper;
import com.example.angella.eventsapi.service.CommentService;
import com.example.angella.eventsapi.utils.AuthUtils;
import com.example.angella.eventsapi.web.dto.CommentDto;
import com.example.angella.eventsapi.web.dto.CreateCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private final CommentMapper commentMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ORGANIZATION_OWNER')")
    public ResponseEntity<CommentDto> createComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateCommentRequest request,
            @RequestParam Long eventId) {
        var createdComment = commentService.save(
                commentMapper.toEntity(request),
                AuthUtils.getCurrentUserId(userDetails),
                eventId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(commentMapper.toDto(createdComment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ORGANIZATION_OWNER')")
    @Accessible(checkBy = AccessCheckType.COMMENT)
    public ResponseEntity<?> deleteComment(@PathVariable Long id, @RequestParam Long eventId) {
        commentService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

}
