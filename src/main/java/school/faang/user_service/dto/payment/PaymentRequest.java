package school.faang.user_service.dto.payment;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PaymentRequest(
        @NotNull
        long paymentNumber,

        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Amount must be positive")
        @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
        BigDecimal amount,

        @NotBlank(message = "CurrencyCode must not be blank")
        String currencyCode
) {
}