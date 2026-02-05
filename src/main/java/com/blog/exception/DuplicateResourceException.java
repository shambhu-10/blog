package com.blog.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DuplicateResourceException forField(String resourceName, String fieldName, Object fieldValue) {
        return new DuplicateResourceException(
                String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue)
        );
    }
}
