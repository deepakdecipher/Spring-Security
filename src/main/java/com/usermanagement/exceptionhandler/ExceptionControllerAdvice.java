package com.usermanagement.exceptionhandler;
import com.usermanagement.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionControllerAdvice extends RuntimeException {
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ExceptionResponse> usernameNotFoundException(UsernameNotFoundException ex) {
        return new ResponseEntity<>(new ExceptionResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ExceptionResponse> nullPointerException(NullPointerException ex) {
        return new ResponseEntity<>(new ExceptionResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> userAlreadyExistsException(UserAlreadyExistsException ex) {
        return new ResponseEntity<>(new ExceptionResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ExceptionResponse> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        String errorMessage = processFieldErrors(result.getFieldErrors());
        return new ResponseEntity<>(new ExceptionResponse(400, errorMessage), HttpStatus.BAD_REQUEST);
    }

    private String processFieldErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(","));
    }

    @ExceptionHandler(com.usermanagement.exception.TokenExpiredException.class)
    public ResponseEntity<ExceptionResponse> tokenExpiredException(com.usermanagement.exception.TokenExpiredException ex) {
        return new ResponseEntity<>(new ExceptionResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<ExceptionResponse> tokenExpiredException(WrongPasswordException ex) {
        return new ResponseEntity<>(new ExceptionResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(UnAuthorisedException.class)
    public ResponseEntity<ExceptionResponse> notAuthorisedException(UnAuthorisedException ex) {
        return new ResponseEntity<>(new ExceptionResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(com.usermanagement.exception.UserIdNotFoundException.class)
    public ResponseEntity<ExceptionResponse> userIdNotFoundException(com.usermanagement.exception.UserIdNotFoundException ex) {
        return new ResponseEntity<>(new ExceptionResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserIdNotMatchedException.class)
    public ResponseEntity<ExceptionResponse> userIdNotMatchedException(UserIdNotMatchedException ex) {
        return new ResponseEntity<>(new ExceptionResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ExceptionResponse> emailNotFoundException(EmailNotFoundException ex) {
        return new ResponseEntity<>(new ExceptionResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<ExceptionResponse> executionException(ExecutionException ex) {
        return new ResponseEntity<>(new ExceptionResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(EmailProcessException.class)
    public ResponseEntity<ExceptionResponse> emailProcessException(EmailProcessException ex) {
        return new ResponseEntity<>(new ExceptionResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ExceptionResponse> otpExpiredException(OtpExpiredException ex) {
        return new ResponseEntity<>(new ExceptionResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
