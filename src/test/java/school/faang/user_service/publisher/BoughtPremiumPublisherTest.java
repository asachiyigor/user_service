package school.faang.user_service.publisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import school.faang.user_service.dto.premium.BoughtPremiumEventDto;

@ExtendWith(MockitoExtension.class)
class BoughtPremiumPublisherTest {

  @InjectMocks
  private BoughtPremiumPublisher boughtPremiumPublisher;
  @Mock
  private RedisTemplate<String, Object> redisTemplate;
  @Mock
  private ChannelTopic channelTopic;

  @Test
  @DisplayName("Should send event message to redis")
  void testPublish() {
    BoughtPremiumEventDto eventDto = BoughtPremiumEventDto.builder()
        .userId(1L)
        .sum(BigDecimal.valueOf(10))
        .days(30)
        .receivedAt("2024-12-12 12:12:12")
        .build();
    String topic = "bought_premium_channel";

    when(channelTopic.getTopic()).thenReturn(topic);
    boughtPremiumPublisher.publish(eventDto);

    verify(redisTemplate, times(1)).convertAndSend(topic, eventDto);
  }
}