package school.faang.user_service.service.skil;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.recommendation.SkillRequest;
import school.faang.user_service.repository.SkillRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SkillService {
    private final SkillRepository skillRepository;

    public Skill getSkill(Long id) {
        Optional<Skill> skill = findSkillByIDInDB(id);
        if(skill.isPresent()) {
            return skill.get();
        } else {
            throw new RuntimeException("Skill not found");
        }
    }

    public @NotNull Optional<Skill> findSkillByIDInDB(Long id) {
        return skillRepository.findById(id);
    }



}
