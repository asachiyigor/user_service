package school.faang.user_service.controller.premium;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import school.faang.user_service.dto.premium.PremiumDto;
import school.faang.user_service.entity.premium.PremiumPeriod;
import school.faang.user_service.service.premium.PremiumService;

@WebMvcTest
@ContextConfiguration(classes = {PremiumController.class})
class PremiumControllerTest {

  private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @MockBean
  private PremiumService premiumService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("Should return added Premium dto")
  void testBuyPremium() throws Exception {
    long userId = 10L;
    int days = 30;
    PremiumPeriod period = PremiumPeriod.fromDays(days);
    PremiumDto createDto = createPremiumDto(userId, period);
    PremiumDto responseDto = responsePremiumDto(createDto);

    when(premiumService.buyPremium(userId, period)).thenReturn(responseDto);

    mockMvc.perform(post("/api/v1/premium/buy/{days}", days)
            .header("x-user-id", userId))
        .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(responseDto)))
        .andExpect(status().isOk());

    verify(premiumService, times(1)).buyPremium(userId, period);
  }

  private PremiumDto createPremiumDto(long userId, PremiumPeriod premiumPlan) {
    LocalDateTime startDate = LocalDateTime.now();
    return PremiumDto.builder()
        .userId(userId)
        .startDate(startDate.toString())
        .endDate(startDate.plusDays(premiumPlan.getDays()).toString())
        .build();
  }

  private PremiumDto responsePremiumDto(PremiumDto dto) {
    return PremiumDto.builder()
        .id(1L)
        .userId(dto.userId())
        .startDate(dto.startDate())
        .endDate(dto.endDate())
        .build();
  }

}