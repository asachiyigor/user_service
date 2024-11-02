package school.faang.user_service.dto.recommendation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.faang.user_service.entity.RequestStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationRequestDto {
    private Long id;
    @NotNull
    @NotBlank
    private String message;
    private RequestStatus status;
    private String rejectionReason;
    @NotNull
    private List<Long> skillsIds;
    @NotNull
    private Long requesterId;
    @NotNull
    private Long receiverId;
    private String createdAt;
    private String updatedAt;
}
