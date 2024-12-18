package school.faang.user_service.dto.payment;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PaymentResponse(
        PaymentStatus status,
        int verificationCode,
        long paymentNumber,
        BigDecimal amount,
        String currencyCode,
        String convertAmountWithCommission,
        Currency convertCurrencyCode,
        String message
) {
}
