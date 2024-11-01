package school.faang.user_service.controller.event;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.event.EventDto;
import school.faang.user_service.dto.event.EventFilterDto;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.service.event.EventService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @PostMapping()
    public EventDto create(@RequestBody EventDto event) {
        validateEvent(event);
        return eventService.create(event);
    }

    @GetMapping("/{id}")
    public EventDto getEvent(@PathVariable Long id) {
        return eventService.getEvent(id);
    }

    @GetMapping("/filter")
    public List<EventDto> getEventsByFilter(@RequestBody EventFilterDto filter) {
        return eventService.getEventsByFilter(filter);
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    @PutMapping
    public EventDto updateEvent(@RequestBody EventDto event) {
        validateEvent(event);
        return eventService.updateEvent(event);
    }

    @GetMapping("/owned/{userId}")
    public List<EventDto> getOwnedEvents(@PathVariable Long userId) {
        return eventService.getOwnedEvents(userId);
    }

    @GetMapping("/participated/{userId}")
    public List<EventDto> getParticipatedEvents(@PathVariable Long userId) {
        return eventService.getParticipatedEvents(userId);
    }

    private void validateEvent(EventDto event) {
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new DataValidationException("Event title cannot be empty");
        }
        if (event.getStartDate() == null) {
            throw new DataValidationException("Event start date cannot be null");
        }
        if (event.getOwnerId() == null) {
            throw new DataValidationException("Event owner cannot be null");
        }
    }
}