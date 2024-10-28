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
import school.faang.user_service.service.event.EventService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public EventDto create(@RequestBody EventDto event) {
        return eventService.create(event);
    }

    @GetMapping("/events/{id}")
    public EventDto getEvent(@PathVariable Long id) {
        return eventService.getEvent(id);
    }

    @GetMapping("events/filter")
    public List<EventDto> getEventsByFilter(@RequestBody EventFilterDto filter) {
        return eventService.getEventsByFilter(filter);
    }

    @DeleteMapping("events/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    @PutMapping
    public EventDto updateEvent(@RequestBody EventDto event) {
        return eventService.updateEvent(event);
    }

    @GetMapping("events/owned/{userId}")
    public List<EventDto> getOwnedEvents(@PathVariable Long userId) {
        return eventService.getOwnedEvents(userId);
    }

    @GetMapping("events/participated/{userId}")
    public List<EventDto> getParticipatedEvents(@PathVariable Long userId) {
        return eventService.getParticipatedEvents(userId);
    }
}