package com.example.angella.eventsapi.repository;

import com.example.angella.eventsapi.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByUserId(Long userId);

    List<Image> findByEventId(Long eventId);

    List<Image> findByChatMessageId(Long chatMessageId);

    Optional<Image> findByFilename(String filename);

    @Query("SELECT i FROM Image i WHERE i.user.id = :userId AND i.id = :imageId")
    Optional<Image> findByIdAndUserId(@Param("imageId") Long imageId, @Param("userId") Long userId);
}