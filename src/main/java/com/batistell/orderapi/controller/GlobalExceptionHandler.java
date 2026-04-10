package com.batistell.orderapi.controller;

import com.batistell.orderapi.exception.IdempotencyViolationException;
import com.batistell.orderapi.exception.InsufficientStockException;
import com.batistell.orderapi.exception.PriceChangedException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientStock(InsufficientStockException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Unprocessable Entity");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(PriceChangedException.class)
    public ResponseEntity<Map<String, String>> handlePriceChanged(PriceChangedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Conflict - Price Drift");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Conflict");
        response.put("message", "The order was updated concurrently. Please retry.");
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IdempotencyViolationException.class)
    public ResponseEntity<Map<String, String>> handleIdempotencyViolation(IdempotencyViolationException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Already Processed");
        response.put("cachedOrderId", ex.getCachedOrderId().toString());
        // Customarily OK back since the request logically succeeded previously
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<Map<String, String>> handleCircuitBreaker(CallNotPermittedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "A downstream service is currently unavailable. Please try again later.");
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<Map<String, String>> handleBulkheadFull(BulkheadFullException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Too Many Requests");
        response.put("message", "Service is currently operating at capacity. Please try again later.");
        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
