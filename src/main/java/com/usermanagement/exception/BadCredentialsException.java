package com.usermanagement.exception;

public class BadCredentialsException extends RuntimeException{

     public BadCredentialsException(String msg) {
        super(msg);
    }
}
