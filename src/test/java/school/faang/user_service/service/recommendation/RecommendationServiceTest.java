package school.faang.user_service.service.recommendation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import school.faang.user_service.dto.recommendation.RecommendationDto;
import school.faang.user_service.dto.recommendation.SkillOfferDto;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.recommendation.Recommendation;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.recommendation.RecommendationMapperImpl;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.UserSkillGuaranteeRepository;
import school.faang.user_service.repository.recommendation.RecommendationRepository;
import school.faang.user_service.repository.recommendation.SkillOfferRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;
    @Mock
    private SkillOfferRepository skillOfferRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSkillGuaranteeRepository userSkillGuaranteeRepository;
    @Mock
    private SkillRepository skillRepository;
    @Spy
    private RecommendationMapperImpl recommendationMapper;
    @Mock
    private Recommendation recommendation;

    @InjectMocks
    private RecommendationService recommendationService;

    private RecommendationDto recommendationDto;

    private final Long recommendationId = 1L;
    private final Long authorId = 1L;
    private final Long receiverId = 2L;
    private final Long skillId = 1L;
    private final String recommendationContent = "Test recommendation content";

    @BeforeEach
    void setup() {
        recommendationDto = new RecommendationDto();
        recommendationDto.setId(recommendationId);
        recommendationDto.setAuthorId(authorId);
        recommendationDto.setReceiverId(receiverId);
        recommendationDto.setContent("Test recommendation content");

        recommendation = recommendationMapper.toEntity(recommendationDto);
    }

    @Test
    @DisplayName("Test create recommendation - Skill list empty - success")
    void testCreateRecommendationEmptySkillListSuccess() {
        when(userRepository.existsById(authorId)).thenReturn(true);
        when(userRepository.existsById(receiverId)).thenReturn(true);
        when(recommendationRepository.create(authorId, receiverId, recommendationContent)).thenReturn(recommendationId);
        recommendationDto.setSkillOffers(Collections.emptyList());

        RecommendationDto result = recommendationService.createRecommendation(recommendationDto);

        verify(userRepository, times(2)).existsById(anyLong());
        verify(recommendationRepository).create(authorId, receiverId, recommendationContent);
        assertNotNull(result);
        Assertions.assertEquals(recommendationId, result.getId());
        Assertions.assertEquals(authorId, result.getAuthorId());
        Assertions.assertEquals(receiverId, result.getReceiverId());
        Assertions.assertEquals(recommendationContent, result.getContent());

    }

    @Test
    @DisplayName("Test create recommendation - Skill list not empty - success")
    void testCreateRecommendationWithSkillListSuccess() {
        when(userRepository.existsById(authorId)).thenReturn(true);
        when(userRepository.existsById(receiverId)).thenReturn(true);
        when(skillRepository.existsById(skillId)).thenReturn(true);
        when(recommendationRepository.create(authorId, receiverId, recommendationDto.getContent())).thenReturn(1L);
        List<SkillOfferDto> expectedSkillOffers = Collections.singletonList(new SkillOfferDto(1L));
        recommendationDto.setSkillOffers(expectedSkillOffers);

        RecommendationDto result = recommendationService.createRecommendation(recommendationDto);

        verify(recommendationRepository).create(authorId, receiverId, recommendationDto.getContent());
        verify(skillRepository).existsById(anyLong());
        verify(skillOfferRepository).create(anyLong(), anyLong());
        verify(userRepository, times(2)).existsById(anyLong());
        assertNotNull(result);
        Assertions.assertEquals(recommendationDto.getContent(), result.getContent());
        Assertions.assertEquals(recommendationId, result.getId());
        Assertions.assertEquals(authorId, result.getAuthorId());
        Assertions.assertEquals(receiverId, result.getReceiverId());
        Assertions.assertEquals(recommendationDto.getSkillOffers(), expectedSkillOffers);
    }

    @Test
    @DisplayName("Test create recommendation - Guarantee added - success")
    void testCreateRecommendationWithGuaranteeSuccess() {
        when(userRepository.existsById(authorId)).thenReturn(true);
        when(userRepository.existsById(receiverId)).thenReturn(true);
        when(skillRepository.existsById(skillId)).thenReturn(true);
        when(recommendationRepository.create(authorId, receiverId, recommendationDto.getContent())).thenReturn(1L);
        when(skillRepository.findUserSkill(skillId, receiverId)).thenReturn(Optional.of(new Skill()));
        when(userSkillGuaranteeRepository.existsGuarantorForUserAndSkill(receiverId, skillId, authorId)).thenReturn(false);
        List<SkillOfferDto> expectedSkillOffers = Collections.singletonList(new SkillOfferDto(1L));
        recommendationDto.setSkillOffers(expectedSkillOffers);

        RecommendationDto result = recommendationService.createRecommendation(recommendationDto);

        verify(recommendationRepository).create(authorId, receiverId, recommendationDto.getContent());
        verify(skillRepository).existsById(anyLong());
        verify(skillOfferRepository).create(anyLong(), anyLong());
        verify(userRepository, times(2)).existsById(anyLong());
        verify(userSkillGuaranteeRepository).addSkillGuarantee(receiverId, skillId, authorId);
        assertNotNull(result);
        Assertions.assertEquals(recommendationId, result.getId());
        Assertions.assertEquals(authorId, result.getAuthorId());
        Assertions.assertEquals(receiverId, result.getReceiverId());
        Assertions.assertEquals(recommendationDto.getContent(), result.getContent());
        Assertions.assertEquals(recommendationDto.getSkillOffers(), expectedSkillOffers);
    }

    @Test
    @DisplayName("Test create recommendation - Creation Date is older than 6 months - success")
    void testCreateRecommendationCreationDateLessThan6Months() {
        when(userRepository.existsById(authorId)).thenReturn(true);
        when(userRepository.existsById(receiverId)).thenReturn(true);
        when(recommendationRepository.create(authorId, receiverId, recommendationContent)).thenReturn(recommendationId);
        Recommendation recentRecommendation = new Recommendation();
        recentRecommendation.setCreatedAt(LocalDateTime.now().minusMonths(7));
        when(recommendationRepository.findFirstByAuthorIdAndReceiverIdOrderByCreatedAtDesc(authorId, receiverId))
                .thenReturn(Optional.of(recentRecommendation));

        RecommendationDto result = recommendationService.createRecommendation(recommendationDto);

        verify(userRepository, times(2)).existsById(anyLong());
        verify(userRepository, times(2)).existsById(anyLong());
        verify(recommendationRepository).create(authorId, receiverId, recommendationContent);
        assertNotNull(result);
        Assertions.assertEquals(recommendationId, result.getId());
        Assertions.assertEquals(authorId, result.getAuthorId());
        Assertions.assertEquals(receiverId, result.getReceiverId());
        Assertions.assertEquals(recommendationContent, result.getContent());

    }

    @Test
    @DisplayName("Test create recommendation - user not found - negative")
    void testValidateRecommendationUserNotFound() {
        when(userRepository.existsById(authorId)).thenReturn(false);
        DataValidationException dataValidationException = assertThrows(DataValidationException.class, () -> recommendationService.createRecommendation(recommendationDto));
        Assertions.assertEquals("User with specified ID does not exist. Id: 1", dataValidationException.getMessage());
    }

    @Test
    @DisplayName("Test validate unique skills - throws exception on duplicate skills - negative")
    void testValidateSkillsAreUniqueThrowsException() {
        when(userRepository.existsById(receiverId)).thenReturn(true);
        when(userRepository.existsById(authorId)).thenReturn(true);
        recommendationDto.setSkillOffers(List.of(new SkillOfferDto(1L), new SkillOfferDto(1L)));

        DataValidationException dataValidationException = assertThrows(DataValidationException.class, () -> recommendationService.createRecommendation(recommendationDto));
        Assertions.assertEquals("Skill Offer list has duplicate skills", dataValidationException.getMessage());
    }

    @Test
    @DisplayName("Test create recommendation - skill id not found - negative")
    void testValidateSkillIdNotFound() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(skillRepository.existsById(anyLong())).thenReturn(false);
        recommendationDto.setSkillOffers(List.of(new SkillOfferDto(2L)));

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> recommendationService.createRecommendation(recommendationDto));
        Assertions.assertEquals("Skill ID does not exist: 2", dataValidationException.getMessage());
    }

    @Test
    @DisplayName("Test create recommendation - creation date is in last 6 months - negative")
    void testCreateRecommendationDateAfter6Months() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.existsById(anyLong())).thenReturn(true);
        Recommendation recentRecommendation = new Recommendation();
        recentRecommendation.setCreatedAt(LocalDateTime.now().minusMonths(6));
        when(recommendationRepository.findFirstByAuthorIdAndReceiverIdOrderByCreatedAtDesc(authorId, receiverId))
                .thenReturn(Optional.of(recentRecommendation));

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> recommendationService.createRecommendation(recommendationDto));
        Assertions.assertEquals("Recommendation already exists within the last 6 months.", dataValidationException.getMessage());
    }

    @Test
    @DisplayName("Test update recommendation - Skill list not empty - success")
    void testUpdateRecommendationSuccess() {
        when(recommendationRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(authorId)).thenReturn(true);
        when(userRepository.existsById(receiverId)).thenReturn(true);
        when(skillRepository.existsById(anyLong())).thenReturn(true);
        recommendationDto.setContent("Updated content");
        List<SkillOfferDto> expectedSkillOffers = Arrays.asList(new SkillOfferDto(1L), new SkillOfferDto(2L));
        recommendationDto.setSkillOffers(expectedSkillOffers);

        RecommendationDto result = recommendationService.updateRecommendation(1L, recommendationDto);

        verify(recommendationRepository).update(1L, authorId, receiverId, "Updated content");
        verify(skillOfferRepository).deleteAllByRecommendationId(1L);
        verify(skillRepository, times(2)).existsById(anyLong());
        verify(skillOfferRepository, times(2)).create(anyLong(), anyLong());
        verify(userRepository, times(2)).existsById(anyLong());
        Assertions.assertEquals("Updated content", result.getContent());
        Assertions.assertEquals(recommendationDto.getSkillOffers(), expectedSkillOffers);
    }

    @Test
    @DisplayName("Test update recommendation - Skill list empty - success")
    void testUpdateRecommendationSkillListEmptySuccess() {
        when(recommendationRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(authorId)).thenReturn(true);
        when(userRepository.existsById(receiverId)).thenReturn(true);
        recommendationDto.setContent("Updated content");

        RecommendationDto result = recommendationService.updateRecommendation(1L, recommendationDto);

        verify(recommendationRepository).update(1L, authorId, receiverId, "Updated content");
        verify(skillOfferRepository).deleteAllByRecommendationId(1L);
        verify(userRepository, times(2)).existsById(anyLong());
        Assertions.assertEquals("Updated content", result.getContent());
        Assertions.assertNull(recommendationDto.getSkillOffers());
    }

    @Test
    @DisplayName("Test update recommendation - recommendation not found - negative")
    void testValidateRecommendationNotFound() {
        when(recommendationRepository.existsById(anyLong())).thenReturn(false);
        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> recommendationService.updateRecommendation(1L, recommendationDto));
        Assertions.assertEquals("Recommendation not found with id: 1", dataValidationException.getMessage());
    }


    @Test
    @DisplayName("Test delete recommendation - success")
    void testDeleteRecommendationSuccess() {
        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(recommendation));
        recommendationService.deleteRecommendation(1L);
        verify(recommendationRepository).delete(recommendation);
    }

    @Test
    @DisplayName("Test get recommendation by ID - success")
    void testGetRecommendationByIdSuccess() {
        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(recommendation));
        when(recommendationMapper.toDto(recommendation)).thenReturn(recommendationDto);

        RecommendationDto result = recommendationService.getRecommendationById(1L);

        assertNotNull(result);
        Assertions.assertEquals(recommendationDto.getContent(), result.getContent());
    }

    @Test
    @DisplayName("Test get all recommendations - success")
    void testGetAllRecommendations() {
        when(recommendationRepository.findAll()).thenReturn(Collections.singletonList(recommendation));
        when(recommendationMapper.toDto(recommendation)).thenReturn(recommendationDto);

        List<RecommendationDto> result = recommendationService.getAllRecommendations();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(recommendationDto.getContent(), result.get(0).getContent());
    }

    @Test
    @DisplayName("Test get all user recommendations - success")
    void testGetAllUserRecommendations() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Recommendation> page = new PageImpl<>(Collections.singletonList(recommendation));
        when(recommendationRepository.findAllByReceiverId(receiverId, pageable)).thenReturn(page);
        when(recommendationMapper.toDto(recommendation)).thenReturn(recommendationDto);

        Page<RecommendationDto> result = recommendationService.getAllUserRecommendations(receiverId, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(recommendationDto.getContent(), result.getContent().get(0).getContent());
    }

    @Test
    @DisplayName("Test get all given recommendations - success")
    void testGetAllGivenRecommendations() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Recommendation> page = new PageImpl<>(Collections.singletonList(recommendation));
        when(recommendationRepository.findAllByAuthorId(authorId, pageable)).thenReturn(page);
        when(recommendationMapper.toDto(recommendation)).thenReturn(recommendationDto);

        Page<RecommendationDto> result = recommendationService.getAllGivenRecommendations(authorId, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(recommendationDto.getContent(), result.getContent().get(0).getContent());
    }
}