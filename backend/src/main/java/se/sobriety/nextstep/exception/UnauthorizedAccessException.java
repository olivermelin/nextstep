package se.sobriety.nextstep.exception;

/**
 * Thrown when an authenticated user attempts to access another user's data.
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
