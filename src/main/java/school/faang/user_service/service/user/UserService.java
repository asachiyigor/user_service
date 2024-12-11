package school.faang.user_service.service.user;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.json.student.PersonSchemaForUser;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.faang.user_service.dto.event.UserProfileEvent;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.dto.analyticsevent.SearchAppearanceEvent;
import school.faang.user_service.dto.user.UserDto;
import school.faang.user_service.dto.user.UserFilterDto;
import school.faang.user_service.dto.user.UserResponseCsvDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.exception.ErrorMessage;
import school.faang.user_service.exception.ReadFileException;
import school.faang.user_service.exception.UserNotFoundException;
import school.faang.user_service.exception.UserSaveException;
import school.faang.user_service.filter.UserFilter;
import school.faang.user_service.mapper.user.UserMapper;
import school.faang.user_service.puiblisher.viewUserProfile.UserViewProfilePublisher;
import school.faang.user_service.publisher.SearchAppearanceEventPublisher;
import school.faang.user_service.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final List<UserFilter> userFilters;
    private final UserContext userContext;
    private final SearchAppearanceEventPublisher searchAppearanceEventPublisher;
    private final UserViewProfilePublisher userViewProfilePublisher;
    private final CsvMapper csvMapper;
    private final CsvSchema schema;
    private final UserCountryService countryService;

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
                .collect(toList());
    }

    public List<UserDto> getUserSubscribers(long userId) {
        return userRepository.findUserSubsribers(userId).stream()
                .map(userMapper::toDto)
                .toList();
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

    private String generateUsername(UserDto userDto) {
        if (StringUtils.isNotEmpty(userDto.getEmail())) {
            return userDto.getEmail().split("@")[0];
        }
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateDefaultPassword() {
        return RandomStringUtils.randomAlphanumeric(12);
    }

    public List<UserResponseCsvDto> readingUsersFromCsv(MultipartFile file) {
        List<PersonSchemaForUser> persons = null;
        try {
            InputStream inputStream = file.getInputStream();
            persons = parsingCsv(inputStream);
        } catch (IOException e) {
            throw new ReadFileException("File exception: " + e);
        }
        List<CompletableFuture<UserDto>> futures = createPersonsAsync(persons);
        return completeCreatingPersonsAsync(futures).stream()
                .map(this::convertToUserResponseDto)
                .toList();
    }

    public List<PersonSchemaForUser> parsingCsv(InputStream inputStream) throws IOException {
        try {
            MappingIterator<PersonSchemaForUser> iterator = csvMapper.readerFor(PersonSchemaForUser.class)
                    .with(schema)
                    .readValues(inputStream);
            log.info("File successfully parsed.");
            return iterator.readAll().stream()
                    .filter(this::validateMinimalRequiredFields)
                    .collect(toList());
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
                        .collect(toList()))
                .join();
    }

    private UserResponseCsvDto convertToUserResponseDto(UserDto userDto) {
        return UserResponseCsvDto.builder()
                .email(userDto.getEmail())
                .response(userDto.getAboutMe())
                .build();
    }

    public void notificationUserWasViewed(UserProfileEvent userProfileEvent) {
        userViewProfilePublisher.publish(userProfileEvent);
        log.info("UserProfileEvent was send.");
    }
}