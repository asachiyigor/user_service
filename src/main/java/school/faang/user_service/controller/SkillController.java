package school.faang.user_service.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.faang.user_service.dto.skill.SkillCandidateDto;
import school.faang.user_service.dto.skill.SkillDto;
import school.faang.user_service.entity.recommendation.SkillOffer;
import school.faang.user_service.service.SkillService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Tag(name = "Skill API", description = "API for managing slills")
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
    public List<SkillOffer> acquireSkillFromOffers(@PathVariable("id") @NotNull long skillId,
                                                   @RequestParam @NotNull long userId) {
        return skillService.acquireSkillFromOffers(skillId, userId);
    }
}


