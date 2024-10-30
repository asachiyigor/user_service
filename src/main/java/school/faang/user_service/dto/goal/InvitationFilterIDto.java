package school.faang.user_service.dto.goal;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import school.faang.user_service.entity.RequestStatus;

@Data
@Validated
@NoArgsConstructor
@AllArgsConstructor
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
