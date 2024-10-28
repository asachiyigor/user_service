package school.faang.user_service.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.RecommendationRequestDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.entity.recommendation.SkillRequest;
import school.faang.user_service.mapper.RecommendationRequestMapper;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.recommendation.RecommendationRequestRepository;
import school.faang.user_service.repository.recommendation.SkillRequestRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendationRequestService {
    private static final int LIMIT_MONTHS_RECOMMENDATION_REQUEST = 6;
    private static final int LIMIT_DAYS = 30 * LIMIT_MONTHS_RECOMMENDATION_REQUEST;

    private final RecommendationRequestRepository recommendationRequestRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final SkillRequestRepository skillRequestRepository;
    private final RecommendationRequestMapper recommendationRequestMapper;

    public boolean isValidRequest(RecommendationRequestDto recommendationRequestDto) {
        return recommendationRequestDto != null && !recommendationRequestDto.getMessage().isEmpty();
    }

    public RecommendationRequestDto create(RecommendationRequestDto recommendationRequestDto) {

        RecommendationRequest recommendationRequest = recommendationRequestRepository
                .findById(recommendationRequestDto.getId())
                .orElse(null);
        if (recommendationRequest != null) {
            return recommendationRequestDto;
        }

        long requesterId = recommendationRequestDto.getRequesterId();
        long receiverId = recommendationRequestDto.getReceiverId();
        User requester = userRepository.findById(requesterId).orElse(null);
        User receiver = userRepository.findById(receiverId).orElse(null);
        if (requester == null || receiver == null) {
            return null;
        }

        RecommendationRequest lastRecommendationRequest = recommendationRequestRepository
                .findLatestPendingRequest(requesterId, receiverId)
                .orElse(null);
        if (lastRecommendationRequest != null) {
            LocalDateTime lastTimeRequest = lastRecommendationRequest.getCreatedAt();
            if (Duration.between(lastTimeRequest, LocalDateTime.now()).toDays() <= LIMIT_DAYS) {
                return null;
            }
        }

        if (skillRepository.countExisting(recommendationRequestDto.getSkillIds()) == 0) {
            return null;
        }

        RecommendationRequest recommendationRequestEntity = recommendationRequestMapper.toEntity(recommendationRequestDto);
        long idRecommendationRequest = recommendationRequestEntity.getId();
        List<SkillRequest> skillRequests = new ArrayList<>();
        skillRepository.findAllById(recommendationRequestDto.getSkillIds())
                .forEach(skill -> skillRequests.add(skillRequestRepository.create(idRecommendationRequest, skill.getId())));

        recommendationRequest = recommendationRequestRepository.create(recommendationRequestEntity.getMessage(),
                recommendationRequestEntity.getStatus(),
                skillRequests,
                requester,
                receiver);

        return recommendationRequestMapper.toDto(recommendationRequest);
    }
}