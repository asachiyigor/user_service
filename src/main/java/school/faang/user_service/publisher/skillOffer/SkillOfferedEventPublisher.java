package school.faang.user_service.publisher.skillOffer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;
import school.faang.user_service.publisher.MessagePublish;

@Component
@RequiredArgsConstructor
public class SkillOfferedEventPublisher implements MessagePublish<SkillOfferedEvent> {
    private final ObjectMapper objectMapper;
    private final ReactiveRedisOperations<String, Object> redisTemplate;

    @Value("${spring.data.redis.channels.skillOffer-channel.name}")
    private String topic;

    @Override
    public void publish(SkillOfferedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(topic, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
