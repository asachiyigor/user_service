package school.faang.user_service.validator.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import school.faang.user_service.repository.ProjectSubscriptionRepository;

@Component
@RequiredArgsConstructor
public class ProjectSubscriptionValidator {
    private final ProjectSubscriptionRepository projectSubscriptionRepository;

    public boolean isAlreadySubscribed(long followerId, long projectId) {
        return projectSubscriptionRepository.existsByFollowerIdAndProjectId(followerId, projectId);
    }
}
