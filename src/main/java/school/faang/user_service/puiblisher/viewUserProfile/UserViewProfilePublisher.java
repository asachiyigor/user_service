package school.faang.user_service.puiblisher.viewUserProfile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.event.UserProfileEvent;
import school.faang.user_service.puiblisher.MessagePublish;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserViewProfilePublisher implements MessagePublish<UserProfileEvent> {

    private final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    private final ObjectMapper objectMapper;

    @Value("${spring.data.redis.channels.user-view-channel.name}")
    private String userProfileEventChannel;


    @Override
    public void publish(UserProfileEvent event) {
        try {
            redisTemplate.convertAndSend(userProfileEventChannel, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.error("Can`t parse json UserProfileEvent: ", e);
        }
    }
}
