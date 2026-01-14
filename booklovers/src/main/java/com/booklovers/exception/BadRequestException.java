package com.booklovers.exception;

public class BadRequestException extends BaseException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public int getStatusCode() {
        return 400;
    }
}
