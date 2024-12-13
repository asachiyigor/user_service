package school.faang.user_service.mapper.user;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import school.faang.user_service.dto.user.UserCountryDto;
import school.faang.user_service.entity.Country;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.FIELD,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserCountryMapper {
    Country toUserCountry(UserCountryDto countryDto);

    UserCountryDto toUserCountryDto(Country country);
}