package school.faang.user_service.controller.goal;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.goal.GoalInvitationDto;
import school.faang.user_service.dto.goal.InvitationFilterIDto;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.service.goal.GoalInvitationService;

import java.util.List;
import java.util.zip.DataFormatException;

@Validated
@RestController
@RequestMapping("/goal-invitations/")
@RequiredArgsConstructor
public class GoalInvitationController {
    private final GoalInvitationService goalInvitationService;

    @PostMapping("create/")
    public GoalInvitationDto createInvitation(@RequestBody GoalInvitationDto invitationDto) throws DataFormatException {
        Long inviterId = invitationDto.getInviterId();
        Long invitedId = invitationDto.getInvitedUserId();
        Long goalId = invitationDto.getGoalId();
        RequestStatus status = invitationDto.getStatus();
        if (inviterId == null || invitedId == null || goalId == null || status == null) {
            throw new DataFormatException("в метод createInvitation пришли пустые данне");
        }
        return goalInvitationService.createInvitation(inviterId, invitedId, goalId, status);
    }

    @PostMapping("accept/{id}")
    public GoalInvitationDto acceptGoalInvitation(@PathVariable Long id) throws DataFormatException {
        if (id == null) {
            throw new IllegalArgumentException("в метод acceptGoalInvitation пришли пустые данне");
        }
        return goalInvitationService.acceptGoalInvitation(id);
    }

    @PostMapping("reject/{id}")
    public void rejectGoalInvitation(@PathVariable @Min(1) Long id) {
        if (id == null) {
            throw new IllegalArgumentException("в метод rejectGoalInvitation пришли пустые данне");
        }
        goalInvitationService.rejectGoalInvitation(id);
    }

    @GetMapping("get-invitations/")
    public List<Long> getInvitations(@RequestBody InvitationFilterIDto filter) {
        String patternInviter = filter.getInviterNamePattern();
        String patternInvited = filter.getInvitedNamePattern();
        Long filterInviterById = filter.getInviterId();
        Long filterInvitedById = filter.getInvitedId();
        RequestStatus status = filter.getStatus();
        return goalInvitationService.getInvitations(patternInviter, patternInvited,
                filterInviterById, filterInvitedById, status);
    }
}
