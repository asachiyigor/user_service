package school.faang.user_service.dto.project;

import lombok.Builder;

@Builder
public record ProjectDto(
        long projectId,
        long ownerId
) {
}
