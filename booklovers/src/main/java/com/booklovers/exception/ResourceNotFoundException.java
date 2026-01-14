package com.booklovers.exception;

public class ResourceNotFoundException extends BaseException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s with id %d not found", resourceName, id));
    }
    
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s with identifier '%s' not found", resourceName, identifier));
    }
    
    @Override
    public int getStatusCode() {
        return 404;
    }
}
