package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.entity.ChecklistItem;
import com.example.angella.eventsapi.entity.Event;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.repository.ChecklistItemRepository;
import com.example.angella.eventsapi.repository.EventRepository;
import com.example.angella.eventsapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChecklistService {

    private final ChecklistItemRepository checklistItemRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventAccessService eventAccessService;

    public List<ChecklistItem> getChecklistForEvent(Long eventId) {
        return checklistItemRepository.findAllByEventId(eventId);
    }

    public ChecklistItem createItem(String name, String description, Integer quantity,
                                    Long eventId, Long userId, Long assignedUserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (!eventAccessService.hasParticipant(eventId, userId)) {
            throw new AccessDeniedException("Only event participants can create checklist items");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User assignedUser = null;
        if (assignedUserId != null) {
            assignedUser = userRepository.findById(assignedUserId)
                    .orElseThrow(() -> new EntityNotFoundException("Assigned user not found"));
            // Проверяем, что назначенный пользователь является участником события
            if (!eventAccessService.hasParticipant(eventId, assignedUserId)) {
                throw new AccessDeniedException("Assigned user must be event participant");
            }
        }

        ChecklistItem item = new ChecklistItem();
        item.setName(name);
        item.setDescription(description);
        item.setQuantity(quantity != null ? quantity : 1);
        item.setEvent(event);
        item.setCreatedBy(user);
        item.setAssignedUser(assignedUser);

        return checklistItemRepository.save(item);
    }

    public ChecklistItem updateItem(Long itemId, String name, String description,
                                    Integer quantity, Boolean completed, Long assignedUserId, Long userId) {
        ChecklistItem item = checklistItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Checklist item not found"));

        if (!item.getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Only item creator can update the item");
        }

        if (name != null) item.setName(name);
        if (description != null) item.setDescription(description);
        if (quantity != null) item.setQuantity(quantity);
        if (completed != null) item.setCompleted(completed);

        if (assignedUserId != null) {
            User assignedUser = userRepository.findById(assignedUserId)
                    .orElseThrow(() -> new EntityNotFoundException("Assigned user not found"));
            // Проверяем, что назначенный пользователь является участником события
            if (!eventAccessService.hasParticipant(item.getEvent().getId(), assignedUserId)) {
                throw new AccessDeniedException("Assigned user must be event participant");
            }
            item.setAssignedUser(assignedUser);
        }

        return checklistItemRepository.save(item);
    }

    public void deleteItem(Long itemId, Long userId) {
        ChecklistItem item = checklistItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Checklist item not found"));

        if (!item.getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Only item creator can delete the item");
        }

        checklistItemRepository.deleteById(itemId);
    }

    public ChecklistItem toggleItemCompletion(Long itemId, Long userId) {
        ChecklistItem item = checklistItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Checklist item not found"));

        // Любой участник события может отмечать выполнение
        if (!eventAccessService.hasParticipant(item.getEvent().getId(), userId)) {
            throw new AccessDeniedException("Only event participants can toggle item completion");
        }

        item.setCompleted(!item.isCompleted());
        return checklistItemRepository.save(item);
    }

    public boolean isItemCreator(Long itemId, Long userId) {
        return checklistItemRepository.existsByIdAndCreatedById(itemId, userId);
    }
}