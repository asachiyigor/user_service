package school.faang.user_service.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.dto.recommendation.RecommendationRequestDto;
import school.faang.user_service.dto.recommendation.RejectionDto;
import school.faang.user_service.dto.recommendation.RequestFilterDto;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.entity.recommendation.SkillRequest;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.recommandation.RecommendationRequestMapper;
import school.faang.user_service.mapper.recommandation.RecommendationRequestRejectionMapper;
import school.faang.user_service.repository.recommendation.RecommendationRequestRepository;
import school.faang.user_service.service.skil.SkillRequestService;
import school.faang.user_service.service.skil.SkillService;
import school.faang.user_service.service.user.UserService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class RecommendationRequestService {
    private static final int REQUESTS_PERIOD_DAYS = 180;
    private static final RequestStatus PENDING = RequestStatus.PENDING;

    private final RecommendationRequestRepository requestRepository;
    private final RecommendationRequestMapper requestMapper;
    private final RecommendationRequestRejectionMapper requestRejectionMapper;
    private final List<Filter<RecommendationRequest>> filters;
    private final UserService userService;
    private final SkillRequestService skillRequestService;
    private final SkillService skillService;

    @Transactional
    public RecommendationRequestDto create(@NotNull RecommendationRequestDto requestDto) {
        validateMessageIsEmpty(requestDto.getMessage());
        validateRequesterReceiverNotSameUser(requestDto.getRequesterId(), requestDto.getReceiverId());
        validateUserExist(requestDto.getRequesterId());
        validateUserExist(requestDto.getReceiverId());
        validateRequestPeriod(requestDto.getRequesterId(), requestDto.getReceiverId());
        validateSkillsExist(requestDto.getSkillsIds());

        RecommendationRequest requestEntity = requestMapper.toEntity(requestDto);
        requestEntity.setRequester(userService.getUser(requestDto.getRequesterId()));
        requestEntity.setReceiver(userService.getUser(requestDto.getReceiverId()));
        requestEntity.setStatus(PENDING);
        RecommendationRequest request = requestRepository.save(requestEntity);
        request.setSkills(new ArrayList<>());
        List<Skill> skills = skillService.findAll(requestDto.getSkillsIds());
        skills.forEach(skill -> {
            SkillRequest skillRequest = skillRequestService.create(request, skill);
            request.getSkills().add(skillRequest);
        });

        return requestMapper.toDto(requestRepository.save(request));
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
        return requestMapper.toDto(findRequestInDB(id));
    }

    @Transactional
    public RejectionDto rejectRequest(Long id, @NotNull RejectionDto rejectionDto) {
        RecommendationRequest request = findRequestInDB(id);
        validateRequestOnStatusPending(request);

        request.setRejectionReason(rejectionDto.getReason());
        request.setStatus(rejectionDto.getStatus());
        return requestRejectionMapper.toDto(requestRepository.save(request));
    }

    private void validateRequestOnStatusPending(@NotNull RecommendationRequest request) {
        if (!request.getStatus().equals(PENDING)) {
            throw new DataValidationException("Request cannot be rejected: id=" + request.getId());
        }
    }

    private void validateMessageIsEmpty(@NotNull String message) {
        if (message.isEmpty()) {
            throw new DataValidationException("Recommendation request message cannot be empty");
        }
    }

    private void validateRequesterReceiverNotSameUser(@NotNull Long requesterId, Long receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new DataValidationException("Requester and receiver cannot be the same user");
        }
    }

    private void validateUserExist(Long userId) {
        if (!userService.isUserExistInDB(userId)) {
            throw new DataValidationException("User not found in database: id=" + userId);
        }
    }

    private void validateRequestPeriod(Long requesterId, Long receiverId) {
        Optional<RecommendationRequest> request = requestRepository.findLatestPendingRequest(requesterId, receiverId);
        if (request.isPresent()) {
            long durationDays = Duration.between(request.get().getCreatedAt(), LocalDateTime.now()).toDays();
            if (durationDays <= REQUESTS_PERIOD_DAYS) {
                throw new DataValidationException("Request period is too short: " + durationDays + " days");
            }
        }
    }

    private void validateSkillsExist(List<Long> skillIds) {
        List<Long> existingSkillIds = skillService.findExistingSkills(skillIds);
        List<Long> missingSkills = skillIds.stream()
                .filter(skillId -> !existingSkillIds.contains(skillId))
                .toList();
        if (!missingSkills.isEmpty()) {
            throw new DataValidationException("Skills not found in database: ids" + missingSkills);
        }
    }

    @NotNull
    private RecommendationRequest findRequestInDB(Long id) {
        Optional<RecommendationRequest> requestOptional = requestRepository.findById(id);
        if (requestOptional.isPresent()) {
            return requestOptional.get();
        } else {
            throw new RuntimeException("Request not found: id=" + id);
        }
    }
}
