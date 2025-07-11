package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.entity.Event;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.event.model.EmailNotificationEvent;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.model.EventFilterModel;
import com.example.angella.eventsapi.repository.EventRepository;
import com.example.angella.eventsapi.repository.LocationRepository;
import com.example.angella.eventsapi.repository.ScheduleRepository;
import com.example.angella.eventsapi.repository.specification.EventSpecification;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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

    public Page<Event> filter(EventFilterModel filterModel) {
        return eventRepository.findAll(
                EventSpecification.withFilter(filterModel),
                filterModel.getPage().toPageRequest()
        );
    }

    public Event getById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new EntityNotFoundException(
                        MessageFormat.format("Event with id {0} not found!", eventId)
                ));
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

        Event savedEvent = eventRepository.save(event);

        eventPublisher.publishEvent(new EmailNotificationEvent(
                this,
                event.getCategories().stream().map(c -> c.getId()).collect(Collectors.toSet()),
                event.getName()
        ));

        return savedEvent;
    }

    @Transactional
    public Event update(Long eventId, Event eventForUpdate, Long currentUserId) {
        Event currentEvent = getById(eventId);

        if (!currentEvent.getCreator().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only event creator can update the event");
        }

        if (eventForUpdate.getName() != null &&
                !Objects.equals(eventForUpdate.getName(), currentEvent.getName())) {
            currentEvent.setName(eventForUpdate.getName());
        }

        if (eventForUpdate.getStartTime() != null &&
                !Objects.equals(eventForUpdate.getStartTime(), currentEvent.getStartTime())) {
            currentEvent.setStartTime(eventForUpdate.getStartTime());
        }

        if (eventForUpdate.getEndTime() != null &&
                !Objects.equals(eventForUpdate.getEndTime(), currentEvent.getEndTime())) {
            currentEvent.setEndTime(eventForUpdate.getEndTime());
        }

        Schedule currentSchedule = currentEvent.getSchedule();
        Schedule updatedSchedule = eventForUpdate.getSchedule();
        if (updatedSchedule != null && StringUtils.isNoneBlank(updatedSchedule.getDescription()) &&
                !Objects.equals(currentSchedule.getDescription(), updatedSchedule.getDescription())) {
            currentSchedule.setDescription(updatedSchedule.getDescription());
        }

        if (!CollectionUtils.isEmpty(eventForUpdate.getCategories())) {
            currentEvent.setCategories(categoryService.upsertCategories(eventForUpdate.getCategories()));
        }

        return eventRepository.save(currentEvent);
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