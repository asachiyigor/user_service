package school.faang.user_service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.skill.SkillCandidateDto;
import school.faang.user_service.dto.skill.SkillDto;
import school.faang.user_service.entity.recommendation.SkillOffer;
import school.faang.user_service.service.SkillService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;

    public SkillDto create(@Valid @NotNull SkillDto skillDto) {     // возвращяет скил дто
        return skillService.create(skillDto);
    }

    public List<SkillDto> getUserSills(@Valid @NotNull long userId) {
       return skillService.getUserSkills(userId);
    }

    public List<SkillCandidateDto> getOfferedSkills(@Valid @NotNull long userId) {
        return skillService.getOfferedSkills(userId);
    }

    public List<SkillOffer> acquireSkillFromOffers(@Valid @NotNull long skillId, long userId) {
        return acquireSkillFromOffers(skillId, userId);
    }
}
