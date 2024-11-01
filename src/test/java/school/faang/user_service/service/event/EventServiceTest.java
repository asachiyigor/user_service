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
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.event.Event;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.EventMapper;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.event.EventRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static school.faang.user_service.util.TestDataFactory.createDefaultFilter;
import static school.faang.user_service.util.TestDataFactory.createDefaultUser;
import static school.faang.user_service.util.TestDataFactory.createUserWithInvalidSkills;
import static school.faang.user_service.util.TestDataFactory.createDefaultEventDto;
import static school.faang.user_service.util.TestDataFactory.createDefaultEvent;
import static school.faang.user_service.util.TestDataFactory.EVENT_ID_1;
import static school.faang.user_service.util.TestDataFactory.EVENT_ID_2;
import static school.faang.user_service.util.TestDataFactory.MOSCOW;
import static school.faang.user_service.util.TestDataFactory.LONDON;
import static school.faang.user_service.util.TestDataFactory.USER_ID;

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

    private EventDto eventDto1;
    private EventDto eventDto2;
    private Event event1;
    private Event event2;
    private User user;
    private User user2;
    private EventFilterDto filter;

    @BeforeEach
    void setUp() {
        user = createDefaultUser();
        user2 = createUserWithInvalidSkills();
        eventDto1 = createDefaultEventDto(EVENT_ID_1);
        eventDto2 = createDefaultEventDto(EVENT_ID_2);
        event1 = createDefaultEvent(EVENT_ID_1, MOSCOW);
        event2 = createDefaultEvent(EVENT_ID_2, LONDON);
    }

    @Test
    @DisplayName("Should create event successfully when user has all required skills")
    void createEvent_WithValidSkills_ShouldSucceed() {

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(eventMapper.toEntity(eventDto1)).thenReturn(event1);
        when(eventRepository.save(event1)).thenReturn(event1);
        when(eventMapper.toDto(event1)).thenReturn(eventDto1);

        // Act
        EventDto result = eventService.create(eventDto1);

        // Verify all interactions
        verify(userRepository).findById(USER_ID);
        verify(eventMapper).toEntity(eventDto1);
        verify(eventRepository).save(event1);
        verify(eventMapper).toDto(event1);

        // Assert
        assertNotNull(result);
        assertEquals(event1.getId(), result.getId());
        assertEquals(USER_ID, result.getOwnerId());
    }

    @Test
    @DisplayName("Should throw exception when user doesn't have required skills")
    void createEvent_WithInvalidSkills_ShouldThrowException() {

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user2));

        // Act & Assert
        DataValidationException exception = assertThrows(
                DataValidationException.class,
                () -> eventService.create(eventDto1)
        );

        // Verify all interactions
        verify(userRepository).findById(USER_ID);
        verify(eventMapper, never()).toEntity(any());
        verify(eventRepository, never()).save(any());

        // Assert
        assertTrue(exception.getMessage().contains("doesn't have all required skills"));
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
                () -> eventService.create(eventDto1)
        );

        // Verify all interactions
        verify(userRepository).findById(userId);
        verify(eventMapper, never()).toEntity(any());
        verify(eventRepository, never()).save(any());

        // Assert
        assertTrue(exception.getMessage().contains("User with id " + userId + " not found"));
    }

    @Test
    @DisplayName("Should throw exception when mapper fails during event creation")
    void createEvent_WhenMapperFails_ShouldThrowException() {

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(eventMapper.toEntity(eventDto1)).thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> eventService.create(eventDto1)
        );

        // Verify all interactions
        verify(userRepository).findById(user.getId());
        verify(eventMapper).toEntity(eventDto1);
        verify(eventRepository, never()).save(any());

        // Assert
        assertEquals("Mapping error", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when repository fails during event creation")
    void createEvent_WhenRepositoryFails_ShouldThrowException() {
        // Arrange
        Event mappedEvent = new Event();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(eventMapper.toEntity(eventDto1)).thenReturn(mappedEvent);
        when(eventRepository.save(mappedEvent)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> eventService.create(eventDto1)
        );

        // Verify all interactions
        verify(userRepository).findById(user.getId());
        verify(eventMapper).toEntity(eventDto1);
        verify(eventRepository).save(mappedEvent);

        // Assert
        assertEquals("Database error", exception.getMessage());
    }

    @Test
    @DisplayName("Should return all events when no filters applied")
    void getEventsByFilter_WithNoFilters_ShouldReturnAllEvents() {
        // Arrange
        filter = new EventFilterDto();
        List<Event> events = Arrays.asList(event1, event2);

        when(eventRepository.findAll()).thenReturn(events);
        when(eventMapper.toDto(event1)).thenReturn(eventDto1);
        when(eventMapper.toDto(event2)).thenReturn(eventDto2);

        // Act
        List<EventDto> result = eventService.getEventsByFilter(filter);

        // Verify all interactions
        verify(eventRepository).findAll();
        verify(eventMapper, times(1)).toDto(event1);
        verify(eventMapper, times(1)).toDto(event2);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return filtered events when date range filter applied")
    void getEventsByFilter_WithDateRange_ShouldReturnFilteredEvents() {
        // Arrange
        filter = createDefaultFilter();
        List<Event> events = Arrays.asList(event1, event2);

        when(eventRepository.findAll()).thenReturn(events);
        when(eventMapper.toDto(event1)).thenReturn(eventDto1);

        // Act
        List<EventDto> result = eventService.getEventsByFilter(filter);

        // Verify all interactions
        verify(eventRepository).findAll();
        verify(eventMapper, times(1)).toDto(event1);
        verify(eventMapper, never()).toDto(event2);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return filtered events when location filter applied")
    void getEventsByFilter_WithLocation_ShouldReturnFilteredEvents() {
        // Arrange
        filter = new EventFilterDto();
        filter.setLocation(MOSCOW);

        List<Event> events = Arrays.asList(event1, event2);

        when(eventRepository.findAll()).thenReturn(events);
        when(eventMapper.toDto(event1)).thenReturn(eventDto1);

        // Act
        List<EventDto> result = eventService.getEventsByFilter(filter);

        // Verify all interactions
        verify(eventRepository).findAll();
        verify(eventMapper, times(1)).toDto(event1);
        verify(eventMapper, never()).toDto(event2);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return filtered events when skills filter applied")
    void getEventsByFilter_WithSkills_ShouldReturnFilteredEvents() {
        // Arrange
        filter = new EventFilterDto();
        filter.setSkillIds(Collections.singletonList(1L));
        filter.setTitleContains("Test Event");

        List<Event> events = Arrays.asList(event1, event2);

        when(eventRepository.findAll()).thenReturn(events);
        when(eventMapper.toDto(event1)).thenReturn(eventDto1);

        // Act
        List<EventDto> result = eventService.getEventsByFilter(filter);

        EventDto returnedEvent = result.get(0);
        assertTrue(event1.getTitle().toLowerCase().contains(filter.getTitleContains().toLowerCase()));
        assertEquals("Test Event", returnedEvent.getTitle());

        // Verify all interactions
        verify(eventRepository).findAll();
        verify(eventMapper, times(1)).toDto(event1);

        // Assert
        assertTrue(event1.getRelatedSkills().stream()
                .map(Skill::getId)
                .anyMatch(filter.getSkillIds()::contains));
    }

    @Test
    @DisplayName("Should return event when valid ID provided")
    void getEvent_WithValidId_ShouldReturnEvent() {
        // given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));
        when(eventMapper.toDto(event1)).thenReturn(eventDto1);

        // when
        EventDto result = eventService.getEvent(1L);

        // then
        assertNotNull(result);
        assertEquals(eventDto1.getId(), result.getId());
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
    @DisplayName("Should update event successfully when valid data provided")
    void updateEvent_WithValidData_ShouldSucceed() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));
        when(eventMapper.toEntity(eventDto1)).thenReturn(event1);
        when(eventRepository.save(any(Event.class))).thenReturn(event1);
        when(eventMapper.toDto(event1)).thenReturn(eventDto1);

        // when
        EventDto result = eventService.updateEvent(eventDto1);

        // then
        assertNotNull(result);
        assertEquals(eventDto1.getId(), result.getId());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Should throw exception when event not found")
    void updateEvent_WhenEventNotFound_ThrowsException() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DataValidationException.class, () -> {
            eventService.updateEvent(eventDto1);
        });

        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("Should return owned events for given user ID")
    void getOwnedEvents_WithUserId_ShouldReturnUserEvents() {
        // given
        List<Event> events = Collections.singletonList(event1);
        when(eventRepository.findAllByUserId(1L)).thenReturn(events);
        when(eventMapper.toDto(event1)).thenReturn(eventDto1);

        // when
        List<EventDto> results = eventService.getOwnedEvents(1L);

        // then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(eventDto1.getId(), results.get(0).getId());
    }

    @Test
    @DisplayName("Should return participated events for given user ID")
    void getParticipatedEvents_WithUserId_ShouldReturnEvents() {
        // given
        List<Event> events = Collections.singletonList(event1);
        when(eventRepository.findParticipatedEventsByUserId(1L)).thenReturn(events);
        when(eventMapper.toDto(event1)).thenReturn(eventDto1);

        // when
        List<EventDto> results = eventService.getParticipatedEvents(1L);

        // then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(eventDto1.getId(), results.get(0).getId());
    }
}