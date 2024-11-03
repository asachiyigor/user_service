package school.faang.user_service.dto.goal;

import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;
import school.faang.user_service.entity.RequestStatus;

@Validated
@Builder
@Getter
public class InvitationFilterIDto {
    private String inviterNamePattern;
    private String invitedNamePattern;
    private Long inviterId;
    private Long invitedId;
    private RequestStatus status;
}
