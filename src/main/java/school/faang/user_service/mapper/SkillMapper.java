package school.faang.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import school.faang.user_service.dto.skill.SkillCandidateDto;
import school.faang.user_service.dto.skill.SkillDto;
import school.faang.user_service.entity.Skill;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SkillMapper {

    SkillDto toDto(Skill skill);

    @Mapping(target = "users", ignore = true)
    Skill toEntity(SkillDto skillDto);

    default List<SkillDto> listSkillToDto(List<Skill> skills) {
        return skills.stream().map(this::toDto).toList();
    }

    SkillCandidateDto candidateToDto(Skill skill);

    default List<SkillCandidateDto> skillCandidateToDto(List<Skill> skillsList) {
        return skillsList.stream().map(this::candidateToDto).toList();
    }
}


