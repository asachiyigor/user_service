package school.faang.user_service.puiblisher;

public interface MessagePublish<T> {
    void publish(T event);
}