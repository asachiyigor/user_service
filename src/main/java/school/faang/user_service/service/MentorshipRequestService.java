package school.faang.user_service.service;

import java.util.List;
import school.faang.user_service.dto.MentorshipRequestDto;
import school.faang.user_service.dto.RequestFilterDto;

public interface MentorshipRequestService {

  MentorshipRequestDto requestMentorship(MentorshipRequestDto mentorshipRequestDto);

  List<MentorshipRequestDto> getRequests(RequestFilterDto requestFilterDto);

}
