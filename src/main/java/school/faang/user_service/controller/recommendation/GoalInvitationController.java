package school.faang.user_service.controller.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.goal.GoalInvitationDto;
import school.faang.user_service.dto.goal.InvitationFilterIDto;
import school.faang.user_service.entity.RequestStatus;

import java.util.List;
import java.util.zip.DataFormatException;

@Component
@RequiredArgsConstructor
public class RecommendationController {
    private final GoalInvitationService goalInvitationService;

    public GoalInvitationDto createInvitation(GoalInvitationDto invitationDto) throws DataFormatException {
        Long inviterId = invitationDto.getInviterId();
        Long invitedId = invitationDto.getInvitedUserId();
        Long goalId = invitationDto.getGoalId();
        RequestStatus status = invitationDto.getStatus();
        if (inviterId == null || invitedId == null || goalId == null || status == null) {
            throw new DataFormatException("в метод createInvitation пришли пустые данне");
        }
        return goalInvitationService.createInvitation(inviterId, invitedId, goalId, status);
    }

    public boolean acceptGoalInvitation(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("в метод acceptGoalInvitation пришли пустые данне");
        }
        return goalInvitationService.acceptGoalInvitation(id);
    }

    public void rejectGoalInvitation(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("в метод rejectGoalInvitation пришли пустые данне");
        }
        goalInvitationService.rejectGoalInvitation(id);
    }

    public List<Long> getInvitations(InvitationFilterIDto filter) {
        String patternInviter = filter.getInviterNamePattern();
        String patternInvited = filter.getInvitedNamePattern();
        Long filterInviterById = filter.getInviterId();
        Long filterInvitedById = filter.getInvitedId();
        RequestStatus status = filter.getStatus();
        if (patternInviter.isEmpty() || patternInvited.isEmpty() || filterInviterById == null || filterInvitedById == null || status == null) {
            throw new IllegalArgumentException("в метод getInvitations пришли пустые данне");
        }
        return goalInvitationService.getInvitations(patternInviter, patternInvited,
                filterInviterById, filterInvitedById, status);
    }
}
