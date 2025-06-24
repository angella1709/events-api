package com.example.angella.eventsapi.configuration;

import com.example.angella.eventsapi.aop.AccessCheckType;
import com.example.angella.eventsapi.service.checker.AccessCheckerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public Map<AccessCheckType, AccessCheckerService> accessCheckerServices(Collection<AccessCheckerService> checkerServices) {
        return checkerServices.stream().collect(Collectors.toMap(AccessCheckerService::getType, Function.identity()));
    }

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();

        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();

        eventMulticaster.setTaskExecutor(executor);
        return eventMulticaster;
    }

}
