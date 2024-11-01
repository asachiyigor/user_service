package school.faang.user_service.exeption;

public class DataValidationException extends NullPointerException {
    public DataValidationException(String message) {
        super(message);
    }
}
