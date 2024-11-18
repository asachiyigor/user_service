package school.faang.user_service.dto.recommendation;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationDto {
    Long id;
    @NotNull
    Long receiverId;

    @NotNull
    Long authorId;

    @NotBlank(message = "Content cannot be null or empty")
    @Size(min = 10, max = 255, message = "Content size should 10 to 255 characters long")
    String content;

    List<SkillOfferDto> skillOffers;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
}
