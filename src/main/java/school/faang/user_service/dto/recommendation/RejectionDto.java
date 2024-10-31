package school.faang.user_service.dto.recommendation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.recommendation.RecommendationRequest;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectionDto {
    private String reason;
    private RequestStatus status;
}
