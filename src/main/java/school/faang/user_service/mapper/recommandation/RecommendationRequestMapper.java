package school.faang.user_service.mapper.recommandation;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import school.faang.user_service.dto.recommendation.RecommendationRequestDto;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.entity.recommendation.SkillRequest;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface RecommendationRequestMapper {

    RecommendationRequest toEntity(RecommendationRequestDto recommendationRequestDto);

    @Mapping(source = "requester.id", target = "requesterId")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "skills", target = "skillsIds", qualifiedByName = "map")
    RecommendationRequestDto toDto(RecommendationRequest recommendationRequest);

    @Named("map")
    default List<Long> getSkillsIds(@NotNull List<SkillRequest> skills) {
        return skills.stream().map(skillRequest -> skillRequest.getSkill().getId()).toList();
    }


}
