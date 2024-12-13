package school.faang.user_service.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolConfig {
    @Value("${event-cleanup.threadNumber}")
    private int numberOfThreads;

    @Bean
    public ExecutorService eventThreadPool() {
        return Executors.newFixedThreadPool(numberOfThreads);
    }
}
