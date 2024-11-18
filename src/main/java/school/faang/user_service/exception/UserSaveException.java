package school.faang.user_service.exception;

public class UserSaveException extends RuntimeException {
    public UserSaveException(String message) {
        super(message);
    }
}