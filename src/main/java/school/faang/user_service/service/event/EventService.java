package school.faang.user_service.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.dto.event.EventDto;
import school.faang.user_service.dto.event.EventFilterDto;
import school.faang.user_service.dto.skill.SkillDto;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.event.Event;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.event.EventMapper;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.event.EventRepository;
import school.faang.user_service.service.config.CleanupConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final CleanupConfig cleanupConfig;
    private final ExecutorService executorService;

    public EventDto create(EventDto event) {
        validateUserSkills(event);
        return eventMapper.toDto(eventRepository.save(eventMapper.toEntity(event)));
    }

    public EventDto getEvent(long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new DataValidationException("Event not found with id: " + eventId));
        return eventMapper.toDto(event);
    }

    public List<EventDto> getEventsByFilter(EventFilterDto filter) {
        List<Event> events = eventRepository.findAll();
        return filterEvents(events, filter).stream()
                .map(eventMapper::toDto)
                .toList();
    }

    public void deleteEvent(long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new DataValidationException("Event not found with id: " + eventId);
        }
        eventRepository.deleteById(eventId);
    }

    public EventDto updateEvent(EventDto eventDto) {
        validateUserSkills(eventDto);
        eventRepository.findById(eventDto.getId())
                .orElseThrow(() -> new DataValidationException("Event not found"));
        Event updatedEvent = eventMapper.toEntity(eventDto);
        Event savedEvent = eventRepository.save(updatedEvent);
        return eventMapper.toDto(savedEvent);
    }

    public List<EventDto> getOwnedEvents(long userId) {
        List<Event> events = eventRepository.findAllByUserId(userId);
        return events.stream()
                .map(eventMapper::toDto)
                .toList();
    }

    public List<EventDto> getParticipatedEvents(long userId) {
        List<Event> events = eventRepository.findParticipatedEventsByUserId(userId);
        return events.stream()
                .map(eventMapper::toDto)
                .toList();
    }

    @Transactional
    @Async("eventThreadPool")
    public void clearEvents() {
        log.info("Clear events job started");
        EventFilterDto eventFilterDto = EventFilterDto.builder()
                .endDateTo(LocalDateTime.now())
                .build();
        List<Long> eventsIds = getEventsByFilter(eventFilterDto)
                .stream()
                .map(EventDto::getId)
                .collect(Collectors.toList());

        int chunkSize = cleanupConfig.getChunkSize();
        for (int i = 0; i < eventsIds.size(); i += chunkSize) {
            List<Long> chunk = eventsIds.subList(i, Math.min(i + chunkSize, eventsIds.size()));
            executorService.submit(() -> eventRepository.deleteAllById(chunk));
        }
    }

    private void validateUserSkills(EventDto event) {
        User owner = userRepository.findById(event.getOwnerId())
                .orElseThrow(() -> new DataValidationException(
                        String.format("User with id %d not found", event.getOwnerId())
                ));

        Set<String> userSkills = extractUserSkills(owner);
        Set<String> eventSkills = extractEventSkills(event);

        if (!userSkills.containsAll(eventSkills)) {
            throw new DataValidationException(
                    String.format("User %d doesn't have all required skills for event %d",
                            owner.getId(), event.getId())
            );
        }
    }

    private Set<String> extractUserSkills(User user) {
        return user.getSkills().stream()
                .map(Skill::getTitle)
                .collect(Collectors.toSet());
    }

    private Set<String> extractEventSkills(EventDto eventDto) {
        return eventDto.getRelatedSkills().stream()
                .map(SkillDto::getTitle)
                .collect(Collectors.toSet());
    }

    private List<Event> filterEvents(List<Event> events, EventFilterDto filter) {
        return events.stream()
                .filter(event -> matchesAllFilters(event, filter))
                .toList();
    }

    private boolean matchesAllFilters(Event event, EventFilterDto filter) {
        return matchesTitle(event, filter)
                && matchesDateRange(event, filter)
                && matchesSkills(event, filter)
                && matchesLocation(event, filter)
                && matchesEndDateBefore(event, filter);
    }

    private boolean matchesTitle(Event event, EventFilterDto filter) {
        return filter.getTitleContains() == null ||
                event.getTitle().toLowerCase().contains(filter.getTitleContains().toLowerCase());
    }

    private boolean matchesDateRange(Event event, EventFilterDto filter) {
        boolean afterStartDate = filter.getStartDateFrom() == null ||
                !event.getStartDate().isBefore(filter.getStartDateFrom());

        boolean beforeEndDate = filter.getStartDateTo() == null ||
                !event.getStartDate().isAfter(filter.getStartDateTo());

        return afterStartDate && beforeEndDate;
    }

    private boolean matchesEndDateBefore(Event event, EventFilterDto filter) {
        return filter.getEndDateTo() == null ||
                event.getEndDate().isBefore(filter.getEndDateTo());
    }

    private boolean matchesSkills(Event event, EventFilterDto filter) {
        return filter.getSkillIds() == null ||
                event.getRelatedSkills().stream()
                        .map(Skill::getId)
                        .anyMatch(filter.getSkillIds()::contains);
    }

    private boolean matchesLocation(Event event, EventFilterDto filter) {
        return filter.getLocation() == null ||
                event.getLocation().equalsIgnoreCase(filter.getLocation());
    }
}