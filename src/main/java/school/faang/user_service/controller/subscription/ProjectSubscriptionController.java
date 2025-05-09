package school.faang.user_service.controller.subscription;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.project.ProjectDto;
import school.faang.user_service.service.subscription.ProjectSubscriptionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@Validated
public class ProjectSubscriptionController {

    private final ProjectSubscriptionService subscriptionService;

    @PostMapping("/users/{userId}/projects")
    public void followProject(@PathVariable @Positive long userId,
                              @RequestBody ProjectDto projectDto) {
        subscriptionService.followProject(userId, projectDto);
    }

    @PostMapping("/api/v1/subscriptions/projects/{projectId}")
    public void followProjectByProjectId(@RequestHeader("x-user-id") long userId,
                                         @PathVariable @Positive long projectId) {
        subscriptionService.followProjectByProjectId(userId, projectId);
    }

    @GetMapping("projects/subscribers/{projectId}")
    public List<Long> getProjectSubscriptions(@PathVariable @Positive long projectId) {
        return subscriptionService.getProjectSubscriptions(projectId);
    }
}