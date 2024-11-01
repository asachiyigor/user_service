package school.faang.user_service;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import school.faang.user_service.service.goal.GoalInvitationService;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.entity.goal.GoalInvitation;
import school.faang.user_service.exeption.DataValidationException;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class TestOne {
    @InjectMocks
    private GoalInvitationService service;

    private Goal goal = new Goal();
    private User user = new User();
    private GoalInvitation goalInvitation = new GoalInvitation();

    @Test
    @DisplayName("school.faang.user_service.TestOne method create")
    void testPositiveCreateInvitation() {
        Long expectedGoalId = 1L;
        Long expectedUserIdOne = 1L;
        Long expectedUserIdTwo = 3L;
        goal.setId(expectedGoalId);
        user.setId(expectedUserIdOne);
        goalInvitation.setInviter(user);
        goalInvitation.setGoal(goal);
        goalInvitation.setStatus(RequestStatus.ACCEPTED);
        assertThrows(DataValidationException.class, () -> service.createInvitation(null, null, expectedGoalId, RequestStatus.PENDING));
    }
}
