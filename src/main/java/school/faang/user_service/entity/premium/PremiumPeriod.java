package school.faang.user_service.entity.premium;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.NoSuchElementException;
import lombok.Getter;

@Getter
public enum PremiumPeriod {
  ONE_MONTH(30, BigDecimal.valueOf(10)),
  THREE_MONTHS(90, BigDecimal.valueOf(25)),
  ONE_YEAR(365, BigDecimal.valueOf(80));
  private final int days;
  private final BigDecimal price;

  PremiumPeriod(int days, BigDecimal price) {
    this.days = days;
    this.price = price;
  }

  public static PremiumPeriod fromDays(int days) {
    return Arrays.stream(values())
        .filter(premiumPeriod -> premiumPeriod.days == days)
        .findAny()
        .orElseThrow(() -> new NoSuchElementException(String.format("no plan with %d", days)));
  }
}
