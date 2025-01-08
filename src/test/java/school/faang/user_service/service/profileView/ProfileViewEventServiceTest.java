package school.faang.user_service.service.profileView;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import school.faang.user_service.dto.profileView.ProfileViewEventDto;
import school.faang.user_service.publisher.profileView.ProfileViewEventPublisher;

import static org.mockito.Mockito.*;

public class ProfileViewEventServiceTest {
    @Mock
    private ProfileViewEventPublisher profileViewEventPublisher;

    @InjectMocks
    private ProfileViewEventService profileViewEventService;

    ProfileViewEventServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void viewUserProfile_ShouldPublishEvent() {
        Long userId = 1L;
        Long userIdViewing = 2L;

        profileViewEventService.viewUserProfile(userId, userIdViewing);
        verify(profileViewEventPublisher, times(1)).publish(any(ProfileViewEventDto.class));
    }
}
