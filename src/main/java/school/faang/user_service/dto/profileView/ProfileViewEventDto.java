package school.faang.user_service.dto.profileView;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileViewEventDto {
    private long userId;
    private Long userIdViewing;

    private String dateTimeOfViewing;
}
