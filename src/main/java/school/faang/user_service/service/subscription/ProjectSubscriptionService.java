package school.faang.user_service.service.subscription;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.event.ProjectFollowerEvent;
import school.faang.user_service.dto.project.ProjectDto;
import school.faang.user_service.puiblisher.followerEvent.ProjectFollowerEventPublisher;
import school.faang.user_service.repository.ProjectSubscriptionRepository;
import school.faang.user_service.validator.event.ProjectSubscriptionValidator;

import java.util.List;

@Slf4j
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
        } else {
            log.debug("Project with id: {} is already subscribed by user with id: {}", projectDto.projectId(), followerId);
        }
    }

    public void followProjectByProjectId(long followerId, long projectId) {
        if (!projectSubscriptionValidator.isAlreadySubscribed(followerId, projectId)) {
            projectSubscriptionRepository.followProject(followerId, projectId);
        } else {
            log.debug("Project with id: {} is already subscribed by user with id: {}", projectId, followerId);
        }
    }

    public List<Long> getProjectSubscriptions(long projectId) {
        return projectSubscriptionRepository.findProjectSubscribers(projectId); }
}