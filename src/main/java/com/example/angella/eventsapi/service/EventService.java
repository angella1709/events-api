package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.entity.*;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.model.EventFilterModel;
import com.example.angella.eventsapi.repository.EventRepository;
import com.example.angella.eventsapi.repository.LocationRepository;
import com.example.angella.eventsapi.repository.specification.EventSpecification;
import com.example.angella.eventsapi.web.dto.UpdateEventRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final LocationRepository locationRepository;
    private final UserService userService;
    private final ImageService imageService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<Event> findAll() {
        List<Event> events = eventRepository.findAll();
        // Инициализируем ленивые коллекции
        events.forEach(this::initializeLazyCollections);
        return events;
    }

    @Transactional(readOnly = true)
    public Page<Event> filter(EventFilterModel filterModel) {
        Page<Event> page = eventRepository.findAll(
                EventSpecification.withFilter(filterModel),
                filterModel.getPage().toPageRequest()
        );
        // Инициализируем ленивые коллекции для каждой страницы
        page.getContent().forEach(this::initializeLazyCollections);
        return page;
    }

    @Transactional(readOnly = true)
    public Event getById(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new EntityNotFoundException(
                        MessageFormat.format("Event with id {0} not found!", eventId)
                ));
        initializeLazyCollections(event);
        return event;
    }

    @Transactional(readOnly = true)
    public Event getByIdWithRelations(Long id) {
        Event event = eventRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        initializeLazyCollections(event);
        return event;
    }

    @Transactional
    public Event create(Event event, Long creatorId) {
        event.setCategories(categoryService.upsertCategories(event.getCategories()));
        var location = locationRepository.findByCityAndStreet(
                        event.getLocation().getCity(),
                        event.getLocation().getStreet())
                .orElseGet(() -> locationRepository.save(event.getLocation()));
        event.setLocation(location);
        User creator = userService.findById(creatorId);
        event.setCreator(creator);
        event.addParticipant(creator);
        Event savedEvent = eventRepository.save(event);
        initializeLazyCollections(savedEvent);
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

        if (StringUtils.isNotBlank(request.getDescription())) {
            existingEvent.setDescription(request.getDescription());
        }

        Event updatedEvent = eventRepository.save(existingEvent);
        initializeLazyCollections(updatedEvent);
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

        if (!event.getParticipants().contains(participant)) {
            return false;
        }

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

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEventByAdmin(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found");
        }
        eventRepository.deleteById(eventId);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCities() {
        return eventRepository.findAllDistinctCities();
    }

    @Transactional(readOnly = true)
    public List<String> searchCities(String search) {
        if (search == null || search.trim().isEmpty()) {
            return getAllCities();
        }
        return eventRepository.findDistinctCitiesBySearch(search.trim());
    }

    @Transactional(readOnly = true)
    public List<Image> getEventImages(Long eventId) {
        return imageService.getEventImages(eventId);
    }

    @Transactional(readOnly = true)
    public Image getMainEventImage(Long eventId) {
        List<Image> images = imageService.getEventImages(eventId);
        return images.isEmpty() ? null : images.get(0);
    }

    @Transactional(readOnly = true)
    public List<Event> findAllWithImages() {
        List<Event> events = eventRepository.findAll();
        // Инициализируем ленивые коллекции и загружаем изображения
        events.forEach(event -> {
            initializeLazyCollections(event);
            List<Image> images = imageService.getEventImages(event.getId());
            event.setImages(images != null ? new HashSet<>(images) : new HashSet<>());
        });
        return events;
    }

    @Transactional(readOnly = true)
    public List<Event> findFeaturedEvents() {
        List<Event> events = eventRepository.findAllOrderByStartTimeDesc();

        // Фильтруем только будущие мероприятия
        Instant now = Instant.now();
        List<Event> futureEvents = events.stream()
                .filter(event -> event.getStartTime().isAfter(now))
                .collect(Collectors.toList());

        // Инициализируем ленивые коллекции и загружаем изображения
        futureEvents.forEach(event -> {
            initializeLazyCollections(event);
            List<Image> images = imageService.getEventImages(event.getId());
            event.setImages(images != null ? new HashSet<>(images) : new HashSet<>());
        });

        return futureEvents.size() > 6 ? futureEvents.subList(0, 6) : futureEvents;
    }

    @Transactional(readOnly = true)
    public List<Event> findUpcomingEvents() {
        List<Event> events = eventRepository.findAllOrderByStartTimeAsc();

        // Фильтруем только будущие мероприятия
        Instant now = Instant.now();
        List<Event> futureEvents = events.stream()
                .filter(event -> event.getStartTime().isAfter(now))
                .collect(Collectors.toList());

        // Инициализируем ленивые коллекции и загружаем изображения
        futureEvents.forEach(event -> {
            initializeLazyCollections(event);
            List<Image> images = imageService.getEventImages(event.getId());
            event.setImages(images != null ? new HashSet<>(images) : new HashSet<>());
        });

        return futureEvents.size() > 6 ? futureEvents.subList(0, 6) : futureEvents;
    }

    @Transactional(readOnly = true)
    public List<Event> findUserEventsWithImages(Long userId) {
        User user = userService.findById(userId);

        // Получаем все события и фильтруем по участникам
        List<Event> allEvents = eventRepository.findAll();
        Instant now = Instant.now();

        List<Event> userEvents = allEvents.stream()
                .filter(event -> event.getParticipants().stream()
                        .anyMatch(participant -> participant.getId().equals(userId)))
                .filter(event -> event.getStartTime().isAfter(now)) // Только будущие
                .collect(Collectors.toList());

        // Инициализируем ленивые коллекции и загружаем изображения
        userEvents.forEach(event -> {
            initializeLazyCollections(event);
            List<Image> images = imageService.getEventImages(event.getId());
            event.setImages(images != null ? new HashSet<>(images) : new HashSet<>());
        });

        return userEvents;
    }

    @Transactional(readOnly = true)
    public List<Event> findAllFutureEvents() {
        List<Event> events = eventRepository.findAll();

        // Фильтруем только будущие мероприятия
        Instant now = Instant.now();
        List<Event> futureEvents = events.stream()
                .filter(event -> event.getStartTime().isAfter(now))
                .collect(Collectors.toList());

        // Инициализируем ленивые коллекции и загружаем изображения
        futureEvents.forEach(event -> {
            initializeLazyCollections(event);
            List<Image> images = imageService.getEventImages(event.getId());
            event.setImages(images != null ? new HashSet<>(images) : new HashSet<>());
        });

        return futureEvents;
    }

    // Вспомогательный метод для инициализации ленивых коллекций
    private void initializeLazyCollections(Event event) {
        // Инициализируем participants
        if (event.getParticipants() != null) {
            event.getParticipants().size(); // Это загрузит коллекцию
        }
        // Инициализируем categories
        if (event.getCategories() != null) {
            event.getCategories().size();
        }
        // Инициализируем comments
        if (event.getComments() != null) {
            event.getComments().size();
        }
        // Инициализируем chatMessages
        if (event.getChatMessages() != null) {
            event.getChatMessages().size();
        }
        // Инициализируем tasks
        if (event.getTasks() != null) {
            event.getTasks().size();
        }
    }
}