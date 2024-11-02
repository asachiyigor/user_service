package school.faang.user_service.service.goal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.mapstruct.ap.shaded.freemarker.template.utility.NullArgumentException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import school.faang.user_service.dto.goal.GoalInvitationDto;
import school.faang.user_service.dto.goal.InvitationFilterIDto;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.entity.goal.GoalInvitation;
import school.faang.user_service.exeption.DataValidationException;
import school.faang.user_service.mapper.GoalInvitationMapper;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.goal.GoalInvitationRepository;
import school.faang.user_service.repository.goal.GoalRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.zip.DataFormatException;

@Service
@Validated
@RequiredArgsConstructor
public class GoalInvitationService {
    private final GoalInvitationMapper invitationMapper;
    private final GoalInvitationRepository goalInvitationRepository;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private static final int MAX_GOALS = 3;

    public GoalInvitationDto createInvitation(@Min(1) Long inviterId, @Min(1) Long invitedId, @Min((1)) Long goalId, @NotNull RequestStatus status) {
        GoalInvitation invitation = new GoalInvitation();
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new DataValidationException("Goal not found"));
        User invitedUser = userRepository.findById(invitedId).orElseThrow(() -> new DataValidationException("User not found"));
        User inviterUser = userRepository.findById(inviterId).orElseThrow(() -> new DataValidationException("Inviter not found"));
        if (invitation.getInviter() == invitation.getInvited()) {
            throw new DataValidationException("Users must be unequal and real");
        }
        invitation.setInvited(invitedUser);
        invitation.setInviter(inviterUser);
        invitation.setGoal(goal);
        invitation.setStatus(status);
        invitation = goalInvitationRepository.save(invitation);
        return invitationMapper.toDto(invitation);
    }

    public GoalInvitationDto acceptGoalInvitation(long id) throws DataFormatException {
        Optional<GoalInvitation> goalInvited = goalInvitationRepository.findById(id);
        if (goalInvited.isEmpty()) {
            throw new DataValidationException("GoalInvitation id not found");
        }

        GoalInvitation goalInvitation = goalInvited.get();
        User invitedUser = goalInvitation.getInvited();
        if (invitedUser.getGoals().size() >= MAX_GOALS) {
            return rejectGoalInvitation(id);
        }
        goalRepository.findById(goalInvitation.getGoal().getId()).orElseThrow(DataFormatException::new);
        invitedUser.getGoals().add(goalInvitation.getGoal());
        invitedUser.getReceivedGoalInvitations().add(goalInvitation);
        goalInvitation.setStatus(RequestStatus.ACCEPTED);


        userRepository.save(invitedUser);
        goalInvitation = goalInvitationRepository.save(goalInvitation);
        return invitationMapper.toDto(goalInvitation);
    }

    public GoalInvitationDto rejectGoalInvitation(long id) {
        Optional<GoalInvitation> goalInvitationOptional = goalInvitationRepository.findById(id);
        if (goalInvitationOptional.isPresent()) {
            GoalInvitation goalInvitation = goalInvitationOptional.get();
            Optional<Goal> goal = goalRepository.findById(goalInvitation.getGoal().getId());
            if (goal.isEmpty()) {
                return invitationMapper.toDto(null);
            }
            goalInvitation.setStatus(RequestStatus.REJECTED);
            goalInvitation = goalInvitationRepository.save(goalInvitation);
            return invitationMapper.toDto(goalInvitation);
        }
        return invitationMapper.toDto(null);
    }

    public List<Long> getInvitations(InvitationFilterIDto filterDto) {
        List<GoalInvitation> goalInvitations = goalInvitationRepository.findAll();
        return goalInvitations.stream()
                .filter(invitation -> findAllMatches(invitation, filterDto))
                .map(GoalInvitation::getId)
                .toList();
    }

    private boolean findAllMatches(GoalInvitation goalInvitations, InvitationFilterIDto filterDto) {
        return filterByNameInvited(goalInvitations, filterDto)
                && filterByNameInviter(goalInvitations, filterDto)
                && filterByInviter(goalInvitations, filterDto)
                && filterByInvited(goalInvitations, filterDto)
                && filterByStatus(goalInvitations, filterDto);
    }

    private boolean filterByInviter(GoalInvitation goalInvitation, InvitationFilterIDto filterDto) {
        return filterDto.getInviterId() == null
                || Objects.equals(filterDto.getInviterId(), goalInvitation.getInviter().getId());
    }

    private boolean filterByInvited(GoalInvitation goalInvitation, InvitationFilterIDto filterDto) {
        return filterDto.getInvitedId() == null
                || Objects.equals(filterDto.getInvitedId(), goalInvitation.getInvited().getId());
    }

    private boolean filterByNameInvited(GoalInvitation goalInvitation, InvitationFilterIDto filterDto) {
        return filterDto.getInvitedNamePattern() == null
                || Objects.equals(filterDto.getInvitedNamePattern(), goalInvitation.getInvited().getUsername());
    }

    private boolean filterByNameInviter(GoalInvitation goalInvitation, InvitationFilterIDto filterDto) {
        return filterDto.getInviterNamePattern() == null
                || Objects.equals(filterDto.getInviterNamePattern(), goalInvitation.getInviter().getUsername());
    }

    private boolean filterByStatus(GoalInvitation goalInvitation, InvitationFilterIDto filterDto) {
        return filterDto.getStatus() == null
                || Objects.equals(filterDto.getStatus(), goalInvitation.getStatus());
    }
}