package school.faang.user_service.service.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.user.UserDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.event.Event;
import school.faang.user_service.exception.ErrorMessage;
import school.faang.user_service.exception.UserNotFoundException;
import school.faang.user_service.mapper.user.UserMapper;
import school.faang.user_service.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @Spy
    UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("Get user by ID successfully")
    void getUserTest() {
        long userId = 1L;
        User user = User.builder()
                .id(userId)
                .username("username")
                .participatedEvents(List.of(new Event(), new Event()))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserDto result = userService.getUserDtoByID(userId);
        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
    }


    @Test
    @DisplayName("Get user not found by ID")
    void getUserNotFoundTest() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserDtoByID(userId)
        );

        String expectedMessage = String.format(ErrorMessage.USER_NOT_FOUND, userId);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Get users by IDs successfully")
    void getUsersIdsTest() {
        List<Long> userIds = List.of(1L, 2L);
        User user1 = User.builder().id(1L).participatedEvents(List.of(new Event(), new Event())).build();
        User user2 = User.builder().id(2L).participatedEvents(List.of(new Event(), new Event())).build();
        when(userRepository.findAllById(userIds)).thenReturn(List.of(user1, user2));
        List<UserDto> result = userService.getUsersByIds(userIds);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Get no users found by IDs")
    void getUsersNotFoundTest() {
        List<Long> userIds = List.of(1L, 2L);
        when(userRepository.findAllById(userIds)).thenReturn(Collections.emptyList());
        List<UserDto> result = userService.getUsersByIds(userIds);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("User exists by ID is true")
    void existsByIdTrue() {
        long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        assertTrue(userService.isUserExistByID(userId));
        verify(userRepository, times(1)).existsById(userId);
    }

    @Test
    @DisplayName("User exists by ID is false")
    void existsByIdFalse() {
        long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);
        assertFalse(userService.isUserExistByID(userId));
        verify(userRepository, times(1)).existsById(userId);
    }

    @Test
    @DisplayName("Save user")
    void saveUser() {
        User user = new User();
        userService.saveUser(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Get not existing users from empty list")
    void getNotExistingUsersEmptyListTest() {
        List<Long> userIds = Collections.emptyList();
        List<Long> notExistingUserIds = UserService.getNotExistingUserIds(userRepository, userIds);
        assertTrue(notExistingUserIds.isEmpty());
    }

    @Test
    @DisplayName("Get not existing users from valid list")
    void getNotExistingUsersValidListTest() {
        List<Long> userIds = List.of(1L, 2L, 3L);
        when(userRepository.findAllById(userIds)).thenReturn(List.of(User.builder().id(2L).build()));
        List<Long> notExistingUserIds = UserService.getNotExistingUserIds(userRepository, userIds);
        assertEquals(2, notExistingUserIds.size());
        assertTrue(notExistingUserIds.contains(1L));
        assertTrue(notExistingUserIds.contains(3L));
    }
}