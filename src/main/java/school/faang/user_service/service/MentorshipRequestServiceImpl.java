package school.faang.user_service.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.MentorshipRequestDto;
import school.faang.user_service.dto.RequestFilterDto;
import school.faang.user_service.entity.MentorshipRequest;
import school.faang.user_service.mapper.MentorshipRequestMapper;
import school.faang.user_service.repository.mentorship.MentorshipRequestRepository;

@Service
@RequiredArgsConstructor
public class MentorshipRequestServiceImpl implements MentorshipRequestService {

  private static final int VALID_MONTHS = 3;
  private static final String ERROR_USERS_VALIDATION = "Not allowed, requester and receiver must be registered in Mentorship Request Database";
  private static final String ERROR_REQUEST_OLD = "Not allowed more than one request per valid period";
  private static final String ERROR_SELF_REQUEST = "Not allowed to send request to self";
  private final MentorshipRequestRepository mentorshipRequestRepository;
  private final MentorshipRequestMapper mentorshipRequestMapper;

  public MentorshipRequestDto requestMentorship(MentorshipRequestDto mentorshipRequestDto) {
    validateMentorshipRequest(mentorshipRequestDto);

    MentorshipRequest mentorshipRequest = mentorshipRequestRepository.create(
        mentorshipRequestDto.getRequesterId(),
        mentorshipRequestDto.getReceiverId(), mentorshipRequestDto.getDescription());

    return mentorshipRequestMapper.toDto(mentorshipRequest);
  }

  @Override
  public List<MentorshipRequestDto> getRequests(RequestFilterDto requestFilterDto) {
    return mentorshipRequestRepository.findAll().stream()
        .map(mentorshipRequestMapper::toDto)
        .filter(mentorshipRequestDto -> mentorshipRequestDto.getDescription()
            .contains(requestFilterDto.getDescription()))
        .filter(mentorshipRequestDto -> mentorshipRequestDto.getRequesterId()
            .equals(requestFilterDto.getRequesterId()))
        .filter(mentorshipRequestDto -> mentorshipRequestDto.getReceiverId()
            .equals(requestFilterDto.getReceiverId()))
        .filter(mentorshipRequestDto -> mentorshipRequestDto.getStatus()
            .equals(requestFilterDto.getStatus()))
        .toList();
  }

  private void validateMentorshipRequest(MentorshipRequestDto mentorshipRequestDto) {
//    if (!areUsersValid(mentorshipRequestDto)) {
//      throw new RuntimeException(ERROR_USERS_VALIDATION);
//    }

    if (!isRequestOlderThanValidMonths(mentorshipRequestDto)) {
      throw new RuntimeException(ERROR_REQUEST_OLD);
    }

    if (!isNotSelfRequest(mentorshipRequestDto)) {
      throw new RuntimeException(ERROR_SELF_REQUEST);
    }
  }

  private boolean isNotSelfRequest(MentorshipRequestDto mentorshipRequestDto) {
    return !mentorshipRequestDto.getRequesterId().equals(mentorshipRequestDto.getReceiverId());
  }

  private boolean isRequestOlderThanValidMonths(MentorshipRequestDto mentorshipRequestDto) {
    LocalDateTime thresholdDate = LocalDateTime.now().minusMonths(VALID_MONTHS);
    return mentorshipRequestRepository.findLatestRequest(mentorshipRequestDto.getRequesterId(),
            mentorshipRequestDto.getReceiverId())
        .filter(mentorshipRequest -> mentorshipRequest.getCreatedAt().isBefore(thresholdDate))
        .isPresent();
  }

  private boolean areUsersValid(MentorshipRequestDto mentorshipRequestDto) {
    return mentorshipRequestRepository.existAcceptedRequest(mentorshipRequestDto.getRequesterId(),
        mentorshipRequestDto.getReceiverId());
  }

}
