package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.entity.Image;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.repository.ImageRepository;
import com.example.angella.eventsapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String uploadAvatar(MultipartFile file, Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // Удаляем старый аватар если есть
            if (user.getAvatar() != null) {
                deleteImageFile(user.getAvatar());
                imageRepository.delete(user.getAvatar());
            }

            Image image = saveImage(file, user);
            user.setAvatar(image);
            userRepository.save(user);

            return "/images/" + image.getFilename();
        } catch (IOException e) {
            log.error("Error uploading avatar", e);
            throw new RuntimeException("Failed to upload image");
        }
    }

    private Image saveImage(MultipartFile file, Object entity) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID() + fileExtension;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
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
        if (entity instanceof User) {
            image.setUser((User) entity);
        }
        // Добавить обработку других типов entity

        return imageRepository.save(image);
    }

    private void deleteImageFile(Image image) {
        try {
            Path filePath = Paths.get(image.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete image file: {}", image.getFilePath());
        }
    }

    public void deleteImage(Long imageId, Long userId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found"));

        // Проверяем права доступа
        if (image.getUser() != null && !image.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Cannot delete other user's image");
        }

        deleteImageFile(image);
        imageRepository.delete(image);
    }
}