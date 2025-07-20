package com.example.angella.eventsapi.repository;

import com.example.angella.eventsapi.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findAllByEventId(Long eventId, Pageable pageable);
    boolean existsByIdAndEventIdAndAuthorId(Long id, Long eventId, Long authorId);

    boolean existsByIdAndAuthorId(Long messageId, Long userId);
}