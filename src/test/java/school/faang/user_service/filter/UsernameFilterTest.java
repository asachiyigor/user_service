package school.faang.user_service.filter;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
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
import school.faang.user_service.service.user.UserCountryService;
import school.faang.user_service.service.user.UserService;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ExtendWith(MockitoExtension.class)
class UsernameFilterTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    static final Long USER_ID = 1L;
    static final String USER_NAME = "testUsername";
    static final Long USER_ID_TWO = 2L;
    static final String USER_NAME_TWO = "test2Username";

    private List<UserFilter> userFilters;
    private User user;
    private User userTwo;
    private Stream<User> users;
    private List<User> filteredUsers;
    private UsernameFilter usernameFilter;
    private UserContext userContext;
    private SearchAppearanceEventPublisher searchAppearanceEventPublisher;
    private CsvMapper csvMapper;
    private CsvSchema csvSchema;
    private UserCountryService userCountryService;


    @BeforeEach
    void setUp() {
        initializeUserFilters();
        initializeUserService();
        initializeTestEntities();
    }

    @Test
    @DisplayName("Should return true when username field is not empty")
    void testIsApplicable_patternWithFilledUsername() {
        UserFilterDto userFilterDto = new UserFilterDto();
        userFilterDto.setUsernamePattern("testUsername");
        boolean isApplicable = usernameFilter.isApplicable(userFilterDto);
        assertTrue(isApplicable);
    }

    @Test
    @DisplayName("Should return false when username field is empty")
    void testIsApplicable_patternWithNotFilledUsername() {
        UserFilterDto userFilterDto = new UserFilterDto();
        boolean isApplicable = usernameFilter.isApplicable(userFilterDto);
        assertFalse(isApplicable);
    }
    @Test
    @DisplayName("Should find users with username filter")
    void shouldFindUsersWithEmailFilter() {
        UserFilterDto filter = UserFilterDto.builder()
                .usernamePattern("testUsername")
                .build();
        Stream<User> receiverUsers = usernameFilter.apply(filteredUsers.stream(), filter);
        assertThat(receiverUsers.toList()).hasSize(1);
    }

    private void initializeUserFilters() {
        usernameFilter = new UsernameFilter();
    }

    private void initializeUserService() {
        userService = new UserService(
                userRepository,
                userMapper,
                userFilters, userContext, searchAppearanceEventPublisher, csvMapper,  csvSchema, userCountryService);
    }

    private void initializeTestEntities() {
        user = createTestUser();
        userTwo = createTestUserTwo();
        filteredUsers = List.of(user, userTwo);
    }

    private User createTestUser() {
        return User.builder()
                .id(USER_ID)
                .username(USER_NAME)
                .build();
    }

    private User createTestUserTwo() {
        return User.builder()
                .id(USER_ID_TWO)
                .username(USER_NAME_TWO)
                .build();
    }
}