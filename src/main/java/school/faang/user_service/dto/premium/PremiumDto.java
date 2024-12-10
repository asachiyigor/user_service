package school.faang.user_service.dto.premium;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PremiumDto(
    long id,
    long userId,
    LocalDateTime startDate,
    LocalDateTime endDate
) {

}
