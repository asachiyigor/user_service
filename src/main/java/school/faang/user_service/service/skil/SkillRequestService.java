package school.faang.user_service.service.skil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.entity.recommendation.SkillRequest;
import school.faang.user_service.repository.recommendation.SkillRequestRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SkillRequestService {

    private final SkillRequestRepository skillRequestRepository;

    @Transactional
    public SkillRequest create(RecommendationRequest request, Skill skill) {
        return skillRequestRepository.save(new SkillRequest(request, skill));
    }

    public SkillRequest getSkillRequest(Long id) {
        Optional<SkillRequest> skillRequest = skillRequestRepository.findById(id);
        if(skillRequest.isPresent()) {
            return skillRequest.get();
        } else {
            throw new RuntimeException("Skill request not found");
        }
    }
}
