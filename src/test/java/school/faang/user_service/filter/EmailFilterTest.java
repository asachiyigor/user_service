package school.faang.user_service.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.dto.user.UserFilterDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.mapper.user.UserMapper;
import school.faang.user_service.publisher.SearchAppearanceEventPublisher;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.service.user.UserService;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ExtendWith(MockitoExtension.class)
class EmailFilterTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    static final Long USER_ID = 1L;
    static final String USER_EMAIL = "test@mail.com";
    static final Long USER_ID_TWO = 2L;
    static final String USER_EMAIL_TWO = "test2@mail.com";

    private List<UserFilter> userFilters;
    private User user;
    private User userTwo;
    private Stream<User> users;
    private List<User> filteredUsers;
    private EmailFilter emailFilter;
    private UserContext userContext;
    private SearchAppearanceEventPublisher searchAppearanceEventPublisher;

    @BeforeEach
    void setUp() {
        initializeUserFilters();
        initializeUserService();
        initializeTestEntities();
    }

    @Test
    @DisplayName("Should return true when email field is not empty")
    void testIsApplicable_patternWithFilledEmail() {
        UserFilterDto userFilterDto = new UserFilterDto();
        userFilterDto.setEmailPattern("test@mail.com");
        boolean isApplicable = emailFilter.isApplicable(userFilterDto);
        assertTrue(isApplicable);
    }

    @Test
    @DisplayName("Should return false when email field is empty")
    void testIsApplicable_patternWithNotFilledEmail() {
        UserFilterDto userFilterDto = new UserFilterDto();
        boolean isApplicable = emailFilter.isApplicable(userFilterDto);
        assertFalse(isApplicable);
    }

    @Test
    @DisplayName("Should find users with email filter")
    void shouldFindUsersWithEmailFilter() {
        UserFilterDto filter = UserFilterDto.builder()
                .emailPattern("test@mail.com")
                .build();
        Stream<User> receiverUsers = emailFilter.apply(filteredUsers.stream(), filter);
        assertThat(receiverUsers.toList()).hasSize(1);
    }

    private void initializeUserFilters() {
        emailFilter = new EmailFilter();
    }

    private void initializeUserService() {
        userService = new UserService(
                userRepository,
                userMapper,
                userFilters, userContext, searchAppearanceEventPublisher);
    }

    private void initializeTestEntities() {
        user = createTestUser();
        userTwo = createTestUserTwo();
        filteredUsers = List.of(user, userTwo);
    }

    private User createTestUser() {
        return User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .build();
    }

    private User createTestUserTwo() {
        return User.builder()
                .id(USER_ID_TWO)
                .email(USER_EMAIL_TWO)
                .build();
    }
}