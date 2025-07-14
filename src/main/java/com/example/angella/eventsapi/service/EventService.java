package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.entity.*;
import com.example.angella.eventsapi.event.model.EmailNotificationEvent;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.model.EventFilterModel;
import com.example.angella.eventsapi.repository.EventRepository;
import com.example.angella.eventsapi.repository.LocationRepository;
import com.example.angella.eventsapi.repository.ScheduleRepository;
import com.example.angella.eventsapi.repository.specification.EventSpecification;
import com.example.angella.eventsapi.web.dto.UpdateEventRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final ScheduleRepository scheduleRepository;
    private final LocationRepository locationRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Event> filter(EventFilterModel filterModel) {
        return eventRepository.findAll(
                EventSpecification.withFilter(filterModel),
                filterModel.getPage().toPageRequest()
        );
    }

    @Transactional(readOnly = true)
    public Event getById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new EntityNotFoundException(
                        MessageFormat.format("Event with id {0} not found!", eventId)
                ));
    }

    @Transactional(readOnly = true)
    public Event getByIdWithRelations(Long id) {
        return eventRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
    }
    @Transactional
    public Event create(Event event, Long creatorId) {
        event.setCategories(categoryService.upsertCategories(event.getCategories()));
        event.setSchedule(scheduleRepository.save(event.getSchedule()));
        var location = locationRepository.findByCityAndStreet(
                        event.getLocation().getCity(),
                        event.getLocation().getStreet())
                .orElseGet(() -> locationRepository.save(event.getLocation()));
        event.setLocation(location);
        User creator = userService.findById(creatorId);
        event.setCreator(creator);
        event.addParticipant(creator);
        Event savedEvent = eventRepository.save(event);

        eventPublisher.publishEvent(new EmailNotificationEvent(
                this,
                event.getCategories().stream().map(c -> c.getId()).collect(Collectors.toSet()),
                event.getName()
        ));
        return savedEvent;
    }

    @Transactional
    public Event updateEvent(Long eventId, UpdateEventRequest request, Long currentUserId) {
        Event existingEvent = getByIdWithRelations(eventId);

        // Проверка прав
        if (!existingEvent.getCreator().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only event creator can update the event");
        }

        // Обновление простых полей
        if (StringUtils.isNotBlank(request.getName())) {
            existingEvent.setName(request.getName());
        }
        if (request.getStartTime() != null) {
            existingEvent.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            existingEvent.setEndTime(request.getEndTime());
        }

        // Обновление расписания
        if (StringUtils.isNotBlank(request.getSchedule())) {
            Schedule schedule = existingEvent.getSchedule();
            if (schedule == null) {
                schedule = new Schedule();
                existingEvent.setSchedule(schedule);
            }
            schedule.setDescription(request.getSchedule());
            scheduleRepository.save(schedule);
        }

        // Обновление категорий
        if (!CollectionUtils.isEmpty(request.getCategories())) {
            Set<Category> updatedCategories = request.getCategories().stream()
                    .map(categoryName -> new Category(null, categoryName, null))
                    .collect(Collectors.toSet());
            existingEvent.setCategories(categoryService.upsertCategories(updatedCategories));
        }

        if (StringUtils.isNotBlank(request.getCity()) || StringUtils.isNotBlank(request.getStreet())) {
            Location location = locationRepository.findByCityAndStreet(
                    request.getCity() != null ? request.getCity() : existingEvent.getLocation().getCity(),
                    request.getStreet() != null ? request.getStreet() : existingEvent.getLocation().getStreet()
            ).orElseGet(() -> {
                Location newLocation = new Location();
                newLocation.setCity(request.getCity());
                newLocation.setStreet(request.getStreet());
                return locationRepository.save(newLocation);
            });
            existingEvent.setLocation(location);
        }

        Event updatedEvent = eventRepository.save(existingEvent);

        // Отправка уведомлений
        eventPublisher.publishEvent(new EmailNotificationEvent(
                this,
                updatedEvent.getCategories().stream().map(Category::getId).collect(Collectors.toSet()),
                updatedEvent.getName()
        ));

        return updatedEvent;
    }

    @Transactional
    public boolean addParticipant(Long eventId, Long participantId) {
        Event event = getById(eventId);
        User participant = userService.findById(participantId);
        boolean isAdded = event.addParticipant(participant);
        if (isAdded) {
            eventRepository.save(event);
        }
        return isAdded;
    }

    @Transactional
    public boolean removeParticipant(Long eventId, Long participantId) {
        Event event = getById(eventId);
        User participant = userService.findById(participantId);
        boolean isRemoved = event.removeParticipant(participant);
        if (isRemoved) {
            eventRepository.save(event);
        }
        return isRemoved;
    }

    @Transactional
    public void deleteById(Long id, Long currentUserId) {
        Event event = getById(id);
        if (!event.getCreator().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only event creator can delete the event");
        }
        eventRepository.deleteById(id);
    }

    public boolean hasParticipant(Long eventId, Long participantId) {
        return eventRepository.existsByIdAndParticipantsId(eventId, participantId);
    }

    public boolean isEventCreator(Long eventId, Long userId) {
        return eventRepository.existsByIdAndCreatorId(eventId, userId);
    }
}