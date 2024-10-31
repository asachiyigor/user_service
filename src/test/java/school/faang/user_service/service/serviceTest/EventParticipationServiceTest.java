package school.faang.user_service.service.serviceTest;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.UserDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.mapper.UserMapper;
import school.faang.user_service.repository.event.EventParticipationRepository;
import school.faang.user_service.service.event.EventParticipationService;
import school.faang.user_service.validator.participationServiceValidator.EventParticipationValidator;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventParticipationServiceTest {
    @Mock
    EventParticipationValidator eventParticipationValidator;

    @Mock
    private EventParticipationRepository eventParticipationRepository;

    @InjectMocks
    private EventParticipationService eventParticipationService;

    @Mock
    private UserMapper userMapper;

    @Test
    @DisplayName("registerParticipant is done")
    public void testPositiveRegisterParticipant() {
        long eventId = 2L;
        User user = createTestUser();

        eventParticipationService.registerParticipant(eventId, user.getId());

        Mockito.verify(eventParticipationRepository, Mockito.times(1))
                .register(eventId, user.getId());
    }

    @Test
    @DisplayName("registerParticipant throws exception")
    public void registerParticipantThrowsExceptionIfAlreadyRegistered() {
        long eventId = 1L;
        User user = createTestUser();
        Mockito.when(eventParticipationValidator.isUserRegistered(Mockito.anyLong(),
                Mockito.anyLong(), Mockito.eq(eventParticipationRepository))).thenReturn(true);

        Assert.assertThrows(IllegalArgumentException.class, () ->
                eventParticipationService.registerParticipant(eventId, user.getId()));
    }

    @Test
    @DisplayName("unregisterParticipant throws exception")
    public void unregisterParticipantThrowsExceptionIfNotRegistered() {
        User exitingUser = createTestUser();
        long eventId = 2L;

        when(eventParticipationValidator.isUserRegistered(eventId, exitingUser.getId(),
                eventParticipationRepository)).thenReturn(false);

        Assert.assertThrows(IllegalArgumentException.class,
                () -> eventParticipationService.unregisterParticipant(eventId, exitingUser.getId()));
    }

    @Test
    @DisplayName("unregisterParticipant is done")
    public void testPositiveUnregisterUser() {
        User exitingUser = createTestUser();
        long eventId = 1L;

        Mockito.when(eventParticipationValidator.isUserRegistered(Mockito.anyLong(), Mockito.eq(exitingUser.getId()),
                Mockito.any(EventParticipationRepository.class))).thenReturn(true);

        eventParticipationService.unregisterParticipant(eventId, exitingUser.getId());

        Mockito.verify(eventParticipationRepository, Mockito.times(1))
                .unregister(eventId, exitingUser.getId());

    }

    @Test
    @DisplayName("getParticipant")
    public void getParticipantsListReturnsListOfUserDto() {
        long eventId = 1L;
        User exitingUser = createTestUser();
        UserDto expectedDto = createDto();

        List<User> expectedUsers = Collections.singletonList(exitingUser);
        List<UserDto> expectedDtos = Collections.singletonList(expectedDto);

        Mockito.when(eventParticipationRepository.findAllParticipantsByEventId(Mockito.anyLong())).thenReturn(expectedUsers);

        Mockito.when(userMapper.toDto(exitingUser)).thenReturn(expectedDto);

        List<UserDto> actualDtos = eventParticipationService.getParticipantsList(eventId);

        Mockito.verify(eventParticipationRepository, Mockito.times(1)).findAllParticipantsByEventId(eventId);
        Mockito.verify(userMapper, Mockito.times(1)).toDto(exitingUser);

        Assertions.assertEquals(expectedDtos.size(), actualDtos.size());
        Assertions.assertEquals(expectedDtos, actualDtos);
    }

    @Test
    public void mustReturnCorrectCountParticipants() {
        int expectedCount = 5;
        long eventId = 1L;

        when(eventParticipationService.getParticipantsCount(eventId)).thenReturn(expectedCount);

        int actualResult = eventParticipationService.getParticipantsCount(eventId);

        Mockito.verify(eventParticipationRepository, Mockito.times(1)).countParticipants(eventId);

        Assertions.assertEquals(expectedCount, actualResult);
    }

    private User createTestUser() {
        return User.builder().id(2L).username("asd").email("123@123").build();
    }

    private UserDto createDto() {
        return UserDto.builder().id(1L).username("Aleksey").email("123@123").build();
    }
}