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

    @EntityGraph(attributePaths = {"creator", "categories", "description", "location","creator.roles"})
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithRelations(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"categories", "location", "description", "creator"})
    Page<Event> findAll(Specification<Event> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"categories", "location", "description", "creator"})
    List<Event> findAll();

    @Override
    @EntityGraph(attributePaths = {"categories", "location", "description", "creator"})
    Optional<Event> findById(Long id);

    @EntityGraph(attributePaths = {"categories", "location", "description", "creator", "participants"})
    @Query("SELECT e FROM Event e ORDER BY e.startTime DESC")
    List<Event> findAllOrderByStartTimeDesc();

    @EntityGraph(attributePaths = {"categories", "location", "description", "creator", "participants"})
    @Query("SELECT e FROM Event e ORDER BY e.startTime ASC")
    List<Event> findAllOrderByStartTimeAsc();


    @EntityGraph(attributePaths = {"categories", "location", "description", "creator"})
    @Query("SELECT e FROM Event e JOIN e.categories c WHERE c IN :categories ORDER BY e.startTime")
    List<Event> findByCategoriesOrderByStartTime(@Param("categories") Set<Category> categories);

    boolean existsByIdAndParticipantsId(Long eventId, Long userId);

    boolean existsByIdAndCreatorId(Long eventId, Long userId);

    //Города
    @Query("SELECT DISTINCT l.city FROM Location l ORDER BY l.city")
    List<String> findAllDistinctCities();

    @Query("SELECT DISTINCT l.city FROM Location l WHERE LOWER(l.city) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY l.city")
    List<String> findDistinctCitiesBySearch(@Param("search") String search);
}