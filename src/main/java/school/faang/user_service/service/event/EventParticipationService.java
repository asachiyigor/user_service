package school.faang.user_service.service.event;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.UserDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.mapper.UserMapper;
import school.faang.user_service.repository.event.EventParticipationRepository;
import school.faang.user_service.validator.participationServiceValidator.EventParticipationValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventParticipationService {
    private final EventParticipationRepository eventParticipationRepository;
    private final UserMapper userMapper;
    private final EventParticipationValidator eventParticipationValidator;

    public void registerParticipant(long eventId, long userId) {
        if (eventParticipationValidator.isUserRegistered(eventId, userId, eventParticipationRepository)) {
            throw new IllegalArgumentException("User already registered for event with ID: " + eventId);
        }
        eventParticipationRepository.register(eventId, userId);
    }

    public void unregisterParticipant(long eventId, long userId) {
        if (!eventParticipationValidator.isUserRegistered(eventId, userId, eventParticipationRepository)) {
            throw new IllegalArgumentException("User not registered for event with ID: " + eventId);
        }
        eventParticipationRepository.unregister(eventId, userId);
    }

    public List<UserDto> getParticipantsList(long eventId) {
        List<User> allUsers = eventParticipationRepository.findAllParticipantsByEventId(eventId);
        return allUsers.stream().map(userMapper::toDto).toList();
    }

    public int getParticipantsCount(long eventId) {
        return eventParticipationRepository.countParticipants(eventId);
    }
}
