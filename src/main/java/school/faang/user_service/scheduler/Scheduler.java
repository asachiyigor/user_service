package school.faang.user_service.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import school.faang.user_service.service.event.EventService;

@RequiredArgsConstructor
@Component
public class Scheduler {
    private final EventService eventService;

    @Scheduled(cron = "#{cleanupConfig.getCronExpression()}")
    public void clearEvents() {
        eventService.clearEvents();
    }
}
