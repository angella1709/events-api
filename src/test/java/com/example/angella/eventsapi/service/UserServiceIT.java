package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.ServiceIntegrationTest;
import com.example.angella.eventsapi.entity.Role;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.exception.RegisterUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceIT extends ServiceIntegrationTest {

    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя для некоторых тестов
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser = userService.registerUser(testUser);
    }

    @Test
    void registerUser_ShouldSaveUserWithEncryptedPassword() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("rawpassword");

        User savedUser = userService.registerUser(newUser);

        assertNotNull(savedUser.getId());
        assertNotEquals("rawpassword", savedUser.getPassword());
        assertEquals(Set.of(Role.ROLE_USER), savedUser.getRoles());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    @Test
    void registerUser_ShouldThrowWhenUsernameExists() {
        User duplicateUser = new User();
        duplicateUser.setUsername("testuser"); // Дублируем username
        duplicateUser.setEmail("newemail@example.com");
        duplicateUser.setPassword("password");

        assertThrows(RegisterUserException.class, () -> userService.registerUser(duplicateUser));
    }

    @Test
    void registerUser_ShouldThrowWhenEmailExists() {
        User duplicateUser = new User();
        duplicateUser.setUsername("newusername");
        duplicateUser.setEmail("test@example.com"); // Дублируем email
        duplicateUser.setPassword("password");

        assertThrows(RegisterUserException.class, () -> userService.registerUser(duplicateUser));
    }

    @Test
    void findByUsername_ShouldReturnUser() {
        User foundUser = userService.findByUsername("testuser");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
        assertEquals(Set.of(Role.ROLE_USER), foundUser.getRoles());
    }

    @Test
    void findByUsername_ShouldThrowWhenUserNotFound() {
        assertThrows(EntityNotFoundException.class, () -> userService.findByUsername("nonexisting"));
    }

    @Test
    void findById_ShouldReturnUser() {
        User foundUser = userService.findById(testUser.getId());

        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals(Set.of(Role.ROLE_USER), foundUser.getRoles());
    }

    @Test
    void findById_ShouldThrowWhenUserNotFound() {
        assertThrows(EntityNotFoundException.class, () -> userService.findById(9999L));
    }

    @Test
    void registerUser_ShouldAssignUserRoleByDefault() {
        User newUser = new User();
        newUser.setUsername("defaultroleuser");
        newUser.setEmail("default@example.com");
        newUser.setPassword("password");

        User savedUser = userService.registerUser(newUser);

        assertEquals(Set.of(Role.ROLE_USER), savedUser.getRoles());
    }
}