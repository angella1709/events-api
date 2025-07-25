package com.example.angella.eventsapi.service.checker;

import com.example.angella.eventsapi.aop.AccessCheckType;
import com.example.angella.eventsapi.service.EventService;
import com.example.angella.eventsapi.utils.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParticipantRemovalCheckerService
        extends AbstractAccessCheckerService<ParticipantRemovalCheckerService.ParticipantRemovalAccessData> {

    private final EventService eventService;

    @Override
    protected boolean check(ParticipantRemovalAccessData accessData) {
        return eventService.isEventCreator(accessData.eventId(), accessData.currentUserId())
                || accessData.currentUserId().equals(accessData.participantId());
    }

    @Override
    protected ParticipantRemovalAccessData getAccessData(HttpServletRequest request) {
        Long eventId = getFromPathVariable(request, "eventId", Long::valueOf);
        Long participantId = getFromPathVariable(request, "participantId", Long::valueOf);

        return new ParticipantRemovalAccessData(
                eventId,
                AuthUtils.getAuthenticatedUser().getId(),
                participantId
        );
    }

    @Override
    public AccessCheckType getType() {
        return AccessCheckType.PARTICIPANT_REMOVAL;
    }

    protected record ParticipantRemovalAccessData(
            Long eventId,
            Long currentUserId,
            Long participantId
    ) implements AccessData {}
}