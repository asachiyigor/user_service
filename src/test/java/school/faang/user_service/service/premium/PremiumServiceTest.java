package school.faang.user_service.service.premium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.client.PaymentServiceClient;
import school.faang.user_service.dto.payment.PaymentRequest;
import school.faang.user_service.dto.payment.PaymentResponse;
import school.faang.user_service.dto.payment.PaymentStatus;
import school.faang.user_service.dto.premium.BoughtPremiumEventDto;
import school.faang.user_service.entity.Country;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.premium.Premium;
import school.faang.user_service.entity.premium.PremiumPeriod;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.premium.PremiumMapper;
import school.faang.user_service.publisher.BoughtPremiumPublisher;
import school.faang.user_service.repository.premium.PremiumRepository;
import school.faang.user_service.service.user.UserService;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PremiumServiceTest {

  @InjectMocks
  private PremiumServiceImpl premiumService;
  @Mock
  private PremiumRepository premiumRepository;
  @Mock
  private UserService userService;
  @Spy
  private PremiumMapper premiumMapper = Mappers.getMapper(PremiumMapper.class);
  @Mock
  private BoughtPremiumPublisher boughtPremiumPublisher;
  @Mock
  private PaymentServiceClient paymentServiceClient;
  @Captor
  private ArgumentCaptor<Premium> premiumArgumentCaptor;
  @Captor
  private ArgumentCaptor<BoughtPremiumEventDto> eventArgumentCaptor;

  @Test
  @DisplayName("Should throw exception when user already has premium")
  void testNegativeBuyPremiumAlreadyExists() {
    PremiumPeriod period = PremiumPeriod.THREE_MONTHS;
    when(premiumRepository.existsByUserId(1L)).thenReturn(true);
    var exception = assertThrows(
        DataValidationException.class, () -> premiumService.buyPremium(1L, period));

    verify(premiumRepository, times(1)).existsByUserId(1L);
    verify(premiumRepository, times(1)).existsByUserId(1L);

    assertEquals("User already has premium subscription!", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when payment failed")
  void testNegativeBuyPremiumPaymentFailed() {
    PremiumPeriod period = PremiumPeriod.THREE_MONTHS;
    PaymentRequest request = PaymentRequest.builder()
        .amount(period.getPrice())
        .currencyCode("USD")
        .build();
    PaymentResponse response = PaymentResponse.builder()
        .status(PaymentStatus.FAILED)
        .build();

    when(premiumRepository.existsByUserId(1L)).thenReturn(false);
    when(paymentServiceClient.sendPayment(request)).thenReturn(response);

    var exception = assertThrows(
        DataValidationException.class, () -> premiumService.buyPremium(1L, period));

    verify(premiumRepository, times(1)).existsByUserId(1L);
    verify(paymentServiceClient, times(1)).sendPayment(request);

    assertEquals("Payment failed!", exception.getMessage());
  }

  @Test
  @DisplayName("Should save new premium")
  void testPositiveBuyPremiumPayment() {
    long userId = 99L;
    PremiumPeriod period = PremiumPeriod.THREE_MONTHS;
    PaymentRequest request = PaymentRequest.builder()
        .amount(period.getPrice())
        .currencyCode("USD")
        .build();
    PaymentResponse response = PaymentResponse.builder()
        .status(PaymentStatus.SUCCESS)
        .build();
    User user = User.builder()
        .id(userId)
        .active(true)
        .username("Ivam")
        .email("mail@mail.ru")
        .password("ccc")
        .country(new Country())
        .build();

    when(premiumRepository.existsByUserId(userId)).thenReturn(false);
    when(paymentServiceClient.sendPayment(request)).thenReturn(response);
    when(userService.getUserById(userId)).thenReturn(user);

    premiumService.buyPremium(userId, period);

    verify(premiumRepository, times(1)).existsByUserId(userId);
    verify(paymentServiceClient, times(1)).sendPayment(request);
    verify(premiumRepository, times(1)).save(premiumArgumentCaptor.capture());
    verify(boughtPremiumPublisher, times(1)).publish(eventArgumentCaptor.capture());

    Premium premium = premiumArgumentCaptor.getValue();
    BoughtPremiumEventDto eventDto = eventArgumentCaptor.getValue();

    assertEquals(userId, premium.getUser().getId());
    assertEquals(period.getDays(), eventDto.days());
  }
}