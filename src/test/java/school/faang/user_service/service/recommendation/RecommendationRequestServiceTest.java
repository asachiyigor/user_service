package school.faang.user_service.service.recommendation;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.recommendation.RecommendationRequestDto;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.entity.recommendation.SkillRequest;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.recommandation.RecommendationRequestMapper;
import school.faang.user_service.repository.recommendation.RecommendationRequestRepository;
import school.faang.user_service.service.skil.SkillRequestService;
import school.faang.user_service.service.skil.SkillService;
import school.faang.user_service.service.user.UserService;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendationRequestServiceTest {
    @InjectMocks
    private RecommendationRequestService requestService;
    @Mock
    private RecommendationRequestRepository requestRepository;
    @Mock
    private UserService userService;
    @Mock
    private SkillService skillService;
    @Mock
    private SkillRequestService skillRequestService;
    @Spy
    private RecommendationRequestMapper requestMapper = Mappers.getMapper(RecommendationRequestMapper.class);


    private RecommendationRequestDto requestDto;
    private RecommendationRequest requestSaved;
    private static final RequestStatus PENDING = RequestStatus.PENDING;

    @Test
    @DisplayName("testCreateWithUserExistence")
    public void testCreateWithUserExistence() {
        requestDto = getRequestDto();
        when(userService.isUserExistByID(requestDto.getRequesterId())).thenReturn(false);

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> requestService.create(requestDto));
        assertTrue(dataValidationException.getMessage()
                .contains("User not found in database"));
    }

    @Test
    @DisplayName("testCreateWithRequestPeriodShort")
    public void testCreateWithRequestPeriodShort() {
        requestDto = getRequestDto();
        RecommendationRequest existingRequest = new RecommendationRequest();
        existingRequest.setCreatedAt(LocalDateTime.now().minusDays(60));

        when(userService.isUserExistByID(requestDto.getRequesterId())).thenReturn(true);
        when(userService.isUserExistByID(requestDto.getReceiverId())).thenReturn(true);
        when(requestRepository.findLatestPendingRequest(1L, 2L))
                .thenReturn(Optional.of(existingRequest));

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> requestService.create(requestDto));
        assertTrue(dataValidationException.getMessage().contains("Request period is too short"));
    }

    @Test
    @DisplayName("testCreateWithSkillsExistence")
    public void testCreateWithSkillsExistence() {
        requestSaved = getRequestSaved();
        requestDto = getRequestDto();
        requestDto.getSkillsIds().add(5L);
        List<Long> existSkillIds = List.of(1L, 2L);

        when(userService.isUserExistByID(requestDto.getRequesterId())).thenReturn(true);
        when(userService.isUserExistByID(requestDto.getReceiverId())).thenReturn(true);
        when(requestRepository.findLatestPendingRequest(requestDto.getRequesterId(), requestDto.getReceiverId()))
                .thenReturn(Optional.of(requestSaved));
        when(skillService.findExistingSkills(requestDto.getSkillsIds())).thenReturn(existSkillIds);

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> requestService.create(requestDto));
        assertTrue(dataValidationException.getMessage().contains("Skills not found in database"));
    }

    @Test
    @DisplayName("testIsCreateRequest")
    public void testIsCreateRequest() {
        requestDto = getRequestDto();
        requestSaved = getRequestSaved();
        RecommendationRequest requestEntity = getRequestEntity();
        List<Long> existSkillIds = List.of(1L, 2L);
        Skill skillFirst = new Skill();
        Skill skillSecond = new Skill();
        skillFirst.setId(1L);
        skillSecond.setId(2L);
        SkillRequest skillRequestFirst = new SkillRequest(requestSaved, skillFirst);
        SkillRequest skillRequestSecond = new SkillRequest(requestSaved, skillSecond);
        requestSaved.getSkills().add(skillRequestFirst);
        requestSaved.getSkills().add(skillRequestSecond);


        when(userService.isUserExistByID(requestDto.getRequesterId())).thenReturn(true);
        when(userService.isUserExistByID(requestDto.getReceiverId())).thenReturn(true);
        when(requestRepository.findLatestPendingRequest(requestDto.getRequesterId(), requestDto.getReceiverId()))
                .thenReturn(Optional.of(requestSaved));
        when(skillService.findExistingSkills(requestDto.getSkillsIds())).thenReturn(existSkillIds);
        when(requestMapper.toEntity(requestDto)).thenReturn(requestEntity);
        when(userService.getUserById(requestDto.getRequesterId())).thenReturn(requestEntity.getRequester());
        when(userService.getUserById(requestDto.getRequesterId())).thenReturn(requestEntity.getReceiver());
        when(requestRepository.save(requestEntity)).thenReturn(requestSaved);
        when(skillService.findAllByIDs(requestDto.getSkillsIds())).thenReturn(List.of(skillFirst, skillSecond));
        when(skillRequestService.create(requestSaved, skillFirst)).thenReturn(skillRequestFirst);
        when(skillRequestService.create(requestSaved, skillSecond)).thenReturn(skillRequestSecond);

        RecommendationRequestDto result = requestService.create(requestDto);

        verify(userService).getUserById(requestDto.getRequesterId());
        verify(userService).getUserById(requestDto.getReceiverId());
        verify(requestRepository).save(requestEntity);
        verify(skillService).findAllByIDs(requestDto.getSkillsIds());
        verify(skillRequestService).create(requestSaved, skillFirst);
        verify(skillRequestService).create(requestSaved, skillSecond);

        assertEquals(result, requestMapper.toDto(requestSaved));
        assertEquals(PENDING, result.getStatus());
        assertEquals(2, result.getSkillsIds().size());
    }

    @Test
    @DisplayName("testGetRequestById")
    public void testGetRequestById() {
        requestSaved = getRequestSaved();
        requestDto = getRequestDto();

        when(requestRepository.findById(1L)).thenReturn(Optional.ofNullable(requestSaved));
        when(requestMapper.toDto(requestSaved)).thenReturn(requestDto);

        assertEquals(requestDto, requestService.getRequest(1L));
    }


    private RecommendationRequestDto getRequestDto() {
        return RecommendationRequestDto.builder()
                .id(1L)
                .message("папапап")
                .status(PENDING)
                .requesterId(1L)
                .receiverId(2L)
                .skillsIds(new ArrayList<>(Arrays.asList(1L, 2L)))
                .build();
    }

    private RecommendationRequest getRequestSaved() {
        return RecommendationRequest.builder()
                .id(1L)
                .message("папапап")
                .status(PENDING)
                .requester(new User())
                .receiver(new User())
                .skills(new ArrayList<>())
                .createdAt(LocalDateTime.of(2024, Month.FEBRUARY, 1, 0, 0, 0))
                .build();
    }

    private RecommendationRequest getRequestEntity() {
        return RecommendationRequest.builder()
                .requester(new User())
                .receiver(new User())
                .createdAt(LocalDateTime.now())
                .build();
    }
}