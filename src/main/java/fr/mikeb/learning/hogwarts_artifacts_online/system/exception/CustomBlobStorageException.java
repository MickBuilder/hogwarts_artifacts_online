package fr.mikeb.learning.hogwarts_artifacts_online.system.exception;

public class CustomBlobStorageException extends RuntimeException {
    public CustomBlobStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}