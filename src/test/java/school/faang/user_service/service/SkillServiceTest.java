package school.faang.user_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import school.faang.user_service.dto.skill.SkillCandidateDto;
import school.faang.user_service.dto.skill.SkillDto;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.recommendation.SkillOffer;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.SkillMapper;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.recommendation.SkillOfferRepository;
import school.faang.user_service.service.skil.SkillService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class SkillServiceTest {
    @InjectMocks
    private SkillService skillService;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private SkillOfferRepository skillOfferRepository;
    @Mock
    private UserRepository userRepository;
    @Spy
    private SkillMapper skillMapper = Mappers.getMapper(SkillMapper.class);

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateWithBlankTitle() {
        SkillDto skillDto = new SkillDto();
        skillDto.setTitle("");

        assertThrows(DataValidationException.class, () -> skillService.create(skillDto));
    }

    @Test
    public void testCreateWithExistingTitle() {
        SkillDto skillDto = prepareData(true);

        assertThrows(DataValidationException.class, () -> skillService.create(skillDto));
    }

    @Test
    public void testCreateSavesSkill() {
        SkillDto skillDto = prepareData(false);

        Skill skillEntity = new Skill();
        skillEntity.setTitle(skillDto.getTitle());
        when(skillMapper.toEntity(skillDto)).thenReturn(skillEntity);

        when(skillRepository.save(skillEntity)).thenReturn(skillEntity);
        skillService.create(skillDto);
        verify(skillRepository, times(1)).save(skillEntity);
    }

    private SkillDto prepareData(boolean existsByTitle) {
        SkillDto skillDto = new SkillDto();
        skillDto.setTitle("title");
        when(skillRepository.existsByTitle(skillDto.getTitle())).thenReturn(existsByTitle);
        return skillDto;
    }

    @Test
    public void testGetUserSkillsUserExists() {
        long userId = 1L;
        List<Skill> skillList = Arrays.asList(
                Skill.builder().id(1L).title("Java").build(),
                Skill.builder().id(2L).title("Spring").build());
        List<SkillDto> expectedSkillDtos = Arrays.asList(new SkillDto(1L, "Java"), new SkillDto(2L, "Spring"));

        when(userRepository.existsById(userId)).thenReturn(true);
        when(skillRepository.findAllByUserId(userId)).thenReturn(skillList);
        when(skillMapper.listSkillToDto(skillList)).thenReturn(expectedSkillDtos);

        List<SkillDto> result = skillService.getUserSkills(userId);

        assertEquals(expectedSkillDtos, result);
        verify(userRepository, times(1)).existsById(userId);
        verify(skillRepository, times(1)).findAllByUserId(userId);
        verify(skillMapper, times(2)).listSkillToDto(skillList);
    }

    @Test
    public void testGetUserSkillsUserDoesNotExist() {
        long userId = 2L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(DataValidationException.class, () -> skillService.getUserSkills(userId));

        verify(userRepository, times(1)).existsById(userId);
        verify(skillRepository, never()).findAllByUserId(anyLong());
        verify(skillMapper, never()).listSkillToDto(anyList());
    }

    @Test
    public void testGetOfferedSkillsUserNotFound() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(DataValidationException.class, () -> skillService.getOfferedSkills(1L));

        verify(userRepository).existsById(1L);
    }

    @Test
    public void testGetOfferedSkillsReturnsCorrectOffersAmount() {
        long userId = 1L;

        Skill skill1 = Skill.builder().id(1L).title("Java").build();
        Skill skill2 = Skill.builder().id(2L).title("Spring").build();
        List<Skill> skillsList = Arrays.asList(skill1, skill2);

        SkillDto skillDto1 = new SkillDto(1L, "Java");
        SkillDto skillDto2 = new SkillDto(2L, "Spring");
        SkillCandidateDto candidateDto1 = new SkillCandidateDto(skillDto1, 0L);
        SkillCandidateDto candidateDto2 = new SkillCandidateDto(skillDto2, 0L);
        List<SkillCandidateDto> skillCandidateDtoList = Arrays.asList(candidateDto1, candidateDto2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(skillRepository.findSkillsOfferedToUser(userId)).thenReturn(skillsList);
        when(skillMapper.skillCandidateToDto(skillsList)).thenReturn(skillCandidateDtoList);

        when(skillOfferRepository.countAllOffersOfSkill(1L, userId)).thenReturn(5);
        when(skillOfferRepository.countAllOffersOfSkill(2L, userId)).thenReturn(3);

        List<SkillCandidateDto> result = skillService.getOfferedSkills(userId);

        verify(skillRepository).findSkillsOfferedToUser(userId);
        verify(skillMapper, times(2)).skillCandidateToDto(skillsList);
        verify(skillOfferRepository).countAllOffersOfSkill(1L, userId);
        verify(skillOfferRepository).countAllOffersOfSkill(2L, userId);

        assertEquals(5, result.get(0).getOffersAmount());
        assertEquals(3, result.get(1).getOffersAmount());
    }

    @Test
    public void testAcquireSkillFromOffersSkillDoesNotExist() {
        long skillId = 1L;
        long userId = 100L;

        when(skillRepository.existsById(skillId)).thenReturn(false);

        assertThrows(DataValidationException.class, () -> skillService.acquireSkillFromOffers(skillId, userId));
    }

    @Test
    public void testAcquireSkillFromOffersSkillOwnedByUser() {
        long skillId = 1L;
        long userId = 100L;
        Skill skill = Skill.builder().id(skillId).title("Java").build();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(skillRepository.existsById(skillId)).thenReturn(true);
        when(skillRepository.findAllByUserId(userId)).thenReturn(Collections.singletonList(skill));
        when(skillRepository.findUserSkill(skillId, userId)).thenReturn(Optional.of(skill));

        List<SkillOffer> result = skillService.acquireSkillFromOffers(skillId, userId);

        assertEquals(0, result.size());
    }

    @Test
    public void testAcquireSkillFromOffersSkillNotOwnedByUser() {
        long skillId = 1L;
        long userId = 100L;
        Skill skill = Skill.builder().id(skillId).title("Java").build();

        SkillOffer skillOffer1 = new SkillOffer();
        SkillOffer skillOffer2 = new SkillOffer();
        List<SkillOffer> expectedOffers = Arrays.asList(skillOffer1, skillOffer2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(skillRepository.existsById(skillId)).thenReturn(true);
        when(skillRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());
        when(skillRepository.findUserSkill(skillId, userId)).thenReturn(Optional.empty());
        when(skillOfferRepository.findAllOffersOfSkill(skillId, userId)).thenReturn(expectedOffers);

        List<SkillOffer> result = skillService.acquireSkillFromOffers(skillId, userId);

        assertEquals(expectedOffers.size(), result.size());
        assertEquals(expectedOffers, result);
    }
}
