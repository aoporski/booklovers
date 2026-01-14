package com.booklovers.exception;

public class ConflictException extends BaseException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    @Override
    public int getStatusCode() {
        return 409;
    }
}
