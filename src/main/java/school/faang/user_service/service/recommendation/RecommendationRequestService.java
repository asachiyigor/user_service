package school.faang.user_service.service.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.recommendation.RecommendationRequestDto;
import school.faang.user_service.dto.recommendation.RejectionDto;
import school.faang.user_service.dto.recommendation.RequestFilterDto;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.recommendation.RecommendationRequestRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationRequestService {
    private static final int REQUESTS_PERIOD_DAYS = 160;

    private final RecommendationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public RecommendationRequestDto create(RecommendationRequestDto recommendationRequestDto) {
        validateUsersExistence(recommendationRequestDto);
        validateRequestPeriod(recommendationRequestDto);
        validateSkillRequestsExistence(recommendationRequestDto);

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

    private void validateUsersExistence(@NotNull RecommendationRequestDto requestDto) {
        if (!userRepository.existsById(requestDto.getRequesterId())) {
            log.error("RequesterId: {} not found in database", requestDto.getRequesterId());
            throw new IllegalArgumentException("Requester not found in database");
        }
        if (!userRepository.existsById(requestDto.getReceiverId())) {
            log.error("ReceiverId: {} not found in database", requestDto.getReceiverId());
            throw new IllegalArgumentException("Receiver not found in database");
        }
        if (requestDto.getReceiverId().equals(requestDto.getRequesterId())) {
            log.error("RequesterId: {} and receiverId {} cannot be the same user",
                    requestDto.getRequesterId(),
                    requestDto.getReceiverId()
            );
            throw new IllegalArgumentException("Requester and receiver cannot be the same user");
        }
    }

    private void validateRequestPeriod(@NotNull RecommendationRequestDto recommendationRequestDto) {
        RecommendationRequest recommendationRequest = requestRepository.findLatestPendingRequest(
                recommendationRequestDto.getRequesterId(),
                recommendationRequestDto.getReceiverId()
        ).orElse(null);
        if (recommendationRequest != null) {
            long durationDays = Duration.between(recommendationRequest.getCreatedAt(), LocalDateTime.now()).toDays();
            if (durationDays <= REQUESTS_PERIOD_DAYS) {
                log.error("Request period is too short: {} days", durationDays);
                throw new IllegalArgumentException("Request period is too short");
            }
        }
    }

    private void validateSkillRequestsExistence(@NotNull RecommendationRequestDto requestDto) {
        List<Long> skillIdsNotExist =  new ArrayList<>(requestDto.getSkillsIds());
        requestDto.getSkillsIds().forEach(skillId -> {
            if (!skillRepository.existsById(skillId)) {
                skillIdsNotExist.add(skillId);
            }
        });
        if (!skillIdsNotExist.isEmpty()) {
            log.error("SkillIds: {} not found in database", skillIdsNotExist);
            throw new IllegalArgumentException("Skills not found in database");
        }
    }
}
