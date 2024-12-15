package school.faang.user_service.dto.recommendation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record RecommendationEventDto(
    @Positive
    Long id,

    @Positive
    Long receiverId,

    @Positive
    Long authorId,

    @NotNull
    String receivedAt
) {

}
