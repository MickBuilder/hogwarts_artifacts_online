package fr.mikeb.learning.hogwarts_artifacts_online.system.exception;

public class PasswordChangeIllegalArgumentException extends RuntimeException {
    public PasswordChangeIllegalArgumentException(String message) {
        super(message);
    }
}