package com.example.angella.eventsapi.repository;

import com.example.angella.eventsapi.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByEventId(Long eventId, Pageable pageable);

    boolean existsByIdAndEventIdAndUserId(Long id, Long eventId, Long userId);

}
