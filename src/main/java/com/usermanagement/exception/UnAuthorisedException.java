package com.usermanagement.exception;

public class UnAuthorisedException extends RuntimeException{
    public UnAuthorisedException(String msg) {
        super(msg);
    }
}
