package school.faang.user_service.config.context.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.json.student.DtoBanShema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import school.faang.user_service.service.user.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSubscriberForUserBan implements MessageListener {
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            userService.banUsers(objectMapper.readValue(message.getBody(), DtoBanShema.class).getIds());
            log.debug("user banned is finished!");
        } catch (IOException e) {
            log.error("Error processing message: {}", new String(message.getBody(), StandardCharsets.UTF_8), e);
        }
    }
}