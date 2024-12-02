package com.volco.creditapp.api.rest.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volco.creditapp.application.exceptions.InvalidStateException;
import com.volco.creditapp.application.exceptions.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Order(1)
@ControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private ResponseEntity<Object> buildResponseEntity(HttpStatus httpStatus, String message) {
        try {
            return ResponseEntity.status(httpStatus).body(objectMapper.writeValueAsString(message != null ? message : ""));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing error message");
        }
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleException(ConstraintViolationException ex) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleException(IllegalArgumentException ex) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleException(ResourceNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleException(InvalidStateException ex) {
        return buildResponseEntity(HttpStatus.CONFLICT, ex.getMessage());
    }
}