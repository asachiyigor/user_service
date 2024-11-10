package school.faang.user_service.service.recommendation;

import school.faang.user_service.dto.recommendation.RequestFilterDto;

import java.util.stream.Stream;

public interface Filter<T> {
    boolean isApplicable(RequestFilterDto filterDto);

    Stream<T> apply(Stream<T> stream, RequestFilterDto filterDto);
}
