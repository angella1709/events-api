package com.example.angella.eventsapi.service.checker;

import com.example.angella.eventsapi.aop.AccessCheckType;
import com.example.angella.eventsapi.aop.Access;
import jakarta.servlet.http.HttpServletRequest;

public interface AccessCheckerService {

    boolean check(HttpServletRequest request, Access accessible);

    AccessCheckType getType();

}
