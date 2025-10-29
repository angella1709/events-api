package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.entity.*;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.repository.EventRepository;
import com.example.angella.eventsapi.repository.ImageRepository;
import com.example.angella.eventsapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserService userService;
    private final EventRepository eventRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // АВАТАР ПОЛЬЗОВАТЕЛЯ
    public String uploadAvatar(MultipartFile file, Long userId) {
        try {
            User user = userService.findById(userId);

            // Удаляем старый аватар только если это не заглушка
            if (user.getAvatar() != null && !user.getAvatar().getFilename().equals("default-avatar.png")) {
                deleteImageFile(user.getAvatar());
                imageRepository.delete(user.getAvatar());
            }

            Image image = saveImage(file, user, null, null);
            user.setAvatar(image);
            userService.save(user);

            return "/images/" + image.getFilename();
        } catch (IOException e) {
            log.error("Error uploading avatar", e);
            throw new RuntimeException("Failed to upload image");
        }
    }

    // ИЗОБРАЖЕНИЕ ДЛЯ СОБЫТИЯ
    public String uploadEventImage(MultipartFile file, Long eventId, Long userId) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EntityNotFoundException("Event not found"));

            // Проверяем права доступа - только участники события могут загружать изображения
            if (!eventRepository.existsByIdAndParticipantsId(eventId, userId)) {
                throw new AccessDeniedException("Only event participants can upload images");
            }

            // ИСПРАВЛЕНИЕ: Сохраняем изображение и связываем с событием
            Image image = saveImage(file, null, event, null);

            // Обновляем связь события с изображением
            if (event.getImages() == null) {
                event.setImages(new java.util.HashSet<>());
            }
            event.getImages().add(image);
            eventRepository.save(event);

            log.info("Image successfully uploaded and linked to event {}: {}", eventId, image.getFilename());

            return "/images/" + image.getFilename();
        } catch (IOException e) {
            log.error("Error uploading event image", e);
            throw new RuntimeException("Failed to upload image");
        }
    }

    // ИЗОБРАЖЕНИЕ ДЛЯ ЧАТА
    public Image saveImageForChat(ChatMessage chatMessage, MultipartFile file) throws IOException {
        Image image = saveImage(file, null, null, chatMessage);
        return image;
    }

    // ОСНОВНОЙ МЕТОД СОХРАНЕНИЯ ИЗОБРАЖЕНИЯ
    private Image saveImage(MultipartFile file, User user, Event event, ChatMessage chatMessage) throws IOException {
        log.info("Starting image upload: originalFilename={}, size={}, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        String fileExtension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            fileExtension = originalFilename.substring(lastDotIndex);
        }

        String filename = UUID.randomUUID() + fileExtension;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
        }

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        Image image = new Image();
        image.setFilename(filename);
        image.setOriginalFilename(originalFilename);
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setFilePath(filePath.toString());

        // Устанавливаем связь в зависимости от типа entity
        if (user != null) {
            image.setUser(user);
        } else if (event != null) {
            image.setEvent(event);
        } else if (chatMessage != null) {
            image.setChatMessage(chatMessage);
        }

        Image savedImage = imageRepository.save(image);
        log.info("Image saved successfully: filename={}, id={}, path={}",
                filename, savedImage.getId(), filePath.toString());

        return savedImage;
    }

    // УДАЛЕНИЕ ФАЙЛА ИЗОБРАЖЕНИЯ
    private void deleteImageFile(Image image) {
        try {
            Path filePath = Paths.get(image.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("Image file deleted: {}", image.getFilePath());
        } catch (IOException e) {
            log.warn("Failed to delete image file: {}", image.getFilePath(), e);
        }
    }

    // УДАЛЕНИЕ ИЗОБРАЖЕНИЯ
    public void deleteImage(Long imageId, Long userId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found"));

        // Проверяем права доступа
        if (image.getUser() != null && !image.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Cannot delete other user's image");
        }

        if (image.getChatMessage() != null && !image.getChatMessage().getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("Cannot delete other user's chat images");
        }

        deleteImageFile(image);
        imageRepository.delete(image);
    }

    // ПОЛУЧЕНИЕ ИЗОБРАЖЕНИЙ ПОЛЬЗОВАТЕЛЯ
    public java.util.List<Image> getUserImages(Long userId) {
        return imageRepository.findByUserId(userId);
    }

    // ПОЛУЧЕНИЕ ИЗОБРАЖЕНИЙ СОБЫТИЯ
    public java.util.List<Image> getEventImages(Long eventId) {
        return imageRepository.findByEventId(eventId);
    }

    // ПОЛУЧЕНИЕ ИЗОБРАЖЕНИЙ СООБЩЕНИЯ ЧАТА
    public java.util.List<Image> getChatMessageImages(Long chatMessageId) {
        return imageRepository.findByChatMessageId(chatMessageId);
    }

    // ПОЛУЧЕНИЕ ИЗОБРАЖЕНИЯ ПО ID С ПРОВЕРКОЙ ПРАВ
    public Image getImageById(Long imageId, Long userId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found"));

        // Проверяем права доступа
        if (image.getUser() != null && !image.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Cannot access other user's image");
        }

        return image;
    }

    public Image getDefaultAvatar() {
        // Ищем существующую заглушку в базе
        return imageRepository.findByFilename("default-avatar.png")
                .orElseGet(() -> createDefaultAvatar());
    }

    private Image createDefaultAvatar() {
        Image defaultAvatar = new Image();
        defaultAvatar.setFilename("default-avatar.png");
        defaultAvatar.setOriginalFilename("default-avatar.png");
        defaultAvatar.setContentType("image/png");
        defaultAvatar.setSize(0L);
        defaultAvatar.setFilePath("system/default-avatar.png");

        return imageRepository.save(defaultAvatar);
    }
}