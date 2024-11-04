package school.faang.user_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.goal.GoalInvitationDto;
import school.faang.user_service.dto.goal.InvitationFilterIDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.entity.goal.GoalInvitation;
import school.faang.user_service.entity.goal.UserGoal;
import school.faang.user_service.exeption.DataValidationException;
import school.faang.user_service.mapper.invitation.GoalInvitationMapper;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.goal.GoalInvitationRepository;
import school.faang.user_service.repository.goal.GoalRepository;
import school.faang.user_service.repository.goal.UserGoalRepository;
import school.faang.user_service.service.goal.GoalInvitationService;
import school.faang.user_service.entity.RequestStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestGoalInvitationService {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalInvitationRepository goalInvitationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    UserGoalRepository userGoalRepository;

    @InjectMocks
    private GoalInvitationService service;

    @Spy
    private GoalInvitationMapper goalInvitationMapper = Mappers.getMapper(GoalInvitationMapper.class);

    @Test
    @DisplayName("Creat method error test")
    void testNegativeCreateInvitation() {
        Long expectedGoalId = 1L;
        assertThrows(DataValidationException.class,
                () -> service.createInvitation(null, null, expectedGoalId, RequestStatus.PENDING));
    }

    @Test
    @DisplayName("Test creat invitation")
    void testPositiveCreateInvitation() {
        Long expectedInvitedId = 2L;
        Long expectedInviterId = 1L;
        Long expectedGoalId = 1L;
        RequestStatus expectedStatus = RequestStatus.PENDING;
        GoalInvitation expectedGoalInvitation =
                createGoalInvitation(expectedInviterId, expectedInvitedId, expectedGoalId, expectedStatus);
        System.out.println(expectedGoalInvitation);

        when(goalRepository.findById(expectedGoalId)).thenReturn(Optional.of(getGoal(expectedGoalId)));
        when(userRepository.findById(expectedInvitedId)).thenReturn(Optional.of(getUser(expectedInvitedId)));
        when(userRepository.findById(expectedInviterId)).thenReturn(Optional.of(getUser(expectedInviterId)));

        when(goalInvitationRepository.save(expectedGoalInvitation)).thenReturn(expectedGoalInvitation);
        GoalInvitationDto actualInvitationDto = goalInvitationMapper.toDto(expectedGoalInvitation);
        GoalInvitationDto expectedInvitationDto =
                service.createInvitation(expectedInviterId, expectedInvitedId, expectedGoalId, expectedStatus);
        assertEquals(expectedInvitationDto, actualInvitationDto);
    }

    @Test
    @DisplayName("Positive test method acceptGoalInvitation")
    void testPositiveAcceptInvitation() {
        GoalInvitation goalInvitation = createGoalInvitation(1L, 2L, 1L, RequestStatus.ACCEPTED);
        goalInvitation.getInvited().setGoals(Arrays.asList(getGoal(1L)));
        goalInvitation.setId(1L);
        UserGoal userGoal = getUserGoal();
        when(goalInvitationRepository.findById(1L)).thenReturn(Optional.of(goalInvitation));
        when(goalRepository.findById(1L)).thenReturn(Optional.of(getGoal(1L)));
        when(goalInvitationRepository.save(goalInvitation)).thenReturn(goalInvitation);
        when(userGoalRepository.save(userGoal)).thenReturn(userGoal);

        GoalInvitationDto expectedInvitationDto = service.acceptGoalInvitation(goalInvitation.getId());
        GoalInvitationDto actualInvitationDto = goalInvitationMapper.toDto(goalInvitation);
        assertEquals(expectedInvitationDto, actualInvitationDto);
    }

    @Test
    @DisplayName("Negative test method acceptGoalInvitation")
    void testNegativeAcceptInvitation() {
        long expectedGoaInvitationId = 1L;
        GoalInvitation goalInvitation = createGoalInvitation(1L, 1L, 2L, RequestStatus.ACCEPTED);
        goalInvitation.getInvited().setGoals(List.of(getGoal(1L)));
        goalInvitation.setId(expectedGoaInvitationId);
        goalInvitation.getInvited().setGoals(List.of(getGoal(3L), getGoal(2L), getGoal(4L), getGoal(5L)));

        when(goalInvitationRepository.findById(1L)).thenReturn(Optional.of(goalInvitation));
        when(goalRepository.existsById(2L)).thenReturn(false);
        when(goalInvitationRepository.save(goalInvitation)).thenReturn(goalInvitation);

        GoalInvitationDto actualInvitationDto = goalInvitationMapper.toDto(createGoalInvitation( 5L, 2L, 1L, RequestStatus.REJECTED));
        GoalInvitationDto expectedInvitationDto = service.acceptGoalInvitation(expectedGoaInvitationId);

        assertNotEquals(expectedInvitationDto, actualInvitationDto);
    }

    @Test
    @DisplayName("Test filters")
    void testFilterInvitations() {
        InvitationFilterIDto filterIDto = getFilterDtoInvitations("Max", "Alex", 1L, 2L, RequestStatus.ACCEPTED);
        when(goalInvitationRepository.findAll()).thenReturn(getExpectedListInvitations());
        List<Long> expectedListInvitationsId = service.getInvitations(filterIDto);
        assertFalse(expectedListInvitationsId.containsAll(List.of(1L, 4L)));
        assertTrue(expectedListInvitationsId.contains(1L));
    }

    private GoalInvitation createGoalInvitation(Long inviterId, Long invitedId, Long goalId, RequestStatus status) {
        return GoalInvitation.builder()
                .inviter(getUser(inviterId))
                .invited(getUser(invitedId))
                .goal(getGoal(goalId))
                .status(status)
                .build();
    }

    private User getUser(Long userId) {
        return User.builder()
                .id(userId)
                .goals(List.of(getGoal(1L)))
                .aboutMe("i am")
                .receivedGoalInvitations(new ArrayList<>())
                .contacts(new ArrayList<>())
                .build();
    }

    private Goal getGoal(Long goalId) {
        return Goal.builder()
                .id(goalId)
                .build();
    }

    private UserGoal getUserGoal() {
        return UserGoal.builder()
                .user(getUser(2L))
                .goal(getGoal(1L))
                .build();
    }

    private List<GoalInvitation> getExpectedListInvitations() {
        GoalInvitation goalInvitation = createGoalInvitation(1L, 2L, 1L, RequestStatus.ACCEPTED);
        goalInvitation.getInviter().setUsername("Max");
        goalInvitation.getInvited().setUsername("Alex");
        goalInvitation.setId(1L);
        GoalInvitation goalInvitation1 = createGoalInvitation(2L, 1L, 2L, RequestStatus.ACCEPTED);
        goalInvitation1.setId(2L);
        GoalInvitation goalInvitation2 = createGoalInvitation(3L, 4L, 3L, RequestStatus.REJECTED);
        goalInvitation2.setId(3L);
        GoalInvitation goalInvitation3 = createGoalInvitation(5L, 6L, 2L, RequestStatus.REJECTED);
        goalInvitation3.setId(4L);
        GoalInvitation goalInvitation4 = createGoalInvitation(1L, 3L, 4L, RequestStatus.REJECTED);
        goalInvitation4.setId(5L);

        return List.of(goalInvitation, goalInvitation1, goalInvitation2, goalInvitation3, goalInvitation4);
    }

    private InvitationFilterIDto getFilterDtoInvitations(
            String inviterName, String invitedName, Long idInviter, Long idInvited, RequestStatus status) {
        return InvitationFilterIDto.builder()
                .inviterNamePattern(inviterName)
                .invitedNamePattern(invitedName)
                .inviterId(idInviter)
                .invitedId(idInvited)
                .status(status)
                .build();
    }
}

