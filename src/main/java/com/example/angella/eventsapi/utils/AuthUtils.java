package com.example.angella.eventsapi.utils;

import com.example.angella.eventsapi.exception.ServerException;
import com.example.angella.eventsapi.security.AppUserDetails;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@UtilityClass
public class AuthUtils {

    public Long getCurrentUserId(UserDetails userDetails) {
        if (userDetails instanceof AppUserDetails details) {
            return details.getId();
        }

        throw new ServerException("UserDetails is not instanceof AppUserDetails");
    }

    public AppUserDetails getAuthenticatedUser() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof AppUserDetails details) {
            return details;
        }

        throw new ServerException("Principal in security context is not instanceof AppUserDetails");
    }

}
