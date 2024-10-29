package school.faang.user_service.serrvice.goal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.entity.goal.GoalInvitation;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.goal.GoalInvitationRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GoalInvitationService {
    private final GoalInvitationRepository goalInvitationRepository;
    private final UserRepository userRepository;

    public void createInvitation(long inviterUser, long invitedUser, Goal goal) {
        if (goal == null) throw new IllegalArgumentException("Goal equals null");
        Optional<User> inviterUserGet = userRepository.findById(inviterUser);
        Optional<User> invitedUserGet = userRepository.findById(invitedUser);

        if (inviterUser == invitedUser || invitedUserGet.isEmpty() || inviterUserGet.isEmpty()) {
            throw new IllegalArgumentException("Users must be unequal and real");
        }
        GoalInvitation newGoalInventory = new GoalInvitation();
        newGoalInventory.setInviter(inviterUserGet.get());
        newGoalInventory.setInvited(invitedUserGet.get());
        newGoalInventory.setGoal(goal);
        newGoalInventory.setStatus(RequestStatus.PENDING);
        goalInvitationRepository.save(newGoalInventory);
    }
}