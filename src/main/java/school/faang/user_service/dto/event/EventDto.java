package school.faang.user_service.dto.event;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import school.faang.user_service.dto.skill.SkillDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
public class EventDto {
    private Long id;

    @NotBlank(message = "Event title cannot be empty")
    private String title;

    @NotNull(message = "Start date cannot be null")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    private String description;

    private List<SkillDto> relatedSkills;

    private String location;

    @Min(value = 1, message = "Maximum attendees must be at least 1")
    private int maxAttendees;
}