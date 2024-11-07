package school.faang.user_service.service.mentorship;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.mentorship.MentorshipRequestDto;
import school.faang.user_service.dto.mentorship.RejectionDto;
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

  private final MentorshipRequestRepository mentorshipRequestRepository;
  private final MentorshipRequestMapper mentorshipRequestMapper;
  private final UserRepository userRepository;

  public static final int VALID_MONTHS = 3;
  public static final String ERROR_REQUESTER_IS_MISSING = "Requester in missing in DB";
  public static final String ERROR_RECEIVER_IS_MISSING = "Receiver is missing in DB";
  public static final String ERROR_EARLY_REQUEST = "Not allowed more than one request per valid period";
  public static final String ERROR_SELF_REQUEST = "Not allowed to send self-request";
  public static final String ERROR_ALREADY_ACCEPTED = "Receiver is mentor of the requester already!";

  @Override
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
    return mentorshipRequestRepository.findById(requestId)
        .orElseThrow(
            () -> new DataValidationException("Mentorship request id=" + requestId + " not found"));
  }

  @Override
  public MentorshipRequestDto acceptRequest(long id) {
    MentorshipRequest request = findById(id);
    if (request.getStatus() != RequestStatus.ACCEPTED) {
      request.setStatus(RequestStatus.ACCEPTED);
      mentorshipRequestRepository.save(request);
      User requester = request.getRequester();
      User mentor = request.getReceiver();
      List<User> requesterMentors = Optional.ofNullable(request.getRequester()
              .getMentors())
          .orElseGet(ArrayList::new);
      requesterMentors.add(mentor);
      userRepository.save(requester);
    } else {
      throw new DataValidationException(ERROR_ALREADY_ACCEPTED);
    }
    return mentorshipRequestMapper.toDto(request);
  }

  @Override
  public MentorshipRequestDto rejectRequest(long id, RejectionDto rejectionDto) {
    MentorshipRequest request = findById(id);
    if (request.getStatus() != RequestStatus.REJECTED) {
      request.setStatus(RequestStatus.REJECTED);
      request.setRejectionReason(rejectionDto.getRejectionReason());
      mentorshipRequestRepository.save(request);
    }
    return mentorshipRequestMapper.toDto(request);
  }

  private boolean descriptionFilter(MentorshipRequestDto requestDto,
      RequestFilterDto filterDto) {
    return filterDto.getDescription() == null || requestDto.getDescription().toLowerCase()
        .contains(filterDto.getDescription().toLowerCase());
  }

  private boolean requesterFilter(MentorshipRequestDto requestDto,
      RequestFilterDto filterDto) {
    return filterDto.getRequesterId() == null || requestDto.getRequesterId()
        .equals(filterDto.getRequesterId());
  }

  private boolean receiverFilter(MentorshipRequestDto requestDto,
      RequestFilterDto filterDto) {
    return filterDto.getReceiverId() == null || requestDto.getReceiverId()
        .equals(filterDto.getReceiverId());
  }

  private boolean statusFilter(MentorshipRequestDto requestDto,
      RequestFilterDto filterDto) {
    return filterDto.getStatus() == null || requestDto.getStatus()
        .equals(filterDto.getStatus());
  }

  private void validateMentorshipRequest(MentorshipRequestDto mentorshipRequestDto) {
    validateExistUsers(mentorshipRequestDto);
    validateTimePeriod(mentorshipRequestDto);
    validateNotSelfRequest(mentorshipRequestDto);
  }

  private void validateNotSelfRequest(MentorshipRequestDto mentorshipRequestDto) {
    if (Objects.equals(mentorshipRequestDto.getRequesterId(),
        mentorshipRequestDto.getReceiverId())) {
      throw new DataValidationException(ERROR_SELF_REQUEST);
    }
  }

  private void validateTimePeriod(MentorshipRequestDto mentorshipRequestDto) {
    LocalDateTime thresholdDate = LocalDateTime.now().minusMonths(VALID_MONTHS);
    mentorshipRequestRepository.findLatestRequest(mentorshipRequestDto.getRequesterId(),
            mentorshipRequestDto.getReceiverId())
        .ifPresent(mentorshipRequest -> {
          if (mentorshipRequest.getCreatedAt().isAfter(thresholdDate)) {
            throw new DataValidationException(ERROR_EARLY_REQUEST);
          }
        });
  }

  private void validateExistUsers(MentorshipRequestDto mentorshipRequestDto) {
    userRepository.findById(mentorshipRequestDto.getRequesterId())
        .orElseThrow(() -> new DataValidationException(ERROR_REQUESTER_IS_MISSING));
    userRepository.findById(mentorshipRequestDto.getReceiverId())
        .orElseThrow(() -> new DataValidationException(ERROR_RECEIVER_IS_MISSING));
  }

}
