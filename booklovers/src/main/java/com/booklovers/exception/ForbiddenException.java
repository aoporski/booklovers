package com.booklovers.exception;

public class ForbiddenException extends BaseException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    @Override
    public int getStatusCode() {
        return 403;
    }
}
