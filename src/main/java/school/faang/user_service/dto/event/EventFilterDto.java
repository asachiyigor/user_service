package school.faang.user_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class EventFilterDto {
    private String titleContains;
    private LocalDateTime startDateFrom;
    private LocalDateTime startDateTo;
    private List<Long> skillIds;
    private String location;
}