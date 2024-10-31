package school.faang.user_service.service.recommendation;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import school.faang.user_service.dto.recommendation.RecommendationRequestDto;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.recommandation.RecommendationRequestMapperImpl;
import school.faang.user_service.mapper.recommandation.RecommendationRequestRejectionMapperImpl;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.recommendation.RecommendationRequestRepository;
import school.faang.user_service.repository.recommendation.SkillRequestRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RecommendationRequestServiceTest {
    @InjectMocks
    private RecommendationRequestService requestService;
    @Mock
    private RecommendationRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private SkillRequestRepository skillRequestRepository;
    @Spy
    private RecommendationRequestMapperImpl requestMapper;
    @Spy
    private RecommendationRequestRejectionMapperImpl requestRejectionMapper;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testCreateWithRequesterExistence() {
        RecommendationRequestDto requestDto = new RecommendationRequestDto();
        requestDto.setRequesterId(-1L);
        requestDto.setReceiverId(2L);
        when(userRepository.existsById(requestDto.getRequesterId())).thenReturn(false);

        assertThrows(DataValidationException.class, () -> requestService.create(requestDto));
    }

    @Test
    public void testCreateWithReceiverExistence() {
        RecommendationRequestDto requestDto = new RecommendationRequestDto();
        requestDto.setRequesterId(1L);
        requestDto.setReceiverId(-2L);
        when(userRepository.existsById(requestDto.getReceiverId())).thenReturn(false);

        assertThrows(DataValidationException.class, () -> requestService.create(requestDto));
    }

    @Test
    public void testCreateWithRequesterEqualsReceiver() {
        RecommendationRequestDto requestDto = new RecommendationRequestDto();
        long userId = 1L;
        requestDto.setRequesterId(userId);
        requestDto.setReceiverId(userId);
        when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(DataValidationException.class, () -> requestService.create(requestDto));
    }

    @Test
    public void testCreateWithRequestPeriodShort() {
        RecommendationRequestDto requestDto = new RecommendationRequestDto();
        requestDto.setCreatedAt(LocalDateTime.of(2024, Month.OCTOBER, 1, 1,1));
        when(requestRepository.existsById(any())).thenReturn(false);

        assertThrows(DataValidationException.class, () -> requestService.create(requestDto));
    }


    @Test
    public void testCreateWithSkillsExistence() {
        RecommendationRequestDto requestDto = new RecommendationRequestDto();
        requestDto.setSkillsIds(List.of(1L, 2L));
        when(skillRepository.existsById(1L)).thenReturn(true);
        when(skillRepository.existsById(2L)).thenReturn(false);

        assertThrows(DataValidationException.class, () -> requestService.create(requestDto));
    }

    @Test
    public void testCreateSaveRequest() {
        RecommendationRequestDto requestDto = new RecommendationRequestDto();
        requestDto.setRequesterId(1L);
        requestDto.setReceiverId(2L);
        requestDto.setCreatedAt(LocalDateTime.of(2024, Month.JANUARY, 1, 1,1));
        requestDto.setSkillsIds(List.of(1L, 2L));
        when(userRepository.existsById(requestDto.getRequesterId())).thenReturn(true);
        when(userRepository.existsById(requestDto.getReceiverId())).thenReturn(true);
        when(requestRepository.existsById(any())).thenReturn(true);
        when(skillRepository.existsById(1L)).thenReturn(true);
        when(skillRepository.existsById(2L)).thenReturn(true);


    }

}