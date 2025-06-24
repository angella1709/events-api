package com.example.angella.eventsapi.service.checker;

import com.example.angella.eventsapi.aop.AccessCheckType;
import com.example.angella.eventsapi.aop.Accessible;
import jakarta.servlet.http.HttpServletRequest;

public interface AccessCheckerService {

    boolean check(HttpServletRequest request, Accessible accessible);

    AccessCheckType getType();

}
