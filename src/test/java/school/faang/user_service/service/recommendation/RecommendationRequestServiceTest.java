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
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.recommandation.RecommendationRequestMapper;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.recommendation.RecommendationRequestRepository;
import school.faang.user_service.service.skil.SkillService;
import school.faang.user_service.service.user.UserService;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.faang.user_service.entity.RequestStatus.PENDING;

@ExtendWith(MockitoExtension.class)
public class RecommendationRequestServiceTest {
    @InjectMocks
    private RecommendationRequestService requestService;
    @Mock
    private RecommendationRequestRepository requestRepository;
    @Mock
    private UserService userService;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private SkillService skillService;
    @Mock
    private RecommendationRequestService skillRequestService;
    @Spy
    private RecommendationRequestMapper requestMapper = Mappers.getMapper(RecommendationRequestMapper.class);


    private RecommendationRequestDto requestDto;
    private RecommendationRequest requestDB;

    @Test
    @DisplayName("testCreateWithMessageIsEmpty")
    public void testCreateWithMessageIsEmpty() {
        requestDto = getRequestDto();
        requestDto.setMessage("");
        assertThrows(DataValidationException.class, () -> requestService.create(requestDto));
    }

    @Test
    @DisplayName("testCreateWithRequesterReceiverSameUser")
    public void testCreateWithRequesterReceiverSameUser() {
        requestDto = getRequestDto();
        requestDto.setReceiverId(1L);
        assertThrows(DataValidationException.class, () -> requestService.create(requestDto));
    }

    @Test
    @DisplayName("testCreateWithUserExistence")
    public void testCreateWithUserExistence() {
        requestDto = getRequestDto();
        when(userService.isUserExistInDB(requestDto.getRequesterId())).thenReturn(false);

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> requestService.create(requestDto));
        assertTrue(dataValidationException.getMessage()
                .contains("User not found in database"));
    }

    @Test
    @DisplayName("testCreateWithRequestPeriodShort")
    public void testCreateWithRequestPeriodShort() {
        requestDto = getRequestDto();
        requestDB = getRequestFromDB();
        requestDB.setCreatedAt(LocalDateTime.of(2024, Month.OCTOBER, 1, 1, 1));
        when(userService.isUserExistInDB(requestDto.getRequesterId())).thenReturn(true);
        when(userService.isUserExistInDB(requestDto.getReceiverId())).thenReturn(true);
        when(requestRepository.findLatestPendingRequest(anyLong(), anyLong())).thenReturn(Optional.of(requestDB));

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> requestService.create(requestDto));
        assertTrue(dataValidationException.getMessage().contains("Request period is too short"));
    }

    @Test
    @DisplayName("testCreateWithSkillsExistence")
    public void testCreateWithSkillsExistence() {
        requestDB = getRequestFromDB();
        requestDto = getRequestDto();
        requestDto.getSkillsIds().add(5L);
        List<Long> existingSkillIds = List.of(1L, 2L);

        when(userService.isUserExistInDB(requestDto.getRequesterId())).thenReturn(true);
        when(userService.isUserExistInDB(requestDto.getReceiverId())).thenReturn(true);
        when(requestRepository.findLatestPendingRequest(anyLong(), anyLong())).thenReturn(Optional.of(requestDB));
        when(skillService.findExistingSkills(requestDto.getSkillsIds())).thenReturn(existingSkillIds);

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> requestService.create(requestDto));
        assertTrue(dataValidationException.getMessage().contains("Skills not found in database"));
    }

    @Test
    public void testIsCreateRequest() {
        requestDto = getRequestDto();
        requestDB = getRequestFromDB();
        List<Long> existingSkillIds = List.of(1L, 2L);
        User requester = userService.getUser(getRequestDto().getRequesterId());
        User receiver = userService.getUser(getRequestDto().getReceiverId());
        RecommendationRequest requestEntity = requestMapper.toEntity(requestDto);
        RecommendationRequest requestDBNew = new RecommendationRequest().builder()
                .id(2L)
                .message(requestEntity.getMessage())
                .status(requestEntity.getStatus())
                .requester(requestEntity.getRequester())
                .receiver(requestEntity.getReceiver())
                .createdAt(LocalDateTime.now())
                .build();
        List<Skill> skills = new ArrayList<>(List.of(
                new Skill().builder().id(1L).title("java").build(),
                new Skill().builder().id(2L).title("java").build()
        ));
        int counter = requestDto.getSkillsIds().size();

        when(userService.isUserExistInDB(requestDto.getRequesterId())).thenReturn(true);
        when(userService.isUserExistInDB(requestDto.getReceiverId())).thenReturn(true);
        when(requestRepository.findLatestPendingRequest(anyLong(), anyLong())).thenReturn(Optional.of(requestDB));
        when(skillRepository.findExistingSkillIds(requestDto.getSkillsIds())).thenReturn(existingSkillIds);

        when(userService.getUser(requestDto.getRequesterId())).thenReturn(requester);
        when(userService.getUser(requestDto.getRequesterId())).thenReturn(receiver);
        verify(requestRepository, times(1)).save(requestEntity);
        when(skillService.findAll(requestDto.getSkillsIds())).thenReturn(skills);
//        verify(skillRequestService, times(counter)).create(requestDBNew, skill);

    }

    private RecommendationRequestDto getRequestDto() {
        return RecommendationRequestDto.builder()
                .message("папапап")
                .requesterId(1L)
                .receiverId(2L)
                .skillsIds(new ArrayList<>(Arrays.asList(1L, 2L)))
                .build();
    }

    private RecommendationRequest getRequestFromDB() {
        return RecommendationRequest.builder()
                .id(1L)
                .message("папапап")
                .requester(userService.getUser(1L))
                .receiver(userService.getUser(2L))
                .status(PENDING)
                .createdAt(LocalDateTime.of(2024, Month.JANUARY, 1, 1, 1))
                .build();
    }
}