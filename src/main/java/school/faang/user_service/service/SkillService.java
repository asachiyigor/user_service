package school.faang.user_service.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import school.faang.user_service.dto.skill.SkillCandidateDto;
import school.faang.user_service.dto.skill.SkillDto;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.recommendation.SkillOffer;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.SkillMapper;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.recommendation.SkillOfferRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Validated
public class SkillService {
    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;
    private final SkillOfferRepository skillOfferRepository;
    private final UserRepository userRepository;

    public SkillDto create(@NotNull SkillDto skill) {
        Skill skillEntity = skillMapper.toEntity(skill);
        validateSkill(skill);
        skillEntity = skillRepository.save(skillEntity);
        return skillMapper.toDto(skillEntity);
    }

    public List<SkillDto> getUserSkills(@NotNull long userId) {
        validateUser(userId);
        List<Skill> skillList = skillRepository.findAllByUserId(userId);
        return skillMapper.listSkillToDto(skillList);
    }

    public List<SkillCandidateDto> getOfferedSkills(@NotNull long userId) {
        validateUser(userId);

        List<Skill> skillsList = skillRepository.findSkillsOfferedToUser(userId);
        List<SkillCandidateDto> skillCandidateDtoList = skillMapper.skillCandidateToDto(skillsList);

        for (SkillCandidateDto candidate : skillCandidateDtoList) {
            long skillId = candidate.getSkill().getId();
            int offersAmount = skillOfferRepository.countAllOffersOfSkill(skillId, userId);
            candidate.setOffersAmount(offersAmount);
        }
        return skillCandidateDtoList;
    }

    public List<SkillOffer> acquireSkillFromOffers(@NotNull long skillId, long userId) {
        validateUser(userId);

        if (!skillRepository.existsById(skillId)) {
            throw new DataValidationException("Такого навыка нет");
        }
        List<Skill> skillList = skillRepository.findAllByUserId(userId);
        Optional<Skill> skill = skillRepository.findUserSkill(skillId, userId);

        List<SkillOffer> skillOffers = new ArrayList<>();

        if (!skillList.contains(skill)) {
            skillOffers.addAll(skillOfferRepository.findAllOffersOfSkill(skillId, userId));
        }
        return skillOffers;
    }

    private void validateSkill(@NotNull SkillDto skill) {
        if (skill.getTitle().isEmpty()) {
            throw new DataValidationException("Пустое значение");
        }

        if (skillRepository.existsByTitle(skill.getTitle())) {
            throw new DataValidationException(skill.getTitle() + " это значеник уже имеется");
        }
    }

    private void validateUser(@NotNull long userId) {
        if (!userRepository.existsById(userId)) {
            throw new DataValidationException("Пользователь с данным ID нет");
        }
    }
}


