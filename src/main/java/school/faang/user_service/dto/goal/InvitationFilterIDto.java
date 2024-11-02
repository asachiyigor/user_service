package school.faang.user_service.dto.goal;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import school.faang.user_service.entity.RequestStatus;

@Validated
@Builder
@Getter
public class InvitationFilterIDto {

    @NotBlank
    @Length(min = 3, max = 30)
    private String inviterNamePattern;

    @Length(min = 3, max = 30)
    private String invitedNamePattern;

    private Long inviterId;
    private Long invitedId;
    private RequestStatus status;
}
