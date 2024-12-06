package school.faang.user_service.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import school.faang.user_service.listener.UserBanEventListener;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    private final ObjectMapper objectMapper;

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.channels.search-appearance-channel.name}")
    private String searchAppearanceTopic;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        return new JedisConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return redisTemplate;
    }

    @Bean
    public ChannelTopic searchAppearanceTopic() {
        return new ChannelTopic(searchAppearanceTopic);
    }

    @Value("${spring.data.redis.channels.user-ban-channel.name}")
    private String userBanTopic;

    @Bean
    public MessageListenerAdapter messageListenerForUserBan(UserBanEventListener userBanEventListener) {
        return new MessageListenerAdapter(userBanEventListener);
    }

    @Bean
    public ChannelTopic banUserChannelTopic() {
        return new ChannelTopic(userBanTopic);
    }

    @Bean
    RedisMessageListenerContainer containerForUserBan(MessageListenerAdapter messageListenerForUserBan) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(jedisConnectionFactory());
        container.addMessageListener(messageListenerForUserBan, banUserChannelTopic());
        return container;
    }
}