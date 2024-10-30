package school.faang.user_service.dto.goal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import school.faang.user_service.entity.RequestStatus;

@Data
@Validated
public class GoalInvitationDto {

    @NotNull
    private Long id;

    @NotNull
    private Long inviterId;

    @NotNull
    private Long invitedUserId;

    @NotNull
    private Long goalId;

    @NotNull
    private RequestStatus status;
}