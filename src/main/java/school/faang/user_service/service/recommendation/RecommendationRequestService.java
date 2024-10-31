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
import school.faang.user_service.repository.SkillRepository;
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
    private final SkillRepository skillRepository;
    private final UserService userService;
    private final SkillRequestService skillRequestService;
    private final SkillService skillService;

    @Transactional
    public RecommendationRequestDto create(RecommendationRequestDto requestDto) {
        validateRequestDto(requestDto);

        RecommendationRequest requestEntity = requestMapper.toEntity(requestDto);
        requestEntity.setRequester(userService.getUser(requestDto.getRequesterId()));
        requestEntity.setReceiver(userService.getUser(requestDto.getReceiverId()));
        requestEntity.setStatus(PENDING);
        RecommendationRequest request = requestRepository.save(requestEntity);
        request.setSkills(new ArrayList<>());
        for (Long skillId : requestDto.getSkillsIds()) {
            Skill skill = skillService.getSkill(skillId);
            SkillRequest skillRequest = skillRequestService.create(request, skill);
            request.getSkills().add(skillRequest);
        }

        return requestMapper.toDto(request);
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
    public RejectionDto rejectRequest(Long id, RejectionDto rejectionDto) {
        RecommendationRequest request = findRequestInDB(id);
        if (!request.getStatus().equals(PENDING)) {
            throw new DataValidationException("Request id: " + id + " cannot be rejected");
        }
        request.setRejectionReason(rejectionDto.getReason());
        request.setStatus(rejectionDto.getStatus());
        return requestRejectionMapper.toDto(requestRepository.save(request));
    }

    @NotNull
    private RecommendationRequest findRequestInDB(Long id) {
        Optional<RecommendationRequest> requestOptional = requestRepository.findById(id);
        if (requestOptional.isPresent()) {
            return requestOptional.get();
        } else {
            throw new RuntimeException("Request id: " + id + " not found");
        }
    }

    private void validateRequestDto(@NotNull RecommendationRequestDto requestDto) {
        validateMessageIsEmpty(requestDto.getMessage());
        validateRequesterNotEqualReceiver(requestDto.getRequesterId(), requestDto.getReceiverId());
        validateUserExist(requestDto.getRequesterId());
        validateUserExist(requestDto.getReceiverId());
        validateRequestPeriod(requestDto.getRequesterId(), requestDto.getReceiverId());
        validateSkillsExist(requestDto.getSkillsIds());
    }

    private void validateMessageIsEmpty(@NotNull String message) {
        if (message.isEmpty()) {
            throw new DataValidationException("Recommendation request message cannot be empty");
        }
    }

    private void validateRequesterNotEqualReceiver(@NotNull Long requesterId, Long receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new DataValidationException("Requester and receiver cannot be the same user");
        }
    }

    private void validateUserExist(Long userId) {
        if (!userService.isUserExistInDB(userId)) {
            throw new DataValidationException("User id: { " + userId + " } not found in database");
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

    public void validateSkillsExist(List<Long> skillIds) {
        List<Long> existingSkillIds = skillRepository.findExistingSkillIdsInDB(skillIds);
        List<Long> missingSkills = skillIds.stream()
                .filter(skillId -> !existingSkillIds.contains(skillId))
                .toList();
        if (!missingSkills.isEmpty()) {
            throw new DataValidationException("Skills: " + missingSkills + " not found in database");
        }
    }
}
