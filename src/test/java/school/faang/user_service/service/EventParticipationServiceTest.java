package school.faang.user_service.service;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import school.faang.user_service.dto.UserDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.mapper.UserMapper;
import school.faang.user_service.repository.event.EventParticipationRepository;
import school.faang.user_service.service.event.EventParticipationService;

import java.util.Collections;
import java.util.List;

public class EventParticipationServiceTest {

    @Mock
    private EventParticipationRepository eventParticipationRepository;

    @InjectMocks
    private EventParticipationService participationService;

    @Mock
    private UserMapper userMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("registerParticipant is done")
    public void registerParticipantIsDone() {
        participationService.registerParticipant(2, 2);
        Mockito.verify(eventParticipationRepository, Mockito.times(1))
                .register(2, 2);
    }

    @Test
    @DisplayName("registerParticipant throws exception")
    public void registerParticipantThrowsExceptionIfAlreadyRegistered() {
        User exitingsUser = User.builder()
                .id(1L)
                .build();

        Mockito.when(eventParticipationRepository.findAllParticipantsByEventId(1))
                .thenReturn(Collections.singletonList(exitingsUser));

        Assert.assertThrows(IllegalArgumentException.class,
                () -> participationService.registerParticipant(1, 1));
    }

    @Test
    @DisplayName("unregisterParticipant throws exception")
    public void unregisterParticipantThrowsExceptionIfNotRegistered() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> participationService.unregisterParticipant(2, 2));
    }

    @Test
    @DisplayName("unregisterParticipant is done")
    public void unregisterParticipantIsDone() {
        User exitingUser = User.builder()
                .id(2L)
                .build();

        Mockito.when(eventParticipationRepository.findAllParticipantsByEventId(1))
                .thenReturn(Collections.singletonList(exitingUser));

        participationService.unregisterParticipant(1, 2);

        Mockito.verify(eventParticipationRepository, Mockito.times(1))
                .unregister(1, 2);
    }

    @Test
    @DisplayName("getParticipant")
    public void getParticipantsListReturnsListOfUserDto() {
        User user = User.builder().id(1L).username("Aleksey").email("123@123").build();
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("Aleksey")
                .email("123@123")
                .build();

        Mockito.when(eventParticipationRepository.findAllParticipantsByEventId(1))
                .thenReturn(Collections.singletonList(user));
        Mockito.when(userMapper.toDto(user)).thenReturn(userDto);

        List<UserDto> result = participationService.getParticipantsList(1L);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(userDto, result.get(0));

        Mockito.verify(eventParticipationRepository, Mockito.times(1)).findAllParticipantsByEventId(1L);
        Mockito.verify(userMapper, Mockito.times(1)).toDto(user);

    }


}