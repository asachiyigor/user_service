package school.faang.user_service.service.skil;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.repository.SkillRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SkillService {
    private final SkillRepository skillRepository;

    public List<Skill> findAll(@NotNull List<Long> ids) {
        validateIds(ids);
        return skillRepository.findAllById(ids).stream().toList();
    }

    public List<Long> findExistingSkills(@NotNull List<Long> ids) {
        validateIds(ids);
        return skillRepository.findExistingSkillIds(ids);
    }

    private void validateIds(@NotNull List<Long> ids) {
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("Переданный список индексов скилов некорректен");
        }
    }
}
