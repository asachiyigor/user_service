package school.faang.user_service.dto.analyticsevent;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SearchAppearanceEvent {
    private Long requesterId;
    private Long foundUserId;
    private LocalDateTime requestedAt;
}