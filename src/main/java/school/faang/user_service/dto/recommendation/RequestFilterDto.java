package school.faang.user_service.dto.recommendation;

import lombok.*;
import school.faang.user_service.entity.RequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RequestFilterDto {
    private String requesterName;
    private String receiverName;
    private RequestStatus status;
    private String message;
    private String rejectionReason;
    private Long recommendationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
