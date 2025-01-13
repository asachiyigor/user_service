package school.faang.user_service.publisher.profileView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.profileView.ProfileViewEventDto;
import school.faang.user_service.puiblisher.MessagePublish;

@Component
@RequiredArgsConstructor
@Setter
public class ProfileViewEventPublisher implements MessagePublish<ProfileViewEventDto> {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Logger log = LoggerFactory.getLogger(ProfileViewEventPublisher.class);

    @Value("${spring.data.redis.channels.profileView-channel.name}")
    private String topic;

    @Override
    public void publish(ProfileViewEventDto event) {
        redisTemplate.convertAndSend(topic, event);
        log.info("Published event to Redis: {}", event);
    }
}
