package school.faang.user_service.service.validatorTests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.entity.User;
import school.faang.user_service.repository.event.EventParticipationRepository;
import school.faang.user_service.validator.participationServiceValidator.EventParticipationValidator;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventParticipationValidatorTest {

    @InjectMocks
    EventParticipationValidator eventParticipationValidator;

    @Mock
    EventParticipationRepository eventParticipationRepository;

    @Test
    @DisplayName("Positive test: must return true when user is registered")
    public void mustReturnTrueWhenUserIsRegistered() {
        long eventId = 1L;
        User expectedUser = createUser();

        when(eventParticipationRepository.findAllParticipantsByEventId(eventId))
                .thenReturn(Collections.singletonList(expectedUser));

        assertTrue(eventParticipationValidator.isUserRegistered(eventId, expectedUser.getId(),
                eventParticipationRepository));
    }

    @Test
    @DisplayName("Negative test: must return false when user is not registered")
    public void mustReturnTrueWhenUserIsNotRegistered() {
        long eventId = 1L;
        User exitingUser = createUser();

        when(eventParticipationRepository.findAllParticipantsByEventId(eventId)).thenReturn(Collections.emptyList());

        assertFalse(eventParticipationValidator.isUserRegistered(1L, exitingUser.getId(),
                eventParticipationRepository));
    }

    private User createUser() {
        return User.builder().id(2L).username("testUser").email("test@test.com").build();
    }
}
