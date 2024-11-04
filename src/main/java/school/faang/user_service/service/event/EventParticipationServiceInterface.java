package school.faang.user_service.service.event;

import org.springframework.stereotype.Component;
import school.faang.user_service.dto.UserDto;

import java.util.List;

@Component
public interface EventParticipationServiceInterface {
    void registerParticipant(long eventId, long userId);

    void unregisterParticipant(long eventId, long userId);

    List<UserDto> getParticipantsList(long eventId);

    int getParticipantsCount(long eventId);
}
