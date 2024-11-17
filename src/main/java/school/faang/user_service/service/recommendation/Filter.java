package school.faang.user_service.service.recommendation;


import java.util.stream.Stream;

public interface Filter<F, T> {
    boolean isApplicable(F filterDto);

    Stream<T> apply(Stream<T> stream, F filterDto);
}
