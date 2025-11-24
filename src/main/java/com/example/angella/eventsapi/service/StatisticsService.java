package com.example.angella.eventsapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatisticsService {

    private final UserService userService;
    private final EventService eventService;
    private final CategoryService categoryService;
    private final CommentService commentService;
    private final ChatService chatService;
    private final TaskService taskService;
    private final ChecklistService checklistService;

    public Map<String, Object> getAdminStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Основная статистика
            stats.put("totalUsers", userService.getTotalUsersCount());
            stats.put("totalEvents", eventService.getTotalEventsCount());
            stats.put("totalCategories", categoryService.getTotalCategoriesCount());
            stats.put("upcomingEvents", eventService.getUpcomingEventsCount());

            // Активность пользователей
            stats.put("averageParticipantsPerEvent", eventService.getAverageParticipantsPerEvent());
            stats.put("totalComments", commentService.getTotalCommentsCount());
            stats.put("totalChatMessages", chatService.getTotalMessagesCount());
            stats.put("totalTasks", taskService.getTotalTasksCount());
            stats.put("totalChecklistItems", checklistService.getTotalItemsCount());

            // Статистика по завершенным задачам и чек-листам
            stats.put("completedTasksPercentage", taskService.getCompletedTasksPercentage());
            stats.put("completedChecklistItemsPercentage", checklistService.getCompletedItemsPercentage());

            // Самые активные категории
            stats.put("mostPopularCategories", eventService.getMostPopularCategories(5));

            log.info("Admin statistics generated successfully");

        } catch (Exception e) {
            log.error("Error generating admin statistics", e);
            // Возвращаем базовые значения в случае ошибки
            setDefaultStatistics(stats);
        }

        return stats;
    }

    private void setDefaultStatistics(Map<String, Object> stats) {
        stats.put("totalUsers", 0);
        stats.put("totalEvents", 0);
        stats.put("totalCategories", 0);
        stats.put("upcomingEvents", 0);
        stats.put("averageParticipantsPerEvent", 0);
        stats.put("totalComments", 0);
        stats.put("totalChatMessages", 0);
        stats.put("totalTasks", 0);
        stats.put("totalChecklistItems", 0);
        stats.put("completedTasksPercentage", 0);
        stats.put("completedChecklistItemsPercentage", 0);
        stats.put("mostPopularCategories", java.util.List.of());
    }
}