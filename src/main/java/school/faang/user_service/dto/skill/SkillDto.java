package school.faang.user_service.dto.skill;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@NotNull
@NotEmpty
public class SkillDto {
    private Long id;
    private String title;
}

