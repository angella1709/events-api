package com.example.angella.eventsapi.service;

import com.example.angella.eventsapi.entity.ChatMessage;
import com.example.angella.eventsapi.entity.Event;
import com.example.angella.eventsapi.entity.Image;
import com.example.angella.eventsapi.entity.User;
import com.example.angella.eventsapi.exception.AccessDeniedException;
import com.example.angella.eventsapi.exception.EntityNotFoundException;
import com.example.angella.eventsapi.model.PageModel;
import com.example.angella.eventsapi.repository.ChatMessageRepository;
import com.example.angella.eventsapi.repository.EventRepository;
import com.example.angella.eventsapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    public Page<ChatMessage> getMessages(Long eventId, PageModel pageModel) {
        return chatMessageRepository.findAllByEventId(
                eventId,
                pageModel == null ? Pageable.unpaged() : pageModel.toPageRequest()
        );
    }

    public ChatMessage createMessage(String content, Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Event with id {0} not found!", eventId)
                ));

        if (!eventRepository.existsByIdAndParticipantsId(eventId, userId)) {
            throw new AccessDeniedException("Only event participants can post messages");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("User with id {0} not found!", userId)
                ));

        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setEvent(event);
        message.setAuthor(user);
        message.setImages(new java.util.HashSet<>()); // Инициализируем коллекцию

        return chatMessageRepository.save(message);
    }

    public ChatMessage updateMessage(Long messageId, String newContent, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (!message.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("Only author can edit the message");
        }

        message.setContent(newContent);
        message.setEdited(true);
        return chatMessageRepository.save(message);
    }

    public void deleteMessage(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Message with id {0} not found!", messageId)
                ));

        if (!message.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("Only message author can delete the message");
        }

        // Удаляем связанные изображения
        if (message.getImages() != null && !message.getImages().isEmpty()) {
            for (Image image : message.getImages()) {
                imageService.deleteImage(image.getId(), userId);
            }
        }

        chatMessageRepository.deleteById(messageId);
    }

    public boolean isMessageAuthor(Long messageId, Long userId) {
        return chatMessageRepository.existsByIdAndAuthorId(messageId, userId);
    }

    // НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ИЗОБРАЖЕНИЯМИ

    public ChatMessage addImageToMessage(Long messageId, MultipartFile imageFile, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (!message.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("Only message author can add images");
        }

        try {
            // Сохраняем изображение и связываем с сообщением
            Image image = imageService.saveImageForChat(message, imageFile);

            // Инициализируем коллекцию если она null
            if (message.getImages() == null) {
                message.setImages(new java.util.HashSet<>());
            }

            message.getImages().add(image);
            message.setEdited(true); // Помечаем как отредактированное

            return chatMessageRepository.save(message);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    public List<Image> getMessageImages(Long messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (message.getImages() == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(message.getImages());
    }

    public void removeImageFromMessage(Long messageId, Long imageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (!message.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("Only message author can remove images");
        }

        // Проверяем, что изображение принадлежит этому сообщению
        Image imageToRemove = message.getImages().stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Image not found in this message"));

        // Удаляем изображение из коллекции
        message.getImages().remove(imageToRemove);
        message.setEdited(true);

        // Удаляем файл изображения
        imageService.deleteImage(imageId, userId);

        chatMessageRepository.save(message);
    }
}