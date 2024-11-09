package school.faang.user_service.controller.event;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.UserDto;
import school.faang.user_service.service.event.EventParticipationServiceInterface;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventParticipationController {
    private final EventParticipationServiceInterface eventParticipationServiceInterface;

    @PostMapping("register/{eventId}")
    public void registerParticipant(@PathVariable @Min(1) long eventId, @RequestParam @Min(1) long userId) {
        eventParticipationServiceInterface.registerParticipant(eventId, userId);
    }

    @DeleteMapping("unregister/{eventId}")
    public void unregisterParticipant(@PathVariable @Min(1) long eventId, @RequestParam @Min(1) long userId) {
        eventParticipationServiceInterface.unregisterParticipant(eventId, userId);
    }

    @GetMapping("countParticipants/{eventId}")
    public int getParticipantsCount(@PathVariable @Min(1) long eventId) {
        return eventParticipationServiceInterface.getParticipantsCount(eventId);
    }

    @GetMapping("participants/{eventId}")
    public List<UserDto> getParticipant(@PathVariable @Min(1) long eventId) {
        return eventParticipationServiceInterface.getParticipantsList(eventId);
    }
}
