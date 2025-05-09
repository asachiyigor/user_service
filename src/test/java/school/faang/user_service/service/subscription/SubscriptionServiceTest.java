package school.faang.user_service.service.subscription;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.user.UserDto;
import school.faang.user_service.dto.user.UserFilterDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.mapper.user.UserMapper;
import school.faang.user_service.repository.SubscriptionRepository;
import school.faang.user_service.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {
    private User user1;
    private User user2;
    private UserDto userDto1;
    private UserDto userDto2;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        user1 = createUser(1L, "john_doe", "john@example.com");
        user2 = createUser(2L, "jane_smith", "jane@example.com");
        userDto1 = createUserDto(1L, "john_doe", "john@example.com");
        userDto2 = createUserDto(2L, "jane_smith", "jane@example.com");
    }

    @Test
    @DisplayName("Should successfully follow user when both users exist and not subscribed")
    public void followUser_Success() {
        long followerId = 1L;
        long followeeId = 2L;
        User follower = new User();
        follower.setId(followerId);
        User followee = new User();
        followee.setId(followeeId);
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followeeId)).thenReturn(Optional.of(followee));
        when(subscriptionRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId))
                .thenReturn(false);
        subscriptionService.followUser(followerId, followeeId);
        verify(subscriptionRepository).followUser(followerId, followeeId);
    }

    @Test
    @DisplayName("Should throw exception when trying to follow already followed user")
    void followUser_ExistingSubscription_ThrowsException() {
        long followerId = 1L;
        long followeeId = 2L;
        User follower = new User();
        follower.setId(followerId);
        User followee = new User();
        followee.setId(followeeId);
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followeeId)).thenReturn(Optional.of(followee));
        when(subscriptionRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId))
                .thenReturn(true);
        assertThrows(DataValidationException.class,
                () -> subscriptionService.followUser(followerId, followeeId));
        verify(subscriptionRepository, never()).followUser(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Should throw exception when user tries to follow themselves")
    public void followUser_SameUser_ThrowsException() {
        long userId = 1L;
        assertThrows(DataValidationException.class, () -> {
            subscriptionService.followUser(userId, userId);
        });
        verify(userRepository, never()).findById(anyLong());
        verify(subscriptionRepository, never()).followUser(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Should return followers filtered by username pattern")
    void getFollowers_WithUsernameFilter_ReturnsFilteredList() {
        long followeeId = 1L;
        UserFilterDto filter = new UserFilterDto();
        filter.setUsernamePattern("john");
        when(subscriptionRepository.findByFolloweeId(followeeId))
                .thenReturn(Stream.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(userDto1);
        List<UserDto> filteredFollowers = subscriptionService.getFollowers(followeeId, filter);
        assertEquals(1, filteredFollowers.size());
        assertEquals(userDto1, filteredFollowers.get(0));
    }

    @Test
    @DisplayName("Should return followers filtered by email pattern")
    void getFollowers_WithEmailFilter_ReturnsFilteredList() {
        long followeeId = 1L;
        UserFilterDto filter = new UserFilterDto();
        filter.setEmailPattern("john");
        when(subscriptionRepository.findByFolloweeId(followeeId))
                .thenReturn(Stream.of(user1, user2));
        List<UserDto> filteredFollowers = subscriptionService.getFollowers(followeeId, filter);
        assertEquals(1, filteredFollowers.size());
    }

    @Test
    @DisplayName("Should return empty list when no followers found")
    void getFollowers_WithEmptyRepository_ReturnsEmptyList() {
        long followeeId = 1L;
        UserFilterDto filter = new UserFilterDto();
        when(subscriptionRepository.findByFolloweeId(followeeId))
                .thenReturn(null);
        List<UserDto> filteredFollowers = subscriptionService.getFollowers(followeeId, filter);
        assertTrue(filteredFollowers.isEmpty());
    }

    @Test
    @DisplayName("Should return all followers when no filter is applied")
    void getFollowers_WithNoFilter_ReturnsAllFollowers() {
        long followeeId = 1L;
        UserFilterDto filter = new UserFilterDto();
        when(subscriptionRepository.findByFolloweeId(followeeId))
                .thenReturn(Stream.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(userDto1);
        when(userMapper.toDto(user2)).thenReturn(userDto2);
        List<UserDto> followers = subscriptionService.getFollowers(followeeId, filter);
        assertEquals(2, followers.size());
    }

    @Test
    @DisplayName("Should return following users filtered by username pattern")
    void getFollowersCount_ReturnsCorrectCount() {
        long followeeId = 1L;
        long expectedCount = 5L;
        when(subscriptionRepository.findFollowersAmountByFolloweeId(followeeId))
                .thenReturn((int) expectedCount);
        long result = subscriptionService.getFollowersCount(followeeId);
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("Should return following users filtered by email pattern")
    void getFollowing_WithUsernameFilter_ReturnsFilteredList() {
        long followerId = 1L;
        UserFilterDto filter = new UserFilterDto();
        filter.setUsernamePattern("john");
        when(subscriptionRepository.findByFollowerId(followerId))
                .thenReturn(Stream.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(userDto1);
        List<UserDto> filteredFollowing = subscriptionService.getFollowing(followerId, filter);
        assertEquals(1, filteredFollowing.size());
        assertEquals(userDto1, filteredFollowing.get(0));
    }

    @Test
    @DisplayName("Should return following users filtered by email pattern")
    void getFollowing_WithEmailFilter_ReturnsFilteredList() {
        long followerId = 1L;
        UserFilterDto filter = new UserFilterDto();
        filter.setEmailPattern("john");
        when(subscriptionRepository.findByFollowerId(followerId))
                .thenReturn(Stream.of(user1, user2));
        List<UserDto> filteredFollowing = subscriptionService.getFollowing(followerId, filter);
        assertEquals(1, filteredFollowing.size());
    }

    @Test
    @DisplayName("Should return empty list when no following users found")
    void getFollowing_WithEmptyRepository_ReturnsEmptyList() {
        long followerId = 1L;
        UserFilterDto filter = new UserFilterDto();
        when(subscriptionRepository.findByFollowerId(followerId))
                .thenReturn(null);
        List<UserDto> filteredFollowing = subscriptionService.getFollowing(followerId, filter);
        assertTrue(filteredFollowing.isEmpty());
    }

    @Test
    @DisplayName("Should return all following users when no filter is applied")
    void getFollowing_WithNoFilter_ReturnsAllFollowing() {
        long followerId = 1L;
        UserFilterDto filter = new UserFilterDto();
        when(subscriptionRepository.findByFollowerId(followerId))
                .thenReturn(Stream.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(userDto1);
        when(userMapper.toDto(user2)).thenReturn(userDto2);
        List<UserDto> following = subscriptionService.getFollowing(followerId, filter);
        assertEquals(2, following.size());
    }

    @Test
    @DisplayName("Should return correct total number of following users")
    void getFollowingCount_ReturnsCorrectCount() {
        long followerId = 1L;
        long expectedCount = 5L;
        when(subscriptionRepository.findFolloweesAmountByFollowerId(followerId))
                .thenReturn((int) expectedCount);
        long result = subscriptionService.getFollowingCount(followerId);
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("Should successfully unfollow user when subscription exists")
    void unfollowUser_ValidInput_Success() {
        long followerId = 1L;
        long followeeId = 2L;
        when(userRepository.findById(followerId)).thenReturn(Optional.of(user1));
        when(userRepository.findById(followeeId)).thenReturn(Optional.of(user2));
        subscriptionService.unfollowUser(followerId, followeeId);
        verify(subscriptionRepository).unfollowUser(followerId, followeeId);
    }

    @Test
    @DisplayName("Should throw exception when user tries to unfollow themselves")
    void unfollowUser_SameUser_ThrowsException() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        DataValidationException exception = assertThrows(DataValidationException.class,
                () -> subscriptionService.unfollowUser(userId, userId));
        assertEquals("User cannot unfollow themselves", exception.getMessage());
        verify(subscriptionRepository, never()).unfollowUser(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Should throw exception when follower user does not exist")
    void unfollowUser_FollowerNotFound_ThrowsException() {
        long followerId = 1L;
        long followeeId = 2L;
        when(userRepository.findById(followerId)).thenReturn(Optional.empty());
        assertThrows(DataValidationException.class,
                () -> subscriptionService.unfollowUser(followerId, followeeId));
        verify(subscriptionRepository, never()).unfollowUser(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Should throw exception when followee user does not exist")
    void unfollowUser_FolloweeNotFound_ThrowsException() {
        long followerId = 1L;
        long followeeId = 2L;
        when(userRepository.findById(followerId)).thenReturn(Optional.of(user1));
        when(userRepository.findById(followeeId)).thenReturn(Optional.empty());
        assertThrows(DataValidationException.class,
                () -> subscriptionService.unfollowUser(followerId, followeeId));
        verify(subscriptionRepository, never()).unfollowUser(anyLong(), anyLong());
    }

    private User createUser(long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }

    private UserDto createUserDto(long id, String username, String email) {
        UserDto userDto = new UserDto();
        userDto.setId(id);
        userDto.setUsername(username);
        userDto.setEmail(email);
        return userDto;
    }
}