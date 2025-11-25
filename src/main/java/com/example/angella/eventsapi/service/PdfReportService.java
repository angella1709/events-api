package com.example.angella.eventsapi.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PdfReportService {

    public byte[] generateStatisticsReport(Map<String, Object> stats) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Инициализация PDF документа
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Установка отступов
            document.setMargins(20, 20, 20, 20);

            // Заголовок отчета
            addTitlePage(document, stats);

            // Основное содержание
            addPlatformOverview(document, stats);
            addUserActivity(document, stats);
            addEventStatistics(document, stats);
            addCompletionRates(document, stats);
            addPopularCategories(document, stats);

            // Футер
            addFooter(document);

            document.close();

            log.info("PDF report generated successfully, size: {} bytes", outputStream.size());
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF report", e);
            // Возвращаем простой PDF с сообщением об ошибке
            return createErrorPdf(e.getMessage());
        }
    }

    private void addTitlePage(Document document, Map<String, Object> stats) {
        // Главный заголовок
        Paragraph title = new Paragraph("СТАТИСТИЧЕСКИЙ ОТЧЕТ")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold()
                .setMarginBottom(20);
        document.add(title);

        // Подзаголовок
        Paragraph subtitle = new Paragraph("Events Platform Analytics")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14)
                .setItalic()
                .setMarginBottom(30);
        document.add(subtitle);

        // Дата генерации
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        Paragraph date = new Paragraph("Дата генерации: " + generatedAt)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setMarginBottom(40);
        document.add(date);

        // Разделительная линия
        LineSeparator line = new LineSeparator(new SolidLine());
        line.setMarginBottom(30);
        document.add(line);
    }

    private void addPlatformOverview(Document document, Map<String, Object> stats) {
        // Заголовок раздела
        Paragraph header = new Paragraph("ОБЗОР ПЛАТФОРМЫ")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(15);
        document.add(header);

        // Добавляем описание
        Paragraph description = new Paragraph("Основные метрики платформы EventsPlatform, показывающие общую активность и использование системы.")
                .setFontSize(10)
                .setItalic()
                .setMarginBottom(15);
        document.add(description);

        Map<String, Object> overview = (Map<String, Object>) stats.get("platformOverview");

        // Таблица с основными метриками
        Table table = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(20);

        // Заголовки таблицы
        table.addHeaderCell(createHeaderCell("Показатель"));
        table.addHeaderCell(createHeaderCell("Значение"));

        // Данные с описаниями
        addTableRow(table, "Общее количество пользователей", overview.get("totalUsers").toString() + " пользователей");
        addTableRow(table, "Всего мероприятий", overview.get("totalEvents").toString() + " мероприятий");
        addTableRow(table, "Активных мероприятий", overview.get("activeEvents").toString() + " предстоящих");
        addTableRow(table, "Категорий мероприятий", overview.get("totalCategories").toString() + " категорий");
        addTableRow(table, "Среднее количество участников", overview.get("averageParticipants").toString() + " человек");

        document.add(table);
    }

    private void addUserActivity(Document document, Map<String, Object> stats) {
        Paragraph header = new Paragraph("АКТИВНОСТЬ ПОЛЬЗОВАТЕЛЕЙ")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(15);
        document.add(header);

        Paragraph description = new Paragraph("Статистика взаимодействия пользователей с платформой: комментарии, сообщения, задачи и чек-листы.")
                .setFontSize(10)
                .setItalic()
                .setMarginBottom(15);
        document.add(description);

        Map<String, Object> activity = (Map<String, Object>) stats.get("userActivity");

        Table table = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(20);

        table.addHeaderCell(createHeaderCell("Вид активности"));
        table.addHeaderCell(createHeaderCell("Количество"));

        addTableRow(table, "Комментарии к мероприятиям", activity.get("totalComments").toString() + " комментариев");
        addTableRow(table, "Сообщения в чатах", activity.get("totalChatMessages").toString() + " сообщений");
        addTableRow(table, "Созданные задачи", activity.get("totalTasksCreated").toString() + " задач");
        addTableRow(table, "Элементы чек-листов", activity.get("totalChecklistItems").toString() + " элементов");

        Object messagesPerUser = activity.get("messagesPerUser");
        if (messagesPerUser != null) {
            addTableRow(table, "Сообщений на пользователя", String.format("%.1f сообщений/пользователь", messagesPerUser));
        }

        document.add(table);
    }

    private void addEventStatistics(Document document, Map<String, Object> stats) {
        Paragraph header = new Paragraph("СТАТИСТИКА МЕРОПРИЯТИЙ")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(15);
        document.add(header);

        Paragraph description = new Paragraph("Анализ мероприятий: общее количество, активные события и вовлеченность участников.")
                .setFontSize(10)
                .setItalic()
                .setMarginBottom(15);
        document.add(description);

        Map<String, Object> events = (Map<String, Object>) stats.get("eventStatistics");

        Table table = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(20);

        table.addHeaderCell(createHeaderCell("Показатель"));
        table.addHeaderCell(createHeaderCell("Значение"));

        addTableRow(table, "Всего мероприятий на платформе", events.get("totalEvents").toString() + " мероприятий");
        addTableRow(table, "Предстоящих мероприятий", events.get("upcomingEvents").toString() + " активных");
        addTableRow(table, "Среднее количество участников", events.get("averageParticipants").toString() + " человек");

        Object eventsWithChats = events.get("eventsWithChats");
        if (eventsWithChats != null) {
            addTableRow(table, "Мероприятий с активными чатами", eventsWithChats.toString() + " с чатами");
        }

        document.add(table);
    }

    private void addCompletionRates(Document document, Map<String, Object> stats) {
        Paragraph header = new Paragraph("ЭФФЕКТИВНОСТЬ ВЫПОЛНЕНИЯ")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(15);
        document.add(header);

        Paragraph description = new Paragraph("Процент выполнения задач и чек-листов, показывающий общую продуктивность участников мероприятий.")
                .setFontSize(10)
                .setItalic()
                .setMarginBottom(15);
        document.add(description);

        Map<String, Object> completion = (Map<String, Object>) stats.get("completionRates");

        Table table = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(20);

        table.addHeaderCell(createHeaderCell("Тип задач"));
        table.addHeaderCell(createHeaderCell("Уровень выполнения"));

        addTableRow(table, "Задачи мероприятий", completion.get("tasksCompleted") + "% выполнено");
        addTableRow(table, "Элементы чек-листов", completion.get("checklistItemsCompleted") + "% выполнено");

        Object overallCompletion = completion.get("overallCompletion");
        if (overallCompletion != null) {
            addTableRow(table, "Общая эффективность", overallCompletion + "% выполнено");
        }

        document.add(table);
    }

    private void addPopularCategories(Document document, Map<String, Object> stats) {
        Paragraph header = new Paragraph("ПОПУЛЯРНЫЕ КАТЕГОРИИ")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(15);
        document.add(header);

        Map<String, Object> categories = (Map<String, Object>) stats.get("popularCategories");
        List<Map<String, Object>> categoryList = (List<Map<String, Object>>) categories.get("categories");

        if (categoryList != null && !categoryList.isEmpty()) {
            Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell(createHeaderCell("Категория"));
            table.addHeaderCell(createHeaderCell("Количество мероприятий"));

            for (Map<String, Object> category : categoryList) {
                table.addCell(new Cell().add(new Paragraph(category.get("name").toString())));
                table.addCell(new Cell().add(new Paragraph(category.get("eventCount").toString())));
            }

            document.add(table);
        } else {
            document.add(new Paragraph("Нет данных по категориям").setItalic());
        }
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("\n\n"));

        Paragraph footer = new Paragraph("Отчет сгенерирован автоматически системой Events Platform")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8)
                .setItalic();
        document.add(footer);
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(5);
    }

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)).setPadding(5));
        table.addCell(new Cell().add(new Paragraph(value)).setPadding(5));
    }

    private byte[] createErrorPdf(String errorMessage) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            Paragraph errorTitle = new Paragraph("ОШИБКА ГЕНЕРАЦИИ ОТЧЕТА")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(20);
            document.add(errorTitle);

            Paragraph errorMsg = new Paragraph("При генерации отчета произошла ошибка:")
                    .setMarginBottom(10);
            document.add(errorMsg);

            Paragraph errorDetails = new Paragraph(errorMessage)
                    .setItalic()
                    .setMarginBottom(20);
            document.add(errorDetails);

            Paragraph contact = new Paragraph("Пожалуйста, обратитесь к администратору системы.")
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(contact);

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error creating error PDF", e);
            return new byte[0];
        }
    }
}