package school.faang.user_service.mapper.recommandation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import school.faang.user_service.dto.recommendation.RejectionDto;
import school.faang.user_service.entity.recommendation.RecommendationRequest;

@Mapper(componentModel="spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface RecommendationRequestRejectionMapper {
    @Mapping(source = "status", target = "status")
    @Mapping(source = "rejectionReason", target = "reason")
    RejectionDto toDto(RecommendationRequest request);
}
