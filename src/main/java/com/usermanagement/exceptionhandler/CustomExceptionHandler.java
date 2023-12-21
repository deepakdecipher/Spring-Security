package com.usermanagement.exceptionhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.exception.ExceptionResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomExceptionHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(400);
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setCode(400);
        if (Optional.ofNullable(request.getAttribute("invalidToken")).isPresent()) {
            exceptionResponse.setMessage("Invalid Token. Please enter valid token.");
            response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
        } else if (Optional.ofNullable(request.getAttribute("tokenExpired")).isPresent()) {
            exceptionResponse.setMessage("Session has expired. Please login again.");
            response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
        } else {
            exceptionResponse.setMessage(authException.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
        }
    }
}
