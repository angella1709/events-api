package com.example.angella.eventsapi.repository;

import com.example.angella.eventsapi.entity.Category;
import com.example.angella.eventsapi.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @EntityGraph(attributePaths = {"creator", "categories", "schedule", "location","creator.roles"})
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithRelations(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"categories", "location", "schedule", "creator"})
    Page<Event> findAll(Specification<Event> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"categories", "location", "schedule", "creator"})
    List<Event> findAll();

    @Override
    @EntityGraph(attributePaths = {"categories", "location", "schedule", "creator"})
    Optional<Event> findById(Long id);

    @EntityGraph(attributePaths = {"categories", "location", "schedule", "creator"})
    @Query("SELECT e FROM Event e ORDER BY e.startTime ASC")
    List<Event> findAllOrderByStartTimeAsc();

    @EntityGraph(attributePaths = {"categories", "location", "schedule", "creator"})
    @Query("SELECT e FROM Event e ORDER BY e.startTime DESC")
    List<Event> findAllOrderByStartTimeDesc();

    @EntityGraph(attributePaths = {"categories", "location", "schedule", "creator"})
    @Query("SELECT e FROM Event e WHERE e.categories IN :categories ORDER BY e.startTime ASC")
    List<Event> findByCategoriesOrderByStartTime(@Param("categories") Set<Category> categories);

    boolean existsByIdAndParticipantsId(Long eventId, Long userId);

    boolean existsByIdAndCreatorId(Long eventId, Long userId);
}