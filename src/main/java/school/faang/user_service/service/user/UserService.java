package school.faang.user_service.service.user;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import school.faang.user_service.entity.User;
import school.faang.user_service.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUser(Long id) {
       Optional<User> user = findUserByIdInDB(id);
       if (user.isPresent()) {
           return user.get();
       } else {
           throw new RuntimeException("User id:" + id + " not found");
       }
    }

    @NotNull
    private Optional<User> findUserByIdInDB(Long id) {
        return userRepository.findById(id);
    }

    public boolean isUserExistInDB(Long userId) {
        return userRepository.existsById(userId);
    }

}
