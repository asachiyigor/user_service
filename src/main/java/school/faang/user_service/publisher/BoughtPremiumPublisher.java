package school.faang.user_service.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.premium.BoughtPremiumEventDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoughtPremiumPublisher {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ChannelTopic boughtPremiumChannelTopic;

  public void publish(BoughtPremiumEventDto eventDto) {
    String topic = boughtPremiumChannelTopic.getTopic();
    redisTemplate.convertAndSend(topic, eventDto);
    log.info("Message {} send via {}", eventDto, topic);
  }

}
