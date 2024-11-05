package school.faang.user_service.service.mentorship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.mentorship.MentorshipRequestDto;
import school.faang.user_service.entity.MentorshipRequest;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.exception.mentorship.DataValidationException;
import school.faang.user_service.mapper.mentorship.MentorshipRequestMapper;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.mentorship.MentorshipRequestRepository;

@ExtendWith(MockitoExtension.class)
public class MentorshipRequestServiceTest {

  private User requester;
  private User receiver;
  private MentorshipRequestDto dto;
  private MentorshipRequestDto latestDto;
  private MentorshipRequestDto newLateDto;
  private LocalDateTime thresholdDate;
  private MentorshipRequest latestRequest;

  @InjectMocks
  private MentorshipRequestServiceImpl mentorshipRequestService;

  @Mock
  private MentorshipRequestRepository mentorshipRequestRepository;

  @Mock
  private MentorshipRequestMapper mentorshipRequestMapper;

  @Mock
  private UserRepository userRepository;

  @BeforeEach
  void setup() {
    LocalDateTime originDate = LocalDateTime.now();
    requester = User.builder()
        .id(1L)
        .build();
    receiver = User.builder()
        .id(2L)
        .build();
    dto = MentorshipRequestDto.builder()
        .id(1L)
        .description("Test description")
        .requesterId(requester.getId())
        .receiverId(receiver.getId())
        .status(RequestStatus.PENDING)
        .rejectionReason("Test rejection reason")
        .createdAt(originDate.toString())
        .updatedAt(originDate.plusDays(2).toString())
        .build();
    int latestRequestDate = 2;

    thresholdDate = originDate.minusMonths(latestRequestDate);
  }

  @Test
  @DisplayName("Test create request with requester is missing in DB")
  public void testCreateWithNonExistRequester() {
    DataValidationException exception = assertThrows(DataValidationException.class,
        () -> mentorshipRequestService.requestMentorship(dto));

    verify(userRepository).findById(dto.getRequesterId());

    assertEquals(
        MentorshipRequestServiceImpl.ERROR_REQUESTER_IS_MISSING,
        exception.getMessage());
  }

  @Test
  @DisplayName("Test create request with receiver is missing in DB")
  public void testCreateWithNonExistReceiver() {
    when(userRepository.findById(dto.getRequesterId())).thenReturn(Optional.of(requester));
    when(userRepository.findById(dto.getReceiverId())).thenReturn(Optional.empty());

    DataValidationException exception = assertThrows(DataValidationException.class,
        () -> mentorshipRequestService.requestMentorship(dto));

    verify(userRepository).findById(dto.getRequesterId());
    verify(userRepository).findById(dto.getReceiverId());

    assertEquals(
        MentorshipRequestServiceImpl.ERROR_RECEIVER_IS_MISSING,
        exception.getMessage());
  }

  @Test
  @DisplayName("Test create request with requester and receiver are in DB")
  public void testCreateWithExistUsers() {
    when(userRepository.findById(dto.getRequesterId())).thenReturn(Optional.of(requester));
    when(userRepository.findById(dto.getReceiverId())).thenReturn(Optional.of(receiver));

    mentorshipRequestService.requestMentorship(dto);

    verify(userRepository).findById(dto.getRequesterId());
    verify(userRepository).findById(dto.getReceiverId());
  }

  @Test
  public void testCreateWithTooEarlyDate() {
    latestRequest = new MentorshipRequest();
    latestRequest.setCreatedAt(thresholdDate);
    latestRequest.setRequester(requester);
    latestRequest.setReceiver(receiver);

    when(userRepository.findById(dto.getRequesterId())).thenReturn(Optional.of(requester));
    when(userRepository.findById(dto.getReceiverId())).thenReturn(Optional.of(receiver));
    when(mentorshipRequestRepository.findLatestRequest(dto.getRequesterId(),
        dto.getReceiverId())).thenReturn(Optional.of(latestRequest));

    DataValidationException exception = assertThrows(DataValidationException.class,
        () -> mentorshipRequestService.requestMentorship(dto));

    verify(userRepository).findById(dto.getRequesterId());
    verify(userRepository).findById(dto.getReceiverId());
    verify(mentorshipRequestRepository).findLatestRequest(dto.getRequesterId(),
        dto.getReceiverId());

    assertEquals(
        MentorshipRequestServiceImpl.ERROR_EARLY_REQUEST,
        exception.getMessage());
  }

  @Test
  public void testCreateSaveRequest() {

  }


  @Test
  public void testNotValidUsersMentorshipRequestInvalid() {

  }

  @Test
  public void testSelfRequestMentorshipRequestInvalid() {

  }

  @Test
  public void testEarlierThanAllowedMentorshipRequestInvalid() {

  }

  @Test
  public void testValidDataMentorshipRequest() {

  }

  @Test
  public void testNullFiltersGetRequestsInvalid() {

  }

  @Test
  public void testGetAllRequests() {

  }

  @Test
  public void testGetResetFiltersRequests() {

  }

  @Test
  public void testGetFilteredRequests() {

  }

  @Test
  public void testFindByIdNonExistingMentorshipRequestInvalid() {

  }

  @Test
  public void testFindByIdExistingMentorshipRequest() {

  }

  @Test
  public void testAcceptedRequestAcceptInvalid() {

  }

  @Test
  public void testNotAcceptedRequestAccept() {

  }


}
