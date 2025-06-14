package school.faang.user_service.controller.mentorship;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.mentorship.MentorshipRequestDto;
import school.faang.user_service.dto.mentorship.RejectionDto;
import school.faang.user_service.dto.mentorship.RequestFilterDto;
import school.faang.user_service.service.mentorship.MentorshipRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Mentorship Request API", description = "API for managing requests for mentorship")
@RequestMapping("/mentorship")
public class MentorshipRequestController {

  private final MentorshipRequestService mentorshipRequestService;

  @PostMapping("/reject/{id}")
  public MentorshipRequestDto rejectRequest(@PathVariable long id,
      @RequestBody RejectionDto rejectionDto) {
    return mentorshipRequestService.rejectRequest(id, rejectionDto);
  }

  @PostMapping("/accept/{id}")
  public MentorshipRequestDto acceptRequest(@PathVariable long id) {
    return mentorshipRequestService.acceptRequest(id);
  }

  @PostMapping("/add")
  public MentorshipRequestDto requestMentorship(@Valid
      @RequestBody MentorshipRequestDto mentorshipRequestDto) {
    return mentorshipRequestService.requestMentorship(mentorshipRequestDto);
  }

  @GetMapping("/list")
  public List<MentorshipRequestDto> getRequests(
      @RequestBody RequestFilterDto requestFilterDto) {
    return mentorshipRequestService.getRequests(requestFilterDto);
  }

}
