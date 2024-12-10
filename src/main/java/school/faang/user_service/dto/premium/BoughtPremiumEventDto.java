package school.faang.user_service.dto.premium;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record BoughtPremiumEventDto(
    @Positive
    Long userId,

    @Positive
    BigDecimal sum,

    @NotNull
    int days,

    @NotNull
    String receivedAt
) {

}
