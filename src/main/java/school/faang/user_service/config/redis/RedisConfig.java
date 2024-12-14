package school.faang.user_service.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import school.faang.user_service.listener.UserBanEventListener;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String host;

  @Value("${spring.data.redis.port}")
  private int port;

  @Value("${spring.data.redis.channels.search-appearance-channel.name}")
  private String searchAppearanceTopic;

  @Value("${spring.data.redis.channels.user-ban-channel.name}")
  private String userBanTopic;

  @Value("${spring.data.redis.channels.bought-premium.name}")
  private String boughtPremiumChannelTopic;

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
    return new JedisConnectionFactory(config);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(jedisConnectionFactory());
    redisTemplate.setDefaultSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

  @Bean
  public ChannelTopic searchAppearanceTopic() {
    return new ChannelTopic(searchAppearanceTopic);
  }

  @Bean
  public MessageListenerAdapter messageListenerForUserBan(
      UserBanEventListener userBanEventListener) {
    return new MessageListenerAdapter(userBanEventListener);
  }

  @Bean
  public ChannelTopic userBanTopic() {
    return new ChannelTopic(userBanTopic);
  }

  @Bean
  ChannelTopic boughtPremiumChannelTopic() {
    return new ChannelTopic(boughtPremiumChannelTopic);
  }
}