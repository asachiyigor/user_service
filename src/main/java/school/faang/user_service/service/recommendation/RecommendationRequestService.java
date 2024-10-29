package school.faang.user_service.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.recommendation.RecommendationRequestDto;
import school.faang.user_service.dto.recommendation.RejectionDto;
import school.faang.user_service.dto.recommendation.RequestFilterDto;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.entity.recommendation.SkillRequest;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.recommandation.RecommendationRequestMapper;
import school.faang.user_service.mapper.recommandation.RecommendationRequestRejectionMapper;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.recommendation.RecommendationRequestRepository;
import school.faang.user_service.repository.recommendation.SkillRequestRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class RecommendationRequestService {
    private static final int REQUESTS_PERIOD_DAYS = 160;
    private static final RequestStatus PENDING = RequestStatus.PENDING;

    private final RecommendationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final RecommendationRequestMapper requestMapper;
    private final SkillRequestRepository skillRequestRepository;
    private final RecommendationRequestRejectionMapper requestRejectionMapper;
    private final List<Filter<RecommendationRequest>> filters;

    public RecommendationRequestDto create(RecommendationRequestDto requestDto) {
        validateUsersExistence(requestDto);
        validateRequestPeriod(requestDto);
        validateSkillRequestsExistence(requestDto);

        RecommendationRequest requestEntity = requestMapper.toEntity(requestDto);
        long idRequest = requestEntity.getId();
        List<SkillRequest> skills = requestEntity.getSkills();
        requestDto.getSkillsIds().forEach(skillId -> {
            SkillRequest skillRequest = skillRequestRepository.create(idRequest, skillId);
            skills.add(skillRequest);
        });
        RecommendationRequest recommendationRequest = requestRepository.save(requestEntity);
        return requestMapper.toDto(recommendationRequest);
    }

    public List<RecommendationRequestDto> getRequests(RequestFilterDto filterDto) {
        Stream<RecommendationRequest> requestStream = requestRepository.findAll().stream();
        for (Filter<RecommendationRequest> filter : filters) {
            if (filter.isApplicable(filterDto)) {
                requestStream = filter.apply(requestStream, filterDto);
            }
        }
        return requestStream
                .distinct()
                .map(requestMapper::toDto)
                .toList();
    }

    public RecommendationRequestDto getRequest(Long id) {
        RecommendationRequest request = getRecommendationRequestFromBD(id);
        return requestMapper.toDto(request);
    }

    public RejectionDto rejectRequest(Long id, RejectionDto rejectionDto) {
        RecommendationRequest request = getRecommendationRequestFromBD(id);
        if (!request.getStatus().equals(PENDING)) {
            throw new DataValidationException("Request id: " + id + " cannot be rejected");
        }
        RecommendationRequest requestEntity = requestRejectionMapper.toEntity(rejectionDto);
        return requestRejectionMapper.toDto(requestRepository.save(requestEntity));
    }

    private void validateUsersExistence(@NotNull RecommendationRequestDto requestDto) {
        if (!userRepository.existsById(requestDto.getRequesterId())) {
            throw new DataValidationException("Requester id: " + requestDto.getRequesterId() + " not found in database");
        }
        if (!userRepository.existsById(requestDto.getReceiverId())) {
            throw new DataValidationException("Receiver id: " + requestDto.getReceiverId() + " not found in database");
        }
        if (requestDto.getReceiverId().equals(requestDto.getRequesterId())) {
            throw new DataValidationException("Requester and receiver cannot be the same user");
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
                throw new DataValidationException("Request period is too short: " + durationDays + " days");
            }
        }
    }

    private void validateSkillRequestsExistence(@NotNull RecommendationRequestDto requestDto) {
        List<Long> skillIdsNotExist = new ArrayList<>(requestDto.getSkillsIds());
        requestDto.getSkillsIds().forEach(skillId -> {
            if (!skillRepository.existsById(skillId)) {
                skillIdsNotExist.add(skillId);
            }
        });
        if (!skillIdsNotExist.isEmpty()) {
            throw new DataValidationException("Skills: " + skillIdsNotExist + " not found in database");
        }
    }

    private RecommendationRequest getRecommendationRequestFromBD(Long id) {
        return requestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Request id: " + id
                + " not found in database: "));
    }
}
