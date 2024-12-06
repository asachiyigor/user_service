package school.faang.user_service.dto.analyticsevent;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchAppearanceEvent {
    private Long requesterId;
    private Long foundUserId;
}