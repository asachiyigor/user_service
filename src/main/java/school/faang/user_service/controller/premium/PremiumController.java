package school.faang.user_service.controller.premium;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.premium.PremiumDto;
import school.faang.user_service.entity.premium.PremiumPeriod;
import school.faang.user_service.service.premium.PremiumService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/premium")
public class PremiumController {

  private final PremiumService premiumService;

  @PostMapping("/buy/{days}")
  public PremiumDto buyPremium(@RequestHeader("x-user-id") long userId, @PathVariable int days) {
    PremiumPeriod premiumPlan = PremiumPeriod.fromDays(days);
    return premiumService.buyPremium(userId, premiumPlan);
  }

}
