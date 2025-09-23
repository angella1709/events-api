package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.service.ImageService;
import com.example.angella.eventsapi.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/avatar")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {

        String imageUrl = imageService.uploadAvatar(
                file,
                AuthUtils.getCurrentUserId(userDetails)
        );
        return ResponseEntity.ok(imageUrl);
    }

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> uploadEventImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file) {

        String imageUrl = imageService.uploadEventImage(
                file,
                eventId,
                AuthUtils.getCurrentUserId(userDetails)
        );
        return ResponseEntity.ok(imageUrl);
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> deleteImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long imageId) {

        imageService.deleteImage(imageId, AuthUtils.getCurrentUserId(userDetails));
        return ResponseEntity.noContent().build();
    }
}