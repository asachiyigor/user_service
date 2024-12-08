package school.faang.user_service.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import school.faang.user_service.service.event.EventService;

@RequiredArgsConstructor
@Configuration
public class Scheduler {
    private final EventService eventService;

    @Scheduled(cron = "#{cleanupConfig.getCronExpression()}")
    public void clearEvents() {
        eventService.clearEvents();
    }
}
