package com.example.angella.eventsapi.web.controller;

import com.example.angella.eventsapi.service.SubscriptionService;
import com.example.angella.eventsapi.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> subscribeToCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long categoryId) {

        Long userId = AuthUtils.getCurrentUserId(userDetails);
        subscriptionService.subscribeOnCategory(userId, categoryId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> unsubscribeFromCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long categoryId) {

        Long userId = AuthUtils.getCurrentUserId(userDetails);
        subscriptionService.unsubscribeFromCategory(userId, categoryId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories/check")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Boolean> checkCategorySubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long categoryId) {

        Long userId = AuthUtils.getCurrentUserId(userDetails);
        boolean isSubscribed = subscriptionService.hasCategorySubscription(userId, categoryId);

        return ResponseEntity.ok(isSubscribed);
    }
}