package school.faang.user_service.dto.premium;

import lombok.Builder;

@Builder
public record PremiumDto(
    long id,
    long userId,
    String startDate,
    String endDate
) {

}
