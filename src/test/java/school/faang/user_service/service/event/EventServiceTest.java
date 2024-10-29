package school.faang.user_service.service.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.event.EventDto;
import school.faang.user_service.dto.event.EventFilterDto;
import school.faang.user_service.dto.skill.SkillDto;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.UserSkillGuarantee;
import school.faang.user_service.entity.event.Event;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.EventMapper;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.event.EventRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    private EventDto eventDto;
    private EventDto eventDto2;
    private Event event;
    private Event event2;
    private User user;
    private List<Skill> userSkills;
    private List<SkillDto> eventSkills;
    private EventFilterDto filter;

    @BeforeEach
    void setUp() {
        long userId = 1L;
        long eventId = 1L;
        long eventId2 = 2L;

        Skill skillJava = new Skill();
        skillJava.setId(1L);
        skillJava.setTitle("Java");
        Skill skillKotlin = new Skill();
        skillKotlin.setId(2L);
        skillKotlin.setTitle("Kotlin");

        user = createUserWithSkills(userId, List.of(skillJava, skillKotlin));

        eventSkills = List.of(
                new SkillDto(1L, "Java"),
                new SkillDto(2L, "Kotlin"));

        eventDto = createEventDto(1L, userId, Set.of(
                new SkillDto(1L, "Java"),
                new SkillDto(2L, "Kotlin")
        ));

        eventDto2 = createEventDto(2L, userId, Set.of(
                new SkillDto(3L, "Python"),
                new SkillDto(4L, "GO")));

        event = new Event();
        event.setId(eventId);
        event.setTitle("Test Event");
        event.setRelatedSkills(user.getSkills());
        event.setStartDate(LocalDateTime.now().plusDays(3));
        event.setEndDate(LocalDateTime.now().plusDays(4));
        event.setLocation("Moscow");

        event2 = new Event();
        event2.setId(eventId2);
        event2.setTitle("Test Event2");
        event2.setRelatedSkills(user.getSkills());
        event2.setStartDate(LocalDateTime.now().plusDays(1));
        event2.setEndDate(LocalDateTime.now().plusDays(6));
        event2.setLocation("London");
    }

    @Test
    @DisplayName("Should create event successfully when user has all required skills")
    void createEvent_WithValidSkills_ShouldSucceed() {

        Event dtoEvent = new Event();
        Event entityEvent = new Event();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(eventMapper.toEntity(eventDto)).thenReturn(dtoEvent);
        when(eventRepository.save(dtoEvent)).thenReturn(entityEvent);
        when(eventMapper.toDto(entityEvent)).thenReturn(eventDto);

        // Act
        EventDto result = eventService.create(eventDto);

        // Assert
        assertNotNull(result);
        assertEquals(event.getId(), result.getId());
        assertEquals(user.getId(), result.getOwnerId());

        // Verify all interactions
        verify(userRepository).findById(user.getId());
        verify(eventMapper).toEntity(eventDto);
        verify(eventRepository).save(dtoEvent);
        verify(eventMapper).toDto(entityEvent);
    }

    @Test
    @DisplayName("Should throw exception when user doesn't have required skills")
    void createEvent_WithInvalidSkills_ShouldThrowException() {
        // Arrange
        long userId = 1L;

        // User has only JS skill
        User user = createUserWithSkills(userId, List.of(
                new Skill(3L, "JS", List.of(new User(), new User()), List.of(new UserSkillGuarantee(), new UserSkillGuarantee()), List.of(new Event(), new Event()), List.of(new Goal()), LocalDateTime.now(), LocalDateTime.now())
        ));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        DataValidationException exception = assertThrows(
                DataValidationException.class,
                () -> eventService.create(eventDto)
        );

        assertTrue(exception.getMessage().contains("doesn't have all required skills"));
        verify(userRepository).findById(userId);
        verify(eventMapper, never()).toEntity(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user not found during event creation")
    void createEvent_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        DataValidationException exception = assertThrows(
                DataValidationException.class,
                () -> eventService.create(eventDto)
        );

        assertTrue(exception.getMessage().contains("User with id " + userId + " not found"));
        verify(userRepository).findById(userId);
        verify(eventMapper, never()).toEntity(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when mapper fails during event creation")
    void createEvent_WhenMapperFails_ShouldThrowException() {

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(eventMapper.toEntity(eventDto)).thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> eventService.create(eventDto)
        );

        assertEquals("Mapping error", exception.getMessage());
        verify(userRepository).findById(user.getId());
        verify(eventMapper).toEntity(eventDto);
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when repository fails during event creation")
    void createEvent_WhenRepositoryFails_ShouldThrowException() {
        // Arrange
        Event mappedEvent = new Event();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(eventMapper.toEntity(eventDto)).thenReturn(mappedEvent);
        when(eventRepository.save(mappedEvent)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> eventService.create(eventDto)
        );

        assertEquals("Database error", exception.getMessage());
        verify(userRepository).findById(user.getId());
        verify(eventMapper).toEntity(eventDto);
        verify(eventRepository).save(mappedEvent);
    }

    @Test
    @DisplayName("Should return all events when no filters applied")
    void getEventsByFilter_WithNoFilters_ShouldReturnAllEvents() {
        // Arrange
        filter = new EventFilterDto(); // пустой фильтр
        List<Event> events = Arrays.asList(event, event2);

        when(eventRepository.findAll()).thenReturn(events);
        when(eventMapper.toDto(event)).thenReturn(eventDto);
        when(eventMapper.toDto(event2)).thenReturn(eventDto2);

        // Act
        List<EventDto> result = eventService.getEventsByFilter(filter);

        // Assert
        assertEquals(2, result.size());
        verify(eventRepository).findAll();
        verify(eventMapper, times(1)).toDto(event);
        verify(eventMapper, times(1)).toDto(event2);
    }

    @Test
    @DisplayName("Should return filtered events when date range filter applied")
    void getEventsByFilter_WithDateRange_ShouldReturnFilteredEvents() {
        // Arrange
        filter = new EventFilterDto();
        filter.setStartDateFrom(LocalDateTime.now().plusDays(1));
        filter.setStartDateTo(LocalDateTime.now().plusDays(3));

        List<Event> events = Arrays.asList(event, event2);

        when(eventRepository.findAll()).thenReturn(events);
        when(eventMapper.toDto(event)).thenReturn(eventDto2);

        // Act
        List<EventDto> result = eventService.getEventsByFilter(filter);

        // Assert
        assertEquals(1, result.size());
        verify(eventRepository).findAll();
        verify(eventMapper, times(1)).toDto(event);
        verify(eventMapper, never()).toDto(event2);
    }

    @Test
    @DisplayName("Should return filtered events when location filter applied")
    void getEventsByFilter_WithLocation_ShouldReturnFilteredEvents() {
        // Arrange
        filter = new EventFilterDto();
        filter.setLocation("Moscow");

        List<Event> events = Arrays.asList(event, event2);

        when(eventRepository.findAll()).thenReturn(events);
        when(eventMapper.toDto(event)).thenReturn(eventDto);

        // Act
        List<EventDto> result = eventService.getEventsByFilter(filter);

        // Assert
        assertEquals(1, result.size());
        verify(eventRepository).findAll();
        verify(eventMapper, times(1)).toDto(event);
        verify(eventMapper, never()).toDto(event2);
    }

    @Test
    @DisplayName("Should return filtered events when skills filter applied")
    void getEventsByFilter_WithSkills_ShouldReturnFilteredEvents() {
        // Arrange
        filter = new EventFilterDto();
        filter.setSkillIds(Collections.singletonList(1L));

        List<Event> events = Arrays.asList(event, event2);

        when(eventRepository.findAll()).thenReturn(events);
        when(eventMapper.toDto(event)).thenReturn(eventDto);

        // Act
        List<EventDto> result = eventService.getEventsByFilter(filter);

        assertEquals(2, result.size(), "Should return exactly one event matching all filter criteria");
        EventDto returnedEvent = result.get(0);
        assertEquals("Test Event", returnedEvent.getTitle(), "Should return event with matching title");

        verify(eventRepository).findAll();
        verify(eventMapper, times(1)).toDto(event);
        verify(eventMapper, never()).toDto(event);

        assertTrue(event.getTitle().toLowerCase().contains(filter.getTitleContains().toLowerCase()),
                "Event title should contain filter text");
        assertEquals(event.getLocation().toLowerCase(), filter.getLocation().toLowerCase(),
                "Event location should match filter");
        assertTrue(event.getRelatedSkills().stream()
                        .map(Skill::getId)
                        .anyMatch(filter.getSkillIds()::contains),
                "Event should have matching skill ID");
    }

    @Test
    @DisplayName("Should return event when valid ID provided")
    void getEvent_WithValidId_ShouldReturnEvent() {
        // given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventMapper.toDto(event)).thenReturn(eventDto);

        // when
        EventDto result = eventService.getEvent(1L);

        // then
        assertNotNull(result);
        assertEquals(eventDto.getId(), result.getId());
    }

    @Test
    @DisplayName("Should throw exception when getting event with invalid ID")
    void getEvent_WithInvalidId_ShouldThrowException() {
        // given
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(DataValidationException.class, () -> eventService.getEvent(999L));
    }

    @Test
    @DisplayName("Should delete event successfully when valid ID provided")
    void deleteEvent_WithValidId_ShouldSucceed() {
        // given
        when(eventRepository.existsById(1L)).thenReturn(true);

        // when
        eventService.deleteEvent(1L);

        // then
        verify(eventRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting event with invalid ID")
    void deleteEvent_WithInvalidId_ShouldThrowException() {
        // given
        when(eventRepository.existsById(999L)).thenReturn(false);

        // when & then
        assertThrows(DataValidationException.class, () -> eventService.deleteEvent(999L));
    }

    @Test
    void updateEvent_ValidData_Success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventMapper.toEntity(eventDto)).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toDto(event)).thenReturn(eventDto);

        // when
        EventDto result = eventService.updateEvent(eventDto);

        // then
        assertNotNull(result);
        assertEquals(eventDto.getId(), result.getId());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Should update event successfully when valid data provided")
    void updateEvent_WithValidData_ShouldSucceed() {
        // given
        List<Event> events = Arrays.asList(event);
        when(eventRepository.findAllByUserId(1L)).thenReturn(events);
        when(eventMapper.toDto(event)).thenReturn(eventDto);

        // when
        List<EventDto> results = eventService.getOwnedEvents(1L);

        // then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(eventDto.getId(), results.get(0).getId());
    }

    @Test
    @DisplayName("Should return owned events for given user ID")
    void getOwnedEvents_WithUserId_ShouldReturnUserEvents() {
        // given
        List<Event> events = Arrays.asList(event);
        when(eventRepository.findParticipatedEventsByUserId(1L)).thenReturn(events);
        when(eventMapper.toDto(event)).thenReturn(eventDto);

        // when
        List<EventDto> results = eventService.getParticipatedEvents(1L);

        // then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(eventDto.getId(), results.get(0).getId());
    }

    private User createUserWithSkills(Long id, List<Skill> skills) {
        User user = new User();
        user.setId(id);
        user.setSkills(skills);
        return user;
    }

    private EventDto createEventDto(Long id, Long userId, Set<SkillDto> skills) {
        EventDto eventDto = new EventDto();
        eventDto.setId(id);
        eventDto.setOwnerId(userId);
        eventDto.setRelatedSkills(eventSkills);
        return eventDto;
    }
}