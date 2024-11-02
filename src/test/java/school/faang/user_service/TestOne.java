package school.faang.user_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import school.faang.user_service.service.goal.GoalInvitationService;
import school.faang.user_service.entity.RequestStatus;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class TestOne {
    @InjectMocks
    private GoalInvitationService service;


    @Test
    @DisplayName("school.faang.user_service.TestOne method create")
    void testNullPointerExceptionFromCreateInvitation() {
        Long expectedGoalId = 1L;
        assertThrows(NullPointerException.class, () -> service.createInvitation(null, null, expectedGoalId, RequestStatus.PENDING));
    }
}

