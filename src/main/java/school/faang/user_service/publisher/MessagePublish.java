package school.faang.user_service.publisher;

public interface MessagePublish<T> {
    void publish(T event);
}