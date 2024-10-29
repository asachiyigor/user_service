package school.faang.user_service.service.impl.recommendation;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.RecommendationRequestDto;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.mapper.RecommendationRequestMapper;
import school.faang.user_service.repository.recommendation.RecommendationRequestRepository;
import school.faang.user_service.repository.recommendation.SkillRequestRepository;
import school.faang.user_service.service.RecommendationRequestService;
import school.faang.user_service.validator.recommendation.RecommendationRequestValidator;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendationRequestServiceImpl implements RecommendationRequestService {
    private final RecommendationRequestValidator requestValidator;
    private final RecommendationRequestMapper recommendationRequestMapper;
    private final RecommendationRequestRepository recommendationRequestRepository;
    private final SkillRequestRepository skillRequestRepository;

    public RecommendationRequestDto create(RecommendationRequestDto recommendationRequestDto) {
        requestValidator.validateRecommendationRequest(recommendationRequestDto);
        RecommendationRequest recommendationRequestEntity = recommendationRequestMapper.toEntity(recommendationRequestDto);
        RecommendationRequest recommendationRequest = recommendationRequestRepository.save(recommendationRequestEntity);
        saveSkillRequests(recommendationRequestDto, recommendationRequest);
        return recommendationRequestMapper.toDto(recommendationRequest);
    }

    @Override
    public void saveSkillRequests(@NotNull RecommendationRequestDto recommendationRequestDto, RecommendationRequest recommendationRequest) {
        List<Long> skillIds = recommendationRequestDto.getSkillIds();
        skillIds.forEach(skillId -> skillRequestRepository.create(recommendationRequest.getId(), skillId));
    }
}