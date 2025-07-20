package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.TestConfig;
import com.example.angella.eventsapi.entity.Role;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.exception.RegisterUserException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class UserServiceIT extends TestConfig {

    @Autowired
    private UserService userService;

    @Test
    void registerUser_ShouldSuccessfullyRegisterNewUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRoles(Set.of(Role.ROLE_USER));

        User registeredUser = userService.registerUser(user);

        assertNotNull(registeredUser.getId());
        assertEquals("testuser", registeredUser.getUsername());
        assertEquals("test@example.com", registeredUser.getEmail());
        assertTrue(registeredUser.getRoles().contains(Role.ROLE_USER));
    }

    @Test
    void registerUser_ShouldThrowExceptionWhenUsernameExists() {
        User user1 = new User();
        user1.setUsername("existinguser");
        user1.setEmail("user1@example.com");
        user1.setPassword("password");
        userService.registerUser(user1);

        User user2 = new User();
        user2.setUsername("existinguser");
        user2.setEmail("user2@example.com");
        user2.setPassword("password");

        assertThrows(RegisterUserException.class, () -> userService.registerUser(user2));
    }

    @Test
    void findByUsername_ShouldReturnUser() {
        User user = new User();
        user.setUsername("finduser");
        user.setEmail("find@example.com");
        user.setPassword("password");
        userService.registerUser(user);

        User foundUser = userService.findByUsername("finduser");

        assertEquals("finduser", foundUser.getUsername());
        assertEquals("find@example.com", foundUser.getEmail());
    }
}