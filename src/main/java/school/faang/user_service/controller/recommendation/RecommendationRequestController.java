package school.faang.user_service.controller.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.RecommendationRequestDto;
import school.faang.user_service.service.recommendation.RecommendationRequestService;

@Component
@RequiredArgsConstructor
public class RecommendationRequestController {

    private final RecommendationRequestService recommendationRequestService;

    public RecommendationRequestDto requestRecommendation(RecommendationRequestDto recommendationRequestDto) {
        if (!recommendationRequestService.isValidRequest(recommendationRequestDto)) {
            return null;
        }
        return recommendationRequestService.create(recommendationRequestDto);
    }
}
