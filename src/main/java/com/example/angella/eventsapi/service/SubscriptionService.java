package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.entity.Category;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final CategoryService categoryService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailSenderService emailSenderService;

    public void sendNotifications(Collection<Long> categoriesId, String eventName) {
        Set<String> emails = userRepository.getEmailsBySubscriptions(categoriesId);
        emails.forEach(email -> {
            String title = "Новое мероприятие в вашей подписке!";
            String body = String.format("Доступно новое мероприятие: %s", eventName);
            emailSenderService.send(email, title, body);
        });
    }

    @Transactional
    public void subscribeOnCategory(Long userId, Long categoryId) {
        User user = userService.findById(userId);
        Category category = categoryService.findById(categoryId);

        if (!user.getSubscribedCategories().contains(category)) {
            user.getSubscribedCategories().add(category);
            userService.save(user);
            log.info("User {} subscribed to category {}", userId, categoryId);
        }
    }

    @Transactional
    public void unsubscribeFromCategory(Long userId, Long categoryId) {
        User user = userService.findById(userId);
        boolean removed = user.getSubscribedCategories()
                .removeIf(c -> c.getId().equals(categoryId));

        if (removed) {
            userService.save(user);
            log.info("User {} unsubscribed from category {}", userId, categoryId);
        }
    }

    public boolean hasCategorySubscription(Long userId, Long categoryId) {
        return userRepository.existsByIdAndSubscribedCategoriesId(userId, categoryId);
    }
    
    protected Set<String> getSubscribersEmails(Collection<Long> categoryIds) {
        return userRepository.getEmailsBySubscriptions(categoryIds);
    }
}