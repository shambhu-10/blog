package com.blog.exception;

public class ResourceNotFoundException extends RuntimeException{

    /**
     * constructor with message only
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * constructor with message and cause
     * useful when wrapping other exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResourceNotFoundException forId(String resourceName, Long id) {
        return new ResourceNotFoundException(
                String.format("%s not found with id: %d", resourceName, id)
        );
    }

    public static ResourceNotFoundException forField(String resourceName, String fieldName, Object fieldValue) {
        return new ResourceNotFoundException(
                String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue)
        );
    }
}
