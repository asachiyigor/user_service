package school.faang.user_service.service.user;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.json.student.PersonSchemaForUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.user.UserDto;
import school.faang.user_service.entity.Country;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.event.Event;
import school.faang.user_service.exception.ErrorMessage;
import school.faang.user_service.exception.ReadFileException;
import school.faang.user_service.exception.UserNotFoundException;
import school.faang.user_service.mapper.user.UserMapper;
import school.faang.user_service.repository.UserRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
    @Mock
    private UserCountryService countryService;
    @Mock
    private CsvMapper csvMapper;
    @Mock
    private CsvSchema csvSchema;

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

    @Test
    @DisplayName("Create Persons Async When Email Already Exists")
    public void createPersonsAsyncWhenEmailExists() {
        PersonSchemaForUser personOne = new PersonSchemaForUser();
        personOne.setEmail("john.doe@example.com");
        UserDto result = UserDto.builder()
                .email(personOne.getEmail())
                .aboutMe("Already exists")
                .build();
        when(userRepository.existsByEmail(personOne.getEmail())).thenReturn(true);
        List<CompletableFuture<UserDto>> futures = userService.createPersonsAsync(List.of(personOne));
        for (CompletableFuture<UserDto> future : futures) {
            UserDto expected = future.join();
            assertEquals(expected, result);
        }
    }

    @Test
    @DisplayName("Create Multiple Persons Asynchronously")
    void testCreatePersonsAsync() {
        PersonSchemaForUser personOne = new PersonSchemaForUser();
        personOne.setEmail("john.doe@example.com");
        PersonSchemaForUser personTwo = new PersonSchemaForUser();
        personTwo.setEmail("jane.smith@example.com");
        List<PersonSchemaForUser> persons = List.of(personOne, personTwo);
        List<CompletableFuture<UserDto>> createdUsers = userService.createPersonsAsync(persons);
        Assertions.assertNotNull(createdUsers);
        Assertions.assertEquals(2, createdUsers.size());
    }

    @Test
    @DisplayName("Parse CSV File Successfully")
    public void testParseCsv() throws IOException {
        ObjectReader testObject = Mockito.mock(ObjectReader.class);
        InputStream inputStream = new ByteArrayInputStream("file".getBytes());
        MappingIterator<Object> iterator = Mockito.mock(MappingIterator.class);
        Mockito.when(csvMapper.readerFor(any(Class.class))).thenReturn(testObject);
        Mockito.when(testObject.with(csvSchema)).thenReturn(testObject);
        Mockito.when(testObject.readValues(any(InputStream.class))).thenReturn(iterator);
        Mockito.when(iterator.readAll()).thenReturn(new ArrayList<>());
        userService.readingUsersFromCsv(inputStream);
        Mockito.verify(csvMapper, Mockito.times(1)).readerFor(PersonSchemaForUser.class);
        Mockito.verify(testObject, Mockito.times(1)).with(csvSchema);
        Mockito.verify(testObject, Mockito.times(1)).readValues(inputStream);
        Mockito.verify(iterator, Mockito.times(1)).readAll();
    }

    @Test
    @DisplayName("Create User from CSV - Successful Creation")
    void testCreateUserFromCsv_SuccessfulCreation() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");
        userDto.setCountry(new Country());
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setParticipatedEvents(new ArrayList<>());
        when(countryService.createCountryIfNotExists(any())).thenReturn(new Country());
        when(userMapper.toEntity(userDto)).thenReturn(savedUser);
        when(userRepository.save(any())).thenReturn(savedUser);
        assertDoesNotThrow(() -> userService.createUserFromCsv(userDto));
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("Create User from CSV Throws Exception")
    public void testCreateUserCSV_ThrowsException() {
        ObjectReader testObject = Mockito.mock(ObjectReader.class);
        InputStream inputStream = new ByteArrayInputStream("file".getBytes());
        when(csvMapper.readerFor(any(Class.class))).thenReturn(testObject);
        when(testObject.with(csvSchema)).thenReturn(testObject);
        assertThrows(Exception.class, () -> userService.readingUsersFromCsv(inputStream));
    }

    @Test
    @DisplayName("Read File Throws ReadFileException")
    void testReadFileException() {
        InputStream inputStream = new ByteArrayInputStream("invalid,csv,file,content".getBytes());
        Assertions.assertThrows(ReadFileException.class, () -> {
            userService.readingUsersFromCsv(inputStream);
        }, "Expected ReadFileException to be thrown");
    }

    @Test
    @DisplayName("Generate Username from Email")
    void testGenerateUsername_WithEmail() {
        UserDto userDto = new UserDto();
        userDto.setEmail("john.doe@example.com");
        String username = invokeGenerateUsername(userDto);
        assertEquals("john.doe", username);
    }

    @Test
    @DisplayName("Generate Random Username When No Email")
    void testGenerateUsername_WithoutEmail() {
        UserDto userDto = new UserDto();
        String username = invokeGenerateUsername(userDto);
        assertTrue(username.startsWith("user_"));
        assertEquals(13, username.length());
    }

    @Test
    @DisplayName("Generate Default Password")
    void testGenerateDefaultPassword() {
        String password = invokeGenerateDefaultPassword();
        assertNotNull(password);
        assertEquals(12, password.length());
        assertTrue(password.matches("^[a-zA-Z0-9]+$"));
    }

    @Test
    @DisplayName("Validate Minimal Required Fields - Valid Person")
    void testValidateMinimalRequiredFields_ValidPerson() {
        PersonSchemaForUser person = new PersonSchemaForUser();
        person.setEmail("valid@example.com");
        boolean result = invokeValidateMinimalRequiredFields(person);
        assertTrue(result);
    }

    @Test
    @DisplayName("Validate Minimal Required Fields - Invalid Person")
    void testValidateMinimalRequiredFields_InvalidPerson() {
        PersonSchemaForUser person = new PersonSchemaForUser();
        person.setEmail("");
        boolean result = invokeValidateMinimalRequiredFields(person);
        assertFalse(result);
    }

    private String invokeGenerateUsername(UserDto userDto) {
        try {
            java.lang.reflect.Method method = UserService.class.getDeclaredMethod("generateUsername", UserDto.class);
            method.setAccessible(true);
            return (String) method.invoke(userService, userDto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeGenerateDefaultPassword() {
        try {
            java.lang.reflect.Method method = UserService.class.getDeclaredMethod("generateDefaultPassword");
            method.setAccessible(true);
            return (String) method.invoke(userService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean invokeValidateMinimalRequiredFields(PersonSchemaForUser person) {
        try {
            java.lang.reflect.Method method = UserService.class.getDeclaredMethod("validateMinimalRequiredFields", PersonSchemaForUser.class);
            method.setAccessible(true);
            return (boolean) method.invoke(userService, person);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}