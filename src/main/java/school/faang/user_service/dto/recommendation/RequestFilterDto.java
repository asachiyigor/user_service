package school.faang.user_service.dto.recommendation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.faang.user_service.entity.RequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
