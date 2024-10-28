package school.faang.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import school.faang.user_service.dto.RecommendationRequestDto;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.entity.recommendation.SkillRequest;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface RecommendationRequestMapper {

    @Mapping(source = "requester.id", target = "requesterId")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "skills", target = "skillIds", qualifiedByName = "mapSkillsToSkillIds")
    RecommendationRequestDto toDto(RecommendationRequest recommendationRequest);

    RecommendationRequest toEntity(RecommendationRequestDto recommendationRequestDto);


    private static List<Long> mapSkillsToSkillIds(List<SkillRequest> skillRequests) {
        return skillRequests.stream()
                .map(skillRequest -> skillRequest.getSkill().getId())
                .toList();
    }
}
