package school.faang.user_service.controller.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import school.faang.user_service.dto.event.EventDto;
import school.faang.user_service.dto.event.EventFilterDto;
import school.faang.user_service.service.event.EventService;
import school.faang.user_service.util.TestDataFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@WebMvcTest(EventController.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    private static EventDto eventDto;

    @BeforeEach
    void setUp() {
        eventDto = TestDataFactory.createDefaultEventDto(TestDataFactory.EVENT_ID_1);
    }

    @Test
    public void getEvent_ExistingId_ReturnsEvent() throws Exception {

        when(eventService.getEvent(1L)).thenReturn(eventDto);

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventDto.getId()))
                .andExpect(jsonPath("$.title").value(eventDto.getTitle()));

        verify(eventService).getEvent(1L);
    }

    @Test
    public void getEventsByFilter_ValidFilter_ReturnsFilteredEvents() throws Exception {
        EventFilterDto filterDto = new EventFilterDto();
        filterDto.setTitleContains("Test");
        List<EventDto> filteredEvents = List.of(eventDto);

        when(eventService.getEventsByFilter(any(EventFilterDto.class))).thenReturn(filteredEvents);

        mockMvc.perform(get("/api/events/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(filteredEvents.get(0).getId()))
                .andExpect(jsonPath("$[0].title").value(filteredEvents.get(0).getTitle()));

        verify(eventService).getEventsByFilter(any(EventFilterDto.class));
    }

    @Test
    public void deleteEvent_ExistingId_ReturnsNoContent() throws Exception {
        doNothing().when(eventService).deleteEvent(1L);

        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isOk());

        verify(eventService).deleteEvent(1L);
    }

    @Test
    public void getOwnedEvents_ValidUserId_ReturnsUserEvents() throws Exception {
        List<EventDto> ownedEvents = List.of(eventDto);
        when(eventService.getOwnedEvents(1L)).thenReturn(ownedEvents);

        mockMvc.perform(get("/api/events/owned/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ownedEvents.get(0).getId()))
                .andExpect(jsonPath("$[0].title").value(ownedEvents.get(0).getTitle()));

        verify(eventService).getOwnedEvents(1L);
    }

    @Test
    public void getParticipatedEvents_ValidUserId_ReturnsParticipatedEvents() throws Exception {
        List<EventDto> participatedEvents = List.of(eventDto);
        when(eventService.getParticipatedEvents(1L)).thenReturn(participatedEvents);

        mockMvc.perform(get("/api/events/participated/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(participatedEvents.get(0).getId()))
                .andExpect(jsonPath("$[0].title").value(participatedEvents.get(0).getTitle()));

        verify(eventService).getParticipatedEvents(1L);
    }
}