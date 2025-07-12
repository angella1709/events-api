package com.example.angella.eventsapi.service.checker;

import com.example.angella.eventsapi.aop.AccessCheckType;
import com.example.angella.eventsapi.service.SubscriptionService;
import com.example.angella.eventsapi.utils.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionCheckerService extends AbstractAccessCheckerService<SubscriptionCheckerService.SubscriptionAccessData> {

    private final SubscriptionService subscriptionService;

    @Override
    protected boolean check(SubscriptionAccessData accessData) {
        if (accessData.categoryId == null) {
            return false;
        }
        return subscriptionService.hasCategorySubscription(accessData.currentUser, accessData.categoryId);
    }

    @Override
    protected SubscriptionAccessData getAccessData(HttpServletRequest request) {
        return new SubscriptionAccessData(
                getFromRequestParams(request, "categoryId", Long::valueOf),
                AuthUtils.getAuthenticatedUser().getId()
        );
    }

    @Override
    public AccessCheckType getType() {
        return AccessCheckType.SUBSCRIPTION;
    }

    protected record SubscriptionAccessData(
            Long categoryId,
            Long currentUser
    ) implements AccessData {
    }
}