package school.faang.user_service.dto.mentorship;

import lombok.Data;
import school.faang.user_service.entity.mentorship.RequestStatus;

@Data
public class RequestFilterDto {

  private String description;
  private Long requesterId;
  private Long receiverId;
  private RequestStatus status;
}
