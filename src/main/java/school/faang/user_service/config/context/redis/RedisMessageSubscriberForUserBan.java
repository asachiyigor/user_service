package school.faang.user_service.config.context.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import school.faang.user_service.service.user.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RedisMessageSubscriberForUserBan implements MessageListener {
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            List<Long> idsForBan = objectMapper.readValue(message.getBody(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
            if (idsForBan != null && !idsForBan.isEmpty()) {
                userService.banUsers(idsForBan);
                log.debug("UserService finished is true!");
            } else {
                log.warn("Received empty or null ID list for banning");
            }
        } catch (IOException e) {
            // ошибку не выбрасываем, иначе можно прервать выполнение из списка редиски
            // либо можно зациклить, если настроен на повторный запрос, а это не айс
            log.error("Error processing message: {}", new String(message.getBody(), StandardCharsets.UTF_8), e);
        }
    }
}
