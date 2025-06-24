package com.example.angella.eventsapi.service.checker;

import com.example.angella.eventsapi.aop.AccessCheckType;
import com.example.angella.eventsapi.service.EventService;
import com.example.angella.eventsapi.utils.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParticipantCheckerService extends AbstractAccessCheckerService<ParticipantCheckerService.ParticipantAccessData> {

    private final EventService eventService;

    @Override
    protected boolean check(ParticipantAccessData accessData) {
        return eventService.hasParticipant(accessData.eventId(), accessData.participantId());
    }

    @Override
    protected ParticipantAccessData getAccessData(HttpServletRequest request) {
        var eventId = getFromPathVariable(
                request,
                "id",
                Long::valueOf
        );

        return new ParticipantAccessData(eventId, AuthUtils.getAuthenticatedUser().getId());
    }

    @Override
    public AccessCheckType getType() {
        return AccessCheckType.PARTICIPANT;
    }

    protected record ParticipantAccessData(Long eventId, Long participantId) implements AccessData {
    }
}
