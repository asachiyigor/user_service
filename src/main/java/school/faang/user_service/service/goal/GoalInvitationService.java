package school.faang.user_service.service.goal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.goal.GoalInvitationDto;
import school.faang.user_service.dto.goal.InvitationFilterIDto;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.entity.goal.GoalInvitation;
import school.faang.user_service.mapper.GoalInvitationMapper;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.goal.GoalInvitationRepository;
import school.faang.user_service.repository.goal.GoalRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GoalInvitationService {
    private final GoalInvitationMapper invitationMapper;
    private final GoalInvitationRepository goalInvitationRepository;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final int maxGoals = 3;

    public GoalInvitationDto createInvitation(GoalInvitationDto invitationDto) {
        GoalInvitation invitation = invitationMapper.toEntity(invitationDto);
        Optional<Goal> goal = goalRepository.findById(invitationDto.getGoalId());
        Optional<User> invitedUser = userRepository.findById(invitationDto.getInvitedUserId());
        Optional<User> inviterUser = userRepository.findById(invitationDto.getInviterId());
        if (goal.isEmpty() || goalRepository.findById(invitation.getGoal().getId()).isEmpty()) {
            throw new IllegalArgumentException("Goal equals null");
        }
        if (invitation.getInviter() == invitation.getInvited() || invitedUser.isEmpty() || inviterUser.isEmpty()) {
            throw new IllegalArgumentException("Users must be unequal and real");
        }
        invitation.setInvited(invitedUser.get());
        invitation.setInviter(inviterUser.get());
        invitation.setGoal(goal.get());
        invitation = goalInvitationRepository.save(invitation);
        return invitationMapper.toDto(invitation);
    }

    public boolean acceptGoalInvitation(long id) {
        Optional<GoalInvitation> goalInvited = goalInvitationRepository.findById(id);
        if (goalInvited.isEmpty()) {
            throw new IllegalArgumentException("GoalInvitation id not found");
        }

        GoalInvitation goalInvitation = goalInvited.get();
        User invitedUser = goalInvitation.getInvited();
        if (invitedUser.getGoals().size() >= maxGoals) {
            rejectGoalInvitation(id);
            return false;
        }
        Optional<Goal> targetGoal = goalRepository.findById(goalInvitation.getGoal().getId());
        if (targetGoal.isEmpty()) {
            throw new IllegalArgumentException("Goal id not found");
        }
        invitedUser.getGoals().add(goalInvitation.getGoal());
        invitedUser.getReceivedGoalInvitations().add(goalInvitation);
        goalInvitation.setStatus(RequestStatus.ACCEPTED);

        goalInvitationRepository.save(goalInvitation);
        userRepository.save(invitedUser);
        return true;
    }

    public void rejectGoalInvitation(long id) {
        Optional<GoalInvitation> goalInvitationOptional = goalInvitationRepository.findById(id);
        if (goalInvitationOptional.isPresent()) {
            GoalInvitation goalInvitation = goalInvitationOptional.get();
            Optional<Goal> goal = goalRepository.findById(goalInvitation.getGoal().getId());
            if (goal.isEmpty()) {
                throw new IllegalStateException("there is no such goal");
            }
            goalInvitation.setStatus(RequestStatus.REJECTED);
            goalInvitationRepository.save(goalInvitation);
        }
    }

    public List<Long> getInvitations(InvitationFilterIDto filterIDto) {
        List<GoalInvitation> allGoalInvitations = goalInvitationRepository.findAll();
        String patternInviter = filterIDto.getInviterNamePattern();
        allGoalInvitations = allGoalInvitations.stream()
                .filter(invitation -> invitation.getInviter().getUsername().equals(patternInviter)).toList();
        String patternInvited = filterIDto.getInvitedNamePattern().trim();
        if (!patternInvited.isEmpty()) {
            allGoalInvitations = allGoalInvitations.stream()
                    .filter(invitation -> invitation.getInvited().getUsername().equals(patternInvited)).toList();
        }
        Long filterInviterById = filterIDto.getInviterId();
        if (filterInviterById != null) {
            allGoalInvitations = allGoalInvitations.stream()
                    .filter(invitation -> invitation.getInviter().getId().equals(filterInviterById)).toList();
        }
        Long filterInvitedById = filterIDto.getInvitedId();
        if (filterInvitedById != null) {
            allGoalInvitations = allGoalInvitations.stream()
                    .filter(invitation -> invitation.getInvited().getId().equals(filterInvitedById)).toList();
        }
        RequestStatus status = filterIDto.getStatus();
        if (status != null) {
            allGoalInvitations = allGoalInvitations.stream()
                    .filter(invitation -> invitation.getStatus().equals(status)).toList();
        }
        return allGoalInvitations.stream()
                .map(GoalInvitation::getId).toList();
    }
}