package school.faang.user_service.entity.premium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static school.faang.user_service.entity.premium.PremiumPeriod.fromDays;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import school.faang.user_service.exception.DataValidationException;

class PremiumPeriodTest {

  @ParameterizedTest
  @MethodSource("validDays")
  void testValidDays(int days) throws Exception {
    fromDays(days);
  }

  @ParameterizedTest
  @MethodSource("invalidDays")
  void testInvalidDays(int days) throws Exception {
    var exception = assertThrows(DataValidationException.class, () -> fromDays(days));
    assertEquals(String.format("no plan with %d", days), exception.getMessage());
  }

  static Stream<Object[]> validDays() {
    return Stream.of(
        new Object[]{30},
        new Object[]{90},
        new Object[]{365}
    );
  }

  static Stream<Object[]> invalidDays() {
    return Stream.of(
        new Object[]{0},
        new Object[]{1},
        new Object[]{2}
    );
  }

}