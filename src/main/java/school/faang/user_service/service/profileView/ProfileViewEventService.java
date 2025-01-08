package school.faang.user_service.service.profileView;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import school.faang.user_service.dto.profileView.ProfileViewEventDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.event.Event;
import school.faang.user_service.publisher.profileView.ProfileViewEventPublisher;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Validated
public class ProfileViewEventService {
    private final ProfileViewEventPublisher profileViewEventPublisher;

    public void viewUserProfile(Long userId, Long userIdViewing) {
        ProfileViewEventDto profileViewEventDto = new ProfileViewEventDto(userId, userIdViewing, LocalDateTime.now().toString());
        profileViewEventPublisher.publish(profileViewEventDto);
    }
}
