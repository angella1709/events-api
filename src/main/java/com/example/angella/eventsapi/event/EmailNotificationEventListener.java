package com.example.angella.eventsapi.event;

import com.example.angella.eventsapi.event.model.EmailNotificationEvent;
import com.example.angella.eventsapi.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationEventListener {

    private final SubscriptionService subscriptionService;

    @EventListener(EmailNotificationEvent.class)
    public void onEvent(EmailNotificationEvent event) {
        log.info("Send email for subscribers. Event: " + event);
        subscriptionService.sendNotifications(
                event.getCategories(),
                event.getEventName()
        );
    }

}
