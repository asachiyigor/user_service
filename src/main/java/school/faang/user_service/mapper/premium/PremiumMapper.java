package school.faang.user_service.mapper.premium;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import school.faang.user_service.dto.premium.PremiumDto;
import school.faang.user_service.entity.premium.Premium;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface PremiumMapper {

  @Mapping(target = "user", ignore = true)
  @Mapping(target = "startDate", source = "startDate")
  @Mapping(target = "endDate", source = "endDate")
  Premium toEntity(PremiumDto premiumDto);

  @Mapping(target = "userId", source = "user.id")
  @Mapping(source = "startDate", target = "startDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
  @Mapping(source = "endDate", target = "endDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
  PremiumDto toDto(Premium premium);

}
