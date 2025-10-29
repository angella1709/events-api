package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.entity.Image;
import com.example.angella.eventsapi.entity.Role;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.exception.RegisterUserException;
import com.example.angella.eventsapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByUsernameOrEmail(user.getUsername(), user.getEmail())) {
            throw new RegisterUserException(
                    MessageFormat.format("User with username {0} or email {1} already exists!",
                            user.getUsername(), user.getEmail())
            );
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (CollectionUtils.isEmpty(user.getRoles())) {
            user.addRole(Role.ROLE_USER);
        }

        return userRepository.save(user);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                MessageFormat.format("User with id {0} not found!", id)
        ));
    }
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new EntityNotFoundException(
                        MessageFormat.format("User with name {0} not found!", username)
                ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void addAdminRole(Long userId) {
        User user = findById(userId);
        user.addRole(Role.ROLE_ADMIN);
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void removeAdminRole(Long userId) {
        User user = findById(userId);
        user.getRoles().remove(Role.ROLE_ADMIN);
        userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long userId, String firstName, String lastName) {
        User user = findById(userId);

        if (firstName != null && !firstName.trim().isEmpty()) {
            user.setFirstName(firstName.trim());
        }

        if (lastName != null && !lastName.trim().isEmpty()) {
            user.setLastName(lastName.trim());
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserAvatar(Long userId, Image avatar) {
        User user = findById(userId);

        // Удаляем старый аватар только если это не заглушка
        if (user.getAvatar() != null && !user.getAvatar().getFilename().equals("default-avatar.png")) {
        }

        user.setAvatar(avatar);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByIdWithEvents(Long id) {
        User user = findById(id);

        // Инициализируем ленивые коллекции
        if (user.getCreatedEvents() != null) {
            user.getCreatedEvents().size(); // Это загрузит коллекцию
        }
        if (user.getEvents() != null) {
            user.getEvents().size(); // Это загрузит коллекцию
        }

        return user;
    }
}
