package school.faang.user_service.service.premium;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.user_service.client.PaymentServiceClient;
import school.faang.user_service.dto.payment.PaymentRequest;
import school.faang.user_service.dto.payment.PaymentStatus;
import school.faang.user_service.dto.premium.BoughtPremiumEventDto;
import school.faang.user_service.dto.premium.PremiumDto;
import school.faang.user_service.entity.premium.Premium;
import school.faang.user_service.entity.premium.PremiumPeriod;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.premium.PremiumMapper;
import school.faang.user_service.publisher.BoughtPremiumPublisher;
import school.faang.user_service.repository.premium.PremiumRepository;
import school.faang.user_service.service.user.UserService;

@Service
@RequiredArgsConstructor
public class PremiumServiceImpl implements PremiumService {

  private final PremiumRepository premiumRepository;
  private final UserService userService;
  private final PremiumMapper premiumMapper;
  private final BoughtPremiumPublisher boughtPremiumPublisher;
  private final PaymentServiceClient paymentServiceClient;

  @Override
  public PremiumDto buyPremium(long userId, PremiumPeriod premiumPlan) {
    validatePremiumUser(userId);
    validatePayment(premiumPlan.getPrice());
    PremiumDto premiumDto = createPremiumDto(userId, premiumPlan);
    Premium premium = premiumMapper.toEntity(premiumDto);
    premium.setUser(userService.getUserById(userId));
    premiumDto = premiumMapper.toDto(premiumRepository.save(premium));
    boughtPremiumPublisher.publish(BoughtPremiumEventDto.builder()
        .userId(userId)
        .sum(premiumPlan.getPrice())
        .days(premiumPlan.getDays())
        .receivedAt(LocalDateTime.now().toString())
        .build());
    return premiumDto;
  }

  private void validatePayment(BigDecimal sum) {
    PaymentRequest paymentRequest = PaymentRequest.builder()
        .amount(sum)
        .currencyCode("USD")
        .build();
    if (!paymentServiceClient.sendPayment(paymentRequest).status().equals(PaymentStatus.SUCCESS)) {
      throw new DataValidationException("Payment failed!");
    }

  }

  private PremiumDto createPremiumDto(long userId, PremiumPeriod premiumPlan) {
    LocalDateTime startDate = LocalDateTime.now();
    return PremiumDto.builder()
        .userId(userId)
        .startDate(startDate)
        .endDate(startDate.plusDays(premiumPlan.getDays()))
        .build();
  }

  private void validatePremiumUser(long userId) {
    if (premiumRepository.existsByUserId(userId)) {
      throw new DataValidationException("User already has premium subscription!");
    }
  }
}
