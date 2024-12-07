package school.faang.user_service.service.user;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.dto.analyticsevent.SearchAppearanceEvent;
import school.faang.user_service.dto.user.UserDto;
import school.faang.user_service.dto.user.UserFilterDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.exception.ErrorMessage;
import school.faang.user_service.exception.UserNotFoundException;
import school.faang.user_service.exception.UserSaveException;
import school.faang.user_service.filter.UserFilter;
import school.faang.user_service.mapper.user.UserMapper;
import school.faang.user_service.publisher.SearchAppearanceEventPublisher;
import school.faang.user_service.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final List<UserFilter> userFilters;
    private final UserContext userContext;
    private final SearchAppearanceEventPublisher searchAppearanceEventPublisher;

    public User getUserById(Long id) {
        if (id == null || id <= 0) {
            log.error("Invalid user ID: {}", id);
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        log.debug("Searching for user with id: {}", id);
        publishSearchAppearanceEvent(id);
        return userRepository.findById(id)
                .map(user -> {
                    log.info("User found with id: {}", id);
                    return user;
                })
                .orElseThrow(() -> {
                    log.error("User with id: {} not found", id);
                    return new UserNotFoundException(String.format(ErrorMessage.USER_NOT_FOUND, id));
                });
    }

    public boolean isUserExistByID(Long userId) {
        publishSearchAppearanceEvent(userId);
        return userRepository.existsById(userId);
    }

    public List<UserDto> getUsersByIds(List<Long> ids) {
        List<User> users = userRepository.findAllById(ids);
        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto getUserDtoByID(long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(String.format(ErrorMessage.USER_NOT_FOUND, userId));
        }
        publishSearchAppearanceEvent(userId);
        return userMapper.toDto(optionalUser.get());
    }

    public void saveUser(User user) {
        try {
            log.info("Saving user: {}", user);
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Error saving user: {}", user, e);
            throw new UserSaveException(String.format(ErrorMessage.USER_SAVE_ERROR, user));
        }
    }

    public static List<Long> getNotExistingUserIds(UserRepository userRepository, List<Long> userIds) {
        List<Long> existingUserIds = userRepository.findAllById(userIds)
                .stream()
                .map(User::getId)
                .toList();

        return userIds.stream()
                .filter(id -> !existingUserIds.contains(id))
                .collect(Collectors.toList());
    }

    @Async("worker-pool")
    @Synchronized
    public void banUsers(List<Long> idForBanUsers) {
        List<User> usersToBan = userRepository.findAllById(idForBanUsers);
        usersToBan.forEach(user -> {
            user.setBanned(true);
            userRepository.save(user);
        });
        log.info("All found users were banned");
    }

    public List<UserDto> findByFilter(UserFilterDto filterDto) {
        var users = userRepository.findAll().stream();
        log.info("Applying filters to users. Filter params: {}", filterDto);
        List<UserDto> filteredUsers = userFilters.stream()
                .filter(vacancyFilter -> vacancyFilter.isApplicable(filterDto))
                .flatMap(vacancyFilterActual -> vacancyFilterActual.apply(users, filterDto))
                .map(userMapper::toDto)
                .toList();
        filteredUsers.forEach(userDto -> publishSearchAppearanceEvent(userDto.getId()));
        return filteredUsers;
    }

    private void publishSearchAppearanceEvent(Long foundUserId) {
        SearchAppearanceEvent event = SearchAppearanceEvent.builder()
                .requesterId(userContext.getUserId())
                .foundUserId(foundUserId)
                .build();
        searchAppearanceEventPublisher.publish(event);
    }
}