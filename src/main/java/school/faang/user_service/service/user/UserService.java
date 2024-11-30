package school.faang.user_service.service.user;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.json.student.PersonSchemaForUser;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.user.UserDto;
import school.faang.user_service.dto.user.UserResponseCsvDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.exception.ErrorMessage;
import school.faang.user_service.exception.ReadFileException;
import school.faang.user_service.exception.UserNotFoundException;
import school.faang.user_service.exception.UserSaveException;
import school.faang.user_service.mapper.user.UserMapper;
import school.faang.user_service.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CsvMapper csvMapper;
    private final CsvSchema schema;
    private final UserCountryService countryService;

    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        log.debug("Searching for user with id: {}", id);
        if (user.isPresent()) {
            log.info("User found with id: {}", id);
            return user.get();
        } else {
            log.error("User with id: {} not found", id);
            throw new UserNotFoundException(String.format(ErrorMessage.USER_NOT_FOUND, id));
        }
    }

    public boolean isUserExistByID(Long userId) {
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

    private String generateUsername(UserDto userDto) {
        if (StringUtils.isNotEmpty(userDto.getEmail())) {
            return userDto.getEmail().split("@")[0];
        }
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateDefaultPassword() {
        return RandomStringUtils.randomAlphanumeric(12);
    }

    public List<UserResponseCsvDto> readingUsersFromCsv(InputStream inputStream) {
        List<PersonSchemaForUser> persons = null;
        try {
            persons = parsingCsv(inputStream);
        } catch (IOException e) {
            throw new ReadFileException("File exception: " + e);
        }
        List<CompletableFuture<UserDto>> futures = createPersonsAsync(persons);
        return completeCreatingPersonsAsync(futures).stream()
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
    }

    public List<PersonSchemaForUser> parsingCsv(InputStream inputStream) throws IOException {
        try {
            MappingIterator<PersonSchemaForUser> iterator = csvMapper.readerFor(PersonSchemaForUser.class)
                    .with(schema)
                    .readValues(inputStream);
            log.info("File successfully parsed.");
            return iterator.readAll().stream()
                    .filter(this::validateMinimalRequiredFields)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error parsing CSV file", e);
            throw new ReadFileException("Cannot parse CSV file: " + e.getMessage());
        }
    }

    public void createUserFromCsv(UserDto userDto) {
        log.info("Starting user creation with email: {}", userDto.getEmail());
        userDto.setUsername(generateUsername(userDto));
        userDto.setPassword(generateDefaultPassword());
        userDto.setCountry(countryService.createCountryIfNotExists(userDto.getCountry().getTitle()));
        log.debug("User after installing default setting: {}", userDto);
        User user = userMapper.toEntity(userDto);
        synchronized (userRepository) {
            try {
                user = userRepository.save(user);
                log.info("User successfully saved: {}", user.getId());
            } catch (Exception e) {
                log.error("Error saving user: {}", e.getMessage(), e);
                throw e;
            }
        }
        userMapper.toDto(user);
    }

    public boolean validateMinimalRequiredFields(PersonSchemaForUser person) {
        boolean isValid = person.getEmail() != null && !person.getEmail().isEmpty();
        if (!isValid) {
            log.warn("Skipping user record due to missing critical information: {}", person);
        } else {
            log.debug("User record passed validation: {}", person);
        }
        return isValid;
    }

    public List<CompletableFuture<UserDto>> createPersonsAsync(List<PersonSchemaForUser> persons) {
        return persons.stream()
                .map(person -> CompletableFuture.supplyAsync(() -> {
                    log.info("Starting processing for user: {}", person.getEmail());
                    try {
                        if (userRepository.existsByEmail(person.getEmail())) {
                            log.warn("User with email {} already exists. Skipping creation.", person.getEmail());
                            return UserDto.builder()
                                    .email(person.getEmail())
                                    .aboutMe("Already exists")
                                    .build();
                        }

                        UserDto userDto = userMapper.personToUserDto(person);
                        userDto.setPassword(ThreadLocalRandom.current().nextInt() + "");
                        createUserFromCsv(userDto);
                        log.info("User successfully created: {}", userDto.getUsername());
                        return userDto;
                    } catch (Exception e) {
                        log.error("Warning while creating user: {}", e.getMessage(), e);
                        return UserDto.builder()
                                .email(person.getEmail())
                                .aboutMe("Warning: " + e.getMessage())
                                .build();
                    }
                }))
                .toList();
    }

    public List<UserDto> completeCreatingPersonsAsync(List<CompletableFuture<UserDto>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(dto -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()))
                .join();
    }

    private UserResponseCsvDto convertToUserResponseDto(UserDto userDto) {
        return UserResponseCsvDto.builder()
                .email(userDto.getEmail())
                .response(userDto.getAboutMe())
                .build();
    }
}