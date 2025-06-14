package school.faang.user_service.filter;

import school.faang.user_service.dto.user.UserFilterDto;
import school.faang.user_service.entity.User;

import java.util.stream.Stream;

public interface UserFilter {
    boolean isApplicable(UserFilterDto filter);

    Stream<User> apply(Stream<User> users, UserFilterDto filters);
}