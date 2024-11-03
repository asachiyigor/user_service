package school.faang.user_service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import school.faang.user_service.dto.skill.SkillCandidateDto;
import school.faang.user_service.dto.skill.SkillDto;
import school.faang.user_service.entity.recommendation.SkillOffer;
import school.faang.user_service.service.SkillService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/skills")
public class SkillController {
    private final SkillService skillService;

    @PostMapping
    public SkillDto create(@RequestBody @Valid @NotNull SkillDto skillDto) {     // возвращяет скил дто
        return skillService.create(skillDto);
    }

    @GetMapping("/user")
    public List<SkillDto> getUserSills(@RequestParam @NotNull long userId) {
       return skillService.getUserSkills(userId);
    }

    @GetMapping("/offered")
    public List<SkillCandidateDto> getOfferedSkills(@RequestParam @NotNull long userId) {
        return skillService.getOfferedSkills(userId);
    }

    @GetMapping("/{id}")
    public List<SkillOffer> acquireSkillFromOffers(@PathVariable @NotNull long skillId,
                                                   @RequestParam @NotNull long userId) {
        return acquireSkillFromOffers(skillId, userId);
    }
}


