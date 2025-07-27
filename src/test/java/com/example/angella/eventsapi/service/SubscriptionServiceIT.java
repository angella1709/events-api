package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.ServiceIntegrationTest;
import com.example.angella.eventsapi.entity.Category;
import com.example.angella.eventsapi.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionServiceIT extends ServiceIntegrationTest {

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;

    @Test
    void subscribeOnCategory_ShouldAddSubscription() {
        User user = userService.registerUser(createTestUser("subscriber"));
        Category category = categoryService.upsertCategories(Set.of(new Category(null, "Music", null))).iterator().next();

        subscriptionService.subscribeOnCategory(user.getId(), category.getId());

        assertTrue(subscriptionService.hasCategorySubscription(user.getId(), category.getId()));
    }

    @Test
    void sendNotifications_ShouldFindSubscribers() {
        // Подготовка данных
        Category musicCategory = categoryService.upsertCategories(Set.of(new Category(null, "Music", null))).iterator().next();
        Category sportCategory = categoryService.upsertCategories(Set.of(new Category(null, "Sport", null))).iterator().next();

        User user1 = createTestUser("user1", "user1@example.com");
        User user2 = createTestUser("user2", "user2@example.com");
        userService.registerUser(user1);
        userService.registerUser(user2);

        // Подписки
        subscriptionService.subscribeOnCategory(user1.getId(), musicCategory.getId());
        subscriptionService.subscribeOnCategory(user2.getId(), sportCategory.getId());

        // Тестирование
        Collection<String> emails = subscriptionService.getSubscribersEmails(Set.of(musicCategory.getId()));

        assertEquals(1, emails.size());
        assertTrue(emails.contains("user1@example.com"));
    }

    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        return user;
    }
}