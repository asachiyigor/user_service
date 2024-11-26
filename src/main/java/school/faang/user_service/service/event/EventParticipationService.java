package school.faang.user_service.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.user.UserDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.mapper.user.UserMapper;
import school.faang.user_service.repository.event.EventParticipationRepository;
import school.faang.user_service.validator.event.EventParticipationValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventParticipationService implements EventParticipationServiceInterface {
    private final EventParticipationRepository eventParticipationRepository;
    private final UserMapper userMapper;
    private final EventParticipationValidator eventParticipationValidator;

    @Override
    public void registerParticipant(long eventId, long userId) {
        if (eventParticipationValidator.isUserRegistered(eventId, userId, eventParticipationRepository)) {
            throw new IllegalArgumentException("User already registered for event with ID: " + eventId);
        }
        eventParticipationRepository.register(eventId, userId);
    }

    @Override
    public void unregisterParticipant(long eventId, long userId) {
        if (!eventParticipationValidator.isUserRegistered(eventId, userId, eventParticipationRepository)) {
            throw new IllegalArgumentException("User not registered for event with ID: " + eventId);
        }
        eventParticipationRepository.unregister(eventId, userId);
    }

    @Override
    public List<UserDto> getParticipantsList(long eventId) {
        List<User> allUsers = eventParticipationRepository.findAllParticipantsByEventId(eventId);
        return allUsers.stream().map(userMapper::toDto).toList();
    }

    @Override
    public int getParticipantsCount(long eventId) {
        return eventParticipationRepository.countParticipants(eventId);
    }
}
