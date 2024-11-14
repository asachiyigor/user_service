package school.faang.user_service.service.mentorship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.mentorship.MentorshipRequestDto;
import school.faang.user_service.dto.mentorship.RejectionDto;
import school.faang.user_service.dto.mentorship.RequestFilterDto;
import school.faang.user_service.entity.mentorship.MentorshipRequest;
import school.faang.user_service.entity.mentorship.RequestError;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.exception.mentorship.DataValidationException;
import school.faang.user_service.mapper.mentorship.MentorshipRequestMapper;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.mentorship.MentorshipRequestRepository;

@ExtendWith(MockitoExtension.class)
public class MentorshipRequestServiceTest {

  @InjectMocks
  private MentorshipRequestServiceImpl mentorshipRequestService;

  @Mock
  private MentorshipRequestRepository mentorshipRequestRepository;

  @Spy
  private MentorshipRequestMapper mentorshipRequestMapper = Mappers.getMapper(
      MentorshipRequestMapper.class);

  @Mock
  private UserRepository userRepository;

  private static final int MONTHS_AGO_NOT_OK = 2;
  private static final int MONTHS_AGO_OK = 4;

  @Test
  @DisplayName("Should throw exception when requester does not exist")
  public void testCreateWithNonExistRequester() {
    MentorshipRequest request = getDefaultRequest();
    MentorshipRequestDto requestDto = mentorshipRequestMapper.toDto(request);

    DataValidationException exception = assertThrows(DataValidationException.class,
        () -> mentorshipRequestService.requestMentorship(requestDto));

    verify(userRepository).findById(requestDto.getRequesterId());

    assertEquals(
        RequestError.REQUESTER_IS_MISSING.getMessage(),
        exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when receiver does not exist")
  public void testCreateWithNonExistReceiver() {
    MentorshipRequest request = getDefaultRequest();
    MentorshipRequestDto requestDto = mentorshipRequestMapper.toDto(request);
    User requester = request.getRequester();

    when(userRepository.findById(requestDto.getRequesterId())).thenReturn(Optional.of(requester));
    when(userRepository.findById(requestDto.getReceiverId())).thenReturn(Optional.empty());

    DataValidationException exception = assertThrows(DataValidationException.class,
        () -> mentorshipRequestService.requestMentorship(requestDto));

    verify(userRepository).findById(requestDto.getRequesterId());
    verify(userRepository).findById(requestDto.getReceiverId());

    assertEquals(
        RequestError.RECEIVER_IS_MISSING.getMessage(),
        exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when the mentorship requested again before previous acting period not finished")
  public void testCreateWithTooEarlyDate() {
    MentorshipRequest request = getDefaultRequest();
    MentorshipRequestDto requestDto = mentorshipRequestMapper.toDto(request);
    User requester = request.getRequester();
    User receiver = request.getReceiver();

    MentorshipRequest latestRequest = getDefaultRequest();
    latestRequest.setCreatedAt(calcLatestRequestDate(MONTHS_AGO_NOT_OK));

    when(userRepository.findById(requestDto.getRequesterId())).thenReturn(Optional.of(requester));
    when(userRepository.findById(requestDto.getReceiverId())).thenReturn(Optional.of(receiver));
    when(mentorshipRequestRepository.findLatestRequest(requestDto.getRequesterId(),
        requestDto.getReceiverId())).thenReturn(Optional.of(latestRequest));

    DataValidationException exception = assertThrows(DataValidationException.class,
        () -> mentorshipRequestService.requestMentorship(requestDto));

    verify(userRepository).findById(requestDto.getRequesterId());
    verify(userRepository).findById(requestDto.getReceiverId());
    verify(mentorshipRequestRepository).findLatestRequest(requestDto.getRequesterId(),
        requestDto.getReceiverId());

    assertEquals(
        RequestError.EARLY_REQUEST.getMessage(),
        exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when requester and receiver are the one user")
  public void testCreateSelfRequest() {
    MentorshipRequest request = getDefaultRequest();
    MentorshipRequestDto requestDto = mentorshipRequestMapper.toDto(request);
    User requester = request.getRequester();

    MentorshipRequest latestRequest = getDefaultRequest();
    latestRequest.setCreatedAt(calcLatestRequestDate(MONTHS_AGO_OK));

    requestDto.setReceiverId(requestDto.getRequesterId());

    when(userRepository.findById(requestDto.getRequesterId())).thenReturn(Optional.of(requester));
    when(userRepository.findById(requestDto.getReceiverId())).thenReturn(Optional.of(requester));
    when(mentorshipRequestRepository.findLatestRequest(requestDto.getRequesterId(),
        requestDto.getReceiverId())).thenReturn(Optional.of(latestRequest));

    DataValidationException exception = assertThrows(DataValidationException.class,
        () -> mentorshipRequestService.requestMentorship(requestDto));

    verify(userRepository, times(2)).findById(requestDto.getRequesterId());
    verify(mentorshipRequestRepository).findLatestRequest(requestDto.getRequesterId(),
        requestDto.getReceiverId());

    assertEquals(
        RequestError.SELF_REQUEST.getMessage(),
        exception.getMessage());
  }

  @Test
  @DisplayName("Should create new request when valid input data provided")
  public void testCreateSave() {
    MentorshipRequest request = getDefaultRequest();
    MentorshipRequestDto requestDto = mentorshipRequestMapper.toDto(request);
    User requester = request.getRequester();
    User receiver = request.getReceiver();

    MentorshipRequest latestRequest = getDefaultRequest();
    latestRequest.setCreatedAt(calcLatestRequestDate(MONTHS_AGO_OK));

    when(userRepository.findById(requestDto.getRequesterId())).thenReturn(Optional.of(requester));
    when(userRepository.findById(requestDto.getReceiverId())).thenReturn(Optional.of(receiver));
    when(mentorshipRequestRepository.findLatestRequest(requestDto.getRequesterId(),
        requestDto.getReceiverId())).thenReturn(Optional.of(latestRequest));
    when(mentorshipRequestRepository.create(requestDto.getRequesterId(), requestDto.getReceiverId(),
        requestDto.getDescription())).thenReturn(request);

    var resultDto = mentorshipRequestService.requestMentorship(requestDto);

    verify(userRepository).findById(requestDto.getRequesterId());
    verify(userRepository).findById(requestDto.getReceiverId());
    verify(mentorshipRequestRepository).findLatestRequest(requestDto.getRequesterId(),
        requestDto.getReceiverId());
    verify(mentorshipRequestRepository).create(requestDto.getRequesterId(),
        requestDto.getReceiverId(),
        requestDto.getDescription());

    assertEquals(requestDto.getDescription(), resultDto.getDescription());
  }

  @Test
  @DisplayName("Should return all requests with NO FILTER applied")
  public void testGetRequestsWithNoFilterApplied() {
    RequestFilterDto filterDto = new RequestFilterDto();
    List<MentorshipRequest> requests = requestsToFilter();

    when(mentorshipRequestRepository.findAll()).thenReturn(requests);
    var requestsDto = mentorshipRequestService.getRequests(filterDto);
    verify(mentorshipRequestRepository).findAll();

    assertEquals(requests.size(), requestsDto.size());
  }

  @Test
  @DisplayName("Should return requests with DESCRIPTION FILTER applied")
  public void testGetRequestsWithDescriptionFilterApplied() {
    List<MentorshipRequest> requests = requestsToFilter();
    RequestFilterDto filterDto = new RequestFilterDto();
    filterDto.setDescription("help");

    when(mentorshipRequestRepository.findAll()).thenReturn(requests);
    var requestsDto = mentorshipRequestService.getRequests(filterDto);
    verify(mentorshipRequestRepository).findAll();

    assertEquals(1, requestsDto.size());
  }

  @Test
  @DisplayName("Should return requests with REQUESTER FILTER applied")
  public void testGetRequestsWithRequesterFilterApplied() {
    List<MentorshipRequest> requests = requestsToFilter();
    RequestFilterDto filterDto = new RequestFilterDto();
    long requesterId = requests.get(0).getRequester().getId();
    filterDto.setRequesterId(requesterId);

    when(mentorshipRequestRepository.findAll()).thenReturn(requests);
    var requestsDto = mentorshipRequestService.getRequests(filterDto);
    verify(mentorshipRequestRepository).findAll();

    assertEquals(1, requestsDto.size());
  }

  @Test
  @DisplayName("Should return requests with RECEIVER FILTER applied")
  public void testGetRequestsWithReceiverFilterApplied() {
    List<MentorshipRequest> requests = requestsToFilter();
    RequestFilterDto filterDto = new RequestFilterDto();
    long receiverId = requests.get(0).getReceiver().getId();
    filterDto.setReceiverId(receiverId);

    when(mentorshipRequestRepository.findAll()).thenReturn(requests);
    var requestsDto = mentorshipRequestService.getRequests(filterDto);
    verify(mentorshipRequestRepository).findAll();

    assertEquals(1, requestsDto.size());
  }

  @Test
  @DisplayName("Should return requests with STATUS FILTER applied")
  public void testGetRequestsWithStatusFilterApplied() {
    List<MentorshipRequest> requests = requestsToFilter();
    RequestFilterDto filterDto = new RequestFilterDto();
    filterDto.setStatus(RequestStatus.PENDING);

    when(mentorshipRequestRepository.findAll()).thenReturn(requests);
    var requestsDto = mentorshipRequestService.getRequests(filterDto);
    verify(mentorshipRequestRepository).findAll();

    assertEquals(1, requestsDto.size());
  }

  @Test
  @DisplayName("Should return requests with ALL FILTERS applied")
  public void testGetRequestsWithAllFiltersApplied() {
    List<MentorshipRequest> requests = requestsToFilter();
    RequestFilterDto filterDto = new RequestFilterDto();
    long requesterId = requests.get(0).getRequester().getId();
    long receiverId = requests.get(0).getReceiver().getId();
    filterDto.setRequesterId(requesterId);
    filterDto.setReceiverId(receiverId);
    filterDto.setStatus(RequestStatus.PENDING);
    filterDto.setDescription("HELLO");

    when(mentorshipRequestRepository.findAll()).thenReturn(requests);
    var requestsDto = mentorshipRequestService.getRequests(filterDto);
    verify(mentorshipRequestRepository).findAll();

    assertEquals(1, requestsDto.size());
  }

  @Test
  @DisplayName("Should throw exception for non-existing request")
  public void testFindByIdNonExistingRequest() {
    Long id = 100L;

    when(mentorshipRequestRepository.findById(id)).thenThrow(
        new DataValidationException(RequestError.NOT_FOUND.getMessage() + id));
    var exception = assertThrows(DataValidationException.class,
        () -> mentorshipRequestRepository.findById(id));
    verify(mentorshipRequestRepository).findById(id);

    assertEquals(RequestError.NOT_FOUND.getMessage() + id, exception.getMessage());
  }

  @Test
  @DisplayName("Should return existing request by id")
  public void testFindByIdExistingRequest() {
    MentorshipRequest request = getDefaultRequest();
    long id = request.getId();

    when(mentorshipRequestRepository.findById(id)).thenReturn(Optional.of(request));
    var foundRequest = mentorshipRequestService.findById(id);
    verify(mentorshipRequestRepository).findById(id);

    assertNotNull(foundRequest);
    assertEquals(id, foundRequest.getId());
  }

  @Test
  @DisplayName("Should throw exception when try to accept non-existing request")
  public void testAcceptNonExistRequest() {
    long id = 100L;

    when(mentorshipRequestRepository.findById(id)).thenReturn(Optional.empty());

    DataValidationException exception = assertThrows(DataValidationException.class,
        () -> mentorshipRequestService.acceptRequest(id));

    verify(mentorshipRequestRepository).findById(id);

    assertEquals(RequestError.NOT_FOUND.getMessage() + id, exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when try to accept already accepted request")
  public void testAcceptExistRequestAlreadyAccepted() {
    MentorshipRequest request = getDefaultRequest();
    request.setStatus(RequestStatus.ACCEPTED);
    long id = request.getId();

    when(mentorshipRequestRepository.findById(id)).thenReturn(Optional.of(request));

    DataValidationException exception = assertThrows(DataValidationException.class,
        () -> mentorshipRequestService.acceptRequest(id));

    verify(mentorshipRequestRepository).findById(id);

    assertEquals(RequestError.ALREADY_ACCEPTED.getMessage(), exception.getMessage());
  }

  @Test
  @DisplayName("Should change the status of existing not accepted request to ACCEPTED")
  public void testAcceptExistRequestValid() {
    MentorshipRequest request = getDefaultRequest();
    request.setStatus(RequestStatus.PENDING);
    User requester = request.getRequester();
    long id = request.getId();

    when(mentorshipRequestRepository.findById(id)).thenReturn(Optional.of(request));
    when(mentorshipRequestRepository.save(request)).thenReturn(request);
    when(userRepository.save(request.getRequester())).thenReturn(requester);

    var requestDto = mentorshipRequestService.acceptRequest(id);

    verify(mentorshipRequestRepository).findById(id);
    verify(mentorshipRequestRepository).save(request);
    verify(userRepository).save(request.getRequester());

    assertEquals(RequestStatus.ACCEPTED, requestDto.getStatus());
  }

  @Test
  @DisplayName("Should change the status of existing not rejected request to REJECTED")
  public void testRejectExistRequestValid() {
    MentorshipRequest request = getDefaultRequest();
    request.setStatus(RequestStatus.PENDING);
    long id = request.getId();
    RejectionDto rejectionDto = new RejectionDto("Test rejection reason");

    when(mentorshipRequestRepository.findById(id)).thenReturn(Optional.of(request));
    when(mentorshipRequestRepository.save(request)).thenReturn(request);

    var requestDto = mentorshipRequestService.rejectRequest(id, rejectionDto);

    verify(mentorshipRequestRepository).findById(id);
    verify(mentorshipRequestRepository).save(request);

    assertEquals(RequestStatus.REJECTED, requestDto.getStatus());
  }

  private List<MentorshipRequest> requestsToFilter() {
    MentorshipRequest request = getDefaultRequest();
    MentorshipRequest anotherRequest = getDefaultRequest();
    List<MentorshipRequest> requests = List.of(request, anotherRequest);

    request.setDescription("Hello");
    request.setStatus(RequestStatus.PENDING);

    anotherRequest.setCreatedAt(calcLatestRequestDate(MONTHS_AGO_OK));
    anotherRequest.setDescription("I need your help");
    anotherRequest.setStatus(RequestStatus.REJECTED);
    anotherRequest.setRequester(request.getReceiver());
    anotherRequest.setReceiver(request.getRequester());

    return requests;
  }

  private LocalDateTime calcLatestRequestDate(int monthsAgo) {
    return LocalDateTime.now().minusMonths(monthsAgo);
  }

  private MentorshipRequest getDefaultRequest() {
    User requester = User.builder()
        .id(1L)
        .build();
    User receiver = User.builder()
        .id(2L)
        .build();
    return MentorshipRequest.builder()
        .id(1L)
        .description("Test request")
        .requester(requester)
        .receiver(receiver)
        .status(RequestStatus.PENDING)
        .rejectionReason("Test rejection reason")
        .createdAt(LocalDateTime.now())
        .build();
  }
}
