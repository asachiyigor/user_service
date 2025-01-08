package school.faang.user_service.publisher.profileView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import school.faang.user_service.dto.profileView.ProfileViewEventDto;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileViewEventPublisherTest {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private ProfileViewEventPublisher publisher;

    @Test
    void publish_ShouldSendMessageToRedis() {
        ProfileViewEventDto event = new ProfileViewEventDto(1L, 2L, "2025-01-08T18:43:42.671076");
        String topic = "profileView-topic";

        publisher.setTopic(topic);
        publisher.publish(event);

        verify(redisTemplate, times(1)).convertAndSend(topic, event);
    }
}
