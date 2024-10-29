package school.faang.user_service.validator.recommendation;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.RecommendationRequestDto;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.recommendation.RecommendationRequestRepository;
import school.faang.user_service.repository.recommendation.SkillRequestRepository;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RecommendationRequestValidator {
    private static final int LIMIT_MONTHS = 6;
    private static final int LIMIT_DAYS = 30 * LIMIT_MONTHS;

    private final UserRepository userRepository;
    private final RecommendationRequestRepository recommendationRequestRepository;
    private final SkillRequestRepository skillRequestRepository;

    public void validateRecommendationRequest(RecommendationRequestDto recommendationRequestDto) {
        validateUsers(recommendationRequestDto);
        validateRequestPeriod(recommendationRequestDto);
        validateSkills(recommendationRequestDto);
    }

    private void validateUsers(@NotNull RecommendationRequestDto recommendationRequestDto) {
        if (!userRepository.existsById(recommendationRequestDto.getRequesterId())) {
            throw new IllegalArgumentException("Requester does not found");
        }
        if (!userRepository.existsById(recommendationRequestDto.getReceiverId())) {
            throw new IllegalArgumentException("Receiver does not found");
        }
    }

    private void validateRequestPeriod(@NotNull RecommendationRequestDto recommendationRequestDto) {
        Optional<RecommendationRequest> lastRecommendationRequest = recommendationRequestRepository
                .findLatestPendingRequest(recommendationRequestDto.getRequesterId(), recommendationRequestDto.getReceiverId());
        if (lastRecommendationRequest.isPresent()) {
            if (Duration.between(
                    lastRecommendationRequest.get().getCreatedAt(),
                    recommendationRequestDto.getCreatedAt()).toDays() < LIMIT_DAYS) {
                throw new IllegalArgumentException("Request period too short");
            }
        }
    }

    private void validateSkills(@NotNull RecommendationRequestDto recommendationRequestDto) {
        if (recommendationRequestDto.getSkillIds().stream().anyMatch(skillRequestRepository::existsById)) {
            throw new IllegalArgumentException("Skill already requested");
        }
    }
}
