package school.faang.user_service.service.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Getter
@Configuration
@EnableScheduling
public class CleanupConfig {
    @Value("${event-cleanup.chunkSize}")
    private int chunkSize;
    @Value("${event-cleanup.cron.expression}")
    private String cronExpression;
}