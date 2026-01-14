package com.booklovers.exception;

public class UnauthorizedException extends BaseException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    @Override
    public int getStatusCode() {
        return 401;
    }
}
