package org.example.exceptions;

public sealed class UserException extends Exception {

    public UserException(String message) { super(message); }

    public static final class NotFoundException extends UserException {
        public NotFoundException(String message) { super(message); }
    }

    public static final class StorageException extends UserException {
        public StorageException(String message) { super(message); }
    }

    public static final class InvalidException extends UserException {
        public InvalidException(String message) { super(message); }
    }

    public static final class DatabaseException extends UserException {
        public DatabaseException(String message) { super(message); }
    }

    public static final class ApiException extends UserException {
        public ApiException(String message) { super(message); }
    }
}
