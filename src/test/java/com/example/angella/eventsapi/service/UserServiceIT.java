package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.ServiceIntegrationTest;
import com.example.angella.eventsapi.entity.Role;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.exception.RegisterUserException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceIT extends ServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void registerUser_ShouldSaveUserWithEncryptedPassword() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("rawpassword");
        newUser.setRoles(Set.of(Role.ROLE_USER));

        User savedUser = userService.registerUser(newUser);

        assertNotNull(savedUser.getId());
        assertNotEquals("rawpassword", savedUser.getPassword()); // Проверка хеширования
        assertTrue(savedUser.getRoles().contains(Role.ROLE_USER));
    }

    @Test
    void registerUser_ShouldThrowWhenUsernameExists() {
        User user1 = new User();
        user1.setUsername("duplicate");
        user1.setEmail("user1@example.com");
        user1.setPassword("pass1");
        userService.registerUser(user1);

        User user2 = new User();
        user2.setUsername("duplicate"); // Дубликат username
        user2.setEmail("user2@example.com");
        user2.setPassword("pass2");

        assertThrows(RegisterUserException.class, () -> userService.registerUser(user2));
    }

    @Test
    void findByUsername_ShouldReturnUserWithRoles() {
        User user = new User();
        user.setUsername("userwithroles");
        user.setEmail("roles@example.com");
        user.setPassword("password");
        user.addRole(Role.ROLE_USER);
        userService.registerUser(user);

        User found = userService.findByUsername("userwithroles");

        assertNotNull(found);
        assertTrue(found.getRoles().contains(Role.ROLE_USER));
    }
}