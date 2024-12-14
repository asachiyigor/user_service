package school.faang.user_service.service.skil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.recommendation.SkillOfferDto;
import school.faang.user_service.publisher.skillOffer.SkillOfferedEvent;
import school.faang.user_service.publisher.skillOffer.SkillOfferedEventPublisher;

@Service
@RequiredArgsConstructor
public class SkillOfferService {
    private final SkillOfferedEventPublisher skillOfferedEventPublisher;

    public void createSkillOffer(@Valid SkillOfferDto skillOfferDto) {
        SkillOfferedEvent skillOfferedEvent = SkillOfferedEvent.builder()
                .senderId(skillOfferDto.getSenderId())
                .receiverId(skillOfferDto.getReceiverId())
                .skillId(skillOfferDto.getSkillId())
                .build();
        skillOfferedEventPublisher.publish(skillOfferedEvent);
    }
}
