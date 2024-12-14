package school.faang.user_service.dto.event;

import lombok.Builder;

@Builder
public record UserProfileEvent(
        long viewerId,
        long viewedId
) {
}
