package school.faang.user_service.service;

import school.faang.user_service.dto.RecommendationRequestDto;
import school.faang.user_service.entity.recommendation.RecommendationRequest;

public interface RecommendationRequestService {
    RecommendationRequestDto create(RecommendationRequestDto recommendationRequestDto);

    void saveSkillRequests(RecommendationRequestDto recommendationRequestDto, RecommendationRequest recommendationRequest);
}
