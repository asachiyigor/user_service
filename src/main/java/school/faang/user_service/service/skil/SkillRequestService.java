package school.faang.user_service.service.skil;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.entity.recommendation.SkillRequest;
import school.faang.user_service.repository.recommendation.SkillRequestRepository;

@Component
@RequiredArgsConstructor
public class SkillRequestService {

    private final SkillRequestRepository skillRequestRepository;

    @Transactional
    public SkillRequest create(@NotNull RecommendationRequest request, @NotNull Skill skill) {
        return skillRequestRepository.save(new SkillRequest(request, skill));
    }
}
