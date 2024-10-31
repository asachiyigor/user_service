package school.faang.user_service.service.recommendation;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.recommendation.RecommendationRequestDto;
import school.faang.user_service.entity.recommendation.RecommendationRequest;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.repository.SkillRepository;
import school.faang.user_service.repository.recommendation.RecommendationRequestRepository;
import school.faang.user_service.service.skil.SkillRequestService;
import school.faang.user_service.service.user.UserService;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

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
    private SkillRequestService skillRequestService;

    private RecommendationRequestDto requestDto;
    private RecommendationRequest request;

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

        DataValidationException dataValidationException = assertThrows(DataValidationException.class, () -> requestService.create(requestDto));
    }

    @Test
    @DisplayName("testCreateWithRequestPeriodShort")
    public void testCreateWithRequestPeriodShort() {
        requestDto = getRequestDto();
        request = getRequest();
        request.setCreatedAt(LocalDateTime.of(2024, Month.OCTOBER, 1, 1, 1));
        when(userService.isUserExistInDB(requestDto.getRequesterId())).thenReturn(true);
        when(userService.isUserExistInDB(requestDto.getReceiverId())).thenReturn(true);
        when(requestRepository.findLatestPendingRequest(anyLong(), anyLong())).thenReturn(Optional.of(request));

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> requestService.create(requestDto));
        assertTrue(dataValidationException.getMessage().contains("Request period is too short"));
    }

    @Test
    @DisplayName("testCreateWithSkillsExistence")
    public void testCreateWithSkillsExistence() {
        request = getRequest();
        requestDto = getRequestDto();
        requestDto.getSkillsIds().add(5L);
        List<Long> existingSkillIds = List.of(1L, 2L);

        when(userService.isUserExistInDB(requestDto.getRequesterId())).thenReturn(true);
        when(userService.isUserExistInDB(requestDto.getReceiverId())).thenReturn(true);
        when(requestRepository.findLatestPendingRequest(anyLong(), anyLong())).thenReturn(Optional.of(request));
        when(skillRepository.findExistingSkillIdsInDB(requestDto.getSkillsIds())).thenReturn(existingSkillIds);

        DataValidationException dataValidationException = assertThrows(DataValidationException.class,
                () -> requestService.create(requestDto));
        assertEquals("Skills: [5] not found in database", dataValidationException.getMessage());
    }
//
//    @Test
//    public void testCreateSaveRequest() {
//        requestDto.setRequesterId(1L);
//        requestDto.setReceiverId(2L);
//        requestDto.setCreatedAt(LocalDateTime.of(2024, Month.JANUARY, 1, 1, 1).toString());
//        requestDto.setSkillsIds(List.of(1L, 2L));
//        when(userRepository.existsById(requestDto.getRequesterId())).thenReturn(true);
//        when(userRepository.existsById(requestDto.getReceiverId())).thenReturn(true);
//        when(requestRepository.existsById(any())).thenReturn(true);
//        when(skillRepository.existsById(1L)).thenReturn(true);
//        when(skillRepository.existsById(2L)).thenReturn(true);
//
//
//    }

    private RecommendationRequestDto getRequestDto() {
        return RecommendationRequestDto.builder()
                .message("папапап")
                .requesterId(1L)
                .receiverId(2L)
                .skillsIds(new ArrayList<>(Arrays.asList(1L, 2L)))
                .build();
    }

    private RecommendationRequest getRequest() {
        return RecommendationRequest.builder()
                .id(1L)
                .message("папапап")
                .requester(userService.getUser(1L))
                .receiver(userService.getUser(2L))
                .createdAt(LocalDateTime.of(2024, Month.JANUARY, 1, 1, 1))
                .build();
    }
}