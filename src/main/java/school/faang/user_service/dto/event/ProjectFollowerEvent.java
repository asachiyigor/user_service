package school.faang.user_service.dto.event;

import lombok.Builder;

@Builder
public record ProjectFollowerEvent(
        long followerId,
        long projectId,
        long ownerId
) {
}
