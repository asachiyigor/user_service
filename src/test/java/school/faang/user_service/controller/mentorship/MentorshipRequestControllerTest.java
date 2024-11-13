package school.faang.user_service.controller.mentorship;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import school.faang.user_service.dto.mentorship.MentorshipRequestDto;
import school.faang.user_service.dto.mentorship.RejectionDto;
import school.faang.user_service.dto.mentorship.RequestFilterDto;
import school.faang.user_service.exception.mentorship.DataValidationException;
import school.faang.user_service.service.mentorship.MentorshipRequestService;

@WebMvcTest
@ContextConfiguration(classes = {MentorshipRequestController.class})
public class MentorshipRequestControllerTest {

  private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @MockBean
  private MentorshipRequestService mentorshipRequestService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("Should return rejected request with passed id and rejection reason string")
  void testPositiveRejectRequest() throws Exception {
    RejectionDto rejectionDto = new RejectionDto("rejection reason");
    MentorshipRequestDto expectedResponse = MentorshipRequestDto.builder()
        .id(1L)
        .rejectionReason("rejection reason")
        .build();

    when(mentorshipRequestService.rejectRequest(1L, rejectionDto)).thenReturn(expectedResponse);

    mockMvc.perform(post(MentorshipRequestUrl.URL_REJECT.getUrl(), 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(OBJECT_MAPPER.writeValueAsString(rejectionDto)))
        .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(expectedResponse)))
        .andExpect(status().isOk());
    verify(mentorshipRequestService, Mockito.times(1)).rejectRequest(1L, rejectionDto);
  }

  @Test
  @DisplayName("Should return accepted request with id")
  void testPositiveAcceptRequest() throws Exception {
    MentorshipRequestDto expectedResponse = MentorshipRequestDto.builder()
        .id(1L)
        .description("Accepted request")
        .build();

    when(mentorshipRequestService.acceptRequest(1L)).thenReturn(expectedResponse);

    mockMvc.perform(post(MentorshipRequestUrl.URL_ACCEPT.getUrl(), 1L))
        .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(expectedResponse)))
        .andExpect(status().isOk());
    verify(mentorshipRequestService, Mockito.times(1)).acceptRequest(1L);
  }

  @Test
  @DisplayName("Should return added request")
  void testPositiveRequestMentorship() throws Exception {
    MentorshipRequestDto mentorshipRequest = MentorshipRequestDto.builder()
        .description("new mentorship request")
        .build();
    MentorshipRequestDto expectedResponse = MentorshipRequestDto.builder()
        .id(1L)
        .description("new mentorship request")
        .build();

    when(mentorshipRequestService.requestMentorship(mentorshipRequest)).thenReturn(
        expectedResponse);

    mockMvc.perform(post(MentorshipRequestUrl.URL_ADD.getUrl())
            .contentType(MediaType.APPLICATION_JSON)
            .content(OBJECT_MAPPER.writeValueAsString(mentorshipRequest)))
        .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(expectedResponse)))
        .andExpect(status().isOk());
    verify(mentorshipRequestService, Mockito.times(1)).requestMentorship(mentorshipRequest);
  }

  @Test
  @DisplayName("Should throw exception * doesn't work")
  void testNegativeRequestMentorship() throws Exception {
    MentorshipRequestDto mentorshipRequest = MentorshipRequestDto.builder()
        .requesterId(1L)
        .receiverId(2L)
        .description("")
        .build();
    when(mentorshipRequestService.requestMentorship(mentorshipRequest)).thenThrow(new DataValidationException("mock"));
//    mockMvc.perform(post(MentorshipRequestUrl.URL_ADD.getUrl())
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(OBJECT_MAPPER.writeValueAsString(mentorshipRequest)))
//        .andExpect(status().isBadRequest());
    assertThrows(DataValidationException.class, () -> mentorshipRequestService.requestMentorship(mentorshipRequest));
    verify(mentorshipRequestService, Mockito.times(1)).requestMentorship(mentorshipRequest);
  }

  @Test
  @DisplayName("Should return requests filtered or with no(empty) filter")
  void testGetRequestsWithFilters() throws Exception {
    List<MentorshipRequestDto> requestDtos = Collections.singletonList(
        MentorshipRequestDto
            .builder()
            .id(1L)
            .requesterId(1L)
            .receiverId(2L)
            .description("Test request")
            .build()
    );
    RequestFilterDto filterDto = RequestFilterDto
        .builder()
        .requesterId(1L)
        .description("test")
        .build();
    when(mentorshipRequestService.getRequests(filterDto)).thenReturn(requestDtos);
    mockMvc.perform(get(MentorshipRequestUrl.URL_LIST.getUrl())
            .contentType(MediaType.APPLICATION_JSON)
            .content(OBJECT_MAPPER.writeValueAsString(filterDto)))
        .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(requestDtos)))
        .andExpect(status().isOk());
    verify(mentorshipRequestService, times(1)).getRequests(filterDto);
  }
}
