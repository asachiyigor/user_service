package school.faang.user_service.dto.skill;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillDto {
    private Long id;

    @NotNull(message = "Title не может быть null")
    @NotEmpty
    private String title;
}