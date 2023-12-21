package com.usermanagement.exception;

public class UserIdNotMatchedException extends RuntimeException {

    public UserIdNotMatchedException(String msg) {
        super(msg);
    }
}
