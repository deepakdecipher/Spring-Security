package com.usermanagement.exception;

public class OtpExpiredException extends RuntimeException {
     public OtpExpiredException(String msg){
         super(msg);
     }
}
