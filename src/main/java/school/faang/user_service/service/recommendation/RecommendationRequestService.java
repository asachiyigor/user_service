package school.faang.user_service.service.recommendation;

import org.springframework.stereotype.Component;
import school.faang.user_service.dto.recommendation.RecommendationRequestDto;
import school.faang.user_service.dto.recommendation.RejectionDto;
import school.faang.user_service.dto.recommendation.RequestFilterDto;

import java.util.List;

@Component
public class RecommendationRequestService {
    public RecommendationRequestDto create(RecommendationRequestDto recommendationRequest) {
        return null;
    }

    public List<RecommendationRequestDto> getRequests(RequestFilterDto filterDto) {
        return null;
    }

    public RecommendationRequestDto getRequest(Long id) {
        return null;
    }

    public RejectionDto rejectRequest(Long id, RejectionDto rejectionDto) {
        return null;
    }
}
