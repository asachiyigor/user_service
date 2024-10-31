package school.faang.user_service.exception;

import org.jetbrains.annotations.NotNull;

public class DataValidationException extends RuntimeException {
    public DataValidationException(String message) {
        super(message);
    }

    public DataValidationException(@NotNull MessageError messageError) {
        super(messageError.getMessage());
    }
}
