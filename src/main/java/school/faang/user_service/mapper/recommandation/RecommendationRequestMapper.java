package school.faang.user_service.mapper.recommandation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import school.faang.user_service.dto.recommendation.RecommendationRequestDto;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.entity.recommendation.SkillRequest;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface RecommendationRequestMapper {

    @Mapping(source = "requesterId", target = "requester.id")
    @Mapping(source = "receiverId", target = "receiver.id")
    RecommendationRequest toEntity(RecommendationRequestDto recommendationRequestDto);

    @Mapping(source = "requester.id", target = "requesterId")
    @Mapping(source = "receiver.id", target = "receiverId")
    @Mapping(source = "skills", target = "skillsIds", qualifiedByName = "skillsIds")
    RecommendationRequestDto toDto(RecommendationRequest recommendationRequest);

    @Named("skillsIds")
    default List<Long> getSkillsIds(List<SkillRequest> skills) {
        return skills.stream().map(SkillRequest::getId).toList();
    }
}
