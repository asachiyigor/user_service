package school.faang.user_service.repository.goal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.faang.user_service.entity.goal.UserGoal;

@Repository
public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {
}