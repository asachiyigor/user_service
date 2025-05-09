package school.faang.user_service.service.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.dto.user.UserDto;
import school.faang.user_service.dto.user.UserFilterDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.user.UserMapper;
import school.faang.user_service.repository.SubscriptionRepository;
import school.faang.user_service.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Transactional
    public void followUser(long followerId, long followeeId) {
        if (followerId == followeeId) {
            throw new DataValidationException("User cannot follow themselves");
        }
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new DataValidationException("Follower user not found"));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new DataValidationException("Followee user not found"));

        if (subscriptionRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new DataValidationException("Subscription already exists");
        }

        subscriptionRepository.followUser(followerId, followeeId);
    }

    @Transactional
    public void unfollowUser(long followerId, long followeeId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new DataValidationException("Follower user not found"));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new DataValidationException("Followee user not found"));
        if (followerId == followeeId) {
            throw new DataValidationException("User cannot unfollow themselves");
        }
        subscriptionRepository.unfollowUser(followerId, followeeId);
    }

    public List<UserDto> getFollowers(Long followeeId, UserFilterDto filter) {
        return Optional.ofNullable(subscriptionRepository.findByFolloweeId(followeeId))
                .orElse(Stream.empty())
                .filter(user -> filterUser(user, filter))
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getFollowing(long followerId, UserFilterDto filter) {
        return Optional.ofNullable(subscriptionRepository.findByFollowerId(followerId))
                .orElse(Stream.empty())
                .filter(user -> filterUser(user, filter))
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public long getFollowersCount(long followeeId) {
        return subscriptionRepository.findFollowersAmountByFolloweeId(followeeId);
    }

    public long getFollowingCount(long followerId) {
        return subscriptionRepository.findFolloweesAmountByFollowerId(followerId);
    }

    private boolean filterUser(User user, UserFilterDto filter) {
        return (filter.getUsernamePattern() == null ||
                filter.getUsernamePattern().isEmpty() ||
                user.getUsername().contains(filter.getUsernamePattern())) &&
                (filter.getEmailPattern() == null ||
                        filter.getEmailPattern().isEmpty() ||
                        user.getEmail().contains(filter.getEmailPattern()));
    }
}