package school.faang.user_service.puiblisher.followerEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.event.ProjectFollowerEvent;
import school.faang.user_service.puiblisher.MessagePublish;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectFollowerEventPublisher implements MessagePublish<ProjectFollowerEvent> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.data.redis.channels.project-follower-channel.name}")
    private String projectFollowerChannel;

    @Override
    public void publish(ProjectFollowerEvent event) {
        try {
            redisTemplate.convertAndSend(projectFollowerChannel, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.error("Can`t parse json: ", e);
        }
    }
}
