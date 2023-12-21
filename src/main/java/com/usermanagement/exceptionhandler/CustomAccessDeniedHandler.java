package com.usermanagement.exceptionhandler;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.exception.ExceptionResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        ObjectMapper objectMapper = new ObjectMapper();

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(403);
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setCode(403);
        exceptionResponse.setMessage("Access Denied");
        response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));

    }
}
