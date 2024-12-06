package school.faang.user_service.publisher.skillOffer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkillOfferedEvent {
    private long receiverId;
    private long senderId;
    private long skillId;
}
