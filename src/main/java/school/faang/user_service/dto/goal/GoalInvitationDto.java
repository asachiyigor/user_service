package school.faang.user_service.dto.goal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.faang.user_service.entity.RequestStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalInvitationDto {

    @Min(1)
    private Long id;

    @Min(1)
    private Long inviterId;

    @Min(1)
    private Long invitedUserId;

    @Min(1)
    private Long goalId;

    @NotNull
    private RequestStatus status;
}