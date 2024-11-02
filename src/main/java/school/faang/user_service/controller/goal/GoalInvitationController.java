package school.faang.user_service.controller.goal;

import jakarta.validation.Valid;
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
@RequestMapping("/goal-invitations")
@RequiredArgsConstructor
public class GoalInvitationController {
    private final GoalInvitationService goalInvitationService;

    @PostMapping("/create")
    public GoalInvitationDto createInvitation(@Valid @RequestBody GoalInvitationDto invitationDto) {
        Long inviterId = invitationDto.getInviterId();
        Long invitedId = invitationDto.getInvitedUserId();
        Long goalId = invitationDto.getGoalId();
        RequestStatus status = invitationDto.getStatus();
        return goalInvitationService.createInvitation(inviterId, invitedId, goalId, status);
    }

    @PostMapping("/accept/{id}")
    public GoalInvitationDto acceptGoalInvitation(@PathVariable("id") @Min(1) Long id) throws DataFormatException {
        return goalInvitationService.acceptGoalInvitation(id);
    }

    @PostMapping("/reject/{id}")
    public void rejectGoalInvitation(@PathVariable("id") @Min(1) Long id) {
        goalInvitationService.rejectGoalInvitation(id);
    }

    @GetMapping("/get-invitations")
    public List<Long> getInvitations(@RequestBody InvitationFilterIDto filterDto) {
        return goalInvitationService.getInvitations(filterDto);
    }
}
