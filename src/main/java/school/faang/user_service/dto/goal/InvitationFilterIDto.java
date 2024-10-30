package school.faang.user_service.dto.goal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import school.faang.user_service.entity.RequestStatus;

@Data
@Validated
public class InvitationFilterIDto {

    @NotNull
    @Length(min = 3, max = 30)
    private String inviterNamePattern;

    @Length(min = 3, max = 30)
    private String invitedNamePattern;

    private Long inviterId;
    private Long invitedId;
    private RequestStatus status;
}
