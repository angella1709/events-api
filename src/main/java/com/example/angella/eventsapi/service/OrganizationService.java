package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.entity.Organization;
import com.example.angella.eventsapi.entity.Role;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.repository.OrganizationRepository;
import com.example.angella.eventsapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    private final UserRepository userRepository;

    @Transactional
    public Organization save(Organization organization, Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    if (!user.hasRole(Role.ROLE_ORGANIZATION_OWNER)) {
                        throw new AccessDeniedException("You don't has rights for create organization!");
                    }
                    organization.setOwner(user);
                    return organizationRepository.save(organization);
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("User with id {0} not found!", userId)
                ));
    }

    public Organization findById(Long id) {
        return organizationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                MessageFormat.format("Organization with id {0} not found!", id)
        ));
    }

}
