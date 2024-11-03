package school.faang.user_service.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.MentorshipRequestDto;
import school.faang.user_service.dto.RequestFilterDto;
import school.faang.user_service.service.MentorshipRequestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mentorship")
public class MentorshipRequestController {

  private final MentorshipRequestService mentorshipRequestService;

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
      throw new RuntimeException("Please write why you need mentor");
    }
  }
}
