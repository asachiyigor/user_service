package school.faang.user_service.controller.subscription;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.project.ProjectDto;
import school.faang.user_service.service.ProjectSubscriptionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
public class ProjectSubscriptionController {

    private final ProjectSubscriptionService subscriptionService;

    @PostMapping("/{userId}/projects")
    public void followProject(@PathVariable @Positive long userId,
                              @RequestBody ProjectDto projectDto) {
        subscriptionService.followProject(userId, projectDto);
    }
}