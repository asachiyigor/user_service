package school.faang.user_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.event.ProjectFollowerEvent;
import school.faang.user_service.dto.project.ProjectDto;
import school.faang.user_service.puiblisher.followerEvent.ProjectFollowerEventPublisher;
import school.faang.user_service.repository.ProjectSubscriptionRepository;
import school.faang.user_service.validator.event.ProjectSubscriptionValidator;

@Service
@RequiredArgsConstructor
public class ProjectSubscriptionService {

    private final ProjectSubscriptionRepository projectSubscriptionRepository;
    private final ProjectSubscriptionValidator projectSubscriptionValidator;
    private final ProjectFollowerEventPublisher projectFollowerEventPublisher;

    @Transactional
    public void followProject(long followerId, ProjectDto projectDto) {
        if (!projectSubscriptionValidator.isAlreadySubscribed(followerId, projectDto.projectId())) {
            projectSubscriptionRepository.followProject(followerId, projectDto.projectId());
            projectFollowerEventPublisher.publish(new ProjectFollowerEvent(followerId, projectDto.projectId(),
                    projectDto.ownerId()));
        }
    }
}
