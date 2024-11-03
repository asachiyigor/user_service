package school.faang.user_service.service.mentorship;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.mentorship.MentorshipRequestDto;
import school.faang.user_service.dto.mentorship.RequestFilterDto;
import school.faang.user_service.entity.MentorshipRequest;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.exception.mentorship.DataValidationException;
import school.faang.user_service.mapper.mentorship.MentorshipRequestMapper;
import school.faang.user_service.repository.UserRepository;
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
  private final UserRepository userRepository;

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
        .filter(mentorshipRequestDto -> descriptionFilter(mentorshipRequestDto, requestFilterDto))
        .filter(mentorshipRequestDto -> requesterFilter(mentorshipRequestDto, requestFilterDto))
        .filter(mentorshipRequestDto -> receiverFilter(mentorshipRequestDto, requestFilterDto))
        .filter(mentorshipRequestDto -> statusFilter(mentorshipRequestDto, requestFilterDto))
        .toList();
  }

  @Override
  public MentorshipRequest findById(long requestId) {
    MentorshipRequest request = mentorshipRequestRepository.findById(requestId)
        .orElseThrow(() -> new DataValidationException("Mentorship request not found"));
    return request;
  }

  @Override
  public MentorshipRequestDto acceptRequest(long id) {
    MentorshipRequest request = findById(id);
    if (request.getStatus() != RequestStatus.ACCEPTED) {
      request.setStatus(RequestStatus.ACCEPTED);
      mentorshipRequestRepository.save(request);
      User requester = request.getRequester();
      User mentor = request.getReceiver();
      List<User> requesterMentors = request.getRequester()
          .getMentors();
      requesterMentors.add(mentor);
      userRepository.save(requester);
    } else {
      throw new DataValidationException("Receiver is mentor of the requester already!");
    }
    return mentorshipRequestMapper.toDto(request);
  }

  private boolean descriptionFilter(MentorshipRequestDto requestDto,
      RequestFilterDto filterDto) {
    return requestDto.getDescription() == null || requestDto.getDescription().toLowerCase()
        .contains(filterDto.getDescription().toLowerCase());
  }

  private boolean requesterFilter(MentorshipRequestDto requestDto,
      RequestFilterDto filterDto) {
    return requestDto.getRequesterId() == null || requestDto.getRequesterId()
        .equals(filterDto.getRequesterId());
  }

  private boolean receiverFilter(MentorshipRequestDto requestDto,
      RequestFilterDto filterDto) {
    return requestDto.getReceiverId() == null || requestDto.getReceiverId()
        .equals(filterDto.getReceiverId());
  }

  private boolean statusFilter(MentorshipRequestDto requestDto,
      RequestFilterDto filterDto) {
    return requestDto.getStatus() == null || requestDto.getStatus()
        .equals(filterDto.getStatus());
  }

  private void validateMentorshipRequest(MentorshipRequestDto mentorshipRequestDto) {
    if (!areUsersValid(mentorshipRequestDto)) {
      throw new DataValidationException(ERROR_USERS_VALIDATION);
    }

    if (isRequestEarlierThanValidMonths(mentorshipRequestDto)) {
      throw new DataValidationException(ERROR_REQUEST_OLD);
    }

    if (!isNotSelfRequest(mentorshipRequestDto)) {
      throw new DataValidationException(ERROR_SELF_REQUEST);
    }
  }

  private boolean isNotSelfRequest(MentorshipRequestDto mentorshipRequestDto) {
    return !mentorshipRequestDto.getRequesterId().equals(mentorshipRequestDto.getReceiverId());
  }

  private boolean isRequestEarlierThanValidMonths(MentorshipRequestDto mentorshipRequestDto) {
    LocalDateTime thresholdDate = LocalDateTime.now().minusMonths(VALID_MONTHS);
    return mentorshipRequestRepository.findLatestRequest(mentorshipRequestDto.getRequesterId(),
            mentorshipRequestDto.getReceiverId())
        .filter(mentorshipRequest -> mentorshipRequest.getCreatedAt().isAfter(thresholdDate))
        .isPresent();
  }

  private boolean areUsersValid(MentorshipRequestDto mentorshipRequestDto) {
    userRepository.findById(mentorshipRequestDto.getRequesterId())
        .orElseThrow(() -> new DataValidationException(ERROR_USERS_VALIDATION));
    userRepository.findById(mentorshipRequestDto.getReceiverId())
        .orElseThrow(() -> new DataValidationException(ERROR_USERS_VALIDATION));
    return true;
  }

}
