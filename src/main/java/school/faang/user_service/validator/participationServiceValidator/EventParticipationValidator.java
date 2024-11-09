package school.faang.user_service.validator.participationServiceValidator;

import org.springframework.stereotype.Component;
import school.faang.user_service.entity.User;
import school.faang.user_service.repository.event.EventParticipationRepository;

import java.util.List;

@Component
public class EventParticipationValidator {

    public boolean isUserRegistered(long eventId, long userId,
                                    EventParticipationRepository eventParticipationRepository) {
        boolean isRegistered = true;
        List<User> participants = eventParticipationRepository.findAllParticipantsByEventId(eventId);
        if (participants.stream().noneMatch(user -> user.getId() == userId)) {
            isRegistered = false;
        }
        return isRegistered;
    }
}
