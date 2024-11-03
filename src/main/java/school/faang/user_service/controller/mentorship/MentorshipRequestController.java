package school.faang.user_service.controller.mentorship;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.mentorship.MentorshipRequestDto;
import school.faang.user_service.dto.mentorship.RequestFilterDto;
import school.faang.user_service.exception.mentorship.DataValidationException;
import school.faang.user_service.service.mentorship.MentorshipRequestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mentorship")
public class MentorshipRequestController {

  private final MentorshipRequestService mentorshipRequestService;

  @PostMapping("/accept/{id}")
  public ResponseEntity<MentorshipRequestDto> acceptRequest(@PathVariable long id) {
    return ResponseEntity.ok(mentorshipRequestService.acceptRequest(id));
  }

  @PostMapping("/add")
  public ResponseEntity<MentorshipRequestDto> requestMentorship(@RequestBody
      MentorshipRequestDto mentorshipRequestDto) {
    validateMentorshipRequest(mentorshipRequestDto);

    return ResponseEntity.ok(mentorshipRequestService.requestMentorship(mentorshipRequestDto));
  }

  @GetMapping("/list")
  public ResponseEntity<List<MentorshipRequestDto>> getRequests(@RequestBody RequestFilterDto requestFilterDto) {
    return ResponseEntity.ok(mentorshipRequestService.getRequests(requestFilterDto));
  }


  private void validateMentorshipRequest(MentorshipRequestDto mentorshipRequestDto) {
    if (mentorshipRequestDto.getDescription().isEmpty()) {
      throw new DataValidationException("Please write why you need mentor");
    }
  }
}
