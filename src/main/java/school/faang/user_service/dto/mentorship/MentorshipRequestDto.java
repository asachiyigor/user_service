package school.faang.user_service.dto.mentorship;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.faang.user_service.entity.RequestStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorshipRequestDto {

  private Long id;
  @NotBlank
  private String description;
  private Long requesterId;
  private Long receiverId;
  private RequestStatus status;
  private String rejectionReason;
  private String createdAt;
  private String updatedAt;
}
