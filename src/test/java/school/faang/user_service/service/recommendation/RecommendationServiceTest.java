package school.faang.user_service.service.recommendation;

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
import school.faang.user_service.entity.recommendation.Recommendation;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.recommendation.RecommendationMapperImpl;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.recommendation.RecommendationRepository;
import school.faang.user_service.repository.recommendation.SkillOfferRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
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

        assertNotNull(result);
        assertEquals(recommendationId, result.getId());
        assertEquals(authorId, result.getAuthorId());
        assertEquals(receiverId, result.getReceiverId());
        assertEquals(recommendationContent, result.getContent());
        verify(userRepository, times(2)).existsById(anyLong());
        verify(recommendationRepository).create(authorId, receiverId, recommendationContent);
    }

    @Test
    @DisplayName("Test create recommendation - Skill list not empty - success")
    void testCreateRecommendationWithSkillListSuccess() {
        when(userRepository.existsById(authorId)).thenReturn(true);
        when(userRepository.existsById(receiverId)).thenReturn(true);
        when(skillRepository.existsById(skillId)).thenReturn(true);
        when(recommendationRepository.create(authorId, receiverId, recommendationDto.getContent())).thenReturn(1L);
        recommendationDto.setSkillOffers(Collections.singletonList(new SkillOfferDto(1L)));

        RecommendationDto result = recommendationService.createRecommendation(recommendationDto);

        assertNotNull(result);
        assertEquals(recommendationId, result.getId());
        assertEquals(authorId, result.getAuthorId());
        assertEquals(receiverId, result.getReceiverId());
        assertEquals(recommendationDto.getContent(), result.getContent());
        List<SkillOfferDto> skillOffers = Arrays.asList(new SkillOfferDto(1L));
        assertEquals(recommendationDto.getSkillOffers(), skillOffers);
        verify(recommendationRepository).create(authorId, receiverId, recommendationDto.getContent());
        verify(skillRepository).existsById(anyLong());
        verify(userRepository, times(2)).existsById(anyLong());
    }

    @Test
    @DisplayName("Test create recommendation - user not found")
    void testValidateRecommendationUserNotFound() {
        when(userRepository.existsById(authorId)).thenReturn(false);
        assertThrows(DataValidationException.class, () -> recommendationService.createRecommendation(recommendationDto));
    }

    @Test
    @DisplayName("Test update recommendation - success")
    void testUpdateRecommendationSuccess() {
        when(recommendationRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(authorId)).thenReturn(true);
        when(userRepository.existsById(receiverId)).thenReturn(true);
        when(skillRepository.existsById(anyLong())).thenReturn(true);
        recommendationDto.setContent("Updated content");
        recommendationDto.setSkillOffers(Collections.singletonList(new SkillOfferDto(2L)));

        RecommendationDto result = recommendationService.updateRecommendation(1L, recommendationDto);

        assertEquals("Updated content", result.getContent());
        List<SkillOfferDto> skillOffers = Arrays.asList(new SkillOfferDto(2L));
        assertEquals(recommendationDto.getSkillOffers(), skillOffers);
        verify(recommendationRepository).update(1L, authorId, receiverId, "Updated content");
        verify(skillOfferRepository).deleteAllByRecommendationId(1L);
    }

    @Test
    @DisplayName("Test delete recommendation - success")
    void testDeleteRecommendationSuccess() {
        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(recommendation));

        recommendationService.deleteRecommendation(1L);

        verify(skillOfferRepository).deleteAllByRecommendationId(1L);
        verify(recommendationRepository).delete(recommendation);
    }

    @Test
    @DisplayName("Test get recommendation by ID - success")
    void testGetRecommendationByIdSuccess() {
        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(recommendation));
        when(recommendationMapper.toDto(recommendation)).thenReturn(recommendationDto);

        RecommendationDto result = recommendationService.getRecommendationById(1L);

        assertNotNull(result);
        assertEquals(recommendationDto.getContent(), result.getContent());
    }

    @Test
    @DisplayName("Test get all recommendations - success")
    void testGetAllRecommendations() {
        when(recommendationRepository.findAll()).thenReturn(Collections.singletonList(recommendation));
        when(recommendationMapper.toDto(recommendation)).thenReturn(recommendationDto);

        List<RecommendationDto> result = recommendationService.getAllRecommendations();

        assertEquals(1, result.size());
        assertEquals(recommendationDto.getContent(), result.get(0).getContent());
    }

    @Test
    @DisplayName("Test get all user recommendations - success")
    void testGetAllUserRecommendations() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Recommendation> page = new PageImpl<>(Collections.singletonList(recommendation));
        when(recommendationRepository.findAllByReceiverId(receiverId, pageable)).thenReturn(page);
        when(recommendationMapper.toDto(recommendation)).thenReturn(recommendationDto);

        Page<RecommendationDto> result = recommendationService.getAllUserRecommendations(receiverId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(recommendationDto.getContent(), result.getContent().get(0).getContent());
    }

    @Test
    @DisplayName("Test get all given recommendations - success")
    void testGetAllGivenRecommendations() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Recommendation> page = new PageImpl<>(Collections.singletonList(recommendation));
        when(recommendationRepository.findAllByAuthorId(authorId, pageable)).thenReturn(page);
        when(recommendationMapper.toDto(recommendation)).thenReturn(recommendationDto);

        Page<RecommendationDto> result = recommendationService.getAllGivenRecommendations(authorId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(recommendationDto.getContent(), result.getContent().get(0).getContent());
    }

    @Test
    @DisplayName("Test validate unique skills - throws exception on duplicate skills")
    void testValidateSkillsAreUniqueThrowsException() {
        when(userRepository.existsById(receiverId)).thenReturn(true);
        when(userRepository.existsById(authorId)).thenReturn(true);
        recommendationDto.setSkillOffers(List.of(new SkillOfferDto(1L), new SkillOfferDto(1L)));

        DataValidationException dataValidationException = assertThrows(DataValidationException.class, () -> recommendationService.createRecommendation(recommendationDto));
        assertEquals("Skill Offer list has duplicate skills", dataValidationException.getMessage());
    }
}