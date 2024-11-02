package school.faang.user_service.util;

import lombok.experimental.UtilityClass;
import school.faang.user_service.dto.event.EventDto;
import school.faang.user_service.dto.event.EventFilterDto;
import school.faang.user_service.dto.skill.SkillDto;
import school.faang.user_service.entity.Skill;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.event.Event;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@UtilityClass
public final class TestDataFactory {

    public static final Long USER_ID = 1L;
    public static final Long EVENT_ID_1 = 1L;
    public static final Long EVENT_ID_2 = 2L;
    public static final String MOSCOW = "Moscow";
    public static final String LONDON = "London";

    public static User createDefaultUser() {
        return User.builder()
                .id(USER_ID)
                .skills(createDefaultSkillsList())
                .build();
    }

    private static List<Skill> createDefaultSkillsList() {
        return Arrays.asList(
                createSkill(1L, "Java"),
                createSkill(2L, "Kotlin")
        );
    }

    public static User createUserWithInvalidSkills() {
        return User.builder()
                .id(USER_ID)
                .skills(createInvalidSkillsList())
                .build();
    }

    private static List<Skill> createInvalidSkillsList() {
        return Arrays.asList(
                createSkill(3L, "JS"),
                createSkill(4L, "REACT")
        );
    }

    private static Skill createSkill(Long id, String title) {
        return Skill.builder()
                .id(id)
                .title(title)
                .build();
    }

    private static List<SkillDto> createDefaultSkillDtoList() {
        return Arrays.asList(
                createSkillDto(1L, "Java"),
                createSkillDto(2L, "Kotlin")
        );
    }

    private static SkillDto createSkillDto(Long id, String title) {
        return SkillDto.builder()
                .id(id)
                .title(title)
                .build();
    }

    public static EventDto createDefaultEventDto(Long id) {
        return EventDto.builder()
                .id(id)
                .ownerId(USER_ID)
                .title("Test Event")
                .startDate(LocalDateTime.now().plusDays(3))
                .endDate(LocalDateTime.now().plusDays(4))
                .relatedSkills(createDefaultSkillDtoList())
                .build();
    }

    public static EventFilterDto createDefaultFilter() {
        return EventFilterDto.builder()
                .startDateFrom(LocalDateTime.now().plusDays(1))
                .startDateTo(LocalDateTime.now().plusDays(4))
                .location(MOSCOW)
                .skillIds(Collections.singletonList(1L))
                .build();
    }

    public static Event createDefaultEvent(Long id, String location) {
        return Event.builder()
                .id(id)
                .title("Test Event")
                .relatedSkills(createDefaultSkillsList())
                .startDate(LocalDateTime.now().plusDays(3))
                .endDate(LocalDateTime.now().plusDays(4))
                .location(location)
                .build();
    }
}
